package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49CustomData;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.util.Utils;

/**
 * OpenRTB User object.
 * 
 * @see <a href= "http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-3.pdf"> OpenRTB
 *      2.3 (section 3.2.13)</a>
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class User implements RtbBean {

    public User() {
        // TODO Auto-generated constructor stub
    }

    private String id;

    /**
     * @see #getBuyeruid()
     */
    private String buyeruid;
    private Integer yob;
    private String gender;
    private String keywords;
    private String customdata;

    /**
     * @see #getGeo()
     */
    private Geo geo;
    private List<Data> data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Demand-side user ID. For this to work, an out-of-band mechanism for user ID matching should
     * be set up with the exchange in question. Because different exchanges may actually send
     * different things in this field, and because Lot49's main logic assumes that actually
     * demand-side ID is being returned here, see
     * <a href="mailto:grisha@alum.mit.edu?Subject=TBD">grisha@alum.mit.edu</a>".
     * 
     * @see Utils#cookieToLogModUid(String)
     * 
     * @see Utils#logToCookieModUid(String)
     * 
     * @see <a href="https://developers.google.com/ad-exchange/rtb/cookie-guide">AdX Cookie
     *      Matching</a>
     * 
     * @return the buyeruid
     */
    public String getBuyeruid() {
        return buyeruid;
    }

    public void setBuyeruid(String buyeruid) {
        this.buyeruid = buyeruid;
    }

    /**
     * Year of birth. Russian speakers can go chill out.
     * 
     * @return the year of birth
     */
    public Integer getYob() {
        return yob;
    }

    public void setYob(Integer yob) {
        this.yob = yob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Depending on the exchange, this may be a JSON document of the {@link Lot49CustomData}
     * structure.
     * 
     * @see Lot49CustomData
     * 
     * @return the custom data
     */
    public String getCustomdata() {
        return customdata;
    }

    public void setCustomdata(String customdata) {
        this.customdata = customdata;
    }

    /**
     * Per OpenRTB spec 2.2: <blockquote> Depending on the parent object, this object describes the
     * current geographic location of the device (e.g., based on IP address or GPS), or it may
     * describe the home geo of the user (e.g., based on registration data).</blockquote>
     * <p>
     * Therefore, this describes the home geo of the user.
     * </p>
     * 
     * @see <a href= "http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf" >OpenRTB
     *      2.2 specification</a>
     * 
     * @see Geo
     * 
     * @see Device#getGeo()
     * 
     * @see Lot49Ext#getGeo()
     * 
     * @return the geo location info
     */
    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public Map getExt() {
        return ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

    private Map ext;

}
