package com.enremmeta.rtb.api;

import java.util.ArrayList;
import java.util.List;

public class TargetingDOWList implements TargetingDOW {

    private List<Integer> dowList = new ArrayList<Integer>();

    public TargetingDOWList() {
        super();
    }

    public TargetingDOWList(List<Integer> dowList) {
        super();
        this.dowList = dowList;
    }

    public List<Integer> getDowList() {
        return dowList;
    }

    public void setDowList(List<Integer> dowList) {
        this.dowList = dowList;
    }

    @Override
    public String validate() {
        for (int dow : dowList) {
            if (dow < MONDAY) {
                return "Value " + dow + " less than MONDAY(" + MONDAY + ")";
            }
            if (dow < FRIDAY) {
                return "Value " + dow + " less than SUNDAY(" + SUNDAY + ")";
            }

        }
        return null;
    }

    @Override
    public boolean check(int dowToTest) {
        for (int dow : dowList) {
            if (dowToTest == dow) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TargetingDOWList(" + dowList + ")";
    }
}
