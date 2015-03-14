package com.enremmeta.rtb.api;

import java.util.Set;

import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtGeo;
import com.enremmeta.util.Jsonable;

/**
 * This class is to contain information needed to match against {@link Geo}. We do not use
 * {@link Geo} itself for these purposes because the logic is not as simple as matching everything.
 * 
 * @see Geo
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class TargetingGeo implements Jsonable {

    public TargetingGeo() {
        // TODO Auto-generated constructor stub
    }

    private String city;
    private String metro;
    private String region;
    private String country;
    private String zip;

    public TargetingGeo(String city, String metro, String region, String country, String zip) {
        super();
        this.city = normalize(city);
        this.metro = normalize(metro);
        this.region = normalize(region);
        this.country = normalize(country);
        this.zip = normalize(zip);
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getMetro() {
        return metro;
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

    public String getCountry() {
        return country;
    }

    public String getZip() {
        return zip;
    }

    public boolean matches(Geo geo) {
        if (geo == null) {
            return false;
        }
        if (this.city != null && !city.equalsIgnoreCase(geo.getCity())) {
            return false;
        }
        if (this.region != null && !region.equalsIgnoreCase(geo.getRegion())) {
            // Let's check other regions though...
            Lot49ExtGeo geoExt = (Lot49ExtGeo) geo.getExt().get(Lot49ExtGeo.GEO_EXT_KEY);
            if (geoExt == null) {
                return false;
            }
            Set<String> regions = geoExt.getRegions();
            if (regions == null) {
                return false;
            }
            if (!regions.contains(region)) {
                return false;
            }
        }
        if (this.metro != null && !metro.equalsIgnoreCase(geo.getMetro())) {
            return false;
        }
        if (this.country != null && !country.equalsIgnoreCase(geo.getCountry())) {
            return false;
        }
        if (this.zip != null && !zip.equalsIgnoreCase(geo.getZip())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{Geo: ");
        if (city != null) {
            s.append("City: ").append(city).append(", ");
        }
        if (metro != null) {
            s.append("Metro: ").append(metro).append(", ");
        }
        if (zip != null) {
            s.append("Zip/Postal code: ").append(zip).append(", ");
        }
        if (region != null) {
            s.append("Region: ").append(region).append(", ");
        }
        if (country != null) {
            s.append("Country: ").append(country).append(", ");
        }
        s.append("}");
        return s.toString();
    }
}
