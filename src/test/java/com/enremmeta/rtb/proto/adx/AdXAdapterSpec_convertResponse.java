package com.enremmeta.rtb.proto.adx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
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
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.AdImpl;
import com.enremmeta.rtb.api.FixedDimension;
import com.enremmeta.rtb.api.MarkupType;
import com.enremmeta.rtb.api.RangeDimension;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.TagImpl;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.util.ServiceRunner;
import com.google.protobuf.UninitializedMessageException;
import com.google.protos.adx.NetworkBid;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, CSVParser.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdXAdapterSpec_convertResponse {
    private static final String GEO_TABLE_FILE_NAME = "geo_table_file_name";
    private final static int GEO_RECORD_ID = 1000010;

    private ServiceRunner serviceRunnerSimpleMock;
    private AdXConfig adxCfg;
    private Lot49Config configMock;
    private AdXAdapter adapter;

    @Before
    public void setUp() throws Lot49Exception, IOException {
        Whitebox.setInternalState(AdXAdapter.class, "geo", (Map<Integer, AdxGeo>) null);
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = Mockito.mock(Lot49Config.class);
        
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);

        adxCfg = new AdXConfig();

        Mockito.when(configMock.getExchanges()).thenAnswer(new Answer<ExchangesConfig>() {
            public ExchangesConfig answer(InvocationOnMock invocation) {

                ExchangesConfig exchangesConfigMock = Mockito.mock(ExchangesConfig.class);
                Mockito.when(exchangesConfigMock.getAdx()).thenReturn(adxCfg);
                return exchangesConfigMock;
            }
        });


        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
        
        prepareAdapter();

    }

    private void prepareAdxConfigSecurity(AdXConfig adxCfg) {
        adxCfg.setEncryptionKey(SharedSetUp.ENCRIPTION_KEY);
        adxCfg.setIntegrityKey(SharedSetUp.INTEGRITY_KEY);
    }



    @SuppressWarnings("serial")
    public void prepareAdapter() throws Lot49Exception, IOException {

        prepareAdxConfigSecurity(adxCfg);
        adapter = new AdXAdapter();

    }



    @Test
    public void negatveFlow_emptyResp_shouldThrowUninitializedMessageException() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});

        try{
        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);
        
        fail("should throw exception!");
        }catch(UninitializedMessageException e){
            assertTrue(e.getMessage().contains("Message missing required fields"));
        }

    }
    
    @Test
    public void negatveFlow_EmptyAdomainsInBid() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid());
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.error(anyString());

        try{
            NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

            fail("should throw exception!");
        }catch(UninitializedMessageException e){
            assertTrue(e.getMessage().contains("Message missing required fields"));
        }

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error("adx: Empty adomains in bid for ad null");
    }
    
    @Test
    public void positiveFlow_withMinimumParams() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.THIRD_PARTY_HTML5);
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", Mockito.mock(AdImpl.class));
        }});


        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
    
    @Test
    public void positiveFlow_VAST_PLAIN_FLASH_ONLY() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.VAST_PLAIN_FLASH_ONLY);
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", Mockito.mock(AdImpl.class));
        }});


        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
    
    @Test
    public void positiveFlow_THIRD_PARTY_FLASH() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.THIRD_PARTY_FLASH);
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", Mockito.mock(AdImpl.class));
        }});


        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
    
    @Test
    public void positiveFlow_VAST_VPAID() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.VAST_VPAID);
        Mockito.when(tMock.getMime()).thenReturn(Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH);
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", Mockito.mock(AdImpl.class));
        }});


        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
    
    @Test
    public void negativeFlow_SSL() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.VAST_VPAID);
        Mockito.when(tMock.getMime()).thenReturn(Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH);
        Mockito.when(tMock.isSslCapable()).thenReturn(true);
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", Mockito.mock(AdImpl.class));
        }});

        req.getLot49Ext().setSsl(true);

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.error(anyString());
        
        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error("adx: SSL ERROR: markup for null has http:// but SSL required! http://TEST.TEST");

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
    
    @Test
    public void positiveFlow_tagFixedDimension() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.VAST_VPAID);
        Mockito.when(tMock.getMime()).thenReturn(Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH);
        Mockito.when(tMock.getDimension()).thenReturn(new FixedDimension(100, 50));
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", Mockito.mock(AdImpl.class));
        }});


        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
    
    @Test
    public void positiveFlow_tagPageDimension_banner() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression(){{
                setBanner(new Banner());
            }});
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.VAST_VPAID);
        Mockito.when(tMock.getMime()).thenReturn(Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH);
        Mockito.when(tMock.getDimension()).thenReturn(new RangeDimension(0, 0, 100, 50));
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", Mockito.mock(AdImpl.class));
        }});


        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
    
    @Test
    public void positiveFlow_tagPageDimension_video() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression(){{
                setVideo(new Video());
            }});
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.VAST_VPAID);
        Mockito.when(tMock.getMime()).thenReturn(Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH);
        Mockito.when(tMock.getDimension()).thenReturn(new RangeDimension(0, 0, 100, 50));
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", Mockito.mock(AdImpl.class));
        }});


        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
    
    @Test
    public void positiveFlow_adVendorTypesObjNotNull() throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        OpenRtbResponse resp = new OpenRtbResponse();
        
        resp.setSeatbid(new LinkedList<SeatBid>(){{
            add(new SeatBid(){{
                setBid(new LinkedList<Bid>(){{
                    add(new Bid(){{
                      setId("TEST_BID_ID");
                      setAdomain(new LinkedList<String>(){{
                          add("TEST.DOMAIN");
                      }});
                      setAdm("http://TEST.TEST");
                      setImpid("10101");
                    }});
                }});
            }});
        }});
        
        req.setImp(new LinkedList<Impression>(){{
            add(new Impression());
        }});
        
        req.setId("TEST_REQ_ID");
        
        Tag tMock = Mockito.mock(TagImpl.class);
        Mockito.when(tMock.getMarkupType()).thenReturn(MarkupType.VAST_VPAID);
        Mockito.when(tMock.getMime()).thenReturn(Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH);
        Mockito.when(tMock.getDimension()).thenReturn(new FixedDimension(100, 50));
        req.getLot49Ext().setBidIdToTagObject(new HashMap<String, Tag>(){{
            put("TEST_BID_ID", tMock);
        }});
        
        Ad adMock = Mockito.mock(AdImpl.class);
        Mockito.when(adMock.getExchangeSpecificInstructions()).thenReturn(new HashMap(){{
            put(AdXAdapter.ADX_SPECIFIC_INSTRUCTION_VENDOR_TYPE,
                            new LinkedList<Integer>(){{
                                add(10);
                            }});
        }});
        req.getLot49Ext().setBidRequestIdToAdObject(new HashMap<String, Ad>(){{
            put("TEST_REQ_ID", adMock);
        }});


        NetworkBid.BidResponse nbResp = adapter.convertResponse(req, resp);

        assertEquals(1, nbResp.getAdCount());
        assertEquals(10101, nbResp.getAd(0).getAdslot(0).getId());
        assertEquals(0, nbResp.getAd(0).getAdslot(0).getMaxCpmMicros());
    }
}