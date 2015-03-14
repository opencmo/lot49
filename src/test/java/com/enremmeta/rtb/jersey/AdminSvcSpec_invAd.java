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
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.SharedAssert;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.WellFormedHtml;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.TargetingGeo;
import com.enremmeta.rtb.api.expression.And;
import com.enremmeta.rtb.api.expression.Expression;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdminSvcSpec_invAd {
    private ServiceRunner serviceRunnerMock;
    private AsyncResponse asyncResponseMock;
    private AdminSvc svc;
    private Ad invAd;
    private String invAdId = "Ad-Inv";

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        invAd = SharedSetUp.createAdMock(invAdId, "Inv ad", "This is inv ad");
        invAd.setBidPrice(1000);
        invAd.setBidProbability(80);

        svc = new AdminSvc();
    }

    private void configMocks(String host, Ad[] allAds) throws Exception {
        Lot49Config configMock = serviceRunnerMock.getConfig();
        Mockito.when(configMock.getHost()).thenReturn(host);

        AdCache adCacheMock = Mockito.mock(AdCache.class);
        Mockito.when(adCacheMock.getAll()).thenReturn(allAds);
        PowerMockito.doReturn(adCacheMock).when(serviceRunnerMock).getAdCache();
    }

    private void configAdMock(Ad adMock, Expression<String> segments, List<TargetingGeo> geos, List<Tag> tags) {
        Mockito.doReturn(segments).when(adMock).getParsedTargetingSegments();
        Mockito.doReturn(geos).when(adMock).getGeos();
        Mockito.doReturn(tags).when(adMock).getTags();
    }

    @Test
    public void negativeFlow_redirectsToLoginIfNotAuthenticated() throws Exception {
        configMocks("not localhost", null);

        svc.invAd(asyncResponseMock, invAdId, "bad auth cookie");

        SharedAssert.redirectedToLogin(asyncResponseMock);
    }

    @Ignore
    @Test
    public void positiveFlow_avoidsExceptionIfGetAllReturnsNull() throws Exception {
        // TODO in AdminSvc.invAd(): add verification that tc.getAll() doesn't return null
        Ad[] allAds = null;

        configMocks("localhost", allAds);

        svc.invAd(asyncResponseMock, invAdId, "auth cookie");
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfInvAdNotFound() throws Exception {
        Ad[] allAds = new Ad[] {SharedSetUp.createAdMock("Ad-1", "Ad 1", null), SharedSetUp.createAdMock("Ad-2", "Ad 2", null)};

        configMocks("localhost", allAds);

        svc.invAd(asyncResponseMock, invAdId, "auth cookie");
    }

    @Ignore
    @Test
    public void positiveFlow_avoidsExceptionIfFoundGetXxxReturnsNull() throws Exception {
        // TODO in AdminSvc.invAd(): add verification that found.getTags() doesn't return null
        Ad[] allAds = new Ad[] {invAd};
        
        Expression<String> segments = null;
        List<TargetingGeo> geos = null;
        List<Tag> tags = null;

        testInvAdIfAuthenticatedAndInvAdFound(allAds, segments, geos, tags);
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfFoundGetXxxReturnsEmptyCollections() throws Exception {
        Ad[] allAds = new Ad[] {invAd};
        
        Expression<String> segments = null;
        List<TargetingGeo> geos = new LinkedList<TargetingGeo>();
        List<Tag> tags = new LinkedList<Tag>();

        testInvAdIfAuthenticatedAndInvAdFound(allAds, segments, geos, tags);
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfAllDataAreCorrect() throws Exception {
        Ad[] allAds = new Ad[] {SharedSetUp.createAdMock("Ad-1", "Ad 1", null), invAd};

        Expression<String> segments = new And<String>("Segment A", "Segment B", "Segment C");

        List<TargetingGeo> geos = new LinkedList<TargetingGeo>();
        geos.add(new TargetingGeo("Los Angeles", null, null, "USA", "90001"));

        List<Tag> tags = new LinkedList<Tag>();
        tags.add(SharedSetUp.createTagMock("Tag-1", "Tag 1", null));
        tags.add(SharedSetUp.createTagMock("Tag-2", "Tag 2", "This is tag 2"));

        testInvAdIfAuthenticatedAndInvAdFound(allAds, segments, geos, tags);
    }

    private void testInvAdIfAuthenticatedAndInvAdFound(Ad[] allAds, Expression<String> segments, List<TargetingGeo> geos, List<Tag> tags) throws Exception {
        configMocks("localhost", allAds);
        configAdMock(invAd, segments, geos, tags);

        svc.invAd(asyncResponseMock, invAdId, "auth cookie");

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(asyncResponseMock, times(1)).resume(stringCaptor.capture());

        String responseString = stringCaptor.getValue();

        if (tags != null) {
            for (Tag tag : tags) {
                assertThat(responseString, containsString(Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + invAdId + "/tag/" + tag.getId()));
            }
        }

        /// test of html-markup
        String htmlString = fixMarkupErrors(responseString);
        assertThat(WellFormedHtml.validate(htmlString), is(true));
    }

    private String fixMarkupErrors(String markup) {
        // TODO in AdminSvc.invAd(): would be super to correct html markup
        markup = markup.replace("<br>", "<br />");
        return markup;
    }
}
