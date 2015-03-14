package com.enremmeta.rtb.api.proto.openrtb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a Bid in OpenRTB paradigm.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *
 */
@JsonInclude(Include.NON_NULL)
public class Bid implements RtbBean {

    private String dealid;

    public String getDealid() {
        return dealid;
    }

    public void setDealid(String dealid) {
        this.dealid = dealid;
    }

    public static final Bid PENDING_BID = new Bid();

    public Bid() {
        // TODO Auto-generated constructor stub
    }

    private String id;
    private String impid;

    // Don't ever fucking touch this line.
    private Float price = (float) 0.;

    private String adid;
    private String nurl;
    private String adm;
    public List<String> adomain = new ArrayList<String>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImpid() {
        return impid;
    }

    public void setImpid(String impid) {
        this.impid = impid;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getAdid() {
        return adid;
    }

    public void setAdid(String adid) {
        this.adid = adid;
    }

    public String getNurl() {
        return nurl;
    }

    public void setNurl(String nurl) {
        this.nurl = nurl;
    }

    public String getAdm() {
        return adm;
    }

    public void setAdm(String adm) {
        this.adm = adm;
    }

    /**
     * Initialized to empty ArrayList.
     */
    public List<String> getAdomain() {
        return adomain;
    }

    public void setAdomain(List<String> adomain) {
        this.adomain = adomain;
    }

    public String getIurl() {
        return iurl;
    }

    public void setIurl(String iurl) {
        this.iurl = iurl;
    }

    /**
     * 
     */
    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCrid() {
        return crid;
    }

    public void setCrid(String crid) {
        this.crid = crid;
    }

    public List<Integer> getAttr() {
        return attr;
    }

    public void setAttr(List<Integer> attr) {
        this.attr = attr;
    }

    public RtbBean getExt() {
        return ext;
    }

    public void setExt(RtbBean ext) {
        this.ext = ext;
    }

    private String iurl;
    private String cid;
    private String crid;
    public List<Integer> attr;
    private RtbBean ext;

    @JsonIgnore
    private Map hiddenAttributes = new HashMap();

    @Override
    public String toString() {
        return pp(false);
    }

    /**
     * Little pretty print
     */
    public String pp(boolean pp) {
        String retval = "{ \"bid\" : {" + (pp ? "\n" : "");
        retval += "\"id: \"" + this.id + "\", " + (pp ? "\n" : "");
        retval += "\"cid\": \"" + this.cid + "\", " + (pp ? "\n" : "");
        retval += "\"crid\" : \"" + this.crid + "\", " + (pp ? "\n" : "");
        retval += "}" + (pp ? "\n" : "");
        return retval;
    }

    public Map getHiddenAttributes() {
        return hiddenAttributes;
    }

    public void setHiddenAttributes(Map hiddenAttributes) {
        this.hiddenAttributes = hiddenAttributes;
    }

}
