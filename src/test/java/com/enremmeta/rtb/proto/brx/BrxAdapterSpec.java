package com.enremmeta.rtb.proto.brx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.constants.RtbConstants;
import com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Ext;
import com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Imp;
import com.enremmeta.rtb.proto.brx.BrxRtb095.Linearity;

@RunWith(PowerMockRunner.class)
public class BrxAdapterSpec {


    @Test
    public void convertRequest_shouldTransferReqId() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        Whitebox.setInternalState(req, "id_", "BRX_REQ_TEST_ID");

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("BRX_REQ_TEST_ID", req2.getId());
    }

    @Test
    public void convertRequest_shouldSetIsTest() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        Ext ext = Ext.getDefaultInstance();
        Whitebox.setInternalState(ext, "isTest_", true);
        Whitebox.setInternalState(req, "ext_", ext);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertTrue(req2.getLot49Ext().isTest());
    }

    @Test
    public void convertRequest_shouldSetImpression() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "id_", "TEST_IMP_ID");
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("TEST_IMP_ID", req2.getImp().get(0).getId());
        assertEquals(1, req2.getImp().size());
    }

    @Test
    public void convertRequest_shouldSetVideoApis() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Video vid = BrxRtb095.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "api_", new LinkedList<BrxRtb095.Api>() {
            {
                add(BrxRtb095.Api.BR_HTML5_1_0);
                add(BrxRtb095.Api.VPAID_1_0);
            }
        });

        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "video_", vid);
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(2, req2.getImp().get(0).getVideo().getApi().size());
        assertEquals(101, req2.getImp().get(0).getVideo().getApi().get(0).intValue());
        assertEquals(1, req2.getImp().get(0).getVideo().getApi().get(1).intValue());
    }

    @Test
    public void convertRequest_shouldSetVideoGeometry() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Video vid = BrxRtb095.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "w_", 100);
        Whitebox.setInternalState(vid, "h_", 50);

        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "video_", vid);
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(100, req2.getImp().get(0).getVideo().getW());
        assertEquals(50, req2.getImp().get(0).getVideo().getH());
    }

    @Test
    public void convertRequest_shouldSetVideoLinearityLinear() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Video vid = BrxRtb095.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "linearity_", Linearity.LINEAR);

        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "video_", vid);
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(RtbConstants.LINEARITY_LINEAR, req2.getImp().get(0).getVideo().getLinearity());

    }

    @Test
    public void convertRequest_shouldSetVideoLinearityNonLinear() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Video vid = BrxRtb095.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "linearity_", Linearity.NON_LINEAR);

        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "video_", vid);
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(RtbConstants.LINEARITY_NON_LINEAR,
                        req2.getImp().get(0).getVideo().getLinearity());

    }

    @Test
    public void convertRequest_shouldSetVideoDurationAndBitrate() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Video vid = BrxRtb095.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "minduration_", 10);
        Whitebox.setInternalState(vid, "maxduration_", 100);
        Whitebox.setInternalState(vid, "maxbitrate_", 9600);

        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "video_", vid);
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(10, req2.getImp().get(0).getVideo().getMinduration());
        assertEquals(100, req2.getImp().get(0).getVideo().getMaxduration());
        assertEquals(9600, req2.getImp().get(0).getVideo().getMaxbitrate());
    }

    @Test
    public void convertRequest_shouldSetVideoMimes() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Video vid = BrxRtb095.BidRequest.Video.getDefaultInstance();

        List<BrxRtb095.Mimes> mimes = new LinkedList<BrxRtb095.Mimes>() {
            {
                add(BrxRtb095.Mimes.MP4);
                add(BrxRtb095.Mimes.SHOCKWAVE_FLASH);
            }
        };

        Whitebox.setInternalState(vid, "mimes_", mimes);

        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "video_", vid);
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(2, req2.getImp().get(0).getVideo().getMimes().size());
        assertEquals("video/mp4", req2.getImp().get(0).getVideo().getMimes().get(0));
        assertEquals("application/x-shockwave-flash",
                        req2.getImp().get(0).getVideo().getMimes().get(1));

    }

    @Test
    public void convertRequest_shouldSetVideoPosition() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Video vid = BrxRtb095.BidRequest.Video.getDefaultInstance();

        Whitebox.setInternalState(vid, "pos_", BrxRtb095.Pos.FULLSCREEN);

        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "video_", vid);
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(7, req2.getImp().get(0).getVideo().getPos());
    }

    @Test
    public void convertRequest_shouldSetVideoStartdelay() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Video vid = BrxRtb095.BidRequest.Video.getDefaultInstance();

        Whitebox.setInternalState(vid, "startdelay_", 200);

        Imp imp = Imp.getDefaultInstance();
        Whitebox.setInternalState(imp, "video_", vid);
        Whitebox.setInternalState(req, "imp_", imp);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(200, req2.getImp().get(0).getVideo().getStartdelay());
    }

    private static final String TEST_SITE_PAGE =
                    "http://url_of_the_page_where_the_impression_will_be.shown";

    @Test
    public void convertRequest_shouldSetSiteAndPage() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();
        Whitebox.setInternalState(site, "page_", TEST_SITE_PAGE);

        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(TEST_SITE_PAGE, req2.getSite().getPage());
    }

    @Test
    public void convertRequest_shouldSetSiteCategories() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        List<BrxRtb095.ContentCategory> cats = new LinkedList<BrxRtb095.ContentCategory>() {
            {
                add(BrxRtb095.ContentCategory.IAB14_7);
                add(BrxRtb095.ContentCategory.IAB3_3);
            }
        };

        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();

        Whitebox.setInternalState(site, "cat_", cats);
        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(2, req2.getSite().getCat().size());
        assertEquals("BRXIAB_221", req2.getSite().getCat().get(0));
        assertEquals("BRXIAB_36", req2.getSite().getCat().get(1));
    }

    @Test
    public void convertRequest_shouldSetSiteSelectionCategories() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        List<BrxRtb095.ContentCategory> cats = new LinkedList<BrxRtb095.ContentCategory>() {
            {
                add(BrxRtb095.ContentCategory.IAB14_5);
                add(BrxRtb095.ContentCategory.IAB3_3);
            }
        };

        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();

        Whitebox.setInternalState(site, "sectioncat_", cats);
        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(2, req2.getSite().getSectioncat().size());
        assertEquals("BRXIAB_219", req2.getSite().getSectioncat().get(0));
        assertEquals("BRXIAB_36", req2.getSite().getSectioncat().get(1));
    }

    @Test
    public void convertRequest_shouldSetSiteContentCategories() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        List<BrxRtb095.ContentCategory> cats = new LinkedList<BrxRtb095.ContentCategory>() {
            {
                add(BrxRtb095.ContentCategory.IAB14_5);
                add(BrxRtb095.ContentCategory.IAB3_3);
            }
        };

        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();
        BrxRtb095.BidRequest.Content content = BrxRtb095.BidRequest.Content.getDefaultInstance();
        Whitebox.setInternalState(content, "cat_", cats);

        Whitebox.setInternalState(site, "content_", content);
        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(2, req2.getSite().getContent().getCat().size());
        assertEquals("BRXIAB_219", req2.getSite().getContent().getCat().get(0));
        assertEquals("BRXIAB_36", req2.getSite().getContent().getCat().get(1));
    }

    @Test
    public void convertRequest_shouldSetSiteContentRating() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();
        BrxRtb095.BidRequest.Content content = BrxRtb095.BidRequest.Content.getDefaultInstance();
        Whitebox.setInternalState(content, "contentrating_", "MPAA");

        Whitebox.setInternalState(site, "content_", content);
        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("MPAA", req2.getSite().getContent().getContentrating());
    }

    @Test
    public void convertRequest_shouldSetSiteContentLanguage() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();
        BrxRtb095.BidRequest.Content content = BrxRtb095.BidRequest.Content.getDefaultInstance();
        Whitebox.setInternalState(content, "language_", "da");

        Whitebox.setInternalState(site, "content_", content);
        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("da", req2.getSite().getContent().getLanguage());
    }

    @Test
    public void convertRequest_shouldSetSiteContentIsEmbedable() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();

        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();
        BrxRtb095.BidRequest.Content content = BrxRtb095.BidRequest.Content.getDefaultInstance();
        Whitebox.setInternalState(content, "embeddable_", BrxRtb095.State.YES);

        Whitebox.setInternalState(site, "content_", content);
        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(1, req2.getSite().getContent().getEmbeddable());
    }

    private static final String TEST_SITE_REFERER =
                    "http://url_that_caused_navigation_to_the_current.page";
    private static final String TEST_SITE_DOMAIN = "domain_of_the.site";

    @Test
    public void convertRequest_shouldSetSiteRefererAndDomain() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();

        Whitebox.setInternalState(site, "ref_", TEST_SITE_REFERER);
        Whitebox.setInternalState(site, "domain_", TEST_SITE_DOMAIN);
        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(TEST_SITE_REFERER, req2.getSite().getRef());
        assertEquals(TEST_SITE_DOMAIN, req2.getSite().getDomain());
    }

    private static final String TEST_SITE_KEYWORDS =
                    "List,of,comma,separated,keywords,describing,the,site";

    @Test
    public void convertRequest_shouldSetSiteKeywords() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Site site = BrxRtb095.BidRequest.Site.getDefaultInstance();

        Whitebox.setInternalState(site, "keywords_", TEST_SITE_KEYWORDS);
        Whitebox.setInternalState(req, "site_", site);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(TEST_SITE_KEYWORDS, req2.getSite().getKeywords());
    }

    @Test
    public void convertRequest_shouldSetDeviceAndCarrier() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "carrier_", "012");
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("012", req2.getDevice().getCarrier());
    }

    @Test
    public void convertRequest_shouldSetDeviceConnectionType() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "connectiontype_",
                        BrxRtb095.ConnectionType.CELLULAR_DATA_3G);
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(BrxRtb095.ConnectionType.CELLULAR_DATA_3G.getNumber(),
                        req2.getDevice().getConnectiontype().intValue());
    }

    @Test
    public void convertRequest_shouldSetDeviceDeviceType() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "devicetype_", BrxRtb095.DeviceType.CONNECTED_TV);
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(BrxRtb095.DeviceType.CONNECTED_TV.getNumber(),
                        req2.getDevice().getDevicetype().intValue());
    }

    @Test
    public void convertRequest_shouldSetDeviceIDMD5_IMEIexample() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "didmd5_", DigestUtils.md5Hex("490154203237518"));
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("0eaed834a1e6f4b2bf0c024e0013dea1", req2.getDevice().getDidmd5());

        // TODO: fix and add unit test
        // looks like typo: dev2.setDidsha1(dev2.getDidsha1());
    }

    @Test
    public void convertRequest_shouldSetDeviceDnt() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "dnt_", BrxRtb095.State.YES);
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals(BrxRtb095.State.YES.getNumber(), req2.getDevice().getDnt());

    }

    @Test
    public void convertRequest_shouldSetDeviceMD5PlatformID() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "dpidmd5_", DigestUtils.md5Hex("3f9485a3ef294bc8"));
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("16cd5c590432f8909ebe826130bb219f", req2.getDevice().getDpidmd5());

    }

    @Test
    public void convertRequest_shouldSetDeviceIP() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "ip_", "225.134.55.153");
        Whitebox.setInternalState(device, "ipv6_", "2001:0db8:0a0b:12f0:0000:0000:0000:0001");
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("225.134.55.153", req2.getDevice().getIp());
        assertEquals("2001:0db8:0a0b:12f0:0000:0000:0000:0001", req2.getDevice().getIpv6());

    }

    @Test
    public void convertRequest_shouldSetDeviceLanguage() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "language_", "es");
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("es", req2.getDevice().getLanguage());

    }

    @Test
    public void convertRequest_shouldSetDeviceMakeAndModel() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "make_", "Google");
        Whitebox.setInternalState(device, "model_", "Nexus5");
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("Google", req2.getDevice().getMake());
        assertEquals("Nexus5", req2.getDevice().getModel());

    }

    @Test
    public void convertRequest_shouldSetDeviceOSAndVersion() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "os_", "Android");
        Whitebox.setInternalState(device, "osv_", "6.0");
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("Android", req2.getDevice().getOs());
        assertEquals("6.0", req2.getDevice().getOsv());

    }

    @Test
    public void convertRequest_shouldSetDeviceUserAgent() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        Whitebox.setInternalState(device, "ua_", SharedSetUp.USER_AGENT);
        Whitebox.setInternalState(req, "device_", device);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("Android", req2.getDevice().getOs());
        assertEquals(SharedSetUp.USER_AGENT, req2.getDevice().getUa());

    }

    @Test
    public void convertRequest_shouldSetDeviceUser() {

        BrxAdapter adapter = new BrxAdapter();

        BrxRtb095.BidRequest req = BrxRtb095.BidRequest.getDefaultInstance();
        BrxRtb095.BidRequest.Device device = BrxRtb095.BidRequest.Device.getDefaultInstance();

        BrxRtb095.BidRequest.User user = BrxRtb095.BidRequest.User.getDefaultInstance();
        Whitebox.setInternalState(user, "id_", "TEST_USR_ID");

        Whitebox.setInternalState(req, "user_", user);

        OpenRtbRequest req2 = adapter.convertRequest(BrxRtb095.BidRequest.getDefaultInstance());

        assertEquals("TEST_USR_ID", req2.getUser().getId());

    }
}
