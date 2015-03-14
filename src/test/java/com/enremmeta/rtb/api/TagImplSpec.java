package com.enremmeta.rtb.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class TagImplSpec {
    
    private ServiceRunner serviceRunnerSimpleMock;
    private OrchestratorConfig orchConfig;
    private TagImpl ti;
    OpenRtbRequest req;

    @Before
    public void setUp() throws Exception {

        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);
        Mockito.when(configMock.getStatsUrl()).thenReturn("http://stats.url");

        orchConfig = new OrchestratorConfig();

        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(new LocalOrchestrator(orchConfig));

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        
        Ad ad = new SharedSetUp.Ad_1001001_fake();

        ti = new SharedSetUp.Tag_2002002_tagMarker_1001001_fake(ad);
        
        req = new OpenRtbRequest();
        req.getLot49Ext().setAdapter(ExchangeAdapterFactory.getExchangeAdapter(
                        ExchangeAdapterFactory.getAllExchangeAdapterNames().get(1)));
        
    }

    @Test
    public void negativeFlow_canBid_sslRequired() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "sslCapable", false);
        
        req.getLot49Ext().setSsl(true);
        
        assertEquals("SSL required in the request - I'm sorry, I can't, don't hate me.", ti.canBid(req, new Impression()));

    }
    
    @Test
    public void negativeFlow_canBid_ExcludedMarkups() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "markupType", MarkupType.OWN_FLASH);
        
        req.getLot49Ext().setExcludedMarkups(new HashSet<MarkupType>(){{
          add(MarkupType.OWN_FLASH);  
        }});
        
        assertEquals("Request disallows type: OWN_FLASH", ti.canBid(req, new Impression()));

    }
    
    @Test
    public void negativeFlow_canBid_NotVideo() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        
        assertEquals("Not video", ti.canBid(req, new Impression()));

    }
    
    @Test
    public void negativeFlow_canBid_WrongLinearity() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        
        assertEquals("Linearity: 3", ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(3);  
          }});  
        }}));

    }
    
    @Test
    public void negativeFlow_canBid_WrongDimension() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return false;
            }
            
            @Override
            public String toString() {
                return "TEST_DIM";
            }
        });        
        
        assertEquals("Dimensions: 0x0 (not TEST_DIM)", ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(2);  
          }});  
        }}));

    }
    
    @Test
    public void negativeFlow_canBid_WrongMimes() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return true;
            }
            
        });
        Whitebox.setInternalState(ti, "mime", "TEST_MIME");        
        
        assertEquals("Mime: TEST_MIME not in [WRONG_MIME]", ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(2);
            setMimes(new LinkedList<String>(){{
                add("WRONG_MIME");
            }});
          }});  
        }}));

    }
    
    @Test
    public void negativeFlow_canBid_WrongMaxduration() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return true;
            }
            
        });
        Whitebox.setInternalState(ti, "mime", "TEST_MIME");
        Whitebox.setInternalState(ti, "duration", 1000);     
        
        assertEquals("Max duration: 100", ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(2);
            setMimes(new LinkedList<String>(){{
                add("TEST_MIME");
            }});
            setMaxduration(100);
          }});  
        }}));

    }

    @Test
    public void negativeFlow_canBid_WrongMinduration() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return true;
            }
            
        });
        Whitebox.setInternalState(ti, "mime", "TEST_MIME");
        Whitebox.setInternalState(ti, "duration", 10);     
        
        assertEquals("Min duration: 100", ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(2);
            setMimes(new LinkedList<String>(){{
                add("TEST_MIME");
            }});
            setMaxduration(100);
            setMinduration(100);
          }});  
        }}));

    }
    
    @Test
    public void negativeFlow_canBid_WrongProtocol() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return true;
            }
            
        });
        Whitebox.setInternalState(ti, "mime", "TEST_MIME");
        Whitebox.setInternalState(ti, "duration", 10);
        Whitebox.setInternalState(ti, "protocol", 2);
        
        assertEquals("Protocol: 1(NOT 2)", ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(2);
            setMimes(new LinkedList<String>(){{
                add("TEST_MIME");
            }});
            setMaxduration(100);
            setMinduration(1);
            setProtocol(1);
          }});  
        }}));

    }
    
    @Test
    public void negativeFlow_canBid_WrongProtocols() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return true;
            }
            
        });
        Whitebox.setInternalState(ti, "mime", "TEST_MIME");
        Whitebox.setInternalState(ti, "duration", 10);
        Whitebox.setInternalState(ti, "protocol", 2);
        
        assertEquals("Protocols: 2 not in [1]", ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(2);
            setMimes(new LinkedList<String>(){{
                add("TEST_MIME");
            }});
            setMaxduration(100);
            setMinduration(1);
            setProtocol(2);
            setProtocols(new LinkedList<Integer>(){{
                add(1);
            }});
          }});  
        }}));

    }
    
    @Test
    public void negativeFlow_canBid_WrongApis() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return true;
            }
            
        });
        Whitebox.setInternalState(ti, "mime", "TEST_MIME");
        Whitebox.setInternalState(ti, "duration", 10);
        Whitebox.setInternalState(ti, "protocol", 2);
        Whitebox.setInternalState(ti, "api", 11);
        
        assertEquals("API: 11 not in [22]", ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(2);
            setMimes(new LinkedList<String>(){{
                add("TEST_MIME");
            }});
            setMaxduration(100);
            setMinduration(1);
            setProtocol(2);
            setProtocols(new LinkedList<Integer>(){{
                add(2);
            }});
            setApi(new LinkedList<Integer>(){{
                add(22);
            }});
          }});  
        }}));

    }
    
    @Test
    public void positiveFlow_canBid_video() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "video", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return true;
            }
            
        });
        Whitebox.setInternalState(ti, "mime", "TEST_MIME");
        Whitebox.setInternalState(ti, "duration", 10);
        Whitebox.setInternalState(ti, "protocol", 2);
        Whitebox.setInternalState(ti, "api", 11);
        
        Whitebox.setInternalState(ti, "api", 22);
        
        Whitebox.setInternalState(ti, "passedCount", 0);
        
        ti.canBid(req, new Impression(){{
          setVideo(new Video(){{
            setLinearity(2);
            setMimes(new LinkedList<String>(){{
                add("TEST_MIME");
            }});
            setMaxduration(100);
            setMinduration(1);
            setProtocol(2);
            setProtocols(new LinkedList<Integer>(){{
                add(2);
            }});
            setApi(new LinkedList<Integer>(){{
                add(22);
            }});
          }});  
        }});

        assertEquals(new Integer(1), Whitebox.getInternalState(ti, "passedCount"));
        
    }
    
    @Test
    public void negativeFlow_canBid_BannerWrongDimension() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "banner", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return false;
            }
            
            @Override
            public String toString() {
                return "TEST_DIM";
            }
        });        
        
        assertEquals("Dimensions: 0x0 (not TEST_DIM)", ti.canBid(req, new Impression(){{
            setBanner(new Banner());  
        }}));

    }
    
    @Test
    public void negativeFlow_canBid_BannerWrongMimes() throws Lot49Exception {
        
        Whitebox.setInternalState(ti, "banner", true);
        Whitebox.setInternalState(ti, "dim", new DimensionImpl() {
            
            @Override
            public boolean check(int width, int height) {
                return true;
            }

        });
        
        Whitebox.setInternalState(ti, "mime", "TEST_MIME");
        
        Banner banner = new Banner();
        banner.setMimes(new LinkedList<String>(){{
            add("WRONG_MIME");
        }});
        
        assertEquals("Mime: TEST_MIME not in [WRONG_MIME]", ti.canBid(req, new Impression(){{
            setBanner(banner);  
        }}));

    }
    
    @Test
    public void negativeFlow_canBid_unknown() throws Lot49Exception {
                
        assertEquals("Unknown", ti.canBid(req, new Impression()));

    }
    
    @Test
    public void negativeFlow_getNUrl_nullBid() throws Lot49Exception {
              
        try{
            ti.getNUrl(req, null, "nurlId");
            fail("Expected to throw exception");
        }
        catch(IllegalStateException e){
            assertEquals("Cannot obtain NUrl if bid has not been constructed.", e.getMessage());
        }

    }
    
    @Test
    public void posiveFlow_getNUrl() throws Lot49Exception {
              
        Whitebox.setInternalState(ti, "markupType", MarkupType.VAST_PLAIN_FLASH_ONLY);
        req.setId("TEST_REQ_ID");
        
        assertTrue(ti.getNUrl(req, new Bid(), "nurlId")
                        .contains("http://stats.url/stats/nurl?wp=${market_ratio}&xch=adaptv&crid=2002002&cid=1001001&iid=null&bid=null&bp=0&cid=1001001&crid=2002002"));
        
    }
    
    @Test
    public void posiveFlow_getNUrl_MacrosInNurl() throws Lot49Exception {
              
        Whitebox.setInternalState(ti, "markupType", MarkupType.VAST_PLAIN_FLASH_ONLY);
        req.setId("TEST_REQ_ID");
        
        ExchangeAdapter eaMock = Mockito.mock(ExchangeAdapter.class);
        Mockito.when(eaMock.isMacrosInNurl()).thenReturn(true);
        req.getLot49Ext().setAdapter(eaMock);
        
        assertTrue(ti.getNUrl(req, new Bid(), "nurlId")
                        .contains("http://stats.url/stats/nurl?wp=null&xch=null&crid=2002002&cid=1001001&iid=null&bid=null&bp=0&cid=1001001&crid=2002002"));
        
    }
    
    
}
