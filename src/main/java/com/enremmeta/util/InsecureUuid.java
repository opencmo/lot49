package com.enremmeta.util;

/**
 * Without synchronization, because who cares but this is faster.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public final class InsecureUuid {

    public static String getId() {
        return randomInsecureUuid().toString();
    }

    private InsecureUuid(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    /*
     * The most significant 64 bits of this UUID.
     * 
     * @serial
     */
    private final long mostSigBits;

    /*
     * The least significant 64 bits of this UUID.
     * 
     * @serial
     */
    private final long leastSigBits;

    /**
     * Static factory to retrieve a type 4 (pseudo randomly generated) UUID.
     * 
     * @return A randomly generated {@code UUID}
     */
    public static InsecureUuid randomInsecureUuid() {
        byte[] randomBytes = new byte[16];
        Utils.RANDOM.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f; /* clear version */
        randomBytes[6] |= 0x40; /* set to version 4 */
        randomBytes[8] &= 0x3f; /* clear variant */
        randomBytes[8] |= 0x80; /* set to IETF variant */
        return new InsecureUuid(randomBytes);
    }

    /**
     * Returns a {@code String} object representing this {@code UUID}.
     *
     * <p>
     * The UUID string representation is as described by this BNF: <blockquote>
     * 
     * <pre>
     * {@code
     * UUID                   = <time_low> "-" <time_mid> "-"
     *                          <time_high_and_version> "-"
     *                          <variant_and_sequence> "-"
     *                          <node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               =
     *       "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     *       | "a" | "b" | "c" | "d" | "e" | "f"
     *       | "A" | "B" | "C" | "D" | "E" | "F"
     * }
     * </pre>
     * 
     * </blockquote>
     *
     * @return A string representation of this {@code UUID}
     */
    public String toString() {
        return (digits(mostSigBits >> 32, 8) + "-" + digits(mostSigBits >> 16, 4) + "-"
                        + digits(mostSigBits, 4) + "-" + digits(leastSigBits >> 48, 4) + "-"
                        + digits(leastSigBits, 12));
    }

    /**
     * Returns val represented by the specified number of hex digits.
     */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }
}
