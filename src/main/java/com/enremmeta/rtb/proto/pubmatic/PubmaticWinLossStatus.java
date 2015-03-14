package com.enremmeta.rtb.proto.pubmatic;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PubmaticWinLossStatus {
    WIN(0), TIMEOUT(1), INCOMPLETE(2), INVALID_BID_PRICE(3), OUTBID(4), ADOMAIN_BLOCKED(
                    5), ADOMAIN_MISSING(6), CRID_MISSING(7), CREATIVE_BLOCKED(
                                    8), BID_PRICE_DECRYPTION_FAILED(9), FLOOR(
                                                    10), REQUEST_ID_MISMATCH(11), SSL_EXPECTED(
                                                                    12), CURRENCY_INVALID(13);

    private static final Map<Integer, PubmaticWinLossStatus> lookup =
                    new HashMap<Integer, PubmaticWinLossStatus>();

    static {
        for (PubmaticWinLossStatus s : EnumSet.allOf(PubmaticWinLossStatus.class))
            lookup.put(s.getCode(), s);
    }

    public static PubmaticWinLossStatus get(int code) {
        return lookup.get(code);
    }

    private PubmaticWinLossStatus(int code) {
        this.code = code;

    }

    private final int code;

    public int getCode() {
        return code;
    }

}
