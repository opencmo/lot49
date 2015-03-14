package com.enremmeta.rtb.api;

import java.util.ArrayList;
import java.util.List;

public class TargetingHourList implements TargetingHour {

    private List<Integer> hourList = new ArrayList<Integer>();

    public TargetingHourList() {
        super();
    }

    public TargetingHourList(List<Integer> hourList) {
        super();
        this.hourList = hourList;
    }

    public List<Integer> getHourList() {
        return hourList;
    }

    public void setHourList(List<Integer> hourList) {
        this.hourList = hourList;
    }

    @Override
    public String validate() {
        for (int hour : hourList) {
            if (hour < 0) {
                return "Value " + hour + " less than 0";
            }
            if (hour > 23) {
                return "Value " + hour + " greater than 23";
            }

        }
        return null;
    }

    @Override
    public boolean check(int hourToTest) {
        for (int hour : hourList) {
            if (hourToTest == hour) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TargetingHourList(" + hourList + ")";
    }
}
