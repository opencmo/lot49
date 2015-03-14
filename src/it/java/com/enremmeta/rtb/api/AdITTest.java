package com.enremmeta.rtb.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.proto.openrtb.Deal;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.PMP;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtGeo;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.proto.testexchange.Test1ExchangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Test2ExchangeAdapter;
import com.enremmeta.rtb.test.cases.Lot49Test;


public class AdITTest extends Lot49Test {
    class Ad_181818_fake extends AdImpl {

        public Ad_181818_fake() throws Lot49Exception {
            super();

        }

    }

    ;

    @Test
    public void testUrls() {
        try {

            AdImpl ad = new Ad_181818_fake();

            Set<String> expectedUrls = new HashSet<String>() {
                {
                    add("http://www.cnn.com/us/dc/congress");
                    add("http://www.ask.com");
                    add("http://www.cnn.com/us/dc/congress/");
                }
            };
            Set<String> urls = new HashSet<String>();
            urls.add("malformedUrl");
            urls.add("https://www.cnn.com/us/dc/congress");
            urls.add("http://www.AsK.com");
            urls.add(null);
            ad.setTargetingUrls(urls);
            Map<String, Set<String>> gotUrls = ad.getTargetingUrls();
            System.out.println(gotUrls);
            OpenRtbRequest req = new OpenRtbRequest();
            req.getLot49Ext().setAdapter(new Test2ExchangeAdapter());
            List<Impression> imps = new ArrayList<Impression>();
            imps.add(new Impression());
            req.setImp(imps);
            Site site = new Site();
            site.setPage("http://www.cnn.com/us/dc/congress/senate");
            long t0 = System.currentTimeMillis();
            boolean m = ad.matchTargetingUrls(req, site);
            long t1 = System.currentTimeMillis();
            System.out.println("Time to match: " + ((t1 - t0)));
            assertTrue(m);
            site.setPage("http://www.cnn.com/us/dc/supreme-court");
            m = ad.matchTargetingUrls(req, site);
            assertFalse(m);

        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testPrivateDeals() {
        try {

            // Test 1: one deal - wrong currency
            AdImpl ad = new Ad_181818_fake();
            Map<String, List<String>> acceptedDeals = new HashMap<String, List<String>>();
            List<String> test1Deals = new ArrayList<String>();
            test1Deals.add("123");
            test1Deals.add("456");
            acceptedDeals.put("test1", test1Deals);

            List<String> test2Deals = new ArrayList<String>();
            test2Deals.add("abc");
            test2Deals.add("def");
            acceptedDeals.put("test2", test1Deals);

            ad.setTargetingDeals(acceptedDeals);

            OpenRtbRequest req = new OpenRtbRequest();
            req.getLot49Ext().setAdapter(new Test1ExchangeAdapter());
            List<Impression> imps = new ArrayList<Impression>();
            req.setImp(imps);
            Impression imp1 = new Impression();
            imps.add(imp1);
            PMP pmp = new PMP();
            imp1.setPmp(pmp);
            List<Deal> sentDeals = new ArrayList<Deal>();
            pmp.setDeals(sentDeals);
            Deal d1 = new Deal();
            d1.setId("abc");
            d1.setBidfloorcur("CAN");
            sentDeals.add(d1);

            // Canada $ should be false
            assertNull(ad.matchDeals(req, imp1));

            d1.setBidfloorcur("USD");
            // Still fail - we want deal "abc" but from exchange "test2"
            assertNull(ad.matchDeals(req, imp1));

            d1.setId("123");
            assertEquals(ad.matchDeals(req, imp1), "123");

        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testUrlsFromUrl() {
        try {

            AdImpl ad = new Ad_181818_fake();
            String wlName = "targeting_urls_test.txt";
            File oldWl = new File("/tmp/" + wlName);
            System.out.println(
                            "Deleting old whitelist's local copy " + oldWl + ": " + oldWl.delete());
            File wl = getTestDataFile(wlName);
            String wlUrl = "file://" + wl.getAbsolutePath();
            // wlUrl = "https://s3.amazonaws.com/files.opendsp.com/wl.txt";
            System.out.println("Setting WL to " + wlUrl);
            ad.setTargetingUrlsFromUrl(wlUrl);

            OpenRtbRequest req = new OpenRtbRequest();
            req.getLot49Ext().setAdapter(new Test2ExchangeAdapter());
            List<Impression> imps = new ArrayList<Impression>();
            imps.add(new Impression());
            req.setImp(imps);
            Site site = new Site();

            site.setPage("http://hotcopper.com.au/threads/the-wedding-photos.2615960/?post_id=16180142");
            assertFalse(ad.matchTargetingUrls(req, site));

            site.setPage("http://hotcopper.com.au/threads/and-the-lord-said.2615906/?post_id=16179899#.Vh8zI_mqqko");
            assertFalse(ad.matchTargetingUrls(req, site));

            site.setPage("http://247wallst.com/about");
            assertFalse(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about/");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about/this");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/aboutMary");
            assertFalse(ad.matchTargetingUrls(req, site));

            site.setPage("http://247wallst.com/about2");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about2/");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about2/this");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about2Mary");
            assertTrue(ad.matchTargetingUrls(req, site));

            site.setPage("http://247wallst.com/about3");
            assertFalse(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about3/");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about3//");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about3/this");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://247wallst.com/about3Mary");
            assertFalse(ad.matchTargetingUrls(req, site));

            site.setPage("https://247wallst.com/about3");
            assertFalse(ad.matchTargetingUrls(req, site));
            site.setPage("https://247wallst.com/about3/");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("https://247wallst.com/about3//");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("https://247wallst.com/about3/this");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("https://247wallst.com/about3Mary");
            assertFalse(ad.matchTargetingUrls(req, site));

            site.setPage("https://247wallst.com/about4");
            assertFalse(ad.matchTargetingUrls(req, site));
            site.setPage("https://247wallst.com/about4/");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("https://247wallst.com/about4//");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("https://247wallst.com/about4/this");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("https://247wallst.com/about4Mary");
            assertFalse(ad.matchTargetingUrls(req, site));

            site.setPage("http://www.wsj.com/public/page/sea.html");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://www.wsj.com/public/page/sea.html/otter");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://www.wsj.com/public/page/sea.html5");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://www.wsj.com/public/page/sea.htm");
            assertFalse(ad.matchTargetingUrls(req, site));
            site.setPage("http://www.wsj.com/public/page/");
            assertFalse(ad.matchTargetingUrls(req, site));

            site.setPage("http://www.businessinsider.com/california-wildfire-photos-2015-9");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://www.businessinsider.com/california-wildfire-photos-2015-91");
            assertTrue(ad.matchTargetingUrls(req, site));
            site.setPage("http://www.businessinsider.com/california-wildfire-photos-2015-1");
            assertFalse(ad.matchTargetingUrls(req, site));

        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testOrganizations() {
        try {

            AdImpl ad = new Ad_181818_fake();

            Set<String> expectedPatterns = new HashSet<String>() {
                {
                    add("bank of america");
                    add(".*bank of america.*");
                    add("bank of america inc");
                    add("ask com");
                }
            };
            List<String> orgs = new ArrayList<String>();
            orgs.add(" Bank  of America   ");
            orgs.add(" Bank of America,   Inc.");
            orgs.add("*Bank of America*");
            orgs.add("Ask.com");
            orgs.add("   ");
            orgs.add(null);
            ad.setOrganizations(orgs);
            Set<Pattern> gotPatterns = ad.getOrganizations();
            assertEquals(expectedPatterns.size(), gotPatterns.size());
            for (Pattern p : gotPatterns) {
                String s = p.toString();
                System.out.println("Considering regexp " + s);
                if (!expectedPatterns.contains(s)) {
                    fail("Unexpected regexp " + s);
                } else {
                    expectedPatterns.remove(s);
                }
            }

            Geo geo = new Geo();
            Lot49ExtGeo geoExt = new Lot49ExtGeo();
            geo.getExt().put(Lot49ExtGeo.GEO_EXT_KEY, geoExt);

            geoExt.setOrg("Bank of America and Canada");
            assertTrue(ad.matchOrganizations(geo));

            geoExt.setOrg("Bank of Canada");
            assertFalse(ad.matchOrganizations(geo));

            geoExt.setOrg("Bank of America Inc");
            assertTrue(ad.matchOrganizations(geo));

            geoExt.setOrg("Bank of Canada");
            assertFalse(ad.matchOrganizations(geo));

            geoExt.setOrg("Sperm Bank of America");
            assertTrue(ad.matchOrganizations(geo));

            geoExt.setOrg("Ask.com");
            assertTrue(ad.matchOrganizations(geo));

            geoExt.setOrg("Ask-com");
            assertTrue(ad.matchOrganizations(geo));

            geoExt.setOrg("Askcom");
            assertFalse(ad.matchOrganizations(geo));

        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testSegments() {
        try {
            AdImpl ad = new Ad_181818_fake();

            OpenRtbRequest req = new OpenRtbRequest();
            req.getLot49Ext().setAdapter(new AdaptvAdapter());
            List<Impression> imps = new ArrayList<Impression>();
            imps.add(new Impression());
            req.setImp(imps);

            UserSegments userSegments = new UserSegments();
            HashMap<String, String> score1 = new HashMap<String, String>();
            score1.put("score", "0.5");
            userSegments.getUserSegmentsMap().put("a1", score1);
            HashMap<String, String> score2 = new HashMap<String, String>();
            score1.put("score", "0.7");
            userSegments.getUserSegmentsMap().put("a2", score2);
            ad.getUserSegments().add("a1");
            ad.getUserSegments().add("a3");
            Boolean checkSegments = ad.checkSegments(req, userSegments);
            assertFalse(checkSegments);

            FrequencyCap frequencyCap = new FrequencyCap(1, 10);
            ad.setFrequencyCap(frequencyCap);

            checkSegments = ad.checkSegments(req, userSegments);
            assertTrue(checkSegments);
        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    @Test
    public void testFrequencyCapacity() {
        try {
            AdImpl ad = new Ad_181818_fake();

            OpenRtbRequest req = new OpenRtbRequest();
            req.getLot49Ext().setAdapter(new AdaptvAdapter());
            List<Impression> imps = new ArrayList<Impression>();
            imps.add(new Impression());
            req.setImp(imps);

            FrequencyCap frequencyCap = new FrequencyCap(1, 1);
            ad.setFrequencyCap(frequencyCap);
            Set<String> timestampsTS = new HashSet<>();
            Set<String> timestampsTC = new HashSet<>();
            Map<String, Set<String>> bidHistory = new HashMap<String, Set<String>>();
            Map<String, Set<String>> impressionHistory = new HashMap<String, Set<String>>();
            UserFrequencyCapAttributes userFrequencyCap =
                            new UserFrequencyCapAttributes(bidHistory, impressionHistory);
            bidHistory.put(UserFrequencyCapAttributes.CAMPAIGN_PREFIX + ad.getCampaignId(),
                            timestampsTC);
            bidHistory.put(UserFrequencyCapAttributes.TARGETING_STRATEGY_PREFIX + ad.getId(),
                            timestampsTC);

            ad.checkFrequencyCap(req, userFrequencyCap);

            long secsNow = System.currentTimeMillis() / 1000;
            timestampsTS.add(String.valueOf(secsNow + 1000));
            timestampsTS.add(String.valueOf(secsNow + 2000));
            timestampsTS.add(String.valueOf(secsNow + 3000));

            ad.checkFrequencyCap(req, userFrequencyCap);


        } catch (Lot49Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
