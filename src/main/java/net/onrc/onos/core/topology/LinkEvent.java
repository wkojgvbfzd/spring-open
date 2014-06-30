package net.onrc.onos.core.topology;

import java.nio.ByteBuffer;

import net.onrc.onos.core.topology.web.serializers.LinkEventSerializer;
import net.onrc.onos.core.util.Dpid;
import net.onrc.onos.core.util.PortNumber;
import net.onrc.onos.core.util.SwitchPort;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Self-contained Link event Object.
 * <p/>
 * TODO: We probably want common base class/interface for Self-Contained Event Object.
 */

@JsonSerialize(using = LinkEventSerializer.class)
public class LinkEvent {
    protected final SwitchPort src;
    protected final SwitchPort dst;

    /**
     * Default constructor for Serializer to use.
     */
    @Deprecated
    public LinkEvent() {
        src = null;
        dst = null;
    }

    public LinkEvent(Long srcDpid, Long srcPortNo, Long dstDpid,
                     Long dstPortNo) {
        src = new SwitchPort(srcDpid, srcPortNo);
        dst = new SwitchPort(dstDpid, dstPortNo);
    }

    public LinkEvent(Link link) {
        src = new SwitchPort(link.getSrcSwitch().getDpid(),
                link.getSrcPort().getNumber());
        dst = new SwitchPort(link.getDstSwitch().getDpid(),
                link.getDstPort().getNumber());
    }

    public LinkEvent(Dpid srcDpid, PortNumber srcPortNo,
                     Dpid dstDpid, PortNumber dstPortNo) {
        src = new SwitchPort(srcDpid, srcPortNo);
        dst = new SwitchPort(dstDpid, dstPortNo);
    }

    public SwitchPort getSrc() {
        return src;
    }

    public SwitchPort getDst() {
        return dst;
    }

    @Override
    public String toString() {
        return "[LinkEvent " + src + "->" + dst + "]";
    }

    public static final int LINKID_BYTES = 2 + PortEvent.PORTID_BYTES * 2;

    public static ByteBuffer getLinkID(Dpid srcDpid, PortNumber srcPortNo,
                                       Dpid dstDpid, PortNumber dstPortNo) {
        return getLinkID(srcDpid.value(), (long) srcPortNo.value(),
                         dstDpid.value(), (long) dstPortNo.value());
    }

    public static ByteBuffer getLinkID(Long srcDpid, Long srcPortNo,
                                       Long dstDpid, Long dstPortNo) {
        return (ByteBuffer) ByteBuffer.allocate(LinkEvent.LINKID_BYTES).putChar('L')
                .put(PortEvent.getPortID(srcDpid, srcPortNo))
                .put(PortEvent.getPortID(dstDpid, dstPortNo)).flip();
    }

    public byte[] getID() {
        return getIDasByteBuffer().array();
    }

    public ByteBuffer getIDasByteBuffer() {
        return getLinkID(src.getDpid(), src.getPortNumber(),
                dst.getDpid(), dst.getPortNumber());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dst == null) ? 0 : dst.hashCode());
        result = prime * result + ((src == null) ? 0 : src.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LinkEvent other = (LinkEvent) obj;
        if (dst == null) {
            if (other.dst != null) {
                return false;
            }
        } else if (!dst.equals(other.dst)) {
            return false;
        }
        if (src == null) {
            if (other.src != null) {
                return false;
            }
        } else if (!src.equals(other.src)) {
            return false;
        }
        return true;
    }
}
