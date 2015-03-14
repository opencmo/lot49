package com.enremmeta.rtb.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.expression.Expression;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Content;
import com.enremmeta.rtb.api.proto.openrtb.Deal;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.PMP;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ClientConfig;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.spi.providers.Provider;
import com.enremmeta.rtb.spi.providers.ProviderInfoRequired;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoReceived;

/**
 * <p>
 * Specifies ad configuration. Most functionality is implemented in {@link AdImpl}. An Ad may have
 * one or more {@link Tag}s.
 * </p>
 * <p>
 * It is intended that simple Groovy (but not necessarily) scripts extend those classes class,
 * filling in tag information (e.g., {@link Dimension}, etc.) and the actual
 * {@link Tag#getTag(OpenRtbRequest, Impression, Bid, boolean) tag text}.
 * </p>
 * <ol>
 * <li>The naming convention is such that it's named Ad_&lt;AdId&gt;_&lt;AdName&gt;.</li>
 * <li>Ad config is the one that can be returned from {@link #getId()}.</li>
 * <li><tt>init()</tt> method sets up the necessary fields which will be checked in
 * {@link #canBid1(OpenRtbRequest)}.</li>
 * </ol>
 * 
 * @see Tag
 * 
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014-2015. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *
 */
public interface Ad extends Lot49Plugin {
    //
    // String getLot49M11TsId();
    // String getLot49M11CampaignId();
    // String getLot49M11AdvertiserId();
    // String getLot49M11Referer();
    // String getLot49M11Domain();
    // String getLot49M11Exchange();
    // String getLot49M11Cachebuster();
    //
    public long getLoadedOn();


    @Deprecated
    public void setBidPriceCalculator(BidPriceCalculator bpc);

    /**
     * This is a Map from {@link ExchangeAdapter#getName() exchange name}, for those exchanges that
     * return non-null in {@link ExchangeAdapter#makeExchangeSpecificInstructionsMap()}, to that
     * Map. It is returned with keys only for ever Ad object, and is checked by the Exchange during
     * {@link ExchangeAdapter#convertResponse(OpenRtbRequest, com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse)
     * response conversion phase}.
     * 
     */
    Map<String, Map<String, Object>> getExchangeSpecificInstructions();

    public boolean isTargetingDeals();

    void setDomainBlacklistFromUrl(String urlStr);

    void setDomainWhitelistFromUrl(String urlStr);

    String getNodeId();

    /**
     * @param br
     *            request
     * @param imp
     *            impression
     * @return: <tt>null</tt> if no matches, and deal ID if matches exist.
     */
    public String matchDeals(OpenRtbRequest br, Impression imp);

    public void setFrequencyCap(FrequencyCap fc);

    public FrequencyCap getFrequencyCap();

    public void setFrequencyCapCampaign(FrequencyCap fc);

    public FrequencyCap getFrequencyCapCampaign();

    public Set<Pattern> getOrganizations();

    /**
     * Whether it is required that {@link Site#getDomain() domain} be not <tt>null</tt> null in
     * order to bid.
     */
    public boolean isDomainRequired();

    public long getProjectedSpend();

    public void setProjectedSpend(long n);

    public SortedMap<String, SortedMap<String, AtomicLong>> getOptoutsByExchange();

    public void incrRequestCount(String exchange);

    public void incrRequestCount(String exchange, long n);

    public SortedMap<String, AtomicLong> getBidsByExchange();

    public SortedMap<String, AtomicLong> getRequestsByExchange();

    public long getRequestCount();

    public long getRequestCount(String exchange);

    @Deprecated
    public boolean isSegmentLogicAnd();

    String getAdVersion();

    void incrOptout(OpenRtbRequest req, String name);

    void incrOptout(OpenRtbRequest req, String name, long n);

    void addProviderTargeting(String providerName, String json) throws Lot49Exception;

    Map<Provider, ProviderInfoRequired> getProviderTargeting();

    /**
     * A Campaign is something that can unify together several Ads. This is useful for things such
     * as frequency caps and reports, but not much else.
     * 
     * @return
     */
    String getCampaignId();

    long getBids();

    long getBids(String exchange);

    void incrBids(String exchange, long amount);

    long getWins();

    void incrWins();

    void incrWins(long val);

    long getSpendAmount();

    void incrSpendAmount(long spendAmount);

    long getBidAmount();

    long getRealBids();

    long getRealWins();

    long getRealSpendAmount();

    long getRealBidAmount();

    SortedMap<String, AtomicLong> getRealBidsByExchange();

    void storeAdState();

    /**
     * Client ID. Not necessarily meaningful, only useful in multi-tenant situation.
     * 
     * @see ClientConfig
     */
    String getClientId();

    /**
     * Win rate on which previous allocation was based;
     */
    double getWinRate();

    void setWinRate(double winRate);

    public Set<Integer> getDeviceTypes();

    public Set<String> getDeviceMakes();

    public Set<String> getDeviceModels();

    public Set<String> getBrowsers();

    /**
     * OS as "name version" or just "name"
     */
    public Set<String> getOses();

    public Set<String> getLanguages();

    public void setBidsToMake(long n);

    public long getRemainingBidsToMake();

    public long getOriginalBidsToMake();

    /**
     * Whether we still have bids to make. This is {@link #setBidsToMake(long) set} per current
     * pacing period by {@link AdCache}, and every time we make a bid, we decrement the allowed
     * amount.
     */
    public boolean haveBidsToMake();

    // public void unallocateBid();

    // public boolean allocateBid();

    String getAdvertiserId();

    public String getAdvertiser();

    /**
     * Whether this can target this particular bid request. Important notes:
     * 
     * <ol>
     * <li>This MUST NOT check all of the {@link #getTags() constituent config} 's
     * {@link Tag#canBid(OpenRtbRequest, Impression) canBid()} methods. This only qualifies the bid
     * request's attributes (including user) in absence of a particular ad tag configuration. Thus,
     * on a test bid, all of these methods can be assumed to be true, but not all
     * {@link Tag#canBid(OpenRtbRequest, Impression) canBid()s}.</li>
     * <li>This only checks that information that can be checked without external lookup; in other
     * words, it should not presume that {@link Lot49Ext#getLot49ExtRemote()} is populated.</li>
     * <li>If <b>either</b> this method returns <tt>false</tt>, this targeting configuration is
     * <b>not</b> eligible to bid.</li>
     * </ol>
     * <p>
     * </p>
     * 
     * This will always return <tt>true</tt> if <tt>bidRequest</tt> {@link Lot49Ext#isTest() is a
     * test request}.
     * 
     */
    boolean canBid1(OpenRtbRequest bidRequest);

    /**
     * Whether this can target this particular bid request (but different from
     * {@link #canBid1(OpenRtbRequest)}. Important notes:
     * 
     * <ol>
     * <li>This MUST NOT check all of the {@link #getTags() constituent config} 's
     * {@link Tag#canBid(OpenRtbRequest, Impression) canBid()} methods. This only qualifies the bid
     * request's attributes (including user) in absence of a particular ad tag configuration. Thus,
     * on a test bid, all of these methods can be assumed to be true, but not all
     * {@link Tag#canBid(OpenRtbRequest, Impression) canBid()s}.</li>
     * <li>This will not be called if {@link #needCanBid2()} is <tt>false</tt>.</li>
     * <li>If <b>either</b> this method <b>or</b> {@link #canBid1(OpenRtbRequest)} returns
     * <tt>false</tt>, this ad is <b>not</b> eligible to bid.</li>
     * </ol>
     * <p>
     * </p>
     *
     * This will always return <tt>true</tt> if <tt>bidRequest</tt> {@link Lot49Ext#isTest() is a
     * test request}.
     * 
     * @see #canBid1
     */
    boolean checkSegments(OpenRtbRequest br, UserSegments userSegments);

    public boolean checkIntegralTargeting(OpenRtbRequest req, Site site,
                    IntegralInfoReceived integralInfoReceived);

    boolean checkFrequencyCap(OpenRtbRequest br, UserFrequencyCapAttributes userFrequencyCap);

    long getLastBidTime();

    void setLastBidTime(long msec);

    boolean doAbTesting();

    boolean doCampaignAbTesting();

    boolean doTargetingStrategyAbTesting();

    double getTargetingStrategyAbTestingShare();

    double getCampaignAbTestingShare();

    double getAbTestingControlShare();

    double getCampaignAbTestingControlShare();

    /**
     * Domain white list. For cases where this is reasonably small. A limit will be enforced,
     * specified by a {@link AdCacheConfig#getDomainListMaxSize() configuration option}.
     */
    Set<String> getDomainWhitelist();

    Set<String> getDomainBlacklist();

    /**
     * URLs to target. A limit will be enforced, specified by a
     * {@link AdCacheConfig#getUrlListMaxSize() configuration option}. Currently exact match is
     * required.
     */
    Map<String, Set<String>> getTargetingUrls();

    /**
     * @see #getTargetingDeals()
     */
    Map<String, Set<String>> getTargetingDeals();

    /**
     * Map from {@link ExchangeAdapter#getName() exchange name} to list of targeted Deal IDs
     * 
     * @see PMP
     * @see Deal
     */
    void setTargetingDeals(Map<String, List<String>> deals);

    void setTargetingUrlsFromUrl(String url);

    /**
     * Categories of {@link Site#getCat()} to target. Assumed to be normalized to upper case, with
     * colon separator for hierarchy. Targeting categories for {@link Site#getPagecat() page},
     * {@link Site#getSectioncat() section}, {@link Content#getCat() content} are not supported yet.
     */
    Set<String> getTargetingCategories();

    /**
     * Price we can bid on this targeting configuration in micro-dollars.
     */
    public long getBidPrice(OpenRtbRequest req);

    public BidPriceCalculator getBidPriceCalculator();

    public boolean isDynamicPricing();

    public void setBidPrice(long amount);

    /**
     * Same as {@link #getBidAmount()} but as $ CPM.
     */
    public double getBidPriceCpm(OpenRtbRequest req);

    /**
     * Between 0 and 100. Probability of bidding for pacing purposes. Set to 0 to never bid, and to
     * 100 to bid always (subject to {@link #getBidFrequency()}. Values below 0 and above 100 work
     * the same as 0 and 100 respectively.
     * 
     * @see #getBidFrequency()
     */
    public int getBidProbability();

    /**
     * @see #getBidProbability()
     */
    public void setBidProbability(int bidProbability);

    /**
     * Bid frequency, in seconds. That is, bid no more often than once per this number of seconds.
     * If 0, this is not in play and will bid every time.
     * 
     * @see #getBidProbability()
     */
    public long getBidFrequency();

    /**
     * @see #getBidFrequency()
     */
    public void setBidFrequency(long bidFrequency);

    /**
     * Human-readable description
     */
    String getDesc();

    List<String> getLandingPageUrls();

    /**
     * See OpenRTB specification.
     */
    String getIurl();

    List<TargetingGeo> getGeos();
    
    List<TargetingGeo> getNegativeGeos();

    /**
     * Return list of validation errors, or <tt>null</tt> if validated correctly.
     */
    List<String> validate();

    /**
     * @see Bid#getAdomain()
     */
    List<String> getAdomain();

    /**
     * ID (gleaned from script name by convention).
     */
    String getId();

    /**
     * Human-readable name.
     */
    String getName();

    List<Tag> getTags();

    /**
     * @deprecated
     */
    public Set<String> getUserSegments();

    public boolean checkMeasurementSegments(UserSegments userSegments);

    public Expression<String> getParsedTargetingSegments();

    void init();

    public Set<String> getExchanges();

    public Set<String> getSsps();

    boolean needCanBid2();

    boolean needUserInfo();

    boolean needFrequencyCap();

    boolean needIntegralInfo();

    boolean needExperimentInfo();

    boolean isCampaignFrequencyCap();

    boolean isStrategyFrequencyCap();

    /**
     * Not to be confused with {@link #getBidProbability()}, which is used for pacing purposes, this
     * probability is for A/B testing.
     * 
     */
    TargetingHour getTargetingHour();

    TargetingDOW getTargetingDOW();

    TargetingIntegral getTargetingIntegral();

    /**
     * Searched tag by id
     * 
     * @param id
     *            tag identifier we interested in
     * @return tag if found, null in other case
     */
    Tag findTagById(final String id);
}
