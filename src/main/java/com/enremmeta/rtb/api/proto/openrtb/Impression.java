package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;

public class Impression implements RtbBean, Cloneable {

    private String id;

    private int exp;

    // TODO wtf is this?
    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    private Banner banner;
    private Video video;
    private String displaymanager;
    private String displaymanagerver;
    private Integer instl;
    private String tagid;
    private Float bidfloor = new Float(0);
    private String bidfloorcur;
    private List<String> iframebuster;
    private Map ext;
    private int secure;

    public PMP getPmp() {
        return pmp;
    }

    public void setPmp(PMP pmp) {
        this.pmp = pmp;
    }

    private PMP pmp;

    public int getSecure() {
        return secure;
    }

    public void setSecure(int secure) {
        this.secure = secure;
    }

    @Override
    public Impression clone() throws CloneNotSupportedException {
        Impression i2 = (Impression) super.clone();
        if (video != null) {
            i2.video = this.video.clone();
        }
        if (banner != null) {
            i2.banner = this.banner.clone();
        }
        return i2;
    }

    public Impression() {
        // TODO Auto-generated constructor stub
    }

    public Banner getBanner() {
        return banner;
    }

    public Float getBidfloor() {
        return bidfloor;
    }

    public String getBidfloorcur() {
        return bidfloorcur;
    }

    public String getDisplaymanager() {
        return displaymanager;
    }

    public String getDisplaymanagerver() {
        return displaymanagerver;
    }

    public Map getExt() {
        return ext;
    }

    public String getId() {
        return id;
    }

    public List<String> getIframebuster() {
        return iframebuster;
    }

    public Integer getInstl() {
        return instl;
    }

    public String getTagid() {
        return tagid;
    }

    public Video getVideo() {
        return video;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public void setBidfloor(Float bidfloor) {
        this.bidfloor = bidfloor;
    }

    public void setBidfloorcur(String bidfloorcur) {
        this.bidfloorcur = bidfloorcur;
    }

    public void setDisplaymanager(String displaymanager) {
        this.displaymanager = displaymanager;
    }

    public void setDisplaymanagerver(String displaymanagerver) {
        this.displaymanagerver = displaymanagerver;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIframebuster(List<String> iframebuster) {
        this.iframebuster = iframebuster;
    }

    public void setInstl(Integer instl) {
        this.instl = instl;
    }

    public void setTagid(String tagid) {
        this.tagid = tagid;
    }

    public void setVideo(Video video) {
        this.video = video;
    }
}
