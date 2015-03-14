package com.enremmeta.rtb.spi.providers.maxmind;

import org.junit.BeforeClass;
import org.junit.Test;

import com.enremmeta.rtb.api.proto.openrtb.Geo;

public class MaxMindTest {
    private static MaxMindFacade mmf;

    @BeforeClass
    public static void setUp() throws Throwable {
        MaxMindConfig config = new MaxMindConfig();
        config.setCity("/opt/lot49/data/geo/maxmind/GeoIP2-City.mmdb");
        config.setConnectionType("/opt/lot49/data/geo/maxmind/GeoIP2-Connection-Type.mmdb");
        config.setDomain("/opt/lot49/data/geo/maxmind/GeoIP2-Domain.mmdb");
        config.setIsp("/opt/lot49/data/geo/maxmind/GeoIP2-ISP.mmdb");
        mmf = new MaxMindFacade(config);

    }

    @Test
    public void testIpv4() {
        String ip = "18.18.18.18";
        Geo geo = mmf.getGeo(ip);
        System.out.println(ip + ": " + geo);

    }

    @Test
    public void testIpv6() {
        String ip = "2001:4860:4860::8888";
        Geo geo = mmf.getGeo(ip);
        System.out.println(ip + ": " + geo);

    }

}
