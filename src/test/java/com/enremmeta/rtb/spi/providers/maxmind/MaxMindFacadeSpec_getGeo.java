package com.enremmeta.rtb.spi.providers.maxmind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.ConnectionTypeResponse;
import com.maxmind.geoip2.model.ConnectionTypeResponse.ConnectionType;
import com.maxmind.geoip2.model.DomainResponse;
import com.maxmind.geoip2.model.IspResponse;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Subdivision;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MaxMindFacade.class, CityResponse.class, Location.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class MaxMindFacadeSpec_getGeo {

    @Test
    public void negativeFlow_wrongIpFormat() throws IOException {
        MaxMindFacade mmf = new MaxMindFacade(new MaxMindConfig());
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());
        
        mmf.getGeo("wrong_ip");
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("Error looking up wrong_ip: wrong_ip: unknown error");
    }
    
    @Test
    public void negativeFlow_localhost() throws IOException {
        MaxMindFacade mmf = new MaxMindFacade(new MaxMindConfig());
        assertNull(mmf.getGeo("127.0.0.1"));
    }
    
    @Test
    public void positiveFlow_maxMinfConfigIsEmpty() throws IOException {
        MaxMindFacade mmf = new MaxMindFacade(new MaxMindConfig());
        assertEquals(Geo.class, mmf.getGeo("192.168.1.111").getClass());
    }
    
    @Test
    public void positiveFlow_maxMinfConfigNotEmpty() throws IOException, GeoIp2Exception {
        
        DatabaseReader cityReaderMock = Mockito.mock(DatabaseReader.class);
        CityResponse crMock = PowerMockito.mock(CityResponse.class);
        PowerMockito.when(crMock.getSubdivisions()).thenReturn(new LinkedList<Subdivision>(){{
            add(new Subdivision()); add(new Subdivision());
        }});
        
        Location locationMock = PowerMockito.mock(Location.class);
        PowerMockito.when(locationMock.getTimeZone()).thenReturn("EET");
        PowerMockito.when(crMock.getLocation()).thenReturn(locationMock);
        Mockito.when(cityReaderMock.city(Mockito.any())).thenReturn(crMock);
        
        MaxMindFacade mmfMock = Mockito.mock(MaxMindFacade.class);
        Mockito.when(mmfMock.getGeo(Mockito.any())).thenCallRealMethod();
        Whitebox.setInternalState(mmfMock, "cityReader", cityReaderMock);

        assertTrue(mmfMock.getGeo("192.168.1.111").toString()
                        .contains("Geo: msa: 0, crd: (0.0, 0.0)"));
        
        assertTrue(mmfMock.getGeo("192.168.1.111").toString()
                        .contains("tz: EET"));
    }
    
    @Test
    public void positiveFlow_maxMinfConfigNotEmpty_domainReaderNotNull() throws IOException, GeoIp2Exception {
                
        MaxMindFacade mmfMock = Mockito.mock(MaxMindFacade.class);
        Mockito.when(mmfMock.getGeo(Mockito.any())).thenCallRealMethod();
        
        DatabaseReader domainReaderMock = Mockito.mock(DatabaseReader.class);
        DomainResponse drMock = PowerMockito.mock(DomainResponse.class);
        PowerMockito.when(domainReaderMock.domain(Mockito.any()))
            .thenReturn(new DomainResponse("test_domain.com", "192.168.1.111"));
        Whitebox.setInternalState(mmfMock, "domainReader", domainReaderMock);

        
        assertTrue(mmfMock.getGeo("192.168.1.111").toString()
                        .contains("dom: test_domain.com"));
    }
    
    @Test
    public void positiveFlow_maxMinfConfigNotEmpty_ispReaderNotNull() throws IOException, GeoIp2Exception {
                
        MaxMindFacade mmfMock = Mockito.mock(MaxMindFacade.class);
        Mockito.when(mmfMock.getGeo(Mockito.any())).thenCallRealMethod();
        
        DatabaseReader ispReaderMock = Mockito.mock(DatabaseReader.class);
        IspResponse isprMock = PowerMockito.mock(IspResponse.class);
        PowerMockito.when(ispReaderMock.isp(Mockito.any()))
            .thenReturn(new IspResponse(101011, "autonomous_system_organization", "ip_address",
                            "isp_test", "organization_test"));
        Whitebox.setInternalState(mmfMock, "ispReader", ispReaderMock);

        
        assertTrue(mmfMock.getGeo("192.168.1.111").toString()
                        .contains("isp: isp_test; org: organization_test"));
    }
    
    @Test
    public void positiveFlow_maxMinfConfigNotEmpty_connectionTypeReaderNotNull() throws IOException, GeoIp2Exception {
                
        MaxMindFacade mmfMock = Mockito.mock(MaxMindFacade.class);
        Mockito.when(mmfMock.getGeo(Mockito.any())).thenCallRealMethod();
        
        DatabaseReader connectionTypeReaderMock = Mockito.mock(DatabaseReader.class);
        ConnectionTypeResponse ctrMock = PowerMockito.mock(ConnectionTypeResponse.class);
        PowerMockito.when(connectionTypeReaderMock.connectionType(Mockito.any()))
            .thenReturn(new ConnectionTypeResponse(ConnectionType.DIALUP, "ip_address"));
        Whitebox.setInternalState(mmfMock, "connectionTypeReader", connectionTypeReaderMock);

        
        assertTrue(mmfMock.getGeo("192.168.1.111").toString()
                        .contains("conn: DIALUP"));
    }

}
