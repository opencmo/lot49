package com.enremmeta.rtb.proto.bidswitch;

import com.enremmeta.rtb.RtbBean;

public class BidswitchVideoExt implements RtbBean {
    private int duration;
    private String advertiser_name;
    private String vast_url;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getAdvertiser_name() {
        return advertiser_name;
    }

    public void setAdvertiser_name(String advertiser_name) {
        this.advertiser_name = advertiser_name;
    }

    public String getVast_url() {
        return vast_url;
    }

    public void setVast_url(String vast_url) {
        this.vast_url = vast_url;
    }

}
