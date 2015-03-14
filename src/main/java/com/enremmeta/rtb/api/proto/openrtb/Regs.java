package com.enremmeta.rtb.api.proto.openrtb;

import java.util.Map;

import com.enremmeta.rtb.RtbBean;

public class Regs implements RtbBean {
    private int coppa;
    private Map ext;

    public int getCoppa() {
        return coppa;
    }

    public void setCoppa(int coppa) {
        this.coppa = coppa;
    }

    public Map getExt() {
        return ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }
}
