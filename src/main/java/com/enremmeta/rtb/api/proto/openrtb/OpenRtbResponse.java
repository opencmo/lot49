package com.enremmeta.rtb.api.proto.openrtb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class OpenRtbResponse implements RtbBean {

    public OpenRtbResponse() {
        // TODO Auto-generated constructor stub
    }

    private String id;
    public List<SeatBid> seatbid = new ArrayList<SeatBid>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SeatBid> getSeatbid() {
        return seatbid;
    }

    public void setSeatbid(List<SeatBid> seatbid) {
        this.seatbid = seatbid;
    }

    public String getBidid() {
        return bidid;
    }

    public void setBidid(String bidid) {
        this.bidid = bidid;
    }

    public String getCur() {
        return cur;
    }

    public void setCur(String cur) {
        this.cur = cur;
    }

    public String getCustomdata() {
        return customdata;
    }

    public void setCustomdata(String customdata) {
        this.customdata = customdata;
    }

    public Map getExt() {
        return ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

    private String bidid;
    private String cur = "USD";
    private String customdata;
    private Map ext;
}
