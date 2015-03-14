package com.enremmeta.rtb.api;

public class TargetingHourRange implements TargetingHour {

    private final int minHour;
    private final int maxHour;

    public TargetingHourRange(int minHour, int maxHour) {
        super();
        this.minHour = minHour;
        this.maxHour = maxHour;
    }

    @Override
    public String validate() {
        if (this.maxHour < 0 || this.minHour < 0) {
            return "Value less than 0";
        }
        if (this.minHour > 23 || this.maxHour > 23) {
            return "Value greater than 23";
        }
        if (this.minHour > this.maxHour) {
            return minHour + " > " + maxHour;
        }
        return null;
    }

    @Override
    public boolean check(int hour) {
        if (hour < this.minHour) {
            return false;
        }
        if (hour > this.maxHour) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TargetingHourRange(" + minHour + "," + maxHour + ")";
    }
}
