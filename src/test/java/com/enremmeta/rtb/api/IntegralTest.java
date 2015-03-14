package com.enremmeta.rtb.api;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.spi.providers.integral.IntegralConfig;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoReceived;
import com.enremmeta.rtb.spi.providers.integral.IntegralService;
import com.enremmeta.rtb.spi.providers.integral.result.dto.BrandSafetyDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralAllResponse;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;
import com.enremmeta.rtb.test.cases.Lot49Test;
import com.enremmeta.util.Utils;
import com.google.gson.GsonBuilder;

/**
 * Created by mgorbal
 */
public class IntegralTest extends Lot49Test {

    class Ad_1010_integral_fake extends AdImpl {

        public Ad_1010_integral_fake() throws Lot49Exception {
            super();

            TargetingIntegralTraq targetingIntegralTraq = new TargetingIntegralTraq(0);
            TargetingIntegralViewability targetingIntegralViewability =
                            new TargetingIntegralViewability(50, null, null, 2000, null, null,
                                            null);
            targetingIntegral = new TargetingIntegral(targetingIntegralTraq, null,
                            targetingIntegralViewability);
        }
    }

    // @Test
    public void fix_or_ignore_testServiceCalls() {
        IntegralConfig integralConfig = new IntegralConfig();
        integralConfig.setClientId("49600");
        integralConfig.setPort(8080);
        integralConfig.setHost("http://ec2-52-2-36-93.compute-1.amazonaws.com");
        IntegralService integralService = new IntegralService(integralConfig);
        IntegralInfoReceived integralInfoReceived = new IntegralInfoReceived();
        integralService.checkUrl("http://www.cnn.com", integralInfoReceived);
        assertTrue(!integralInfoReceived.isError());
        IntegralAllResponse integralAllResponse = integralInfoReceived.getIntegralAllResponse();
        assertNotNull(integralAllResponse);
        assertNotNull(integralAllResponse.getTraq());
        assertTrue(integralAllResponse.getTraq() > 600);
        integralService.checkUrl(
                        "http://btpr.vuze.com/service/request_vuze.php?p=789C258DC10AC2301044FF65CF36C11E0B221E3C8A177B2BC8B6D9A641930D6952A5E2BF9BD8DBCC1B78F381537BBBDE2FEDED0C0DC4900876E0533FB01B8D4EE199E914A36FA45CD24A5599462D14CD8FC81ED55B0C6CE54C613103C96D959AA2F0933F1A752836D45439B49455C59191628BC6E5DE471F448145B33D1B95B9C557C01AF53EB3951DFD612923734E1DD41DC0F707245B3ECB&cb=1452790123500",
                        integralInfoReceived);
        assertTrue(!integralInfoReceived.isError());
    }

    @Test
    public void test_TargetingIntegralValidation() {
        TargetingIntegralViewability targetingIntegralViewability =
                        new TargetingIntegralViewability(null, null, null, 2000, null, null, null);
        targetingIntegralViewability.validate(new ViewabilityDto());

        TargetingIntegralTraq targetingIntegralTraq = new TargetingIntegralTraq(0);
        targetingIntegralTraq.validate(0);

        TargetingIntegralBrandSafety targetingIntegralBrandSafety =
                        new TargetingIntegralBrandSafety(false, // isExcludeUnratable
                                        // excludeHighRisk and excludeModerateRisk
                                        null, null, null, null, null, null, null, null);
        targetingIntegralBrandSafety.validate(new BrandSafetyDto());

        targetingIntegralBrandSafety = new TargetingIntegralBrandSafety(false, // isExcludeUnratable
                        // excludeHighRisk and excludeModerateRisk
                        new TargetingIntegralBsc(true, true), new TargetingIntegralBsc(true, true),
                        new TargetingIntegralBsc(true, true), new TargetingIntegralBsc(true, true),
                        new TargetingIntegralBsc(true, true), new TargetingIntegralBsc(true, true),
                        new TargetingIntegralBsc(true, true), new TargetingIntegralBsc(true, true));

        TargetingIntegral targetingIntegral =
                        new TargetingIntegral(null, targetingIntegralBrandSafety, null);
        targetingIntegral.validateBrandSafety(null);
    }


    // @Test
    public void fix_or_ignore_testIntegralTargetingUrls() {

        try {
            File jsonFile = getTestDataFile("test_integral_request.json");
            String jsonString = Utils.readFile(jsonFile);

            GsonBuilder gsonBuilder = new GsonBuilder();

            OpenRtbRequest openRtbRequest =
                            gsonBuilder.create().fromJson(jsonString, OpenRtbRequest.class);

            String auctionUrl = "http://localhost:10000" + Lot49Constants.ROOT_PATH_AUCTIONS + "/"
                            + Lot49Constants.EXCHANGE_BIDSWITCH;
            HttpPost httpPost = new HttpPost(auctionUrl);
            httpPost.addHeader("Content-Type", "application/json");

            File urlsFile = getTestDataFile("integral_targeting_urls_test.txt");
            String urlsString = Utils.readFile(urlsFile);
            String[] targetUrlsRow = urlsString.split("\r\n");
            int idx = 1;
            for (String row : targetUrlsRow) {
                String[] components = row.split("\t");
                if (components.length > 1) {
                    openRtbRequest.getSite().setPage(components[1]);
                    httpPost.setEntity(new StringEntity(gsonBuilder.create().toJson(openRtbRequest,
                                    OpenRtbRequest.class)));

                    int timeout = 500;
                    RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
                                    .setConnectionRequestTimeout(timeout * 1000)
                                    .setSocketTimeout(timeout * 1000).build();
                    CloseableHttpClient httpClient = HttpClientBuilder.create()
                                    .setDefaultRequestConfig(config).build();
                    HttpResponse httpResponse = httpClient.execute(httpPost);

                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        System.out.println(
                                        "URL " + idx++ + " [PASS"
                                                        + (!components[0].equalsIgnoreCase("1")
                                                                        ? " !!NEED TO CHECK" : "")
                                                        + "]: " + row);
                        // assertTrue();
                    } else {
                        System.out.println(
                                        "URL " + idx++ + " [FAIL"
                                                        + (!components[0].equalsIgnoreCase("0")
                                                                        ? " !!NEED TO CHECK" : "")
                                                        + "]: " + row);
                        // assertTrue(components[0].equalsIgnoreCase("0"));
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    // @Test
    public void fix_or_ignore_testIntegralTargetingIntegration() {
        // TODO: move to integration tests
        // require integration targeting in Ads

        int timeout = 3000;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
                        .setConnectionRequestTimeout(timeout * 1000)
                        .setSocketTimeout(timeout * 1000).build();

        CloseableHttpClient httpClient =
                        HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        try {

            String auctionUrl = "http://localhost:10000" + Lot49Constants.ROOT_PATH_AUCTIONS + "/"
                            + Lot49Constants.EXCHANGE_BIDSWITCH;
            HttpPost httpPost = new HttpPost(auctionUrl);
            httpPost.addHeader("Content-Type", "application/json");

            File jsonFile = getTestDataFile("test_integral_request.json");
            String jsonString = Utils.readFile(jsonFile);
            StringEntity params = new StringEntity(jsonString);
            httpPost.setEntity(params);


            HttpResponse httpResponse = httpClient.execute(httpPost);

            assertNotNull(httpResponse);
            assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());

            String responceString = EntityUtils.toString(httpResponse.getEntity());

            GsonBuilder gsonBuilder = new GsonBuilder();
            OpenRtbResponse rtbResponse =
                            gsonBuilder.create().fromJson(responceString, OpenRtbResponse.class);

            assertNotNull(rtbResponse.getBidid());
            assertNotNull(rtbResponse.getCur());
            assertNotNull(rtbResponse.getId());
            assertNotNull(rtbResponse.getSeatbid());
            assertFalse(rtbResponse.getSeatbid().isEmpty());

            SeatBid seatbid = rtbResponse.getSeatbid().get(0);
            assertNotNull(seatbid);

            if (seatbid.getBid() != null) {
                Bid bid = seatbid.getBid().get(0);
                assertNotNull(bid);

                if (bid.getNurl() != null && !bid.getNurl().trim().isEmpty()) {

                    String nUrl = bid.getNurl().replaceAll("wp=[^&]+", "wp=" + bid.getPrice())
                                    .replaceAll("ct=[^&]+", "ct=").replaceAll("cte=[^&]+", "cte=");

                    HttpGet httpGet = new HttpGet(nUrl);

                    httpResponse = httpClient.execute(httpGet);

                    assertNotNull(httpResponse);
                    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());

                    responceString = EntityUtils.toString(httpResponse.getEntity());
                    assertNotNull(responceString);
                } else {
                    assertNotNull(bid.getAdm());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }



    /*
     * package com.enremmeta.rtb.groovy.tc
     * 
     * import com.enremmeta.rtb.api.AdImpl
     * 
     * import com.enremmeta.rtb.api.TargetingGeo import com.enremmeta.rtb.api.FrequencyCap
     * 
     * import com.enremmeta.rtb.api.TargetingIntegralTraq import
     * com.enremmeta.rtb.api.TargetingIntegralBrandSafety import
     * com.enremmeta.rtb.api.TargetingIntegralBsc import
     * com.enremmeta.rtb.api.TargetingIntegralViewability import
     * com.enremmeta.rtb.api.TargetingIntegral
     * 
     * class Ad_1050_MyIpBannerAd extends AdImpl {
     * 
     * void init() {
     * 
     * adomain = ["www.myip.io"] desc = "MyIp Banner Ad" bidAmount = 3000 iurl =
     * "http://creative.us.s3.opendsp.com/creatives/791/myip300x250_updated.jpg"
     * 
     * tags = [ new Tag_791_FlashBanneradMyIP300x250_1050_MyIpBannerAd(this), new
     * Tag_1819_FlashBanneradMyIP300x250_1050_MyIpBannerAd(this) ]
     * 
     * clientId="25" campaignId="62" advertiser="MyIP" advertiserId="62"
     * 
     * TargetingIntegralTraq targetingIntegralTraq = new TargetingIntegralTraq(500);
     * TargetingIntegralBrandSafety targetingIntegralBrandSafety = new TargetingIntegralBrandSafety(
     * false, // isExcludeUnratable // excludeHighRisk and excludeModerateRisk new
     * TargetingIntegralBsc(true, true), new TargetingIntegralBsc(true, true), new
     * TargetingIntegralBsc(true, true), new TargetingIntegralBsc(true, true), new
     * TargetingIntegralBsc(true, true), new TargetingIntegralBsc(true, true), new
     * TargetingIntegralBsc(true, true), new TargetingIntegralBsc(true, true) );
     * TargetingIntegralViewability targetingIntegralViewability = new
     * TargetingIntegralViewability(40,10,10,1000,10,70,1000);
     * 
     * targetingIntegral = new TargetingIntegral(targetingIntegralTraq,
     * targetingIntegralBrandSafety, targetingIntegralViewability); } }
     */
}
