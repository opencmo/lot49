package com.enremmeta.rtb.proto.openx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.ExchangeTargeting;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.jersey.AuctionsSvc;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class OpenXAdapterSpec {
    
    private ServiceRunner serviceRunnerSimpleMock;
    private AuctionsSvc svc;
    Lot49Config configMock;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = Mockito.mock(Lot49Config.class);

        OpenXConfig openxCfg = new OpenXConfig();
        openxCfg.setEncryptionKey("TEST_encryptionKey");
        openxCfg.setIntegrityKey("TEST_integrityKey");

        Mockito.when(configMock.getExchanges()).thenAnswer(new Answer<ExchangesConfig>() {
            public ExchangesConfig answer(InvocationOnMock invocation) {

                ExchangesConfig exchangesConfigMock = Mockito.mock(ExchangesConfig.class);
                Mockito.when(exchangesConfigMock.getOpenx()).thenReturn(openxCfg);
                return exchangesConfigMock;
            }
        });

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
    }

    @Test
    public void convertResponse_negativeTest_requestImpIsNull() throws Lot49Exception {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        //req.setImp(new LinkedList<Impression>());
        
        OpenRtbRequest result = null;
        
        try{
            result = oxAdapter.convertRequest(req);
        }
        catch(Throwable e){
            assertEquals(NullPointerException.class, e.getClass());
        }
    }
    
    @Test
    public void convertResponse_negativeTest_requestImpIsEmpty() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>());
        
        OpenRtbRequest result = null;
        
        try{
            result = oxAdapter.convertRequest(req);
        }
        catch(Throwable e){
            assertEquals(IndexOutOfBoundsException.class, e.getClass());
        }
    }

    @Test
    public void convertResponse_positiveTest() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        OpenRtbRequest result = null;
        
        result = oxAdapter.convertRequest(req);
        
        assertNotNull(result.getLot49Ext().getAdapter());
    }
    
    @Test
    public void convertResponse_positiveTest_noEmptyUser() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        User user = new User();
        user.setId("TEST_USER_ID");
        
        req.setUser(user);
        
        OpenRtbRequest result = null;
        
        result = oxAdapter.convertRequest(req);
        
        assertNotNull(result.getLot49Ext().getAdapter());
        assertEquals("[OpenX:User.ID=TEST_USER_ID]", result.getLot49Ext().getComments().toString());
    }
    
    @Test
    public void convertResponse_positiveTest_CustomUserData() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        User user = new User();
        user.setId("TEST_USER_ID");
        user.setCustomdata("TEST_CUSTOM_DATA");
        req.setUser(user);
        
        OpenRtbRequest result = null;
        
        result = oxAdapter.convertRequest(req);
        
        assertNotNull(result.getLot49Ext().getAdapter());
        assertEquals("[OpenX:User.ID=TEST_USER_ID, OpenX:User.CustomData=TEST_CUSTOM_DATA]", result.getLot49Ext().getComments().toString());
    }
    
    public Iterable<String[]> testData() {
        return Arrays.asList(new String[][] { 
                 { "{\"udat=\"\"}", "{}" }, 
                 { "{\"udat\"=<>}", "{}" },
                 { "{\"udat\":{\"oi\":\"50528,73463,73462,61210,61050,73328,73265,72737,62470\"}, \"uid\" : \"CtQF91R09xIreF7QED9HAg==\"}",
                     "{oi=50528,73463,73462,61210,61050,73328,73265,72737,62470}" },
                 { "{\"udat\":<{\"oi\":\"50528\"}",
                     "{oi=50528}" },
                 { "{\"udat\"=<{\"oi\":\"50528\"}",
                     "{oi=50528}" },
                 { "{\"udat=\"<{\"oi\":\"50528\"}",
                     "{oi=50528}" },
                 { "{\"udat\":\"{\"oi\":\"50528\"}",
                     "{oi=50528}" },
                 { "{\"udat\"=<{\"oi\":\"\"}Domain=.opendsp.com>}",
                     "{oi=}" },
                 { "{\"udat\":{\"oi\":\"\"}Domain=.opendsp.com\"}",
                     "{}" },
                 { "{\"udat\":,}", "{}" },
                 { "{udat=\"{\"oi\":\"\"}", "{oi=}" },
                 { "{\"udat\":\"{\"oi\":\"50528\"}",
                     "{oi=50528}" }

           });
    }
    
    @Test
    public void convertResponse_positiveTest_CustomUserData_cases() throws Lot49Exception, Throwable {
        
        for(String[] testCase: testData()){

            OpenXAdapter oxAdapter = new OpenXAdapter();

            OpenRtbRequest req = new OpenRtbRequest();
            req.setImp(new LinkedList<Impression>(){{
                add(new Impression());
            }});
            User user = new User();
            user.setId("TEST_USER_ID");
            req.setUser(user);


            user.setCustomdata(testCase[0]);

            OpenRtbRequest result = null;

            result = oxAdapter.convertRequest(req);

            assertEquals(testCase[1], result.getLot49Ext().getLot49CustomData().getUdat().toString());
        
            // TODO: refactoring idea.
            // case "\"udat\"=<{\"oi\":\"\"}Domain=.opendsp.com>" is unreachable due previous cases.
        }
    }
    
    @Test
    public void convertResponse_positiveTest_Banner() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression(){{
                Banner banner = new Banner();
                banner.setExt(new HashMap(){{
                  put("matching_ad_id", new LinkedList<Map>(){{
                      add(new HashMap(){{
                          put("ad_height", 200);
                          put("ad_width", 400);
                          put("campaign_id", 101);
                          put("creative_id", 202);
                          put("placement_id", 303);
                      }});
                  }});
                }});
                setBanner(banner);
            }});
        }});
        
        OpenRtbRequest result = null;
        
        result = oxAdapter.convertRequest(req);

        assertEquals(200, result.getImp().get(0).getBanner().getH());
        assertEquals(400, result.getImp().get(0).getBanner().getW());
    }
    
    @Test
    public void convertResponse_positiveTest_isTest() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});

        req.setExt(new HashMap(){{
            put("is_test", 1);
        }});
        
        OpenRtbRequest result = null;
        
        result = oxAdapter.convertRequest(req);
        
        assertNotNull(result.getLot49Ext().getAdapter());
        assertTrue(result.getLot49Ext().isTest());
    }
    
    @Test
    public void checkExchangeTargeting_negativeTest_noBannerOrVideoInImp() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});

        ExchangeTargeting exchangeTargeting = new ExchangeTargeting();
        exchangeTargeting.setOpenxTargeting(new OpenXTargeting(){{
          setRequiredMatchingAdIds(new LinkedList<MatchingAdId>());  
        }});
        
        String result = null;
        
        result = oxAdapter.checkExchangeTargeting(req, new Impression(), exchangeTargeting);
        
        assertNull(result);
    }
    
    @Test
    public void checkExchangeTargeting_negativeTest_noBannerOrVideoExt() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});

        ExchangeTargeting exchangeTargeting = new ExchangeTargeting();
        exchangeTargeting.setOpenxTargeting(new OpenXTargeting(){{
          setRequiredMatchingAdIds(new LinkedList<MatchingAdId>());  
        }});
        
        Impression imp = new Impression();
        imp.setBanner(new Banner());
        imp.setVideo(new Video());
        
        String result = null;
        
        result = oxAdapter.checkExchangeTargeting(req, imp, exchangeTargeting);
        
        assertNull(result);
    }
    
    @Test
    public void checkExchangeTargeting_negativeTest_bannerExtIsEmpty() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});

        ExchangeTargeting exchangeTargeting = new ExchangeTargeting();
        exchangeTargeting.setOpenxTargeting(new OpenXTargeting(){{
          setRequiredMatchingAdIds(new LinkedList<MatchingAdId>());  
        }});
        
        Impression imp = new Impression();
        
        Banner banner = new Banner();
        banner.setExt(new HashMap());
        
        imp.setBanner(banner);
        imp.setVideo(new Video());
        
        String result = null;
        
        result = oxAdapter.checkExchangeTargeting(req, imp, exchangeTargeting);
        
        assertNull(result);
    }
    
    @Test
    public void checkExchangeTargeting_positiveTest_bannerExtContainsEmptyListOfMatchingAdIds() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});

        ExchangeTargeting exchangeTargeting = new ExchangeTargeting();
        exchangeTargeting.setOpenxTargeting(new OpenXTargeting(){{
          setRequiredMatchingAdIds(new LinkedList<MatchingAdId>());  
        }});
        
        Impression imp = new Impression();
        
        Banner banner = new Banner();
        banner.setExt(new HashMap(){{
            put("matching_ad_id", new LinkedList<MatchingAdId>());
        }});
        
        imp.setBanner(banner);
        imp.setVideo(new Video());
        
        String result = null;
        
        result = oxAdapter.checkExchangeTargeting(req, imp, exchangeTargeting);
        
        assertEquals("No match for any of required matching_ad_ids: [] in received matching_ad_ids: []", result);
    }
    
    @Test
    public void checkExchangeTargeting_positiveTest_matchingAds() throws Lot49Exception, Throwable {
        OpenXAdapter oxAdapter = new OpenXAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});

        ExchangeTargeting exchangeTargeting = new ExchangeTargeting();
        exchangeTargeting.setOpenxTargeting(new OpenXTargeting(){{
          setRequiredMatchingAdIds(new LinkedList<MatchingAdId>(){{
            add(new MatchingAdId(10101, 20202, 30303));  
          }});  
        }});
        
        Impression imp = new Impression();
        
        MatchingAdId matchingAdId = new MatchingAdId();
        matchingAdId.setAdHeight(100);
        matchingAdId.setAdWidth(200);
        matchingAdId.setCampaignId(10101);
        matchingAdId.setCreativeId(30303);
        matchingAdId.setDeal(new HashMap());
        matchingAdId.setPlacementId(20202);
        
        assertEquals("{\"campaign_id\" : \"10101\", \"placement_id\" : \"20202\",\"creative_id\" : \"30303\"}", matchingAdId.toString());
        
        Banner banner = new Banner();
        banner.setExt(new HashMap(){{
            put("matching_ad_id", new LinkedList<MatchingAdId>(){{
              add(matchingAdId);
            }});
        }});
        
        imp.setBanner(banner);
        imp.setVideo(new Video());
        
        String result = null;
        
        result = oxAdapter.checkExchangeTargeting(req, imp, exchangeTargeting);
        
        assertNull(result);
        assertEquals(matchingAdId, banner.getExt().get("matching_ad_id_found"));
        
        // TODO: refactoring idea:
        // parameter req not used
        // don't return null when matchingAdId found
    }
}
