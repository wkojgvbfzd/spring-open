package net.onrc.onos.core.util;

import javax.annotation.concurrent.Immutable;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.primitives.UnsignedInts;

/**
 * Immutable class representing a port number.
 */
@Immutable
public final class PortNumber {

    private final int value;

    /**
     * Default constructor.
     */
    protected PortNumber() {
        this.value = 0;
    }

    /**
     * Copy constructor.
     *
     * @param other the object to copy from.
     */
    public PortNumber(PortNumber other) {
        this.value = other.value;
    }

    /**
     * Constructor from a short integer value.
     *
     * @param value the value to use.
     */
    protected PortNumber(short value) {
        this.value = (int) shortToUnsignedLong(value);
    }

    /**
     * Creates the unsigned 16 bit port number.
     *
     * @param number unsigned 16 bit port number.
     * @return PortNumber instance
     */
    public static PortNumber uint16(final short number) {
        return new PortNumber(number);
    }

    /**
     * Creates the unsigned 32 bit port number.
     *
     * @param number unsigned 32 bit port number.
     * @return PortNumber instance
     */
    public static PortNumber uint32(final int number) {
        return new PortNumber(number);
    }

    /**
     * Constructor from an int.
     *
     * @param value the value to use. (Value will not be validated in any way.)
     */
    PortNumber(int value) {
        this.value = value;
    }

    // TODO We may want a factory method version
    //      which does the range validation of parsed value.
    /**
     * Constructor from decimal string.
     *
     * @param decStr decimal string representation of a port number
     */
    public PortNumber(String decStr) {
        this(decStr, 10);
    }

    /**
     * Constructor from string.
     *
     * @param s string representation of a port number
     * @param radix the radix to use while parsing {@code s}
     */
    public PortNumber(String s, int radix) {
        this(UnsignedInts.parseUnsignedInt(s, radix));
    }

    /**
     * Convert unsigned short to unsigned long.
     *
     * @param portno unsigned integer representing port number
     * @return port number as unsigned long
     */
    public static long shortToUnsignedLong(short portno) {
        return UnsignedInts.toLong(0xffff & portno);
    }

    /**
     * Gets the port number as short.
     * <p/>
     * Note: User of this method needs to be careful, handling unsigned value.
     * @return number as short
     */
    public short shortValue() {
        return (short) value;
    }

    /**
     * Gets the value of the port as unsigned integer.
     *
     * @return the value of the port.
     */
    @JsonProperty("value")
    public long value() {
        return 0xffffffffL & value;
    }

    /**
     * Convert the port value as unsigned integer to a string.
     *
     * @return the port value as a string.
     */
    @Override
    public String toString() {
        return UnsignedInts.toString(value);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PortNumber)) {
            return false;
        }

        PortNumber otherPort = (PortNumber) other;

        return value == otherPort.value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
