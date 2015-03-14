package com.enremmeta.rtb.api.proto.openrtb;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtGeo;

/**
 * * Per OpenRTB spec 2.2: <blockquote> Depending on the parent object, this object describes the
 * current geographic location of the device (e.g., based on IP address or GPS), or it may describe
 * the home geo of the user (e.g., based on registration data).</blockquote>
 * 
 * @see <a href= "http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf"> OpenRTB 2.2
 *      specification</a>
 * 
 * @see Geo
 * 
 * @see User#getGeo()
 * 
 * @see Device#getGeo()
 * 
 * @see Lot49Ext#getGeo()
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class Geo implements Serializable, RtbBean {

    private String utcoffset;

    public String getUtcoffset() {
        return utcoffset;
    }

    public void setUtcoffset(String utcoffset) {
        this.utcoffset = utcoffset;
    }

    private static final long serialVersionUID = 1L;

    public Geo() {
        // TODO Auto-generated constructor stub
    }

    public Geo(String city, String metro, String zip, String region, String country) {
        super();
        this.country = country;
        this.region = region;
        this.metro = metro;
        this.city = city;
        this.zip = zip;
    }

    private Float lat;
    private Float lon;
    private String country;
    private String region;
    private String regionfips104;
    private String metro;
    private String city;
    private String zip;
    private Integer type;
    private String regionFullName;

    public String getRegionFullName() {
        return regionFullName;
    }

    public void setRegionFullName(String regionFullName) {
        this.regionFullName = normalize(regionFullName);
    }

    private Map ext = new HashMap();

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{Geo: ");
        if (city != null && city.length() > 0) {
            s.append("cit: ").append(city).append(", ");
        }
        if (metro != null && metro.length() > 0) {
            s.append("msa: ").append(metro).append(", ");
        }
        if (zip != null && zip.length() > 0) {
            s.append("zip: ").append(zip).append(", ");
        }
        if (region != null && region.length() > 0) {
            s.append("reg: ").append(region).append(", ");
        }
        if (country != null && country.length() > 0) {
            s.append("cnt: ").append(country).append(", ");
        }
        if (lon != null) {
            s.append("crd: (").append(lat).append(", ").append(lon).append("),");
        }
        if (ext != null && ext.size() > 0) {
            s.append("Custom:");
            for (Object key : ext.keySet()) {
                s.append(key).append(": ").append(ext.get(key)).append(", ");
            }
        }
        s.append("}");
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        Geo g = null;
        try {
            g = (Geo) o;
        } catch (ClassCastException e) {
            return false;
        }
        return g.toString().equalsIgnoreCase(toString());
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLon() {
        return lon;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getRegionfips104() {
        return regionfips104;
    }

    public void setRegionfips104(String regionfips104) {
        this.regionfips104 = regionfips104;
    }

    public String getMetro() {
        return metro;
    }

    public String getCity() {
        return city;
    }

    public String getZip() {
        return zip;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * This will probably have at least {@link Lot49ExtGeo} under {@link Lot49ExtGeo#GEO_EXT_KEY}
     * key if this is within {@link Lot49Ext#getGeo()}.
     * 
     * @return the map
     */
    public Map getExt() {
        return ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

    public void setMetro(String metro) {
        this.metro = normalize(metro);
    }

    public void setCity(String city) {
        this.city = normalize(city);
    }

    public void setRegion(String region) {
        this.region = normalize(region);
    }

    public void setCountry(String country) {
        this.country = normalize(country);
    }

    public void setZip(String zip) {
        this.zip = normalize(zip);
    }
}
