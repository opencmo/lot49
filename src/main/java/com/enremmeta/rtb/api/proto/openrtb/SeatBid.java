package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;

import com.enremmeta.rtb.RtbBean;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SeatBid implements RtbBean {

    public SeatBid() {
        // TODO Auto-generated constructor stub
    }

    public List<Bid> bid;
    private String seat;
    private String group;
    private String ext;

    public List<Bid> getBid() {
        return bid;
    }

    public void setBid(List<Bid> bid) {
        this.bid = bid;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

}
