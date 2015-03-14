package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;

import com.enremmeta.rtb.RtbBean;

public class Publisher implements RtbBean {

    public Publisher() {
        // TODO Auto-generated constructor stub
    }

    private String id;
    private String name;
    private List<String> cat;

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

    public List<String> getCat() {
        return cat;
    }

    public void setCat(List<String> cat) {
        this.cat = cat;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    private String domain;
    private String ext;

}
