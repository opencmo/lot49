package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;
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
public class Video implements RtbBean, Cloneable {

    public Video() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Video clone() throws CloneNotSupportedException {
        Video v2 = (Video) super.clone();
        // Shallow copy ok for now
        return v2;
    }

    private List<String> mimes;
    private int linearity;
    private int minduration;
    private int maxduration;
    private int protocol;
    private List<Integer> protocols;
    private int w;
    private int h;

    private int startdelay;
    private int sequence;
    private List<Integer> battr;
    private int maxextended;
    private int minbitrate;
    private int maxbitrate;
    private int boxingallowed;
    public List<Integer> playbackmethod;
    public List<Integer> delivery;
    private int pos;

    public List<Banner> companionad;
    public List<Integer> api;
    public List<Integer> companiontype;
    private Map ext;

    public void setExt(Map ext) {
        this.ext = ext;
    }

    public List<String> getMimes() {
        return mimes;
    }

    public void setMimes(List<String> mimes) {
        this.mimes = mimes;
    }

    public int getLinearity() {
        return linearity;
    }

    public void setLinearity(int linearity) {
        this.linearity = linearity;
    }

    public int getMinduration() {
        return minduration;
    }

    public void setMinduration(int minduration) {
        this.minduration = minduration;
    }

    public int getMaxduration() {
        return maxduration;
    }

    public void setMaxduration(int maxduration) {
        this.maxduration = maxduration;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public List<Integer> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<Integer> protocols) {
        this.protocols = protocols;
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

    public int getStartdelay() {
        return startdelay;
    }

    public void setStartdelay(int startdelay) {
        this.startdelay = startdelay;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public List<Integer> getBattr() {
        return battr;
    }

    public void setBattr(List<Integer> battr) {
        this.battr = battr;
    }

    public int getMaxextended() {
        return maxextended;
    }

    public void setMaxextended(int maxextended) {
        this.maxextended = maxextended;
    }

    public int getMinbitrate() {
        return minbitrate;
    }

    public void setMinbitrate(int minbitrate) {
        this.minbitrate = minbitrate;
    }

    public int getMaxbitrate() {
        return maxbitrate;
    }

    public void setMaxbitrate(int maxbitrate) {
        this.maxbitrate = maxbitrate;
    }

    public int getBoxingallowed() {
        return boxingallowed;
    }

    public void setBoxingallowed(int boxingallowed) {
        this.boxingallowed = boxingallowed;
    }

    public List<Integer> getPlaybackmethod() {
        return playbackmethod;
    }

    public void setPlaybackmethod(List<Integer> playbackmethod) {
        this.playbackmethod = playbackmethod;
    }

    public List<Integer> getDelivery() {
        return delivery;
    }

    public void setDelivery(List<Integer> delivery) {
        this.delivery = delivery;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public List<Banner> getCompanionad() {
        return companionad;
    }

    public void setCompanionad(List<Banner> companionad) {
        this.companionad = companionad;
    }

    public List<Integer> getApi() {
        return api;
    }

    public void setApi(List<Integer> api) {
        this.api = api;
    }

    public List<Integer> getCompaniontype() {
        return companiontype;
    }

    public void setCompaniontype(List<Integer> companiontype) {
        this.companiontype = companiontype;
    }

    public Map getExt() {
        return ext;
    }

}
