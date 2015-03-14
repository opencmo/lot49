package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * OpenRTB bid request.The fields' meaning is described in the Javadoc for the getter, not for the
 * field; for example, to find out what {@link #id} field in configuration means, look at
 * {@link #getId()}.
 * 
 * @see <a href= "http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf"> OpenRTB
 *      specification</a>
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
@XmlRootElement
@JsonRootName(value = "")
@JsonInclude(Include.NON_NULL)
public class OpenRtbRequest implements RtbBean {
    private Regs regs;

    public Regs getRegs() {
        return regs;
    }

    public void setRegs(Regs regs) {
        this.regs = regs;
    }

    public OpenRtbRequest() {
        super();
    }


    private PMP pmp;

    /**
     * This is not an OpenRTB standard, but Bidswitch's extension.
     * 
     * @return the PMP
     */
    public PMP getPmp() {
        return pmp;
    }

    public void setPmp(PMP pmp) {
        this.pmp = pmp;
    }

    /**
     * We are going to ignore this in serialization, as this is used only to pass around information
     * internally during Lot49 bid process.
     */
    @JsonIgnore
    private Lot49Ext lot49Ext = new Lot49Ext();

    @JsonIgnore
    public Lot49Ext getLot49Ext() {
        return lot49Ext;
    }


    private String id;
    public List<Impression> imp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Impression> getImp() {
        return imp;
    }

    public void setImp(List<Impression> imp) {
        this.imp = imp;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getAt() {
        return at;
    }

    public void setAt(int at) {
        this.at = at;
    }

    public int getTmax() {
        return tmax;
    }

    public void setTmax(int tmax) {
        this.tmax = tmax;
    }

    public List<String> getWseat() {
        return wseat;
    }

    public void setWseat(List<String> wseat) {
        this.wseat = wseat;
    }

    public int getAllimps() {
        return allimps;
    }

    public void setAllimps(int allimps) {
        this.allimps = allimps;
    }

    public List<String> getCur() {
        return cur;
    }

    public void setCur(List<String> cur) {
        this.cur = cur;
    }

    public List<String> getBcat() {
        return bcat;
    }

    public void setBcat(List<String> bcat) {
        this.bcat = bcat;
    }

    public List<String> getBadv() {
        return badv;
    }

    public void setBadv(List<String> badv) {
        this.badv = badv;
    }

    public Map getExt() {
        return this.ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

    private Map unparseable;

    public Map getUnparseable() {
        return unparseable;
    }

    public void setUnparseable(Map unparseable) {
        this.unparseable = unparseable;
    }

    private Ad ad;

    /**
     * Ad we're responding with.
     * 
     * @return Ad
     */
    public Ad getAd() {
        return ad;
    }

    public void setAd(Ad ad) {
        this.ad = ad;
    }


    private Site site = new Site();
    private App app = new App();
    private Device device = new Device();
    private User user = new User();
    private int at;
    private int tmax;
    public List<String> wseat;
    private int allimps;
    public List<String> cur;
    public List<String> bcat;
    public List<String> badv;
    private Map ext;

}
