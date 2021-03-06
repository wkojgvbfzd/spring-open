package net.onrc.onos.core.intent.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.onrc.onos.core.datagrid.IDatagridService;
import net.onrc.onos.core.datagrid.IEventChannel;
import net.onrc.onos.core.datagrid.IEventChannelListener;
import net.onrc.onos.core.flowprogrammer.IFlowPusherService;
import net.onrc.onos.core.intent.FlowEntry;
import net.onrc.onos.core.intent.Intent;
import net.onrc.onos.core.intent.Intent.IntentState;
import net.onrc.onos.core.intent.IntentOperation;
import net.onrc.onos.core.intent.IntentOperationList;
import net.onrc.onos.core.intent.PathIntent;
import net.onrc.onos.core.intent.ShortestPathIntent;
import net.onrc.onos.core.topology.ITopologyService;

import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PlanInstallModule contains the PlanCalcRuntime and PlanInstallRuntime.
 * <p>
 * It is responsible for converting Intents into FlowMods and seeing that they
 * are properly installed.
 */
public class PlanInstallModule implements IFloodlightModule, IOFMessageListener {

    protected volatile IFloodlightProviderService floodlightProvider;
    protected volatile ITopologyService topologyService;
    protected volatile IDatagridService datagridService;
    protected volatile IFlowPusherService flowPusher;
    private PlanCalcRuntime planCalc;
    private PlanInstallRuntime planInstall;
    private EventListener eventListener;
    private IEventChannel<Long, IntentStateList> intentStateChannel;
    private static final Logger log = LoggerFactory.getLogger(PlanInstallModule.class);

    private static final String PATH_INTENT_CHANNEL_NAME = "onos.pathintent";
    private static final String INTENT_STATE_EVENT_CHANNEL_NAME = "onos.pathintent_state";
    private ConcurrentMap<String, Intent> parentIntentMap = new ConcurrentHashMap<String, Intent>();

    /**
     * EventListener for Intent updates from the PathCalcRuntime module.
     */
    class EventListener extends Thread
            implements IEventChannelListener<Long, IntentOperationList> {

        private BlockingQueue<IntentOperationList> intentQueue = new LinkedBlockingQueue<>();
        private Long key = Long.valueOf(0);

        /**
         * Poll the Intent queue for events.
         */
        @Override
        public void run() {
            while (true) {
                try {
                    IntentOperationList intents = intentQueue.take();
                    // TODO: consider draining the remaining intent lists
                    // and processing in one big batch

                    processIntents(intents);
                } catch (InterruptedException e) {
                    log.warn("Error taking from intent queue: {}", e.getMessage());
                }
            }
        }

        /**
         * Process events received from the event queue.
         * <p>
         * First, compute a plan, and then install it.
         *
         * @param intents list of new Intent events
         */
        private void processIntents(IntentOperationList intents) {
            log("start_processIntents");
            log.debug("Processing OperationList {}", intents);
            log("begin_computePlan");
            List<Set<FlowEntry>> plan = planCalc.computePlan(intents);
            log("end_computePlan");
            log.debug("Plan: {}", plan);
            log("begin_installPlan");
            boolean success = planInstall.installPlan(plan);
            log("end_installPlan");
            Set<Long> domainSwitchDpids = floodlightProvider.getSwitches().keySet();
            log("begin_sendInstallNotif");
            sendNotifications(intents, true, success, domainSwitchDpids);
            log("end_sendInstallNotif");
            log("finish");
        }

        /***
         * This function is for sending intent state notification to other ONOS
         * instances. The argument of "domainSwitchDpids" is required for
         * dispatching this ONOS's managed switches.
         *
         * @param intents list of intents
         * @param installed true if Intents were installed
         * @param success true if updates succeeded
         * @param domainSwitchDpids set of affected switches
         */
        private void sendNotifications(IntentOperationList intents,
                boolean installed, boolean success, Set<Long> domainSwitchDpids) {
            IntentStateList states = new IntentStateList();
            for (IntentOperation i : intents) {
                IntentState newState;
                switch (i.operator) {
                case REMOVE:
                    if (installed) {
                        newState = success ? IntentState.DEL_ACK
                                : IntentState.DEL_PENDING;
                    } else {
                        newState = IntentState.DEL_REQ;
                    }
                    break;
                case ADD:
                default:
                    if (installed) {
                        if (domainSwitchDpids != null) {
                            states.domainSwitchDpids.addAll(domainSwitchDpids);
                        }
                        newState = success ? IntentState.INST_ACK : IntentState.INST_NACK;
                    } else {
                        newState = IntentState.INST_REQ;
                    }
                    break;
                }
                states.put(i.intent.getId(), newState);
            }

            if (log.isTraceEnabled()) {
                log.trace("sendNotifications, states {}, domainSwitchDpids {}",
                        states, states.domainSwitchDpids);
            }

            // Send notifications using the same key every time
            // and receive them by entryAdded() and entryUpdated()
            intentStateChannel.addTransientEntry(key, states);
        }

        /**
         * Uses the entryUpdated() method. This should only be called once
         * because we always use the same key.
         */
        @Override
        public void entryAdded(IntentOperationList value) {
            entryUpdated(value);
        }

        /*
         * (non-Javadoc)
         * @see net.onrc.onos.core.datagrid.IEventChannelListener#entryRemoved(java.lang.Object)
         */
        @Override
        public void entryRemoved(IntentOperationList value) {
            // This channel is a queue, so this method is not needed
        }

        /**
         * Adds the new intents to the event queue.
         *
         * @param value list of new intents
         */
        @Override
        public void entryUpdated(IntentOperationList value) {
            putIntentOpsInfoInParentMap(value);
            log("start_intentNotifRecv");
            log("begin_sendReceivedNotif");
            sendNotifications(value, false, false, null);
            log("end_sendReceivedNotif");
            log("finish");

            log.debug("Added OperationList {}", value);
            try {
                intentQueue.put(value);
            } catch (InterruptedException e) {
                log.warn("Error putting to intent queue: {}", e.getMessage());
            }
        }

        /**
         * Add intent information to parent map.
         *
         * @param intentOps list of Intents and new states
         */
        private void putIntentOpsInfoInParentMap(IntentOperationList intentOps) {
            for (IntentOperation i : intentOps) {
                if (!(i.intent instanceof PathIntent)) {
                    log.warn("Not a path intent: {}", i);
                    continue;
                }
                PathIntent intent = (PathIntent) i.intent;
                Intent parent = intent.getParentIntent();
                if (parent instanceof ShortestPathIntent) {
                    parentIntentMap.put(parent.getId(), parent);
                } else {
                    log.warn("Unsupported Intent: {}", parent);
                    continue;
                }
            }
        }
    }

    /**
     * Formatted log for debugging.
     * <p>
     * TODO: merge this into debugging framework
     *
     * @param step the step of computation
     */
    public static void log(String step) {
        log.debug("Time:{}, Step:{}", System.nanoTime(), step);
    }

    /*
     * (non-Javadoc)
     * @see net.floodlightcontroller.core.module.IFloodlightModule#init
     * (net.floodlightcontroller.core.module.FloodlightModuleContext)
     */
    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        topologyService = context.getServiceImpl(ITopologyService.class);
        datagridService = context.getServiceImpl(IDatagridService.class);
        flowPusher = context.getServiceImpl(IFlowPusherService.class);
        planCalc = new PlanCalcRuntime();
        planInstall = new PlanInstallRuntime(floodlightProvider, flowPusher);
        eventListener = new EventListener();
    }

    /*
     * (non-Javadoc)
     * @see net.floodlightcontroller.core.module.IFloodlightModule#startUp
     * (net.floodlightcontroller.core.module.FloodlightModuleContext)
     */
    @Override
    public void startUp(FloodlightModuleContext context) {
        // start subscriber
        datagridService.addListener(PATH_INTENT_CHANNEL_NAME,
                eventListener,
                Long.class,
                IntentOperationList.class);
        eventListener.start();
        // start publisher
        intentStateChannel = datagridService.createChannel(
                INTENT_STATE_EVENT_CHANNEL_NAME,
                Long.class,
                IntentStateList.class);
        floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);
    }

    /*
     * (non-Javadoc)
     * @see net.floodlightcontroller.core.module.IFloodlightModule#getModuleDependencies()
     */
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(ITopologyService.class);
        l.add(IDatagridService.class);
        l.add(IFlowPusherService.class);
        return l;
    }

    /*
     * (non-Javadoc)
     * @see net.floodlightcontroller.core.module.IFloodlightModule#getModuleServices()
     */
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // no services, for now
        return null;
    }

    /*
     * (non-Javadoc)
     * @see net.floodlightcontroller.core.module.IFloodlightModule#getServiceImpls()
     */
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // no services, for now
        return null;
    }

    /**
     * Handles FlowRemoved messages from the switches.
     */
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        if (msg.getType().equals(OFType.FLOW_REMOVED) &&
                (msg instanceof OFFlowRemoved)) {
            OFFlowRemoved flowRemovedMsg = (OFFlowRemoved) msg;

            if (log.isTraceEnabled()) {
                log.trace("Receive flowRemoved from sw {} : Cookie {}",
                        sw.getId(), flowRemovedMsg.getCookie());
            }

            String intentParentId = Long.toString(flowRemovedMsg.getCookie().getValue());
            Intent intent = parentIntentMap.get(intentParentId);

            // We assume if the path src sw flow entry is expired,
            // the path is expired.
            if (!isFlowSrcRemoved(sw.getId(), intentParentId)) {
                return Command.CONTINUE;
            }

            ShortestPathIntent spfIntent = null;
            if (!(intent instanceof ShortestPathIntent)) {
                return Command.CONTINUE;
            }
            spfIntent = (ShortestPathIntent) intent;
            String pathIntentId = spfIntent.getPathIntentId();

            IntentStateList states = new IntentStateList();
            IntentState newState = IntentState.DEL_ACK;
            states.put(pathIntentId, newState);
            Set<Long> domainSwitchDpids = floodlightProvider.getSwitches().keySet();
            if (domainSwitchDpids != null) {
                states.domainSwitchDpids.addAll(domainSwitchDpids);
            }
            parentIntentMap.remove(intentParentId);
            log.debug("addEntry to intentStateChannel intentId {}, states {}",
                    pathIntentId, newState);

            intentStateChannel.addTransientEntry(flowRemovedMsg.getCookie().getValue(),
                    states);
        }

        return Command.CONTINUE;
    }

    /**
     * Check to see if the flow's source switch entry is removed/expired.
     *
     * @param dpid DPID of affected switch
     * @param shortestPathIntentId Intent to check
     * @return true if the flow's source switch entry is removed or expired,
     *         otherwise false.
     */
    private boolean isFlowSrcRemoved(long dpid, String shortestPathIntentId) {
        Intent intent = parentIntentMap.get(shortestPathIntentId);
        ShortestPathIntent spfIntent = null;
        if (intent instanceof ShortestPathIntent) {
            spfIntent = (ShortestPathIntent) intent;
        }

        if (spfIntent == null) {
            return false;
        }

        long srcSwDpid = spfIntent.getSrcSwitchDpid();
        if (srcSwDpid == dpid) {
            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return "planInstall";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

}
