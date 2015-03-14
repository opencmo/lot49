package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;

/**
 * OpenRTB Deal object.
 * 
 * @see <a href="http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-3.pdf">OpenRTB
 *      2.3 (section 3.2.18)</a>
 * 
 * @see PMP
 *
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class Deal implements RtbBean {

    /**
     * 
     */
    private static final long serialVersionUID = 4587845623732105163L;

    public Deal() {
        // TODO Auto-generated constructor stub
    }

    private String bidfloorcur;

    private String id;
    private float bidfloor;
    private int at;
    private List<String> wseat;
    private Map ext;

    public String getBidfloorcur() {
        return bidfloorcur;
    }

    public void setBidfloorcur(String bidfloorcur) {
        this.bidfloorcur = bidfloorcur;
    }

    public List<String> getWseat() {
        return wseat;
    }

    public void setWseat(List<String> wseat) {
        this.wseat = wseat;
    }

    public List<String> getWadomain() {
        return wadomain;
    }

    public void setWadomain(List<String> wadomain) {
        this.wadomain = wadomain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getBidfloor() {
        return bidfloor;
    }

    public void setBidfloor(float bidfloor) {
        this.bidfloor = bidfloor;
    }

    public int getAt() {
        return at;
    }

    public void setAt(int at) {
        this.at = at;
    }

    private List<String> wadomain;

    public Map getExt() {
        return ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

}
