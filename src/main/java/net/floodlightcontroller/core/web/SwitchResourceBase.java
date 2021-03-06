/**
 *    Copyright 2011, Big Switch Networks, Inc.
 *    Originally created by David Erickson, Stanford University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.core.web;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.annotations.LogMessageDoc;
import net.onrc.onos.core.drivermanager.OFSwitchImplSpringOpenTTP;

import org.projectfloodlight.openflow.protocol.OFFeaturesReply;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFGroupDescStatsEntry;
import org.projectfloodlight.openflow.protocol.OFGroupDescStatsReply;
import org.projectfloodlight.openflow.protocol.OFGroupStatsEntry;
import org.projectfloodlight.openflow.protocol.OFGroupStatsReply;
import org.projectfloodlight.openflow.protocol.OFMatchV3;
import org.projectfloodlight.openflow.protocol.OFOxmList;
import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFPortStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.util.HexString;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for server resources related to switches
 *
 * @author readams
 */

public class SwitchResourceBase extends ServerResource {
    protected final static Logger log = LoggerFactory.getLogger(SwitchResourceBase.class);

    public enum REQUESTTYPE {
        OFSTATS,
        OFFEATURES
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();

    }

    @LogMessageDoc(level = "ERROR",
            message = "Failure retrieving statistics from switch {switch}",
            explanation = "An error occurred while retrieving statistics" +
                    "from the switch",
                    recommendation = LogMessageDoc.CHECK_SWITCH + " " +
                            LogMessageDoc.GENERIC_ACTION)

    protected List<?> getSwitchStatistics(long switchId,
            OFStatsType statType) {
        IFloodlightProviderService floodlightProvider =
                (IFloodlightProviderService) getContext().getAttributes().
                get(IFloodlightProviderService.class.getCanonicalName());

        IOFSwitch sw = floodlightProvider.getSwitches().get(switchId);
        Future<List<OFStatsReply>> future;
        List<OFStatsReply> values = null;
        if (sw != null) {
            OFStatsRequest<?> req = null;
            if (statType == OFStatsType.FLOW) {
                log.debug("Switch Flow Stats req sent for switch {}",
                        sw.getStringId());
                OFMatchV3 match = sw.getFactory().buildMatchV3()
                        .setOxmList(OFOxmList.EMPTY).build();
                req = sw.getFactory()
                        .buildFlowStatsRequest()
                        .setMatch(match)
                        .setOutPort(OFPort.ANY)
                        .setTableId(TableId.ALL)
                        .setXid(sw.getNextTransactionId()).build();
                List<OFFlowStatsEntryMod> flowStats = new ArrayList<OFFlowStatsEntryMod>();
                try {
                    future = sw.getStatistics(req);
                    values = future.get(10, TimeUnit.SECONDS);
                    for(OFStatsReply value : values){
                        for (OFFlowStatsEntry entry : ((OFFlowStatsReply)value).getEntries()) {
                            OFFlowStatsEntryMod entryMod = new OFFlowStatsEntryMod(entry, sw);
                            flowStats.add(entryMod);
                        }
                    }
                    log.debug("Switch flow Stats Entries from switch {} are {}",
                            sw.getStringId(), flowStats);
                } catch (Exception e) {
                    log.error("Failure retrieving statistics from switch " + sw, e);
                }
                return flowStats;
            }
            else if (statType == OFStatsType.PORT){
                log.debug("Switch Port Stats: req sent for all "
                        + "ports in switch {}", sw.getStringId());
                List<OFPortStatsEntryMod> portStats = null;
                req = sw.getFactory()
                        .buildPortStatsRequest()
                        .setPortNo(OFPort.ANY).setXid
                        (sw.getNextTransactionId()).build();
                try {
                    future = sw.getStatistics(req);
                    values = future.get(10, TimeUnit.SECONDS);
                    portStats = new ArrayList<OFPortStatsEntryMod>();
                    for(OFStatsReply value : values){
                        for (OFPortStatsEntry entry : ((OFPortStatsReply)value).getEntries()) {
                            OFPortStatsEntryMod entryMod = new OFPortStatsEntryMod(entry, sw);
                            portStats.add(entryMod);
                        }
                    }
                    log.debug("Switch Port Stats Entries from switch {} are {}",
                            sw.getStringId(), portStats);
                } catch (Exception e) {
                    log.error("Failure retrieving statistics from switch " + sw, e);
                }
                return portStats;
            }
            else if (statType == OFStatsType.GROUP){
                log.debug("Switch Group Stats: req sent for all "
                        + "groups in switch {}", sw.getStringId());
                req = sw.getFactory().buildGroupStatsRequest().setXid
                        (sw.getNextTransactionId()).build();
                List<OFGroupStatsEntryMod> groupStats = new ArrayList<OFGroupStatsEntryMod>();
                try {
                    future = sw.getStatistics(req);
                    values = future.get(10, TimeUnit.SECONDS);
                    for(OFStatsReply value : values){
                        for (OFGroupStatsEntry entry : ((OFGroupStatsReply)value).getEntries()) {
                            OFGroupStatsEntryMod entryMod = new OFGroupStatsEntryMod(entry);
                            groupStats.add(entryMod);
                        }
                    }
                    log.debug("Switch Group Stats Entries from switch {} are {}",
                            sw.getStringId(), groupStats);
                } catch (Exception e) {
                    log.error("Failure retrieving statistics from switch " + sw, e);
                }
                return groupStats;
            }
            else if (statType == OFStatsType.GROUP_DESC){
                log.debug("Switch Group Desc Stats: req sent for all "
                        + "groups in switch {}", sw.getStringId());
                List<OFGroupDescStatsEntryMod> GroupDescStats= new ArrayList<OFGroupDescStatsEntryMod>();
                req = sw.getFactory().buildGroupDescStatsRequest().setXid
                        (sw.getNextTransactionId()).build();
                try {
                    future = sw.getStatistics(req);
                    values = future.get(10, TimeUnit.SECONDS);
                    for(OFStatsReply value : values){
                        for (OFGroupDescStatsEntry entry : ((OFGroupDescStatsReply)value).getEntries()) {
                            OFGroupDescStatsEntryMod entryMod = new OFGroupDescStatsEntryMod(entry);
                            GroupDescStats.add(entryMod);
                        }
                    }
                    log.debug("Switch Group_Desc Stats Entries from switch {} are {}",
                            sw.getStringId(), GroupDescStats);
                } catch (Exception e) {
                    log.error("Failure retrieving statistics from switch " + sw, e);
                }
                return GroupDescStats;
            }
            /*else if (statType == OFStatisticsType.AGGREGATE) {
                OFAggregateStatisticsRequest specificReq = new OFAggregateStatisticsRequest();
                OFMatch match = new OFMatch();
                match.setWildcards(0xffffffff);
                specificReq.setMatch(match);
                specificReq.setOutPort(OFPort.OFPP_NONE.getValue());
                specificReq.setTableId((byte) 0xff);
                req.setStatistics(Collections.singletonList((OFStatistics) specificReq));
                requestLength += specificReq.getLength();
            } /*else if (statType == OFStatisticsType.QUEUE) {
                OFQueueStatisticsRequest specificReq = new OFQueueStatisticsRequest();
                specificReq.setPortNumber(OFPort.OFPP_ALL.getValue());
                // LOOK! openflowj does not define OFPQ_ALL! pulled this from openflow.h
                // note that I haven't seen this work yet though...
                specificReq.setQueueId(0xffffffff);
                req.setStatistics(Collections.singletonList((OFStatistics) specificReq));
                requestLength += specificReq.getLength();
            } else if (statType == OFStatisticsType.DESC ||
                    statType == OFStatisticsType.TABLE) {
                // pass - nothing todo besides set the type above
            }*/
            // XXX S fix when we fix stats
            try {
                future = sw.getStatistics(req);
                values = future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failure retrieving statistics from switch " + sw, e);
            }
        }
        return values;
    }

    protected Object getSwitchStatistics(String switchId, OFStatsType statType) {
        return getSwitchStatistics(HexString.toLong(switchId), statType);
    }
    
    protected Object getSwitchGroupStats(String switchId, OFStatsType statType, Integer groupId) {
        
        IFloodlightProviderService floodlightProvider =
                (IFloodlightProviderService) getContext().getAttributes().
                get(IFloodlightProviderService.class.getCanonicalName());
        IOFSwitch sw = floodlightProvider.getSwitches().get(HexString.toLong(switchId));
        Future<List<OFStatsReply>> future;
        List<OFStatsReply> values = null;
        if (sw != null){
            log.debug("Switch Group Stats: req sent for groupId {} "
                    + "in switch {}",groupId, sw.getStringId());
            OFStatsRequest<?> req = null;
            req = sw.getFactory().buildGroupStatsRequest().setXid
                    (sw.getNextTransactionId()).setGroup(OFGroup.of(groupId)).build();
            List<OFGroupStatsEntryMod> groupStats = new ArrayList<OFGroupStatsEntryMod>();
            try {
                future = sw.getStatistics(req);
                values = future.get(10, TimeUnit.SECONDS);
                if(values.isEmpty()){
                    log.warn("group with groupId {} not found", groupId);
                    return null;
                }
                for(OFStatsReply value : values){
                    for (OFGroupStatsEntry entry : ((OFGroupStatsReply)value).getEntries()) {
                        OFGroupStatsEntryMod entryMod = new OFGroupStatsEntryMod(entry);
                        groupStats.add(entryMod);
                    }
                }
                log.debug("Switch Group Stats Entries from switch {} are {}",
                        sw.getStringId(), groupStats);
            } catch (Exception e) {
                log.error("Failure retrieving statistics from switch " + sw, e);
            }
            return groupStats;
            
        }
        return null;
    }
    //TODO: Java doc
    protected List<?> getSwitchStatisticsForTable(long switchId,
            OFStatsType statType, String tableType) {
        IFloodlightProviderService floodlightProvider =
                (IFloodlightProviderService) getContext().getAttributes().
                get(IFloodlightProviderService.class.getCanonicalName());
        IOFSwitch sw = floodlightProvider.getSwitches().get(switchId);
        Future<List<OFStatsReply>> future;
        List<OFStatsReply> values = null;
        //getting tableId from CPqD driver
        TableId tableId;
        if (sw != null) {
            if ((tableId = ((OFSwitchImplSpringOpenTTP) sw).getTableId(tableType)) == null) {
                log.error("Invalid tableType {} " + tableType);
                return null;
            }
            OFStatsRequest<?> req = null;
            if (statType == OFStatsType.FLOW) {
                log.debug("Switch Flow Stats req for table {} sent to switch {}",
                        tableType,sw.getStringId());
                OFMatchV3 match = sw.getFactory().buildMatchV3()
                        .setOxmList(OFOxmList.EMPTY).build();
                req = sw.getFactory()
                        .buildFlowStatsRequest()
                        .setMatch(match)
                        .setOutPort(OFPort.ANY)
                        .setTableId(tableId)
                        .setXid(sw.getNextTransactionId()).build();
                List<OFFlowStatsEntryMod> flowStats = new ArrayList<OFFlowStatsEntryMod>();
                try {
                    future = sw.getStatistics(req);
                    values = future.get(10, TimeUnit.SECONDS);
                    for(OFStatsReply value : values){
                        for (OFFlowStatsEntry entry : ((OFFlowStatsReply)value).getEntries()) {
                            OFFlowStatsEntryMod entryMod = new OFFlowStatsEntryMod(entry, sw);
                            flowStats.add(entryMod);
                        }
                    }
                    log.debug("Switch flow Stats Entries for table {} from switch {} are {}",
                            tableType, sw.getStringId(), flowStats);
                } catch (Exception e) {
                    log.error("Failure retrieving per table statistics from switch " + sw, e);
                }
                return flowStats;
            }
        }
        //should never get to this point
        log.error("Failure retrieving  {} table statistics from switch {}",tableType, sw);
        return null;
    }

    protected OFFeaturesReply getSwitchFeaturesReply(long switchId) {
        IFloodlightProviderService floodlightProvider =
                (IFloodlightProviderService) getContext().getAttributes().
                get(IFloodlightProviderService.class.getCanonicalName());

        IOFSwitch sw = floodlightProvider.getSwitches().get(switchId);
        //uture<OFFeaturesReply> future;
        OFFeaturesReply featuresReply = null;
        if (sw != null) {
            // XXX S fix when we fix stats
            try {
                //future = sw.getFeaturesReplyFromSwitch();
                //featuresReply = future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failure getting features reply from switch" + sw, e);
            }
        }

        return featuresReply;
    }

    protected OFFeaturesReply getSwitchFeaturesReply(String switchId) {
        return getSwitchFeaturesReply(HexString.toLong(switchId));
    }

}
