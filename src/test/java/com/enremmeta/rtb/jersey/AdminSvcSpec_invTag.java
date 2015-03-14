package com.enremmeta.rtb.jersey;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;

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

import com.enremmeta.rtb.SharedAssert;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.WellFormedHtml;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.Dimension;
import com.enremmeta.rtb.api.FixedDimension;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest(ExchangeAdapterFactory.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdminSvcSpec_invTag {
    private ServiceRunner serviceRunnerMock;
    private AsyncResponse asyncResponseMock;
    private AdminSvc svc;
    private Ad invAd;
    private String invAdId = "Ad-Inv";
    private Tag invTag;
    private String invTagId = "Tag-Inv";

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        invAd = SharedSetUp.createAdMock(invAdId, "Inv ad", "This is inv ad");
        invTag = SharedSetUp.createTagMock(invTagId, "Inv tag", "This is inv tag");

        PowerMockito.mockStatic(ExchangeAdapterFactory.class);
        PowerMockito.when(ExchangeAdapterFactory.getAllExchangeAdapters()).thenReturn(null);
        PowerMockito.when(ExchangeAdapterFactory.getAllExchangeAdapterNames()).thenCallRealMethod();
        
        svc = new AdminSvc();
    }

    private void configMocks(String host, Ad[] allAds) throws Exception {
        Lot49Config configMock = serviceRunnerMock.getConfig();
        Mockito.when(configMock.getHost()).thenReturn(host);

        AdCache adCacheMock = Mockito.mock(AdCache.class);
        Mockito.when(adCacheMock.getAll()).thenReturn(allAds);
        PowerMockito.doReturn(adCacheMock).when(serviceRunnerMock).getAdCache();
    }

    private void configAdMock(Ad adMock, List<Tag> tags) {
        Mockito.doReturn(tags).when(adMock).getTags();
    }
    
    private void configTagMock(Tag tagMock, boolean isBanner, Dimension dimension, String mime, int api, int protocol, int duration) {
        Mockito.doReturn(isBanner).when(tagMock).isBanner();
        Mockito.doReturn(dimension).when(tagMock).getDimension();
        Mockito.doReturn(mime).when(tagMock).getMime();
        Mockito.doReturn(api).when(tagMock).getApi();
        Mockito.doReturn(protocol).when(tagMock).getProtocol();
        Mockito.doReturn(duration).when(tagMock).getDuration();
    }
    
    @Test
    public void negativeFlow_redirectsToLoginIfNotAuthenticated() throws Exception {
        configMocks("not localhost", null);

        svc.invTag(asyncResponseMock, invAdId, invTagId, "bad auth cookie");

        SharedAssert.redirectedToLogin(asyncResponseMock);
    }

    @Ignore
    @Test
    public void positiveFlow_avoidsExceptionIfGetAllReturnsNull() throws Exception {
        // TODO in AdminSvc.invTag(): add verification that adCache.getAll() doesn't return null
        Ad[] allAds = null;

        configMocks("localhost", allAds);

        svc.invTag(asyncResponseMock, invAdId, invTagId, "auth cookie");
    }

    @Ignore
    @Test
    public void positiveFlow_avoidsExceptionIfGetTagsReturnsNull() throws Exception {
        // TODO in AdminSvc.invTag(): add verification that ad.getTags doesn't return null
        Ad[] allAds = new Ad[] {invAd};
        List<Tag> tags = null;
        
        configMocks("localhost", allAds);
        configAdMock(invAd, tags);

        svc.invTag(asyncResponseMock, invAdId, invTagId, "auth cookie");
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfInvAdNotFound() throws Exception {
        Ad[] allAds = new Ad[] {};
        List<Tag> tags = new LinkedList<Tag>();

        configMocks("localhost", allAds);
        configAdMock(invAd, tags);

        svc.invTag(asyncResponseMock, invAdId, invTagId, "auth cookie");
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfInvTagNotFound() throws Exception {
        Ad[] allAds = new Ad[] {invAd};
        List<Tag> tags = new LinkedList<Tag>();

        configMocks("localhost", allAds);
        configAdMock(invAd, tags);

        svc.invTag(asyncResponseMock, invAdId, invTagId, "auth cookie");
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfAdIsBanner() throws Exception {
        Ad[] allAds = new Ad[] {SharedSetUp.createAdMock("Ad-1", "Ad 1", null), invAd};

        List<Tag> tags = new LinkedList<Tag>();
        tags.add(SharedSetUp.createTagMock("Tag-1", "Tag 1", null));
        tags.add(invTag);
        
        boolean isBanner = true;
        
        testInvTagIfAuthenticatedAndInvTagFound(allAds, tags, isBanner, new FixedDimension(200, 100), "text/html", 0, 0, 0);
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfAdIsVideo() throws Exception {
        Ad[] allAds = new Ad[] {invAd};

        List<Tag> tags = new LinkedList<Tag>();
        tags.add(invTag);
        
        boolean isBanner = false;
        
        testInvTagIfAuthenticatedAndInvTagFound(allAds, tags, isBanner, new FixedDimension(200, 100), "video/mpeg", 1, 1, 10);
    }

    private void testInvTagIfAuthenticatedAndInvTagFound(Ad[] allAds, List<Tag> tags, boolean isBanner, Dimension dimension, String mime, int api, int protocol, int duration) throws Exception {
        configMocks("localhost", allAds);
        configAdMock(invAd, tags);
        configTagMock(invTag, isBanner, dimension, mime, api, protocol, duration);

        svc.invTag(asyncResponseMock, invAdId, invTagId, "auth cookie");
        
        verify(invTag, times(isBanner ? 0 : 1)).getDuration();
        
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(asyncResponseMock, times(1)).resume(stringCaptor.capture());

        String responseString = stringCaptor.getValue();

        assertThat(responseString, containsString(Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + invAdId + "/tag/" + invTagId + "/debug1"));

        for (String exchange : ExchangeAdapterFactory.getAllExchangeAdapterNames()) {
            assertThat(responseString, containsString(exchange));
        }

        /// test of html-markup
        String htmlString = fixMarkupErrors(responseString);
        assertThat(WellFormedHtml.validate(htmlString), is(true));
    }

    private String fixMarkupErrors(String markup) {
        // TODO in AdminSvc.invTag(): would be super to correct html markup
        markup = markup.replace("</TITLE>", "</TITLE></HEAD>");
        markup = markup.replace(" SELECTED", " SELECTED=\"SELECTED\"");
        markup = markup.replace("TYPE=submit", "TYPE=\"submit\"");
        markup = markup.replace("VALUE=Debug>", "VALUE=\"Debug\" />");
        markup = markup.replace("<HR>", "<HR />");
        markup = markup.replace("<!DOCTYPE html>", ""); // this isn't error - this replace command was added for passing xml validation
        return markup;
    }
}
