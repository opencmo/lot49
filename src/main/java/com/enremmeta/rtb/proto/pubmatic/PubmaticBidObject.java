package com.enremmeta.rtb.proto.pubmatic;

import java.util.Map;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.RtbBean;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PubmaticBidObject implements RtbBean {
    private String bId;

    public PubmaticBidObject() {
        super();
    }

    public PubmaticBidObject(Map map) throws Lot49Exception {
        super();
        try {
            this.bId = (String) map.get("bId");
            this.status = (Integer) map.get("st");
        } catch (ClassCastException cce) {
            throw new Lot49Exception("Error parsing bInfo " + map, cce);
        }
    }

    public String getbId() {
        return bId;
    }

    public void setbId(String bId) {
        this.bId = bId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @JsonProperty("st")
    private int status;

    @Override
    public String toString() {
        return "{ \"bId\" : \"" + bId + "\", \"st\" : \"" + status + "\" } ";
    }

}
