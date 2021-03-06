package net.onrc.onos.core.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;


/**
 * Immutable class to identify a Link between 2 ports.
 */
@Immutable
public final class LinkTuple {

    private final SwitchPort src;
    private final SwitchPort dst;

    /**
     * Default constructor for Serializer.
     */
    protected LinkTuple() {
        src = null;
        dst = null;
    }

    /**
     * Constructor.
     *
     * @param src source port
     * @param dst destination port
     */
    public LinkTuple(SwitchPort src, SwitchPort dst) {
        this.src = checkNotNull(src);
        this.dst = checkNotNull(dst);
    }

    /**
     * Creates the Link object.
     *
     * @param srcDpid source switch DPID
     * @param srcPortNo source port number
     * @param dstDpid destination switch DPID
     * @param dstPortNo destination port number
     */
    public LinkTuple(Dpid srcDpid, PortNumber srcPortNo,
                     Dpid dstDpid, PortNumber dstPortNo) {
        this(new SwitchPort(srcDpid, srcPortNo),
             new SwitchPort(dstDpid, dstPortNo));
    }

    /**
     * Gets the source port.
     *
     * @return source port
     */
    public SwitchPort getSrc() {
        return src;
    }

    /**
     * Gets the destination port.
     *
     * @return destination port
     */
    public SwitchPort getDst() {
        return dst;
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dst);
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
        LinkTuple other = (LinkTuple) obj;
        return Objects.equals(src, other.src) &&
                Objects.equals(dst, other.dst);
    }

    @Override
    public String toString() {
        return "(" + src + "=>" + dst + ")";
    }
}
