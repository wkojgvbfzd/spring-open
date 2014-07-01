package net.onrc.onos.core.topology;

import java.util.Map;

import org.apache.commons.lang.Validate;

import net.onrc.onos.core.util.Dpid;
import net.onrc.onos.core.util.PortNumber;
import net.onrc.onos.core.util.SwitchPort;

/**
 * Port Object stored in In-memory Topology.
 * <p/>
 * TODO REMOVE following design memo: This object itself may hold the DBObject,
 * but this Object itself will not issue any read/write to the DataStore.
 */
public class PortImpl extends TopologyObject implements Port {

    //////////////////////////////////////////////////////
    /// Topology element attributes
    ///  - any changes made here needs to be replicated.
    //////////////////////////////////////////////////////
    private PortEvent portObj;

    ///////////////////
    /// In-memory index
    ///////////////////


    /**
     * Creates a Port object based on {@link PortEvent}.
     *
     * @param topology Topology instance this object belongs to
     * @param scPort self contained {@link PortEvent}
     */
    public PortImpl(Topology topology, PortEvent scPort) {
        super(topology);
        Validate.notNull(scPort);

        // TODO should we assume portObj is already frozen before this call
        //      or expect attribute update will happen after .
        if (scPort.isFrozen()) {
            this.portObj = scPort;
        } else {
            this.portObj = new PortEvent(scPort);
            this.portObj.freeze();
        }
    }

    /**
     * Creates a Port object with empty attributes.
     *
     * @param topology Topology instance this object belongs to
     * @param switchPort SwitchPort
     */
    public PortImpl(Topology topology, SwitchPort switchPort) {
        this(topology, new PortEvent(switchPort).freeze());
    }

    /**
     * Creates a Port object with empty attributes.
     *
     * @param topology Topology instance this object belongs to
     * @param dpid DPID
     * @param number PortNumber
     */
    public PortImpl(Topology topology, Dpid dpid, PortNumber number) {
        this(topology, new SwitchPort(dpid, number));
        Validate.notNull(dpid);
        Validate.notNull(number);
    }

    public PortImpl(Topology topology, Long dpid, Long number) {
        this(topology, new SwitchPort(dpid, number));
        Validate.notNull(dpid);
        Validate.notNull(number);
    }

    @Deprecated
    public PortImpl(Topology topology, Switch parentSwitch, PortNumber number) {
        this(topology, new SwitchPort(parentSwitch.getDpid(), number));
    }

    @Deprecated
    public PortImpl(Topology topology, Switch parentSwitch, Long number) {
        this(topology, parentSwitch, new PortNumber(number.shortValue()));
    }

    @Override
    public Dpid getDpid() {
        return asSwitchPort().getDpid();
    }

    @Override
    public PortNumber getNumber() {
        return asSwitchPort().getPortNumber();
    }

    @Override
    public SwitchPort asSwitchPort() {
        return portObj.getSwitchPort();
    }

    @Override
    public String getDescription() {
        return getStringAttribute(PortEvent.DESCRIPTION, "");
    }

    void setDescription(String description) {
//        portObj.createStringAttribute(attr, value);
        // TODO implement using attributes
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Long getHardwareAddress() {
        // TODO implement using attributes?
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Switch getSwitch() {
        topology.acquireReadLock();
        try {
            return topology.getSwitch(getDpid());
        } finally {
            topology.releaseReadLock();
        }
    }

    @Override
    public Link getOutgoingLink() {
        topology.acquireReadLock();
        try {
            return topology.getOutgoingLink(asSwitchPort());
        } finally {
            topology.releaseReadLock();
        }
    }

    @Override
    public Link getIncomingLink() {
        topology.acquireReadLock();
        try {
            return topology.getIncomingLink(asSwitchPort());
        } finally {
            topology.releaseReadLock();
        }
    }

    @Override
    public Iterable<Device> getDevices() {
        topology.acquireReadLock();
        try {
            return topology.getDevices(this.asSwitchPort());
        } finally {
            topology.releaseReadLock();
        }
    }

    void replaceStringAttributes(PortEvent updated) {
        Validate.isTrue(this.asSwitchPort().equals(updated.getSwitchPort()),
                "Wrong PortEvent given.");

        // XXX simply replacing whole self-contained object for now
        if (updated.isFrozen()) {
            this.portObj = updated;
        } else {
            this.portObj = new PortEvent(updated).freeze();
        }
    }

    @Override
    public String getStringAttribute(String attr) {
        return portObj.getStringAttribute(attr);
    }

    @Override
    public String getStringAttribute(String attr, String def) {
        final String v = getStringAttribute(attr);
        if (v == null) {
            return def;
        } else {
            return v;
        }
    }

    @Override
    public Map<String, String> getAllStringAttributes() {
        return portObj.getAllStringAttributes();
    }

    @Override
    public String toString() {
        return String.format("%s:%s",
                getSwitch().getDpid(),
                getNumber());
    }


    /**
     * Returns the type of topology object.
     *
     * @return the type of the topology object
     */
    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
