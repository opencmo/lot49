package com.enremmeta.rtb.api.proto.openrtb;

import java.util.Map;

import com.enremmeta.rtb.RtbBean;

/**
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *
 */
public final class Banner implements RtbBean, Cloneable {

    public Banner() {
        super();
    }

    private int hmax;

    private int wmax;

    private int hmin;

    private int wmin;

    public int getHmax() {
        return hmax;
    }

    public void setHmax(int hmax) {
        this.hmax = hmax;
    }

    public int getWmax() {
        return wmax;
    }

    public void setWmax(int wmax) {
        this.wmax = wmax;
    }

    @Override
    public Banner clone() throws CloneNotSupportedException {
        Banner b2 = (Banner) super.clone();
        return b2;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public java.util.List<Integer> getBtype() {
        return btype;
    }

    public void setBtype(java.util.List<Integer> btype) {
        this.btype = btype;
    }

    public java.util.List<Integer> getBattr() {
        return battr;
    }

    public void setBattr(java.util.List<Integer> battr) {
        this.battr = battr;
    }

    public java.util.List<String> getMimes() {
        return mimes;
    }

    public void setMimes(java.util.List<String> mimes) {
        this.mimes = mimes;
    }

    public int getTopframe() {
        return topframe;
    }

    public void setTopframe(int topframe) {
        this.topframe = topframe;
    }

    public java.util.List<Integer> getExpdir() {
        return expdir;
    }

    public void setExpdir(java.util.List<Integer> expdir) {
        this.expdir = expdir;
    }

    public java.util.List<Integer> getApi() {
        return api;
    }

    public void setApi(java.util.List<Integer> api) {
        this.api = api;
    }

    public Map getExt() {
        return ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

    public int getHmin() {
        return hmin;
    }

    public void setHmin(int hmin) {
        this.hmin = hmin;
    }

    public int getWmin() {
        return wmin;
    }

    public void setWmin(int wmin) {
        this.wmin = wmin;
    }

    private int w;
    private int h;
    private String id;
    private int pos;
    private java.util.List<Integer> btype;
    private java.util.List<Integer> battr;
    private java.util.List<String> mimes;
    private int topframe;
    private java.util.List<Integer> expdir;
    private java.util.List<Integer> api;
    private Map ext;

}
