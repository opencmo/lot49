package com.enremmeta.rtb.api.proto.openrtb;

import java.util.Map;

import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;

/**
 * OpenRTB Device object.
 * 
 * @see <a href="http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-3.pdf">OpenRTB
 *      2.3 (section 3.2.11)</a>
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class Device implements RtbBean {

    private int lmt;

    public int getLmt() {
        return lmt;
    }

    public void setLmt(int lmt) {
        this.lmt = lmt;
    }

    private String carrier;
    private java.lang.Integer connectiontype;

    private java.lang.Integer devicetype;

    private String didmd5;

    private String didsha1;

    private String ifa;

    public String getIfa() {
        return ifa;
    }

    public void setIfa(String ifa) {
        this.ifa = ifa;
    }

    public void setDevicetype(java.lang.Integer devicetype) {
        this.devicetype = devicetype;
    }

    private int dnt;

    private String dpidmd5;

    private String dpidsha1;

    private Map ext;

    private String flashver;

    private Geo geo;

    private String ip;

    private String ipv6;

    private java.lang.Integer Js;

    private String language;

    private String macmd5;

    private String macsha1;

    private String make;

    private String model;

    private String os;

    private String Osv;

    private String ua;

    private int w;

    private int h;

    private float pxratio;

    public Device() {
        // TODO Auto-generated constructor stub
    }

    public String getCarrier() {
        return carrier;
    }

    public java.lang.Integer getConnectiontype() {
        return connectiontype;
    }

    public java.lang.Integer getDevicetype() {
        return devicetype;
    }

    public String getDidmd5() {
        return didmd5;
    }

    public String getDidsha1() {
        return didsha1;
    }

    public int getDnt() {
        return dnt;
    }

    public String getDpidmd5() {
        return dpidmd5;
    }

    public String getDpidsha1() {
        return dpidsha1;
    }

    public Map getExt() {
        return ext;
    }

    public String getFlashver() {
        return flashver;
    }

    /**
     * Per OpenRTB spec 2.2:
     * 
     * <blockquote> Depending on the parent object, this object describes the current geographic
     * location of the device (e.g., based on IP address or GPS), or it may describe the home geo of
     * the user (e.g., based on registration data).</blockquote>
     * <p>
     * Therefore, this describes the current geographic location of the device.
     * </p>
     * 
     * @see <a href="http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf">http://www
     *      .iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf</a>
     * 
     * @see Geo
     * 
     * @see User#getGeo()
     * 
     * @see Lot49Ext#getGeo()
     * 
     * @return the geo
     */
    public Geo getGeo() {
        return geo;
    }

    public String getIp() {
        return ip;
    }

    public String getIpv6() {
        return ipv6;
    }

    public java.lang.Integer getJs() {
        return Js;
    }

    public String getLanguage() {
        return language;
    }

    public String getMacmd5() {
        return macmd5;
    }

    public String getMacsha1() {
        return macsha1;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getOs() {
        return os;
    }

    public String getOsv() {
        return Osv;
    }

    public String getUa() {
        return ua;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public void setConnectiontype(java.lang.Integer connectiontype) {
        this.connectiontype = connectiontype;
    }

    public void setDeviceType(java.lang.Integer deviceType) {
        this.devicetype = deviceType;
    }

    public void setDidmd5(String didmd5) {
        this.didmd5 = didmd5;
    }

    public void setDidsha1(String didsha1) {
        this.didsha1 = didsha1;
    }

    public void setDnt(int dnt) {
        this.dnt = dnt;
    }

    public void setDpidmd5(String dpidmd5) {
        this.dpidmd5 = dpidmd5;
    }

    public void setDpidsha1(String dpidsha1) {
        this.dpidsha1 = dpidsha1;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

    public void setFlashver(String flashver) {
        this.flashver = flashver;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }

    public void setJs(java.lang.Integer js) {
        Js = js;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setMacmd5(String macmd5) {
        this.macmd5 = macmd5;
    }

    public void setMacsha1(String macsha1) {
        this.macsha1 = macsha1;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setOsv(String osv) {
        Osv = osv;
    }

    public void setUa(String ua) {
        this.ua = ua;
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

    public float getPxratio() {
        return pxratio;
    }

    public void setPxratio(float pxratio) {
        this.pxratio = pxratio;
    }
}
