package com.enremmeta.rtb.spi.providers.integral.result.dto;

import java.util.Date;

public class BrandSafetyDto {

    String action;

    String risk;

    BscDto bsc;

    Integer vis;

    Date ttl;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public BscDto getBsc() {
        return bsc;
    }

    public void setBsc(BscDto bsc) {
        this.bsc = bsc;
    }

    public Integer getVis() {
        return vis;
    }

    public void setVis(Integer vis) {
        this.vis = vis;
    }

    public Date getTtl() {
        return ttl;
    }

    public void setTtl(Date ttl) {
        this.ttl = ttl;
    }

}
