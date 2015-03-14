package com.enremmeta.rtb.api.proto.openrtb.lot49;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import com.enremmeta.rtb.RtbBean;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.MarkupType;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.TagImpl;
import com.enremmeta.rtb.api.apps.AugmentedRequestData;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Deal;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.PMP;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.jersey.StatsSvc;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.spi.providers.ProviderInfoReceived;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindConfig;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindFacade;
import com.enremmeta.util.BidderCalendar;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal;
import com.maxmind.geoip2.record.MaxMind;

/**
 * Information augmented by Lot49 into the bid request.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © Enremmeta LLC (www.enremmeta.com) 2014-2015. All Rights Reserved. This code
 *         is licensed under Alfero GPL 3.0 (http://www.gnu.org/licenses/agpl-3.0.html)
 *
 */
public final class Lot49Ext implements RtbBean {
    private Set<MarkupType> excludedMarkups;
    private String modUid;

    public String getModUid() {
        return modUid;
    }

    private boolean rawRequestAlreadyLogged = false;

    public boolean isRawRequestAlreadyLogged() {
        return rawRequestAlreadyLogged;
    }

    public void setRawRequestAlreadyLogged(boolean rawRequestAlreadyLogged) {
        this.rawRequestAlreadyLogged = rawRequestAlreadyLogged;
    }

    private boolean privateDeals;

    /**
     * Whether this request has private deals, and we have to deal accordingly.
     * 
     * @see PMP
     * @see Deal
     * @see DirectDeal
     * 
     * @return the value of privateDeals
     */
    public boolean isPrivateDeals() {
        return privateDeals;
    }

    public void setPrivateDeals(boolean privateDeals) {
        this.privateDeals = privateDeals;
    }

    public void setModUid(String modUid) {
        this.modUid = modUid;
    }

    public Set<MarkupType> getExcludedMarkups() {
        return excludedMarkups;
    }

    public void setExcludedMarkups(Set<MarkupType> excludedMarkups) {
        this.excludedMarkups = excludedMarkups;
    }

    private boolean ssl;

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public Lot49Ext() {
        super();
    }

    private boolean lot49Test;

    private Ad[] bid2;
    private Ad[] all;

    public boolean isLot49Test() {
        return lot49Test;
    }

    public void setLot49Test(boolean lot49Test) {
        this.lot49Test = lot49Test;
    }

    /**
     * For tests only
     * 
     * @return the list of ads
     */
    public Ad[] getBid2() {
        return bid2;
    }

    public void setBid2(Ad[] bid2) {
        this.bid2 = bid2;
    }

    /**
     * For tests only
     * 
     * @return the list of ads
     */
    public Ad[] getAll() {
        return all;
    }

    public void setAll(Ad[] all) {
        this.all = all;
    }

    private Map<String, ProviderInfoReceived> providerInfo =
                    new HashMap<String, ProviderInfoReceived>();

    public Map<String, ProviderInfoReceived> getProviderInfo() {
        return providerInfo;
    }

    public void setProviderInfo(Map<String, ProviderInfoReceived> providerInfo) {
        this.providerInfo = providerInfo;
    }

    private String browserName;
    private String browserFamily;

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserFamily() {
        return browserFamily;
    }

    public void setBrowserFamily(String browserFamily) {
        this.browserFamily = browserFamily;
    }

    private long processingTime;

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    private Lot49SubscriptionData subscriptionData;

    public Lot49SubscriptionData getSubscriptionData() {
        return subscriptionData;
    }

    public void setSubscriptionData(Lot49SubscriptionData subscriptionData) {
        this.subscriptionData = subscriptionData;
    }

    private boolean debug;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private Map<String, String> optoutReasons = new HashMap<String, String>();

    public Map<String, String> getOptoutReasons() {
        return optoutReasons;
    }

    public void setOptoutReasons(Map<String, String> optoutReasons) {
        this.optoutReasons = optoutReasons;
    }

    private String receivedBuyerUid;

    public String getReceivedBuyerUid() {
        return receivedBuyerUid;
    }

    public void setReceivedBuyerUid(String receivedBuyerUid) {
        this.receivedBuyerUid = receivedBuyerUid;
    }

    /**
     * In
     * {@link TagImpl#getTag(com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest, com.enremmeta.rtb.api.proto.openrtb.Impression, Bid, boolean)}
     * we generate tag, but we don't want to put it into the map until we're sure we'll return it.
     * So we keep it here.
     * 
     * @return the map of bidId to tag text
     */
    public Map<String, String> getBidIdToTagText() {
        return bidIdToTagText;
    }

    private Map<String, Tag> bidIdToTagObject = new HashMap<String, Tag>();

    private Map<String, Ad> bidRequestIdToAdObject = new HashMap<String, Ad>();


    public Map<String, Ad> getBidRequestIdToAdObject() {
        return bidRequestIdToAdObject;
    }

    public void setBidRequestIdToAdObject(Map<String, Ad> bidRequestIdToAdObject) {
        this.bidRequestIdToAdObject = bidRequestIdToAdObject;
    }

    public void setBidIdToTagText(Map<String, String> bidIdToTag) {
        this.bidIdToTagText = bidIdToTag;
    }

    public Map<String, Tag> getBidIdToTagObject() {
        return bidIdToTagObject;
    }

    public void setBidIdToTagObject(Map<String, Tag> bidIdToTagObject) {
        this.bidIdToTagObject = bidIdToTagObject;
    }

    private Map<String, String> bidIdToTagText = new HashMap<String, String>();

    private boolean forceCookieReset;

    /**
     * Whether to use
     * {@link StatsSvc#proust(UriInfo, String, String, String, String, String, String, String, String, String, String, String, String, String, String, HttpServletRequest, String, String, String)
     * Proust} to reset the user's cookie.
     * 
     * @return the value of forceCookieReset
     */
    public boolean isForceCookieReset() {
        return forceCookieReset;
    }

    private boolean forceCookieResync;

    public boolean isForceCookieResync() {
        return forceCookieResync;
    }


    public void setForceCookieResync(boolean forceCookieResync) {
        this.forceCookieResync = forceCookieResync;
    }

    public void setForceCookieReset(boolean forceCookieReset) {
        this.forceCookieReset = forceCookieReset;
    }

    private Lot49CustomData lot49CustomData = new Lot49CustomData();

    public Lot49CustomData getLot49CustomData() {
        return lot49CustomData;
    }

    private ExchangeAdapter adapter;

    public ExchangeAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(ExchangeAdapter adapter) {
        this.adapter = adapter;
    }

    public void setLot49CustomData(Lot49CustomData lot49CustomData) {
        this.lot49CustomData = lot49CustomData;
    }

    private String remoteHost;
    private int remotePort;
    private String remoteAddr;

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    private boolean rawRequestLogged = false;

    public boolean isRawRequestLogged() {
        return rawRequestLogged;
    }

    public void setRawRequestLogged(boolean rawRequestLogged) {
        this.rawRequestLogged = rawRequestLogged;
    }

    private List<String> comments = new ArrayList<String>();

    /**
     * Something to log.
     * 
     * @return the list of comments
     */
    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    private boolean nurlRequired = false;

    /**
     * Whether the exchange can accept {@link Bid#getAdm() ad markup} directly in the bid response,
     * or a separate {@link Bid#getNurl() URL} is required.
     * 
     * @return the value of nurlRequired
     */
    public boolean isNurlRequired() {
        return nurlRequired;
    }

    public void setNurlRequired(boolean nurlRequired) {
        this.nurlRequired = nurlRequired;

    }

    private String xForwardedFor;

    /**
     * To be set from the <a href="http://en.wikipedia.org/wiki/X-Forwarded-For">X-Forwarded-For</a>
     * header in request handler.
     * 
     * @return the value of xForwardedFor
     */
    public String getxForwardedFor() {
        return xForwardedFor;
    }

    public void setxForwardedFor(String xForwardedFor) {
        this.xForwardedFor = xForwardedFor;
    }

    private int dma;

    private String realIpHeader;

    public String getRealIpHeader() {
        return realIpHeader;
    }

    public void setRealIpHeader(String realIpHeader) {
        this.realIpHeader = realIpHeader;
    }

    private Lot49ExtRemote lot49ExtRemote = new Lot49ExtRemote();

    public Lot49ExtRemote getLot49ExtRemote() {
        return lot49ExtRemote;
    }

    public void setLot49ExtRemote(Lot49ExtRemote lot49ExtRemote) {
        this.lot49ExtRemote = lot49ExtRemote;
    }

    public int getDma() {
        return dma;
    }

    public void setDma(int dma) {
        this.dma = dma;
    }

    private List<String> languages = new ArrayList<String>();

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    private long timestamp = BidderCalendar.getInstance().currentTimeMillis();

    private Geo geo = new Geo();

    /**
     * Geo as looked up by default Lot49 Geo-location provider.
     * 
     * @see Geo
     * @see User#getGeo()
     * @see Device#getGeo()
     * @see MaxMind
     * @see MaxMindConfig
     * @see MaxMindFacade
     * @see <a href= "http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf" >OpenRTB
     *      2.2 specification</a>
     * 
     * @return Geo
     */
    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private int userEthnicity;

    private int userIncome;

    private String userMarital;

    public int getUserEthnicity() {
        return userEthnicity;
    }

    public void setUserEthnicity(int userEthnicity) {
        this.userEthnicity = userEthnicity;
    }

    public int getUserIncome() {
        return userIncome;
    }

    public void setUserIncome(int userIncome) {
        this.userIncome = userIncome;
    }

    public String getUserMarital() {
        return userMarital;
    }

    public void setUserMarital(String userMarital) {
        this.userMarital = userMarital;
    }

    /**
     * To hold the state of bid request -- if true we don't have a bid.
     */
    private boolean noBid = true;

    public boolean isNoBid() {
        return noBid;
    }

    private boolean test = false;

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public void setNoBid(boolean noBid) {
        this.noBid = noBid;
    }

    private List<AugmentedRequestData> augmentedRequestData = new ArrayList<AugmentedRequestData>();

    public List<AugmentedRequestData> getAugmentedRequestData() {
        return augmentedRequestData;
    }

    public void setAugmentedRequestData(List<AugmentedRequestData> augmentedRequestData) {
        this.augmentedRequestData = augmentedRequestData;
    }

    private String ssp;

    public String getSsp() {
        return ssp;
    }

    public void setSsp(String ssp) {
        this.ssp = ssp;
    }


}
