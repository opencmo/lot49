package com.enremmeta.rtb.spi.providers.maxmind;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtGeo;
import com.enremmeta.util.BidderCalendar;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.ConnectionTypeResponse;
import com.maxmind.geoip2.model.DomainResponse;
import com.maxmind.geoip2.model.IspResponse;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Subdivision;

/**
 * Facade for <a href="http://www.maxmind.com/">MaxMind</a> geo DB
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class MaxMindFacade {

    private DatabaseReader makeReader(String type, String path) throws IOException {
        if (path == null) {
            LogUtils.error("No file configured for " + type);
            return null;
        }
        File f = new File(path);
        if (!f.exists()) {
            LogUtils.error("File for " + type + " is missing: " + f);
            return null;
        }
        DatabaseReader reader = new DatabaseReader.Builder(f).fileMode(Reader.FileMode.MEMORY)
                        .withCache(new CHMCache()).build();
        return reader;
    }

    public DatabaseReader getCityReader() {
        return cityReader;
    }

    public MaxMindFacade(MaxMindConfig config) throws IOException {
        super();
        cityReader = makeReader("city", config.getCity());
        connectionTypeReader = makeReader("connectionType", config.getConnectionType());
        domainReader = makeReader("domain", config.getDomain());
        ispReader = makeReader("isp", config.getIsp());
    }

    private final DatabaseReader cityReader;
    private final DatabaseReader connectionTypeReader;
    private final DatabaseReader domainReader;
    private final DatabaseReader ispReader;

    public Geo getGeo(String ip) {

        if (ip == null) {
            return null;
        }
        ip = ip.trim();
        if (ip.length() == 0) {
            return null;
        }
        if (ip.equals("127.0.0.1")) {
            return null;
        }
        final Geo geo = new Geo();

        geo.setExt(new HashMap<String, Object>());
        Lot49ExtGeo geoExt = new Lot49ExtGeo();

        geo.getExt().put(Lot49ExtGeo.GEO_EXT_KEY, geoExt);

        InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            // Ignore this...
            final String msg = e.getMessage();
            if (msg.endsWith(" is not in the database.")) {
                LogUtils.trace("Error looking up " + ip + ": " + msg);
            } else {
                LogUtils.trace("Error looking up " + ip + ": " + msg);
            }
            return geo;
        }

        if (ipAddress == null) {
            LogUtils.trace("Error looking up " + ip);
            return geo;
        }

        try {
            // 1. City
            if (cityReader != null) {
                final CityResponse cityResp = cityReader.city(ipAddress);
                if (cityResp != null) {
                    geo.setCity((cityResp.getCity() == null || cityResp.getCity().getName() == null)
                                    ? "" : cityResp.getCity().getName().toLowerCase());
                    final List<Subdivision> subdivisions = cityResp.getSubdivisions();
                    if (subdivisions != null && subdivisions.size() > 0) {
                        final Subdivision sub = subdivisions.get(0);
                        if (sub != null) {
                            final String subIso = sub.getIsoCode();
                            if (subIso != null) {
                                geo.setRegion(subIso);
                                geoExt.addRegion(subIso);
                                geoExt.addRegion(sub.getName());
                            }
                        }
                        if (subdivisions.size() > 1) {
                            for (int i = 1; i < subdivisions.size(); i++) {
                                final Subdivision sub2 = subdivisions.get(i);
                                if (sub2 != null) {
                                    final String sub2Iso = sub2.getIsoCode();
                                    if (sub2Iso != null) {
                                        geoExt.addRegion(sub2Iso.toLowerCase());
                                        geoExt.addRegion(sub.getName());
                                    }
                                }

                            }
                        }
                    }

                    if (cityResp.getPostal() != null) {
                        geo.setZip(cityResp.getPostal().getCode());
                    }

                    if (cityResp.getCountry() != null
                                    && cityResp.getCountry().getIsoCode() != null) {
                        geo.setCountry(cityResp.getCountry().getIsoCode().toLowerCase());
                    }

                    final Location loc = cityResp.getLocation();
                    if (loc != null) {
                        if (loc.getLatitude() != null && loc.getLongitude() != null) {
                            geo.setLat(loc.getLatitude().floatValue());
                            geo.setLon(loc.getLongitude().floatValue());
                        }
                        if (loc.getMetroCode() != null) {
                            geo.setMetro(loc.getMetroCode().toString());
                        }
                        final String tz = loc.getTimeZone();
                        if (tz != null) {
                            geoExt.setTz(tz);
                            // Since we now have TZ, we can play...
                            final long bidderTs = geoExt.getBidderTs();
                            final DateTime bidderDt =
                                            BidderCalendar.getInstance().toDateTime(bidderTs);
                            geoExt.setBidderHour(bidderDt.getHourOfDay());
                            geoExt.setBidderDow(bidderDt.getDayOfWeek());
                            final DateTimeZone jodaTz = DateTimeZone.forID(tz);
                            final long userTs = jodaTz.convertUTCToLocal(bidderTs);
                            geoExt.setUserTs(userTs);
                            final DateTime userDt =
                                            new DateTime(bidderTs + jodaTz.getOffset(bidderTs));
                            geoExt.setUserHour(userDt.getHourOfDay());
                            geoExt.setUserDow(userDt.getDayOfWeek());

                            final String userTimestampString =
                                            userDt.toString(DateTimeFormat.shortDateTime());
                            geoExt.setUserTsStr(userTimestampString);
                        }
                    }
                }
            }

            if (domainReader != null) {
                final DomainResponse domainResp = domainReader.domain(ipAddress);
                if (domainResp != null) {
                    geoExt.setDom(domainResp.getDomain());
                }
            }

            if (ispReader != null) {
                final IspResponse ispResp = ispReader.isp(ipAddress);
                if (ispResp != null) {
                    geoExt.setIsp(ispResp.getIsp());
                    geoExt.setOrg(ispResp.getOrganization());
                }
            }

            if (connectionTypeReader != null) {
                final ConnectionTypeResponse ctResp =
                                connectionTypeReader.connectionType(ipAddress);
                if (ctResp != null && ctResp.getConnectionType() != null) {
                    geoExt.setConn(ctResp.getConnectionType().name());
                }
            }
        } catch (Exception e) {
            final String msg = e.getMessage();
            if (msg.endsWith(" is not in the database.")) {
                LogUtils.trace("Error looking up " + ip + ": " + msg);
            } else {
                LogUtils.trace("Error looking up " + ip + ": " + msg);
            }
        }

        return geo;
    }

}
