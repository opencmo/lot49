package com.enremmeta.rtb.jersey;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.ReflectionUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.proto.adaptv.AdaptvConfig;
import com.enremmeta.rtb.proto.testexchange.Test2ExchangeAdapter;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(value = Theories.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({Utils.class, ObjectMapper.class, ExchangeAdapterFactory.class, Test2ExchangeAdapter.class, AdaptvAdapter.class, Response.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdminSvcSpec_debug2 {
    private ObjectMapper utilsMapper;
    private ServiceRunner serviceRunnerMock;
    private HttpServletRequest servletRequestMock;
    private AsyncResponse asyncResponseMock;
    private String authCookie = "auth cookie";
    private AdminSvc svc;
    private Ad debugAd;
    private String debugAdId = "Ad-Debug";
    private Tag debugTag;
    private String debugTagId = "Tag-Debug";
    private Bid debugBid;
    private String debugBidId = "Bid-Debug";
    private String debugImpId = "Imp-Debug";
    private ExchangeAdapter<?, ?> adapterMock;
    
    @Before
    public void setUp() throws Exception {
        utilsMapper = Utils.MAPPER;
        
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        servletRequestMock = Mockito.mock(HttpServletRequest.class);
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        debugAd = SharedSetUp.createAdMock(debugAdId, "Debug ad", "This is debug ad");
        debugTag = SharedSetUp.createTagMock(debugTagId, "Debug tag", "This is debug tag");
        
        debugBid = createBid(debugBidId, debugImpId, debugAdId);
        debugBid.setPrice(1f);
        debugBid.setAdm("Ad markup: Click = {CLICK}; Winner price = {WP}");
        debugBid.setNurl("Win notice URL");

        svc = new AdminSvc();
    }

    @After
    public void tearDown() throws Exception {
        if (utilsMapper != Utils.MAPPER) {
            ReflectionUtils.setFinalStatic(Utils.class.getField("MAPPER"), utilsMapper);
        }
    }
    
    private Bid createBid(String id, String impId, String adId) {
        Bid bidMock = new Bid();
        
        bidMock.setId(id);
        bidMock.setImpid(impId);
        bidMock.setCid(adId);
        
        return bidMock;
    }
    
    @SuppressWarnings("unchecked")
    private void configMocks(String host, Ad[] allAds, String exchangeName, boolean isNurlRequired) throws Throwable {
        Lot49Config configMock = serviceRunnerMock.getConfig();
        Mockito.when(configMock.getHost()).thenReturn(host);

        Orchestrator orchestratorMock = Mockito.mock(Orchestrator.class);
        PowerMockito.doReturn(orchestratorMock).when(serviceRunnerMock).getOrchestrator();

        DaoShortLivedMap<CacheableWebResponse> cwrMapMock = Mockito.mock(DaoShortLivedMap.class);
        DaoShortLivedMap<String> nurlMapMock = Mockito.mock(DaoShortLivedMap.class);
        
        AdCache adCacheMock = Mockito.mock(AdCache.class);
        Mockito.when(adCacheMock.getAll()).thenReturn(allAds);
        Mockito.when(adCacheMock.getCwrMap()).thenReturn(cwrMapMock);
        Mockito.when(adCacheMock.getNurlMap()).thenReturn(nurlMapMock);
        PowerMockito.doReturn(adCacheMock).when(serviceRunnerMock).getAdCache();
        
        /// additional mocks
        configMocksForAdaptvAdapter();
        
        adapterMock = createExchangeAdapterMock(exchangeName, Mockito.CALLS_REAL_METHODS, isNurlRequired);
    }

    private void configMocksForAdaptvAdapter() {
        Lot49Config configMock = serviceRunnerMock.getConfig();
        ExchangesConfig exchangesConfigMock = Mockito.mock(ExchangesConfig.class);
        AdaptvConfig adaptvConfigMock = Mockito.mock(AdaptvConfig.class);

        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfigMock);
        Mockito.when(exchangesConfigMock.getAdaptv()).thenReturn(adaptvConfigMock);
        Mockito.when(adaptvConfigMock.getBuyerId()).thenReturn("buyerId");
    }

    private ExchangeAdapter<?, ?> createExchangeAdapterMock(String exchangeName, Answer<Object> defaultAnswer, boolean isNurlRequired) throws Throwable {
        ExchangeAdapter<?, ?> adapterMock = SharedSetUp.createExchangeAdapterMock(exchangeName, defaultAnswer);

        if (adapterMock != null) {
            PowerMockito.doAnswer(i -> { return i.getArgumentAt(1, OpenRtbResponse.class); }).when(adapterMock).convertResponse(any(), any());
            PowerMockito.doCallRealMethod().when(adapterMock).getResponseMediaType();
            PowerMockito.doReturn(isNurlRequired).when(adapterMock).isNurlRequired();
        }
        
        return adapterMock;
    }

    private void configServletRequestMock(String exchangeName, String macroType, String macroKey, String macroVal) {
        String macroPrefix = exchangeName + macroType;
        Mockito.when(servletRequestMock.getParameter(macroPrefix + "key")).thenReturn(macroKey);
        Mockito.when(servletRequestMock.getParameter(macroPrefix + "val")).thenReturn(macroVal);
    }
    
    private void configAdMock(Ad adMock, List<Tag> tags) {
        Mockito.doReturn(tags).when(adMock).getTags();
    }

    private void configTagMock(Tag tagMock, boolean isBanner, Bid bid) {
        Mockito.doReturn(isBanner).when(tagMock).isBanner();
        Mockito.doReturn(isBanner ? MediaType.TEXT_XML : MediaType.APPLICATION_OCTET_STREAM).when(tagMock).getMime();
        Mockito.doReturn(bid).when(tagMock).getBid(any(OpenRtbRequest.class), any(Impression.class));
    }
    
    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfAdNotFound() throws Throwable {
        Ad[] allAds = new Ad[] {};
        List<Tag> tags = null;
        
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        
        configMocks("localhost", allAds, exchangeName, false);
        configAdMock(debugAd, tags);

        svc.debug2(debugAdId, debugTagId, exchangeName, AdminSvc.DEBUG_RESPONSE_TYPE_FULL_BINARY, AdminSvc.DEBUG_ACTION_TYPE_DISPLAY, 
                        servletRequestMock, asyncResponseMock, authCookie);
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfTagNotFound() throws Throwable {
        Ad[] allAds = new Ad[] {debugAd};
        List<Tag> tags = new LinkedList<Tag>();
        
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        
        configMocks("localhost", allAds, exchangeName, false);
        configAdMock(debugAd, tags);

        svc.debug2(debugAdId, debugTagId, exchangeName, null, null, servletRequestMock, asyncResponseMock, authCookie);
    }

    @Ignore
    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfExchangeIsNull() throws Throwable {
        // TODO in AdminSvc.debug2(): throw WebApplicationException if exchange == null or exchange.trim().equals("")
        boolean isBanner = true;
        String exchangeName = null;
        boolean isNurlRequired = false;
        
        testDebug2(isBanner, exchangeName, isNurlRequired);
    }

    @Ignore
    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfExchangeAdapterNotFound() throws Throwable {
        // TODO in AdminSvc.debug2(): add try...catch block around command 'adapter = ExchangeAdapterFactory.getExchangeAdapter(exchange);'
        boolean isBanner = true;
        String exchangeName = "Unknown exchange adapter";
        boolean isNurlRequired = false;
        
        testDebug2(isBanner, exchangeName, isNurlRequired);
    }

    @Ignore
    @Test(timeout = 10000)
    public void positiveFlow_avoidsHangProgramWithSomeCombinationsOfMacro() throws Throwable {
        // TODO in AdminSvc.debug2(): some combinations of macro's key and macro's value (e.g. key = "{CLICK}", value = "{CLICK}AAA") can cause hang of program
        boolean isBanner = true;
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        boolean isNurlRequired = false;
        
        configServletRequestMock(exchangeName, "_click_macro_", "{CLICK}", "{CLICK}AAA");
        
        testDebug2(isBanner, exchangeName, isNurlRequired);
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfErrorOccursInConvertResponse() throws Throwable {
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        boolean isBanner = true;
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        boolean isNurlRequired = false;
        
        configMocks("localhost", allAds, exchangeName, isNurlRequired);
        configAdMock(debugAd, tags);
        configTagMock(debugTag, isBanner, debugBid);
        
        PowerMockito.doThrow(new RuntimeException("Error in convertResponse()")).when(adapterMock).convertResponse(any(), any());

        svc.debug2(debugAdId, debugTagId, exchangeName, AdminSvc.DEBUG_RESPONSE_TYPE_FULL_BINARY, AdminSvc.DEBUG_ACTION_TYPE_DISPLAY, 
                        servletRequestMock, asyncResponseMock, authCookie);
    }

    @Test
    public void positiveFlow_returnsExpectedResponseIfResponseTypeIsFullJsonAndConvertResponseReturnsString() throws Throwable {
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        String exchangeName = Lot49Constants.EXCHANGE_ADAPTV; /// method convertResponse() of exchange adapter should return String
        Class<? extends ExchangeAdapter<?,?>> adapterClass = AdaptvAdapter.class;
        
        boolean isBanner = true;
        boolean isNurlRequired = false;
        String responseType = AdminSvc.DEBUG_RESPONSE_TYPE_FULL_JSON;

        configMocks("localhost", allAds, exchangeName, isNurlRequired);
        configAdMock(debugAd, tags);
        configTagMock(debugTag, isBanner, debugBid);
        
        PowerMockito.doReturn(adapterClass.newInstance()).when(ExchangeAdapterFactory.class);
        ExchangeAdapterFactory.getExchangeAdapter(exchangeName);

        PowerMockito.spy(Response.class);
        
        svc.debug2(debugAdId, debugTagId, exchangeName, responseType, AdminSvc.DEBUG_ACTION_TYPE_DISPLAY, servletRequestMock, asyncResponseMock, authCookie);
        
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponseMock, times(1)).resume(responseCaptor.capture());

        Response responseValue = responseCaptor.getValue();
        
        PowerMockito.verifyStatic(times(1));
        Response.ok();
        
        String analizedString = responseValue.hasEntity() ? responseValue.getEntity().toString() : "";
        String search = debugBid.getAdm();
        
        if (search != null) {
            assertThat(analizedString.indexOf(search) >= 0, is(true));
        }
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfResponseTypeIsFullJsonAndErrorOccursInWriteValueAsString() throws Throwable {
        boolean isBanner = true;
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        boolean isNurlRequired = false;
        String responseType = AdminSvc.DEBUG_RESPONSE_TYPE_FULL_JSON;
        
        ObjectMapper objectMapperMock = PowerMockito.mock(ObjectMapper.class);
        PowerMockito.when(objectMapperMock.writeValueAsString(any())).thenThrow(new RuntimeException("Error in writeValueAsString()"));
        ReflectionUtils.setFinalStatic(Utils.class.getField("MAPPER"), objectMapperMock);

        testDebug2(isBanner, exchangeName, isNurlRequired, responseType, AdminSvc.DEBUG_ACTION_TYPE_DISPLAY);
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfResponseTypeIsTagContainerAndTagIsVideoAndBaseUrlIsBad() throws Throwable {
        boolean isBanner = false;
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        boolean isNurlRequired = false;
        String responseType = AdminSvc.DEBUG_RESPONSE_TYPE_TAG_CONTAINER;

        Lot49Config configMock = serviceRunnerMock.getConfig();
        Mockito.when(configMock.getBaseUrl()).thenReturn("Bad base url");
        
        testDebug2(isBanner, exchangeName, isNurlRequired, responseType, AdminSvc.DEBUG_ACTION_TYPE_DISPLAY);
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfResponseTypeIsUnknown() throws Throwable {
        String responseType = "Unknown response type";

        testDebug2(true, Lot49Constants.EXCHANGE_TEST2, false, responseType, AdminSvc.DEBUG_ACTION_TYPE_DISPLAY);
    }

    public static @DataPoints String[] responseTypes = new String[] {
                    AdminSvc.DEBUG_RESPONSE_TYPE_FULL_BINARY,
                    AdminSvc.DEBUG_RESPONSE_TYPE_FULL_TEXT,
                    AdminSvc.DEBUG_RESPONSE_TYPE_FULL_JSON,
                    AdminSvc.DEBUG_RESPONSE_TYPE_TAG_BINARY,
                    AdminSvc.DEBUG_RESPONSE_TYPE_TAG_TEXT,
                    AdminSvc.DEBUG_RESPONSE_TYPE_TAG_CACHED,
                    AdminSvc.DEBUG_RESPONSE_TYPE_TAG_CONTAINER,
                    null
    };
    
    @Theory
    @Test
    public void positiveFlow_returnsExpectedResponseIfTagIsBanner(String responseType) throws Throwable {
        boolean isBanner = true;
        String exchangeName = Lot49Constants.EXCHANGE_ADAPTV;
        boolean isNurlRequired = false;
        
        testDebug2(isBanner, exchangeName, isNurlRequired, responseType, AdminSvc.DEBUG_ACTION_TYPE_DISPLAY);
    }

    @Theory
    @Test
    public void positiveFlow_returnsExpectedResponseIfTagIsVideo(String responseType) throws Throwable {
        boolean isBanner = false;
        String exchangeName = Lot49Constants.EXCHANGE_TEST2;
        boolean isNurlRequired = true;
        
        testDebug2(isBanner, exchangeName, isNurlRequired, responseType, null);
    }

    private void testDebug2(boolean isBanner, String exchangeName, boolean isNurlRequired) throws Throwable {
        testDebug2(isBanner, exchangeName, isNurlRequired, AdminSvc.DEBUG_RESPONSE_TYPE_FULL_BINARY, AdminSvc.DEBUG_ACTION_TYPE_DISPLAY);
    }

    private void testDebug2(boolean isBanner, String exchangeName, boolean isNurlRequired, String responseType, String actionType) throws Throwable {
        Ad[] allAds = new Ad[] {debugAd};
        
        List<Tag> tags = new LinkedList<Tag>();
        tags.add(debugTag);
        
        String wp_macro_key = "{WP}";
        String wp_macro_val = "12.34";
        
        configMocks("localhost", allAds, exchangeName, isNurlRequired);
        configAdMock(debugAd, tags);
        configTagMock(debugTag, isBanner, debugBid);
        configServletRequestMock(exchangeName, "_wp_macro_", wp_macro_key, wp_macro_val);

        PowerMockito.spy(Response.class);
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), any());
        
        svc.debug2(debugAdId, debugTagId, exchangeName, responseType, actionType, servletRequestMock, asyncResponseMock, authCookie);
        
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponseMock, times(1)).resume(responseCaptor.capture());

        Response responseValue = responseCaptor.getValue();
        
        int expectedResponseOkInvocations = 1;
        int expectedResponseSeeOtherInvocations = 0;
        int expectedCwrMapPutInvocations = 0;
        
        String analizedString = responseValue.hasEntity() ? responseValue.getEntity().toString() : "";
        String search = !isNurlRequired ? debugBid.getAdm().replace(wp_macro_key, wp_macro_val) : "";
        
        if (responseType == null) {
            responseType = AdminSvc.DEBUG_RESPONSE_TYPE_FULL_JSON;
        }

        switch (responseType) {
            case AdminSvc.DEBUG_RESPONSE_TYPE_FULL_BINARY:
                search = null;
                break;
            case AdminSvc.DEBUG_RESPONSE_TYPE_FULL_TEXT:
                search = null;
                break;
            case AdminSvc.DEBUG_RESPONSE_TYPE_TAG_CACHED:
                expectedResponseOkInvocations = 2;
                expectedCwrMapPutInvocations = 1;
                
                analizedString = analizedString.toLowerCase();
                search = "<a href=";
                break;
            case AdminSvc.DEBUG_RESPONSE_TYPE_TAG_CONTAINER:
                expectedResponseOkInvocations = isBanner ? 2 : 1;
                expectedResponseSeeOtherInvocations = isBanner ? 0 : 1;
                expectedCwrMapPutInvocations = 1;
                
                analizedString = analizedString.toLowerCase();
                search = isBanner ? "<iframe src=" : null;
                break;
            default:
        }
        
        PowerMockito.verifyStatic(times(expectedResponseOkInvocations));
        Response.ok();
        
        PowerMockito.verifyStatic(times(expectedResponseSeeOtherInvocations));
        Response.seeOther(any(URI.class));
        
        Mockito.verify(Bidder.getInstance().getAdCache().getCwrMap(), times(expectedCwrMapPutInvocations)).put(any(), any());
        
        if (search != null) {
            assertThat(analizedString.indexOf(search) >= 0, is(true));
        }
    }
}
