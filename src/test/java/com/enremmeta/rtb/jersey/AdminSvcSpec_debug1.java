package com.enremmeta.rtb.jersey;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.WellFormedHtml;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AdminConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.proto.testexchange.Test2ExchangeAdapter;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class, AdminSvc.class, ExchangeAdapterFactory.class, Test2ExchangeAdapter.class, AdaptvAdapter.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdminSvcSpec_debug1 {
    private ServiceRunner serviceRunnerMock;
    private HttpServletRequest servletRequestMock;
    private AsyncResponse asyncResponseMock;
    private AdminSvc svc;
    private Ad debugAd;
    private String debugAdId = "Ad-Debug";
    private Tag debugTag;
    private String debugTagId = "Tag-Debug";

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        servletRequestMock = null;
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        debugAd = SharedSetUp.createAdMock(debugAdId, "Debug ad", "This is debug ad");
        debugTag = SharedSetUp.createTagMock(debugTagId, "Debug tag", "This is debug tag");

        svc = new AdminSvc();
    }

    private void configMocks(String host, Ad[] allAds, String clickSimUrl) throws Exception {
        Lot49Config configMock = serviceRunnerMock.getConfig();
        Mockito.when(configMock.getHost()).thenReturn(host);

        AdminConfig adminConfigMock = Mockito.mock(AdminConfig.class);
        Mockito.when(adminConfigMock.getExchangeClickSimulatorUrl()).thenReturn(clickSimUrl);
        Mockito.when(configMock.getAdmin()).thenReturn(adminConfigMock);

        AdCache adCacheMock = Mockito.mock(AdCache.class);
        Mockito.when(adCacheMock.getAll()).thenReturn(allAds);
        PowerMockito.doReturn(adCacheMock).when(serviceRunnerMock).getAdCache();
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.error(anyString(), any(Throwable.class));
    }

    private void configAdMock(Ad adMock, List<Tag> tags) {
        Mockito.doReturn(tags).when(adMock).getTags();
    }

    /*
    @SuppressWarnings("rawtypes")
    private void configExchangeAdapterFactoryMock(String exchangeName, Answer<Object> answer) {
        PowerMockito.mockStatic(ExchangeAdapterFactory.class);
        
        Map<String, Class<? extends ExchangeAdapter>> adapterMap = Whitebox.getInternalState(ExchangeAdapterFactory.class, "adapterMap");
        Class<? extends ExchangeAdapter> adapterClass = adapterMap.get(exchangeName);
        
        ExchangeAdapter adapterMock = PowerMockito.mock(adapterClass, answer);
        PowerMockito.when(ExchangeAdapterFactory.getExchangeAdapter(exchangeName)).thenReturn(adapterMock);
    }
    */

    @Test
    public void negativeFlow_callsResumeWithInternalServerErrorIfGetAllReturnsNull() throws Exception {
        Ad[] allAds = null;
        List<Tag> tags = null;
        
        testDebug1(allAds, tags, null, null, Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void negativeFlow_callsResumeWithInternalServerErrorIfAdNotFound() throws Exception {
        Ad[] allAds = new Ad[] {};
        List<Tag> tags = null;
        
        testDebug1(allAds, tags, null, null, Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void negativeFlow_callsResumeWithInternalServerErrorIfGetTagsReturnsNull() throws Exception {
        Ad[] allAds = new Ad[] {debugAd};
        List<Tag> tags = null;
        
        testDebug1(allAds, tags, null, null, Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void negativeFlow_callsResumeWithInternalServerErrorIfTagNotFound() throws Exception {
        Ad[] allAds = new Ad[] {debugAd};
        List<Tag> tags = new LinkedList<Tag>();
        
        testDebug1(allAds, tags, null, null, Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void negativeFlow_callsResumeWithInternalServerErrorIfFindTagReturnsNull() throws Exception {
        Ad[] allAds = new Ad[] {debugAd};
        List<Tag> tags = new LinkedList<Tag>();
        
        svc = PowerMockito.mock(AdminSvc.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.doReturn(null).when(svc, "findTag", any(Ad.class), anyString());
        
        testDebug1(allAds, tags, null, null, Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void negativeFlow_callsResumeWithInternalServerErrorIfExchangeAdapterNotFound() throws Exception {
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        String exchangeName = "Unknown adapter";
        String clickSimUrl = null;
        
        testDebug1(allAds, tags, exchangeName, clickSimUrl, Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfAllDataAreCorrect() throws Exception {
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        String clickSimUrl = null;
        
        SharedSetUp.createExchangeAdapterMock(exchangeName, Mockito.CALLS_REAL_METHODS);
        
        testDebug1(allAds, tags, exchangeName, clickSimUrl, Response.Status.OK);
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfClickSimUrlIsNotNull() throws Exception {
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        String clickSimUrl = "ExchangeClickSimulatorUrl";
        
        SharedSetUp.createExchangeAdapterMock(exchangeName, Mockito.CALLS_REAL_METHODS);
        
        testDebug1(allAds, tags, exchangeName, clickSimUrl, Response.Status.OK);
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfExchnageAdapterIsAdaptv() throws Exception {
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        String exchangeName = Lot49Constants.EXCHANGE_ADAPTV;
        String clickSimUrl = "ExchangeClickSimulatorUrl";
        
        SharedSetUp.createExchangeAdapterMock(exchangeName, Mockito.CALLS_REAL_METHODS);
        
        testDebug1(allAds, tags, exchangeName, clickSimUrl, Response.Status.OK);
    }

    @Ignore
    @Test
    public void negativeFlow_callsResumeWithSeeOtherIfExchnageAdapterWithoutMacros() throws Exception {
        // TODO in AdminSvc.debug1(): add '.build()' after 'Response.seeOther(new URI(debug2Url))' and then add 'return;' below
        // TODO in AdminSvc.debug1(): or just remove line with 'response.resume(Response.seeOther(new URI(debug2Url)));' and then remove this test and next one
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        String exchangeName = Lot49Constants.EXCHANGE_ADAPTV;
        String clickSimUrl = "ExchangeClickSimulatorUrl";
        
        SharedSetUp.createExchangeAdapterMock(exchangeName, Mockito.RETURNS_DEFAULTS);
        
        testDebug1(allAds, tags, exchangeName, clickSimUrl, Response.Status.SEE_OTHER);
    }

    @Test
    public void negativeFlow_callsResumeWithInternalServerErrorIfDebug2UrlIsBad() throws Exception {
        debugAdId = "Ad id with spaces"; // leads to bad debug2Url
        debugAd = SharedSetUp.createAdMock(debugAdId, "Debug ad", "This is debug ad");
        
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        String exchangeName = Lot49Constants.EXCHANGE_ADAPTV;
        String clickSimUrl = "ExchangeClickSimulatorUrl";
        
        SharedSetUp.createExchangeAdapterMock(exchangeName, Mockito.RETURNS_DEFAULTS);
        
        testDebug1(allAds, tags, exchangeName, clickSimUrl, Response.Status.INTERNAL_SERVER_ERROR);
    }

    private void testDebug1(Ad[] allAds, List<Tag> tags, String exchangeName, String clickSimUrl, Status expectedResponseStatus) throws Exception {
        configMocks("localhost", allAds, clickSimUrl);
        configAdMock(debugAd, tags);

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), any());
        
        svc.debug1(debugAdId, debugTagId, exchangeName, servletRequestMock, asyncResponseMock, "auth cookie");
        
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponseMock, times(1)).resume(responseCaptor.capture());

        Response responseValue = responseCaptor.getValue();
        
        switch (expectedResponseStatus){
            case OK:
                assertThat(Status.fromStatusCode(responseValue.getStatus()), equalTo(Response.Status.OK));

                PowerMockito.verifyStatic(times(1));
                ExchangeAdapterFactory.getExchangeAdapter(exchangeName);
                
                String responseString = responseValue.getEntity().toString();

                String debug2Url = Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + debugAdId + "/tag/" + debugTagId + "/debug2";
                assertThat(responseString, containsString(debug2Url));

                /// test of html-markup
                String htmlString = fixMarkupErrors(responseString);
                assertThat(WellFormedHtml.validate(htmlString), is(true));
                break;
            case SEE_OTHER:
                assertThat(Status.fromStatusCode(responseValue.getStatus()), equalTo(Response.Status.SEE_OTHER));
                break;
            case INTERNAL_SERVER_ERROR:
                assertThat(Status.fromStatusCode(responseValue.getStatus()), equalTo(Response.Status.INTERNAL_SERVER_ERROR));
                
                PowerMockito.verifyStatic(times(1));
                LogUtils.error(eq("Error"), any(Throwable.class));
                break;
            default:
                throw new Exception("Unexpected response status: " + expectedResponseStatus);
        }
    }

    private String fixMarkupErrors(String markup) {
        // TODO in AdminSvc.debug1(): would be super to correct html markup
        markup = markup.replace("READONLY", "READONLY=\"readonly\"");
        markup = markup.replace(" SIZE=80 ", " SIZE=\"80\" ");
        markup = markup.replace(" name=dummy ", " name=\"dummy\" ");
        markup = markup.replace("<BR>", "<BR />");
        markup = markup.replace("<br>", "<br />");
        markup = markup.replace("</UL>", "</ul>");
        markup = markup.replace("<hr>", "<hr />");
        
        Pattern p = Pattern.compile("<INPUT ([^\\/>]*)>");
        Matcher m = p.matcher(markup);
        markup = m.replaceAll("<INPUT $1 />");
        
        return markup;
    }
}
