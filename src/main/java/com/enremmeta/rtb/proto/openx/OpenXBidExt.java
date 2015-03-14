package com.enremmeta.rtb.proto.openx;

import com.enremmeta.rtb.RtbBean;

public class OpenXBidExt implements RtbBean {
    private MatchingAdId matching_ad_id;

    public MatchingAdId getMatching_ad_id() {
        return matching_ad_id;
    }

    public void setMatching_ad_id(MatchingAdId matching_ad_id) {
        this.matching_ad_id = matching_ad_id;
    }


}
