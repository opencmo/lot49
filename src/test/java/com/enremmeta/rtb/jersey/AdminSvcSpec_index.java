package com.enremmeta.rtb.jersey;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdminSvcSpec_index {
    private ServiceRunner serviceRunnerMock;
    private AsyncResponse asyncResponseMock;
    private AdminSvc svc;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        svc = new AdminSvc();
    }

    private void configMocks(String host, Ad[] validAds, List<Ad> zeroBudgetAds, Map<Ad, String> invalidAds, Map<String, String> uncompilableAds)
                    throws Exception {
        Lot49Config configMock = serviceRunnerMock.getConfig();
        Mockito.when(configMock.getHost()).thenReturn(host);

        AdCache adCacheMock = Mockito.mock(AdCache.class);
        Mockito.when(adCacheMock.getAll()).thenReturn(validAds);
        Mockito.when(adCacheMock.getZeroBudgetAds()).thenReturn(zeroBudgetAds);
        Mockito.when(adCacheMock.getInvalidAds()).thenReturn(invalidAds);
        Mockito.when(adCacheMock.getUncompilableAds()).thenReturn(uncompilableAds);
        PowerMockito.doReturn(adCacheMock).when(serviceRunnerMock).getAdCache();
    }

    @Test
    public void negativeFlow_redirectsToLoginIfNotAuthenticated() throws Exception {
        configMocks("not localhost", null, null, null, null);

        svc.index(asyncResponseMock, null);

        SharedAssert.redirectedToLogin(asyncResponseMock);
    }

    @Ignore
    @Test
    public void positiveFlow_avoidsExceptionIfGetAllReturnsNull() throws Exception {
        // TODO in AdminSvc.index(): add verification that adCache.getAll() doesn't return null
        testIndexIfAuthenticated(null, null, null, null);
    }

    @Ignore
    @Test
    public void positiveFlow_avoidsExceptionIfGetXxxAdsReturnsNull() throws Exception {
        // TODO in AdminSvc.index(): add verification that adCache.getZeroBudgetAds(),
        // adCache.getInvalidAds() or adCache.getUncompilableAds don't return null
        testIndexIfAuthenticated(new Ad[] {}, null, null, null);
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfThereAreNoAds() throws Exception {
        testIndexIfAuthenticated(new Ad[] {}, new ArrayList<Ad>(), new HashMap<Ad, String>(), new HashMap<String, String>());
    }

    @Test
    public void positiveFlow_callsResumeWithExpectedResponseIfThereAreSomeAds() throws Exception {
        Ad[] validAds = new Ad[] {SharedSetUp.createAdMock("ValidAd-1", "Valid ad 1", null),
                        SharedSetUp.createAdMock("ValidAd-2", "Valid ad 2", "This is test of valid ad")};

        List<Ad> zeroBudgetAds = new ArrayList<Ad>();
        zeroBudgetAds.add(SharedSetUp.createAdMock("ZeroBudgetAd-1", "Zero budget ad 1", "This is test of zero budget ad"));

        Map<Ad, String> invalidAds = new HashMap<Ad, String>();
        invalidAds.put(SharedSetUp.createAdMock("InvalidAd-1", "Invalid ad 1", "This is test of invalid ad"), "Error on invalid ad");

        Map<String, String> uncompilableAds = new HashMap<String, String>();
        uncompilableAds.put("UncompilableAd-1", "Error in uncompilabl ad");

        testIndexIfAuthenticated(validAds, zeroBudgetAds, invalidAds, uncompilableAds);
    }

    private void testIndexIfAuthenticated(Ad[] validAds, List<Ad> zeroBudgetAds, Map<Ad, String> invalidAds, Map<String, String> uncompilableAds)
                    throws Exception {
        configMocks("localhost", validAds, zeroBudgetAds, invalidAds, uncompilableAds);

        svc.index(asyncResponseMock, null);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(asyncResponseMock, times(1)).resume(stringCaptor.capture());
        String responseString = stringCaptor.getValue();

        /// valid ads
        if (validAds != null) {
            for (Ad ad : validAds) {
                assertThat(responseString, containsString(ad.getId()));
            }
        }

        /// zero budget ads
        if (zeroBudgetAds != null) {
            assertThat(responseString.contains("Ads with 0 budget"), equalTo(zeroBudgetAds.size() > 0));

            for (Ad ad : zeroBudgetAds) {
                assertThat(responseString, containsString(Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + ad.getId() + "/setBudget"));
            }
        }

        /// invalid ads
        if (invalidAds != null) {
            assertThat(responseString.contains("Invalid ads"), equalTo(invalidAds.size() > 0));

            for (Ad ad : invalidAds.keySet()) {
                assertThat(responseString, containsString(ad.getId()));
            }
        }

        /// uncompilable ads
        if (uncompilableAds != null) {
            assertThat(responseString.contains("Uncompilable ad scripts"), equalTo(uncompilableAds.size() > 0));

            for (String ad : uncompilableAds.keySet()) {
                assertThat(responseString, containsString(ad));
            }
        }

        /// test for html-markup of response (is well formed ?)
        String htmlString = fixMarkupErrors(responseString);
        assertThat(WellFormedHtml.validate(htmlString), is(true));
    }

    private String fixMarkupErrors(String markup) {
        // TODO in AdminSvc.index(): would be super to correct html markup
        markup = markup.replace("<ul.>", "<ul>");
        markup = markup.replace("border=1", "border=\"1\"");
        markup = markup.replace("<INPUT NAME=\"amount\" VALUE=\"1000000000\">", "<INPUT NAME=\"amount\" VALUE=\"1000000000\" />");
        markup = markup.replace("<INPUT TYPE=\"submit\" VALUE=\"Set\">", "<INPUT TYPE=\"submit\" VALUE=\"Set\" />");
        return markup;
    }
}
