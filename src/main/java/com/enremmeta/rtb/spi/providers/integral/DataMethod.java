package com.enremmeta.rtb.spi.providers.integral;


public enum DataMethod {

    ALL,

    BRAND_SAFETY,

    CONTEXTUAL,

    VIEWABILITY,

    PAGE_CLUTTER,

    PAGE_LANGUAGE,

    TRAQ_SCORE;

    public static DataMethod fromString(final String value) {
        try {
            if (value == null) {
                return null;
            } else {
                return valueOf(value.toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }


    public String toString() {
        return this.name().toLowerCase();
    }

}
