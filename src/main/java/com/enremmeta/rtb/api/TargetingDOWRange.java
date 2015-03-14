package com.enremmeta.rtb.api;

public class TargetingDOWRange implements TargetingDOW {

    private final int minDow;
    private final int maxDow;

    public TargetingDOWRange(int minDow, int maxDow) {
        super();
        this.minDow = minDow;
        this.maxDow = maxDow;
    }

    @Override
    public String validate() {
        if (this.minDow < MONDAY || this.maxDow < MONDAY) {
            return "Value less than MONDAY";
        }
        if (this.minDow > SUNDAY || this.maxDow > SUNDAY) {
            return "Value greater than SUNDAY";
        }
        if (this.minDow > this.maxDow) {
            return minDow + " > " + maxDow;
        }
        return null;
    }

    @Override
    public boolean check(int day) {
        if (day < this.minDow) {
            return false;
        }
        if (day > this.maxDow) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TargetingDOWRange(" + minDow + "," + maxDow + ")";
    }
}
