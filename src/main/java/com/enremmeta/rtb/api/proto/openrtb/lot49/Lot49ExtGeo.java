package com.enremmeta.rtb.api.proto.openrtb.lot49;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTimeConstants;

import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.util.BidderCalendar;

/**
 * This will be under key {@link #GEO_EXT_KEY} in {@link Geo#getExt()}.
 * 
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class Lot49ExtGeo implements RtbBean {

    public static final String GEO_EXT_KEY = "lot49extgeo";

    private long bidderTs = BidderCalendar.getInstance().currentTimeMillis();

    private Long userTs;

    private int bidderDow;

    private Integer userDow;

    private Integer userHour;
    private String userTsStr;

    public String getUserTsStr() {
        return userTsStr;
    }

    public void setUserTsStr(String userTimestampString) {
        this.userTsStr = userTimestampString;
    }

    private int bidderHour;

    public long getBidderTs() {
        return bidderTs;
    }

    public void setBidderTs(long bidderTimestamp) {
        this.bidderTs = bidderTimestamp;
    }

    public Long getUserTs() {
        return userTs;
    }

    public void setUserTs(long userTimestamp) {
        this.userTs = userTimestamp;
    }

    /**
     * Constant per {@link DateTimeConstants}.
     */
    public int getBidderDow() {
        return bidderDow;
    }

    public void setBidderDow(int bidderDayOfWeek) {
        this.bidderDow = bidderDayOfWeek;
    }

    /**
     * Constant per {@link DateTimeConstants}.
     */
    public Integer getUserDow() {
        return userDow;
    }

    public void setUserDow(int userDayOfWeek) {
        this.userDow = userDayOfWeek;
    }

    public Integer getUserHour() {
        return userHour;
    }

    public void setUserHour(int userHour) {
        this.userHour = userHour;
    }

    public int getBidderHour() {
        return bidderHour;
    }

    public void setBidderHour(int bidderHour) {
        this.bidderHour = bidderHour;
    }

    public Lot49ExtGeo() {
        // TODO Auto-generated constructor stub
    }

    private String tz;

    public String getTz() {
        return tz;
    }

    public void setTz(String timezone) {
        this.tz = timezone;
    }

    private String dom;

    private String isp;

    public String getDom() {
        return dom;
    }

    public void setDom(String domain) {
        this.dom = domain;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    private String org;

    public String getOrg() {
        return org;
    }

    public void setOrg(String organization) {
        this.org = organization;
    }

    private String conn;

    public String getConn() {
        return conn;
    }

    public void setConn(String connectionType) {
        this.conn = connectionType;
    }

    @Override
    public String toString() {
        StringBuilder retval = new StringBuilder();

        for (Field f : getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            Object val = null;
            try {
                val = f.get(this);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                val = e;
            }

            if (val != null && val instanceof Collection && ((Collection) val).size() == 0) {
                val = null;
            }
            if (val != null) {
                if (retval.length() > 0) {
                    retval.append("; ");
                }
                String fName = f.getName();
                retval.append(fName).append(": ").append(val);
            }
        }
        return retval.toString();
    }

    private Set<String> regions = new HashSet<String>();

    public Set<String> getRegions() {
        return regions;
    }

    public void addRegion(String region) {
        String r = normalize(region);
        if (r != null) {
            regions.add(r);
        }
    }

    public void setRegions(Set<String> regions) {
        this.regions = regions;
    }
}
