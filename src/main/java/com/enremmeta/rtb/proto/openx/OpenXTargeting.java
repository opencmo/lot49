package com.enremmeta.rtb.proto.openx;

import java.util.ArrayList;
import java.util.List;

public class OpenXTargeting {

    public OpenXTargeting() {
        // TODO Auto-generated constructor stub
    }

    private List<MatchingAdId> requiredMatchingAdIds = new ArrayList<MatchingAdId>();

    public List<MatchingAdId> getRequiredMatchingAdIds() {
        return requiredMatchingAdIds;
    }

    public void setRequiredMatchingAdIds(List<MatchingAdId> requiredMatchingAdIds) {
        this.requiredMatchingAdIds = requiredMatchingAdIds;
    }
}
