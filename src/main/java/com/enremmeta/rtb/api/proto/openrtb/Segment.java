package com.enremmeta.rtb.api.proto.openrtb;

import com.enremmeta.rtb.RtbBean;

public class Segment implements RtbBean {

    public Segment() {
        // TODO Auto-generated constructor stub
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    private String id;
    private String name;
    private String value;
    private String ext;

}
