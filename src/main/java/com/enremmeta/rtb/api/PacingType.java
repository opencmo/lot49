package com.enremmeta.rtb.api;

public enum PacingType {
    GREEDY("GREEDY"), HOURLY("HOURLY"), DAILY("DAILY");

    private final String type;

    PacingType(String type) {
        this.type = type;
    }
}
