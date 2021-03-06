package net.onrc.onos.core.topology;

import net.onrc.onos.core.util.Dpid;
import net.onrc.onos.core.util.PortNumber;
import net.onrc.onos.core.util.SwitchPort;

/**
 * Mock topology for packet and optical layer.
 * This is used for unit test to test various topology properties and
 * relationship.
 */
public class MockPacketOpticalTopology extends MockTopology {

    public static final String TOTAL_REGEN_COUNT = "TOTAL_REGEN_COUNT";
    public static final String REGEN_IN_USE_COUNT = "REGEN_IN_USE_COUNT";

    @Override
    public Switch addSwitch(Long switchId) {
        SwitchData switchData = new SwitchData(new Dpid(switchId));
        switchData.createStringAttribute(TopologyElement.TYPE,
                                          TopologyElement.TYPE_PACKET_LAYER);
        switchData.createStringAttribute(TopologyElement.ELEMENT_TYPE,
                                          SwitchType.ETHERNET_SWITCH.
                                                  toString());
        switchData.createStringAttribute(TopologyElement
                                                  .ELEMENT_CONFIG_STATE,
                                          ConfigState.NOT_CONFIGURED.
                                                  toString());
        switchData.createStringAttribute(TopologyElement
                                                  .ELEMENT_ADMIN_STATUS,
                                          AdminStatus.ACTIVE.toString());
        this.putSwitch(switchData);
        return this.getSwitch(switchData.getDpid());
    }

    @Override
    public Port addPort(Switch sw, Long portNumber) {
        PortData portData = new PortData(sw.getDpid(),
                                    PortNumber.uint16(portNumber.shortValue()));
        portData.createStringAttribute(TopologyElement.TYPE,
                                        TopologyElement.TYPE_PACKET_LAYER);
        portData.createStringAttribute(TopologyElement.ELEMENT_TYPE,
                                        PortType.ETHERNET_PORT.toString());
        portData.createStringAttribute(TopologyElement.ELEMENT_CONFIG_STATE,
                                        ConfigState.NOT_CONFIGURED.toString());
        portData.createStringAttribute(TopologyElement.ELEMENT_ADMIN_STATUS,
                                        AdminStatus.ACTIVE.toString());
        this.putPort(portData);
        return this.getPort(sw.getDpid(), portData.getPortNumber());
    }

    /**
     * Adds {@link SwitchType#OPTICAL_SWITCH} to the topology.
     *
     * @param switchId returns Switch
     */
    public Switch addOpticalSwitch(Long switchId) {
        SwitchData switchData = new SwitchData(new Dpid(switchId));
        switchData.createStringAttribute(TopologyElement.TYPE,
                                          TopologyElement.TYPE_OPTICAL_LAYER);
        switchData.createStringAttribute(TopologyElement.ELEMENT_TYPE,
                                          SwitchType.OPTICAL_SWITCH.toString());
        switchData.createStringAttribute(TopologyElement
                                                  .ELEMENT_CONFIG_STATE,
                                          ConfigState.CONFIGURED.toString());
        switchData.createStringAttribute(TopologyElement
                                                  .ELEMENT_ADMIN_STATUS,
                                          AdminStatus.INACTIVE.toString());
        switchData.createStringAttribute(TOTAL_REGEN_COUNT,
                                          Integer.toString(2));
        switchData.createStringAttribute(REGEN_IN_USE_COUNT,
                                          Integer.toString(2));
        this.putSwitch(switchData);
        return this.getSwitch(switchData.getDpid());
    }

    /**
     * Adds {@link PortType#TRANSPONDER_PORT} to the topology.
     *
     * @param sw
     * @param portNumber
     * @return Port
     */
    //todo - add t-port specific properties
    public Port addTPort(Switch sw, Long portNumber) {
        PortData portData = new PortData(sw.getDpid(),
                                    PortNumber.uint16(portNumber.shortValue()));
        portData.createStringAttribute(TopologyElement.TYPE,
                                        TopologyElement.TYPE_OPTICAL_LAYER);
        portData.createStringAttribute(TopologyElement.ELEMENT_TYPE,
                                        PortType.TRANSPONDER_PORT.toString());
        portData.createStringAttribute(TopologyElement.ELEMENT_CONFIG_STATE,
                                        ConfigState.CONFIGURED.toString());
        portData.createStringAttribute(TopologyElement.ELEMENT_ADMIN_STATUS,
                                        AdminStatus.INACTIVE.toString());
        this.putPort(portData);
        return this.getPort(sw.getDpid(), portData.getPortNumber());
    }

    /**
     * Adds {@link PortType#WDM_PORT} to the topology.
     *
     * @param sw
     * @param portNumber
     * @return Port
     */
    //todo - add w-port specific properties
    public Port addWPort(Switch sw, Long portNumber) {
        PortData portData = new PortData(sw.getDpid(),
                                    PortNumber.uint16(portNumber.shortValue()));
        portData.createStringAttribute(TopologyElement.TYPE,
                                        TopologyElement.TYPE_OPTICAL_LAYER);
        portData.createStringAttribute(TopologyElement.ELEMENT_TYPE,
                                        PortType.WDM_PORT.toString());
        portData.createStringAttribute(TopologyElement.ELEMENT_CONFIG_STATE,
                                        ConfigState.CONFIGURED.toString());
        portData.createStringAttribute(TopologyElement.ELEMENT_ADMIN_STATUS,
                                        AdminStatus.INACTIVE.toString());
        this.putPort(portData);
        return this.getPort(sw.getDpid(), portData.getPortNumber());
    }

    /**
     * Adds {@link LinkType#PACKET_TPORT_LINK} ,
     * {@link LinkType#ETHERNET_LINK} and
     * {@link LinkType#WDM_LINK} unidirectional links to the topology.
     *
     * @param srcDpid     Source Dpid
     * @param srcPortNo   Source Port Number
     * @param dstDpid     Destination Dpid
     * @param dstPortNo   Destination Port Number
     * @param type        Type of this link, if its packet or optical
     * @param linkType    Link Type of this link
     * @param configState Config State of this link
     * @param adminStatus Admin Status of this link
     */
    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    public void addUniDirectionalLinks(Long srcDpid, Long srcPortNo,
                                       Long dstDpid, Long dstPortNo,
                                       String type, LinkType linkType,
                                       ConfigState configState,
                                       AdminStatus adminStatus) {

        final Dpid srcDpidObj = new Dpid(srcDpid);
        final Dpid dstDpidObj = new Dpid(dstDpid);
        final PortNumber srcPortNum = PortNumber.uint16(srcPortNo.shortValue());
        final PortNumber dstPortNum = PortNumber.uint16(dstPortNo.shortValue());
        LinkData linkData = new LinkData(new SwitchPort(srcDpidObj,
                                                           srcPortNum),
                                            new SwitchPort(dstDpidObj,
                                                           dstPortNum));
        linkData.createStringAttribute(TopologyElement.TYPE, type);
        linkData.createStringAttribute(TopologyElement.ELEMENT_TYPE,
                                        linkType.toString());
        linkData.createStringAttribute(TopologyElement.ELEMENT_CONFIG_STATE,
                                        configState.toString());
        linkData.createStringAttribute(TopologyElement.ELEMENT_ADMIN_STATUS,
                                        adminStatus.toString());
        LinkData[] links = new LinkData[1];
        links[0] = linkData;
        putLink(links[0]);

    }


    /**
     * create sample topology of 2 packet switches and 3  optical switches.
     * <pre>
     * [P1] ---------> [P2]
     *  |               |
     *  |               |
     * [O1]--->[O3]--> [O2]
     * </pre>
     */
    public void createSamplePacketOpticalTopology1() {
        Switch p1 = addSwitch(1L);
        //addPort(P1, LOCAL_PORT);
        Switch p2 = addSwitch(2L);
        //addPort(P2, LOCAL_PORT);

        Switch o1 = addOpticalSwitch(3L);
        Switch o2 = addOpticalSwitch(4L);
        Switch o3 = addOpticalSwitch(5L);

        addPort(p1, 12L); // P1 -> P2
        //addPort(P1, 14L); // P1 -> O1

        addPort(p2, 21L); // P1 -> P2
        //addPort(P2, 23L); // O2 -> P2

        addTPort(o1, 32L); // P1 -> O1
        addWPort(o1, 34L); // O1 -> O3
        addTPort(o2, 41L); // O2 -> P2
        addWPort(o2, 42L); // O3 -> O2
        addWPort(o3, 51L); // O1 -> O3
        addWPort(o3, 52L); // 03 -> 02

        addUniDirectionalLinks(1L, 12L, 2L, 21L,
                               TopologyElement.TYPE_PACKET_LAYER,
                               LinkType.ETHERNET_LINK,
                               ConfigState.NOT_CONFIGURED,
                               AdminStatus.ACTIVE); // packet link

        addUniDirectionalLinks(1L, 12L, 3L, 32L,
                               TopologyElement.TYPE_PACKET_LAYER +
                                       TopologyElement.TYPE_OPTICAL_LAYER,
                               LinkType.PACKET_TPORT_LINK,
                               ConfigState.CONFIGURED, AdminStatus.INACTIVE);
        // packet-optical links

        addUniDirectionalLinks(3L, 34L, 5L, 51L,
                               TopologyElement.TYPE_OPTICAL_LAYER,
                               LinkType.WDM_LINK, ConfigState.CONFIGURED,
                               AdminStatus.INACTIVE); //wdm links

        addUniDirectionalLinks(5L, 52L, 4L, 42L,
                               TopologyElement.TYPE_OPTICAL_LAYER,
                               LinkType.WDM_LINK, ConfigState.CONFIGURED,
                               AdminStatus.INACTIVE); //wdm links

        addUniDirectionalLinks(4L, 41L, 2L, 21L,
                               TopologyElement.TYPE_PACKET_LAYER +
                                       TopologyElement.TYPE_OPTICAL_LAYER,
                               LinkType.PACKET_TPORT_LINK,
                               ConfigState.CONFIGURED, AdminStatus.INACTIVE);
        //packet-optical links

    }

}
