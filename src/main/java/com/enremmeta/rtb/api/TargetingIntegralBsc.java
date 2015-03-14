package com.enremmeta.rtb.api;

public class TargetingIntegralBsc {

    private boolean excludeHighRisk;

    private boolean excludeModerateRisk;

    public TargetingIntegralBsc(boolean excludeHighRisk, boolean excludeModerateRisk) {
        this.excludeHighRisk = excludeHighRisk;
        this.excludeModerateRisk = excludeModerateRisk;
    }

    public boolean validate(int value) {
        if (value < 251) {
            return false;
        }

        if (excludeHighRisk) {
            if (value >= 251 && value <= 500) {
                return false;
            }
        }

        if (excludeModerateRisk) {
            if (value > 500 && value <= 750) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        return "[Exclude high risk=" + excludeHighRisk + "] and [Exclude moderate risk="
                        + excludeModerateRisk;
    }
}
