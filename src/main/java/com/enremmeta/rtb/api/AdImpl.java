package com.enremmeta.rtb.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.expression.And;
import com.enremmeta.rtb.api.expression.Expression;
import com.enremmeta.rtb.api.expression.Helpers;
import com.enremmeta.rtb.api.expression.Not;
import com.enremmeta.rtb.api.expression.Or;
import com.enremmeta.rtb.api.proto.openrtb.Content;
import com.enremmeta.rtb.api.proto.openrtb.Deal;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.PMP;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtGeo;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtRemote;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.spi.providers.Provider;
import com.enremmeta.rtb.spi.providers.ProviderInfoReceived;
import com.enremmeta.rtb.spi.providers.ProviderInfoRequired;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoReceived;
import com.enremmeta.rtb.spi.providers.integral.IntegralService;
import com.enremmeta.rtb.spi.providers.integral.result.IntegralValidationResult;
import com.enremmeta.rtb.spi.providers.integral.result.dto.BrandSafetyDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.google.common.collect.Sets;

/**
 * Implements most of the default functionality, leaving only things specific to each tag.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 */
public abstract class AdImpl implements Ad, Lot49Constants {

    @Override
    public String getNodeId() {
        return nodeId;
    }

    protected String nodeId;

    @Override
    public void finalize() {
        LogUtils.debug(this + ".finalize() is called.");
    }

    @Override
    public boolean isDomainRequired() {
        return domainRequired;
    }

    @Override
    public long getLoadedOn() {
        return this.loadedOn;
    }

    protected boolean domainRequired = true;

    public void addProviderTargeting(String providerName, String json) throws Lot49Exception {
        Provider provider = Bidder.getInstance().getProviders().get(providerName);
        if (provider == null) {
            throw new Lot49Exception("Provider " + providerName + " does not exist.");
        }
        if (!provider.isEnabled()) {
            throw new Lot49Exception("Provider " + providerName + " is not enabled.");
        }
        ProviderInfoRequired reqInfo = provider.parse(json);
        providerTargeting.put(provider, reqInfo);
    }

    public Map<Provider, ProviderInfoRequired> getProviderTargeting() {
        return providerTargeting;
    }

    protected Map<Provider, ProviderInfoRequired> providerTargeting =
                    new HashMap<Provider, ProviderInfoRequired>();

    protected String clientId;

    @Override
    public String getClientId() {
        return this.clientId;
    }

    private final AtomicLong winRate = new AtomicLong(0);

    @Override
    public double getWinRate() {
        return Double.longBitsToDouble(winRate.get());
    }

    @Override
    public void setWinRate(double wr) {
        winRate.set(Double.doubleToLongBits(wr));
    }

    protected Set<Integer> deviceTypes;

    protected Set<String> deviceModels;
    protected Set<String> deviceMakes;

    protected Set<String> oses;

    @Override
    public Set<String> getOses() {
        return oses;
    }

    protected Set<String> languages;

    @Override
    public Set<Integer> getDeviceTypes() {
        return deviceTypes;
    }

    @Override
    public Set<String> getDeviceModels() {
        return deviceModels;
    }

    @Override
    public Set<String> getDeviceMakes() {
        return deviceMakes;
    }

    @Override
    public Set<String> getLanguages() {
        return languages;
    }

    private final AtomicLong bids = new AtomicLong(0);

    @Override
    public long getBids() {
        return this.bids.get();
    }

    @Override
    public long getBids(String exchange) {
        return this.bidsByExchange.get(exchange).get();
    }

    private final AtomicLong wins = new AtomicLong(0);

    @Override
    public long getWins() {
        return this.wins.get();
    }

    @Override
    public void incrWins() {
        long retval = this.wins.incrementAndGet();
        LogUtils.debug("Wins for " + getId() + ": " + retval);
    }

    @Override
    public void incrWins(long val) {
        long retval = this.wins.addAndGet(val);
        LogUtils.debug("Wins for " + getId() + ": " + retval);
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    protected String campaignId;

    @Override
    public String getCampaignId() {
        return this.campaignId;
    }

    protected String advertiser;

    private boolean unlimitedBudget;

    @Override
    public String getAdvertiser() {
        return advertiser;
    }

    protected TargetingIntegral targetingIntegral = null;

    public TargetingIntegral getTargetingIntegral() {
        return targetingIntegral;
    }

    public void setTargetingIntegral(TargetingIntegral targetingIntegral) {
        this.targetingIntegral = targetingIntegral;
    }

    private DomainTargeting realDomainWhitelist = null;
    private DomainTargeting realDomainBlacklist = null;


    public void setDomainWhitelist(Set<String> wl) {
        // LogUtils.debug(getId() + ": XXX: Entering setDomainWhitelist(" + wl + ")");
        this.realDomainWhitelist = new DomainTargeting(this, wl);
    }

    public void setDomainBlacklist(Set<String> bl) {
        // LogUtils.debug(getId() + ": XXX: Entering setDomainBlacklist(" + bl + ")");
        this.realDomainBlacklist = new DomainTargeting(this, bl);
    }

    @Override
    public void setDomainWhitelistFromUrl(String urlStr) {
        Set<String> domains = targetingDomainSets.get(urlStr);
        if (domains != null) {
            LogUtils.info(getId() + ": Domain whitelist targeting: Already have list from " + urlStr
                            + " with " + domains.size() + " domains ");
            setDomainWhitelist(domains);
            return;
        }
        domains = getSetFromUrl(urlStr);
        setDomainWhitelist(domains);
        targetingDomainSets.put(urlStr, domains);
    }

    @Override
    public void setDomainBlacklistFromUrl(String urlStr) {
        Set<String> domains = targetingDomainSets.get(urlStr);
        if (domains != null) {
            LogUtils.info(getId() + ": Domain blacklist Targeting: Already have list from " + urlStr
                            + " with " + domains.size() + " domains ");
            setDomainBlacklist(domains);
            return;
        }
        domains = getSetFromUrl(urlStr);
        setDomainBlacklist(domains);
        targetingDomainSets.put(urlStr, domains);
    }

    @Override
    public Set<String> getDomainWhitelist() {
        if (this.realDomainWhitelist == null) {
            return null;
        }
        return this.realDomainWhitelist.getOriginalList();
    }

    @Override
    public Set<String> getDomainBlacklist() {
        if (this.realDomainBlacklist == null) {
            return null;
        }
        return this.realDomainBlacklist.getOriginalList();
    }

    private Map<String, Set<String>> targetingUrls;

    protected void setTargetingUrls(Set<String> urls) {
        String msg = "";
        this.targetingUrls = new HashMap<String, Set<String>>();
        for (String url : urls) {
            if (url == null) {
                continue;
            }
            url = url.trim();
            if (url.length() == 0) {
                continue;
            }
            URL realUrl = null;
            try {
                String origUrl = url;
                url = StringUtils.replace(url, "https://", "http://");
                realUrl = new URL(url);
                String host = realUrl.getHost().toLowerCase();
                realUrl = new URL("http", host, realUrl.getPort(), realUrl.getPath());
                if (!realUrl.toString().equals(origUrl)) {
                    // msg += "\nNormalized " + origUrl + " to " + realUrl;
                }
            } catch (MalformedURLException murle) {
                msg += "\nMalformed URL: " + url;
                continue;
            }
            if (realUrl != null) {
                Set<String> paths = this.targetingUrls.get(realUrl.getHost());
                if (paths == null) {
                    paths = new HashSet<String>();
                    this.targetingUrls.put(realUrl.getHost(), paths);
                }
                String path = realUrl.getPath();
                path = StringUtils.replace(path, "//", "/");
                paths.add(path);
            }
        }
        if (msg.length() > 0) {
            LogUtils.info("setTargetingUrls(): " + msg);
        }
    }

    @Override
    public Map<String, Set<String>> getTargetingUrls() {
        return this.targetingUrls;
    }

    private Set<String> getSetFromUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String path = url.getPath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            int lastSlash = path.lastIndexOf("/");
            String fname = path.substring(lastSlash + 1);
            File f = new File("/tmp", fname);
            if (f.exists()) {
                LogUtils.info(getId() + ": URL Targeting: Will not download " + urlStr + " to " + f
                                + " (already exists).");
            } else {
                LogUtils.info(getId() + ": URL Targeting: Will download " + urlStr + " to " + f);
                FileUtils.copyURLToFile(url, f);
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            Set<String> vals = new HashSet<String>();
            int i = 0;
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                i++;
                line = line.trim();
                vals.add(line);
            }
            LogUtils.info(getId() + ": Read " + i + " lines from " + f);
            return vals;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTargetingUrlsFromUrl(String urlStr) {
        Map<String, Set<String>> tarUrls = targetingUrlSets.get(urlStr);
        if (tarUrls != null) {
            LogUtils.info(getId() + ": URL Targeting: Already have list from " + urlStr + " with "
                            + tarUrls.keySet().size() + " domains ");
            this.targetingUrls = tarUrls;
            return;
        }
        Set<String> urls = getSetFromUrl(urlStr);
        setTargetingUrls(urls);
        targetingUrlSets.put(urlStr, this.targetingUrls);
    }

    protected Set<String> targetingCategories;

    private AtomicLong projectedSpend = new AtomicLong(0);

    public long getProjectedSpend() {
        return projectedSpend.get();
    }

    public void setProjectedSpend(long n) {
        this.projectedSpend.set(n);
    }

    @Override
    public Set<String> getTargetingCategories() {
        return this.targetingCategories;
    }

    @Override
    public List<String> validate() {
        final List<String> retval = new ArrayList<String>();

        if (adomain == null || adomain.isEmpty()) {
            retval.add("adomain missing");
        }

        for (String ad : adomain) {
            if (!Utils.validateDomain(ad)) {
                retval.add("Adomain " + ad + " is invalid.");
            }
        }

        if (realDomainWhitelist != null && realDomainWhitelist.getOriginalList() != null) {
            int size = realDomainWhitelist.getOriginalList().size();
            if (size > 0) {
                int maxSize = Bidder.getInstance().getConfig().getAdCache().getDomainListMaxSize();
                if (size > maxSize) {
                    retval.add("Size of domainWhitelist " + size + " exceeds maximum allowed "
                                    + maxSize + ".");
                }
            } else {
                retval.add("Empty domainWhitelist.");
            }
        }

        if (realDomainBlacklist != null && realDomainBlacklist.getOriginalList() != null) {
            int size = realDomainBlacklist.getOriginalList().size();
            if (size > 0) {
                int maxSize = Bidder.getInstance().getConfig().getAdCache().getDomainListMaxSize();
                if (size > maxSize) {
                    retval.add("Size of domainBlacklist " + size + " exceeds maximum allowed "
                                    + maxSize + ".");
                }
            } else {
                retval.add("Empty domainBlacklist.");
            }
        }

        if (targetingUrls != null) {
            final int size = targetingUrls.size();
            if (size > 0) {
                int maxSize = Bidder.getInstance().getConfig().getAdCache().getUrlListMaxSize();
                if (size > maxSize) {
                    retval.add("Size of targetingUrls " + size + " exceeds maximum allowed "
                                    + maxSize + ".");
                }
            } else {
                retval.add("Empty targetingUrls.");
            }
        }

        if (targetingHour != null) {
            final String thVal = targetingHour.validate();
            if (thVal != null) {
                retval.add(thVal);
            }
        }

        if (targetingDOW != null) {
            final String tdVal = targetingDOW.validate();
            if (tdVal != null) {
                retval.add(tdVal);
            }

        }

        if (tags == null || tags.size() == 0) {
            retval.add("No tags found.");
        } else {
            for (final Tag tag : tags) {
                try {
                    final List<String> tagValidation = tag.validate();
                    if (tagValidation != null) {
                        for (String tv : tagValidation) {
                            retval.add("Tag " + tag.getId() + ": " + tv);
                        }
                    }
                } catch (Throwable t) {
                    LogUtils.error("Error validating " + this, t);
                    retval.add(t.getMessage());
                }
            }
        }

        // Validate for each exchange
        if (exchanges == null || exchanges.size() == 0) {
            // Targeted to all exchanges, so validate for all.
            for (@SuppressWarnings("rawtypes")
            final ExchangeAdapter adapter : ExchangeAdapterFactory.getAllExchangeAdapters()) {
                final List<String> adapterValidation = adapter.validateAd(this);
                if (adapterValidation != null) {
                    retval.addAll(adapterValidation);
                }
            }
        } else {
            for (final String exchangeName : exchanges) {
                try {
                    @SuppressWarnings("rawtypes")
                    final ExchangeAdapter adapter =
                                    ExchangeAdapterFactory.getExchangeAdapter(exchangeName);

                    final List<String> adapterValidation = adapter.validateAd(this);
                    if (adapterValidation != null) {
                        retval.addAll(adapterValidation);
                    }
                } catch (IllegalArgumentException iae) {
                    retval.add(iae.toString());
                }
            }
        }

        if (retval.isEmpty()) {
            return null;
        }

        if (bidProbability < 0 || bidProbability > 100) {
            retval.add("bidProbability should be between 0 and 100, but it was " + bidProbability);
        }

        return retval;
    }

    protected boolean abTesting = false;
    protected double abTestingShare = 1.0;
    protected boolean campaignAbTesting = false;
    protected double campaignAbTestingShare;
    protected double abTestingControlShare = 0.5;
    protected double campaignAbTestingControlShare = 0.5;

    @Override
    public boolean doAbTesting() {
        return campaignAbTesting || abTesting;
    }

    @Override
    public boolean doTargetingStrategyAbTesting() {
        return abTesting;
    }

    @Override
    public boolean doCampaignAbTesting() {
        return campaignAbTesting;
    }

    @Override
    public double getTargetingStrategyAbTestingShare() {
        return abTestingShare;
    }

    @Override
    public double getCampaignAbTestingShare() {
        return campaignAbTestingShare;
    }

    @Override
    public double getAbTestingControlShare() {
        return abTestingControlShare;
    }

    @Override
    public double getCampaignAbTestingControlShare() {
        return campaignAbTestingControlShare;
    }

    @Override
    public List<String> getAdomain() {
        return adomain;
    }

    @Override
    public String getAdvertiserId() {
        return advertiserId;
    }

    protected String advertiserId;

    protected AtomicLong budget = new AtomicLong(0);

    public boolean allocateBudget(OpenRtbRequest req) {
        if (unlimitedBudget) {
            return true;
        }
        final long bidAmount = getBidPrice(req);
        final long remaining = budget.addAndGet(-bidAmount);
        if (remaining < 0) {
            budget.addAndGet(bidAmount);
            return false;
        }
        return true;
    }

    protected List<String> adomain;

    protected List<String> landingPageUrls;

    @Override
    public List<String> getLandingPageUrls() {
        return landingPageUrls;
    }

    protected long bidPrice;

    private long lastBidTime = 0;

    public long getLastBidTime() {
        return lastBidTime;
    }

    public void setLastBidTime(long lastBidTime) {
        this.lastBidTime = lastBidTime;
    }

    protected FrequencyCap frequencyCap;
    protected FrequencyCap frequencyCapCampaign;

    public void setFrequencyCap(FrequencyCap fc) {
        this.frequencyCap = fc;
    }

    public FrequencyCap getFrequencyCap() {
        return frequencyCap;
    }

    public void setFrequencyCapCampaign(FrequencyCap fcCampaign) {
        this.frequencyCapCampaign = fcCampaign;
    }

    public FrequencyCap getFrequencyCapCampaign() {
        return frequencyCapCampaign;
    }

    /**
     * For pacing purposes.
     */
    private int bidProbability = 100;

    private long bidFrequency = 0;

    @Override
    public long getBidFrequency() {
        return bidFrequency;
    }

    @Override
    public void setBidFrequency(long bidFrequency) {
        this.bidFrequency = bidFrequency;
    }

    @Override
    public void setBidProbability(int bidProbability) {
        this.bidProbability = bidProbability;
    }

    protected String customUserData = null;

    protected String desc;

    @Override
    public String getIurl() {
        return iurl;
    }

    protected String iurl = null;

    protected List<TargetingGeo> geos;
    protected List<TargetingGeo> negativeGeos;

    private String id = "";

    protected String name;

    private Random rnd = new Random();

    protected Set<String> browsers;

    @Override
    public Set<String> getBrowsers() {
        return browsers;
    }

    protected Set<String> measurementSegments = null;

    @Override
    public boolean checkMeasurementSegments(UserSegments userSegments) {
        if (measurementSegments == null || userSegments == null) {
            return true;
        }
        Set<String> measurementSegmentsCopy = new HashSet<String>(measurementSegments);
        measurementSegmentsCopy.retainAll(userSegments.getSegmentsSet());
        return (measurementSegmentsCopy.size() == 0);
    }

    protected List<Tag> tags;

    @Deprecated
    public void setUserSegments(Set<String> segments) {
        LogUtils.warn("Ad " + getId() + ": Calling deprecated method setUserSegments()");
        if (isSegmentLogicAnd()) {
            this.parsedTargetingSegments = new And(segments);
        } else {
            this.parsedTargetingSegments = new Or(segments);
        }
    }

    @Override
    public Expression<String> getParsedTargetingSegments() {
        return parsedTargetingSegments;
    }

    protected void setTargetingSegments(String expression) throws Lot49Exception {
        this.parsedTargetingSegments = Helpers.parseStringExpression(expression);
    }

    protected Or<String> or(String... terms) {
        return new Or<String>(terms);
    }

    protected And<String> and(String... terms) {
        return new And<String>(terms);
    }

    protected Or<String> or(Expression<String>... terms) {
        return new Or<String>(terms);
    }

    protected And<String> and(Expression<String>... terms) {
        return new And<String>(terms);
    }

    protected Not<String> not(String term) {
        return new Not<String>(term);
    }

    protected Not<String> not(Expression<String> term) {
        return new Not<String>(term);
    }

    private Expression<String> parsedTargetingSegments = null;
    protected String targetingSegments = "";

    public AdImpl() throws Lot49Exception {
        this(false);
    }

    public AdImpl(boolean testMode) throws Lot49Exception {
        super();
        this.testMode = testMode;
        String[] classNameElts = getClass().getSimpleName().split("_");
        if (classNameElts.length != 3) {
            throw new Lot49Exception("Incorrect naming of " + getClass().getName()
                            + ", expected Ad_<adId>_<adName>");
        }
        if (!classNameElts[0].equals("Ad")) {
            throw new Lot49Exception("Incorrect naming of " + getClass().getName()
                            + ", expected Ad_<adId>_<adName>");
        }
        this.id = classNameElts[1];
        this.name = classNameElts[2];
        ServiceRunner bidder = null;
        bidder = Bidder.getInstance();

        exchangeSpecificInstructions = new HashMap<String, Map<String, Object>>() {
            {
                for (final ExchangeAdapter adapter : ExchangeAdapterFactory
                                .getAllExchangeAdapters(testMode)) {
                    Map<String, Object> m = adapter.makeExchangeSpecificInstructionsMap();
                    if (m != null) {
                        put(adapter.getName(), m);
                    }
                }
            }
        };

        init();
    }

    protected Set<String> exchanges = new TreeSet<String>();

    public Set<String> getExchanges() {
        return exchanges;
    }

    private AtomicLong requests = new AtomicLong(0);

    @Override
    public void incrRequestCount(String exchange) {
        requests.incrementAndGet();
        requestsByExchange.get(exchange).incrementAndGet();
    }

    @Override
    public void incrRequestCount(String exchange, long n) {
        requests.addAndGet(n);
        requestsByExchange.get(exchange).addAndGet(n);
    }

    @Override
    public long getRequestCount() {
        return requests.get();
    }

    @Override
    public long getRequestCount(String exchange) {
        return requestsByExchange.get(exchange).get();
    }

    public boolean haveBidsToMake() {
        return remainingBidsToMake.get() > 0;
    }

    private AtomicLong remainingBidsToMake = new AtomicLong(0);

    private AtomicLong originalBidsToMake = new AtomicLong(0);

    // TOOD get this in order as evaluated in

    @Override
    public long getRemainingBidsToMake() {
        return remainingBidsToMake.get();
    }

    @Override
    public long getOriginalBidsToMake() {
        return originalBidsToMake.get();
    }

    @Override
    public void setBidsToMake(long bidsToMake) {
        this.remainingBidsToMake.set(bidsToMake);
        this.originalBidsToMake.set(bidsToMake);
    }

    @Override
    public void incrBids(String exchange, long amount) {
        bidsByExchange.get(exchange).incrementAndGet();
        long bidsMade = this.bids.incrementAndGet();
        long bidsRemaining = this.remainingBidsToMake.decrementAndGet();
        LogUtils.trace("Ad " + getId() + ": bids made: " + bidsMade + "; remaining bids to make: "
                        + bidsRemaining + ".");
        this.bidAmount0.addAndGet(amount);

    }

    private boolean testMode = false;

    protected final Map<String, Map<String, Object>> exchangeSpecificInstructions;


    @Override
    public Map<String, Map<String, Object>> getExchangeSpecificInstructions() {
        return exchangeSpecificInstructions;

    }

    private static final SortedMap<String, AtomicLong> makeSingleExchangeOptoutMap() {

        final SortedMap<String, AtomicLong> retval = new TreeMap<String, AtomicLong>() {

            {
                // 2
                put(DECISION_EVALUATION_ERROR, new AtomicLong(0));
                put(DECISION_NO_IMPRESSIONS, new AtomicLong(0));
                put(DECISION_USER_UNKNOWN, new AtomicLong(0));
                put(DECISION_DOMAIN_REQUIRED, new AtomicLong(0));
                put(DECISION_EXCHANGE, new AtomicLong(0));
                put(DECISION_SSP, new AtomicLong(0));
                put(DECISION_DEVICE, new AtomicLong(0));
                put(DECISION_PROVIDER, new AtomicLong(0));
                put(DECISION_OS, new AtomicLong(0));
                put(DECISION_LANGUAGE, new AtomicLong(0));
                put(DECISION_BROWSER, new AtomicLong(0));
                put(DECISION_GEO, new AtomicLong(0));
                put(DECISION_DOMAIN_UNMATCHED, new AtomicLong(0));
                put(DECISION_URL, new AtomicLong(0));
                put(DECISION_CATEGORY, new AtomicLong(0));
                put(DECISION_HOUR, new AtomicLong(0));
                put(DECISION_DAY, new AtomicLong(0));


                // 3
                put(DECISION_FLOOR, new AtomicLong(0));
                put(DECISION_TAG, new AtomicLong(0));
                put(DECISION_PRIVATE_DEAL, new AtomicLong(0));

                // 4
                put(DECISION_PACING, new AtomicLong(0));

                // 5
                put(DECISION_WRONG_USER, new AtomicLong(0));
                put(DECISION_FREQ_CAP, new AtomicLong(0));
                put(DECISION_INTEGRAL, new AtomicLong(0));
                put(DECISION_INTEGRAL_URL, new AtomicLong(0));
                put(DECISION_VIEWABILITY, new AtomicLong(0));
                put(DECISION_BRANDSAFETY, new AtomicLong(0));
                put(DECISION_TRAQ, new AtomicLong(0));
                put(DECISION_NO_USER, new AtomicLong(0));

                // 6
                put(DECISION_TIMEOUT_USERDATA, new AtomicLong(0));
                put(DECISION_TIMEOUT_INTEGRAL, new AtomicLong(0));
                put(DECISION_TIMEOUT_FC, new AtomicLong(0));
                put(DECISION_TIMEOUT_EXPERIMENT_STATUS, new AtomicLong(0));
                put(DECISION_TIMEOUT_UNKNOWN, new AtomicLong(0));

                // 7
                put(DECISION_INTERNAL_AUCTION, new AtomicLong(0));

                // 8
                put(DECISION_EXPERIMENT_CONTROL_SET, new AtomicLong(0));

            }
        };
        return retval;
    }

    private static final Map<String, Map<String, Set<String>>> targetingUrlSets =
                    new HashMap<String, Map<String, Set<String>>>();

    private static final Map<String, Set<String>> targetingDomainSets =
                    new HashMap<String, Set<String>>();

    private SortedMap<String, SortedMap<String, AtomicLong>> optoutsByExchange =
                    new TreeMap<String, SortedMap<String, AtomicLong>>() {
                        {
                            for (final String xch : ExchangeAdapterFactory
                                            .getAllExchangeAdapterNames()) {
                                put(xch, makeSingleExchangeOptoutMap());
                            }

                        }
                    };

    public SortedMap<String, AtomicLong> getBidsByExchange() {
        return bidsByExchange;
    }

    public SortedMap<String, AtomicLong> getRequestsByExchange() {
        return requestsByExchange;
    }

    private SortedMap<String, AtomicLong> bidsByExchange = new TreeMap<String, AtomicLong>() {
        {
            for (final String xch : ExchangeAdapterFactory.getAllExchangeAdapterNames()) {
                put(xch, new AtomicLong(0));
            }

        }
    };

    private SortedMap<String, AtomicLong> requestsByExchange = new TreeMap<String, AtomicLong>() {
        {
            for (final String xch : ExchangeAdapterFactory.getAllExchangeAdapterNames()) {
                put(xch, new AtomicLong(0));
            }

        }
    };

    private AtomicLong spendAmount = new AtomicLong(0);

    private AtomicLong bidAmount0 = new AtomicLong(0);

    @Override
    public long getSpendAmount() {
        return spendAmount.get();
    }

    @Override
    public void incrSpendAmount(long n) {
        spendAmount.addAndGet(n);
    }

    @Override
    public long getBidAmount() {
        return bidAmount0.get();
    }

    private ConcurrentMap<String, String> uniqOptoutReasonMap =
                    new ConcurrentHashMap<String, String>();

    @Override
    public void incrOptout(OpenRtbRequest req, String name) {
        incrOptout(req, name, 1);
    }

    @Override
    public void incrOptout(OpenRtbRequest req, String name, long n) {

        final String reqId = req.getId();
        final String exchange = req.getLot49Ext().getAdapter().getName();

        String alreadyOptedOut = uniqOptoutReasonMap.putIfAbsent(exchange + "_" + reqId, name);
        if (alreadyOptedOut == null) {
            SortedMap<String, AtomicLong> xchOptout = optoutsByExchange.get(exchange);
            AtomicLong count = xchOptout.get(name);
            if (count == null) {
                LogUtils.error("Nothing in optoutsByExchange for " + name);
            } else {
                count.addAndGet(n);
            }
        } else {
            LogUtils.trace(getId() + ": " + "Not opting out for reason of " + name
                            + " because already for " + alreadyOptedOut + " (" + reqId + ")");
        }
    }

    protected Set<String> ssps = new TreeSet<String>();

    @Override
    public Set<String> getSsps() {
        return ssps;
    }

    @Override
    public String getAdVersion() {
        return adVersion;
    }

    protected String adVersion = "1";

    boolean matchOrganizations(Geo lot49Geo) {
        if (organizations == null || organizations.size() == 0) {
            return true;
        }

        if (lot49Geo == null) {
            return false;
        }
        Map ext = lot49Geo.getExt();
        if (ext == null) {
            return false;
        }
        Lot49ExtGeo geoExt = (Lot49ExtGeo) ext.get(Lot49ExtGeo.GEO_EXT_KEY);
        if (geoExt == null) {
            return false;
        }

        String gotOrg = geoExt.getOrg();
        if (gotOrg == null) {
            return false;
        }
        gotOrg = gotOrg.trim();
        if (gotOrg.length() == 0) {
            return false;
        }
        gotOrg = gotOrg.toLowerCase();
        gotOrg = gotOrg.replaceAll(REGEXP_NON_ALPHANUM_WITH_WILDCARD, " ");
        gotOrg = Utils.removeRedundantWhitespace(gotOrg);

        gotOrg = gotOrg.trim();

        for (Pattern org : organizations) {
            if (org.matcher(gotOrg).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see #getTargetingDeals()
     */
    public Map<String, Set<String>> getTargetingDeals() {
        return this.deals;
    }

    private Map<String, Set<String>> deals;

    /**
     * Map from {@link ExchangeAdapter#getName() exchange name} to list of targeted Deal IDs
     *
     * @see PMP
     * @see Deal
     */
    public void setTargetingDeals(Map<String, List<String>> deals) {
        for (String exchangeName : deals.keySet()) {
            try {
                ExchangeAdapterFactory.getExchangeAdapter(exchangeName);
            } catch (Throwable t) {
                LogUtils.error("Unknown exchange", t);
                continue;
            }
            if (this.deals == null) {
                this.deals = new HashMap<String, Set<String>>();
            }
            List<String> curDeals = deals.get(exchangeName);
            if (curDeals == null || curDeals.size() == 0) {
                continue;
            }
            Set<String> curDealsToSet = this.deals.get(exchangeName);
            if (curDealsToSet == null) {
                curDealsToSet = new HashSet<String>();
                this.deals.put(exchangeName, curDealsToSet);
            }
            for (String deal : curDeals) {
                if (deal == null) {
                    continue;
                }
                deal = deal.trim();
                if (deal.length() == 0) {
                    continue;
                }
                curDealsToSet.add(deal);
            }
        }
        LogUtils.info("Set targeting deals, normalized " + deals + " to " + this.deals);
    }

    public boolean checkIntegralTargeting(OpenRtbRequest req, Site site,
                    IntegralInfoReceived integralInfoReceived) {

        if (targetingIntegral == null) {
            return true;
        }

        final long impCount = req.getImp().size();

        boolean targetingStatus = true;
        if (integralInfoReceived != null && !integralInfoReceived.isError()) {
            if (targetingIntegral.isBrandSafety()) {
                BrandSafetyDto brandSafetyDto = integralInfoReceived.getBrandSafetyDto();

                IntegralValidationResult result =
                                targetingIntegral.validateBrandSafety(brandSafetyDto);
                LogUtils.debug("Integral BrandSafety " + result.getValidationMessage());
                if (!result.isValid()) {
                    incrOptout(req, DECISION_BRANDSAFETY, impCount);
                    req.getLot49Ext().getOptoutReasons().put(getId(),
                                    "Brand Safety not passed = " + result.getValidationMessage());
                    integralInfoReceived.setTargetingStatus("FAIL");
                    LogUtils.debug("Integral check on request " + req.getId()
                                    + " FAILED on BrandSafety");
                    targetingStatus = false;
                }
            }

            if (targetingIntegral.isTraq()) {
                Integer traqScore = integralInfoReceived.getTraqScore();
                if (traqScore == null) {
                    incrOptout(req, DECISION_TRAQ, impCount);
                    req.getLot49Ext().getOptoutReasons().put(getId(),
                                    "No TRAQ received but integral TRAQ specified.");
                    LogUtils.debug("Integral check on request " + req.getId()
                                    + " not received TRAQ");
                    targetingStatus = false;
                } else {
                    IntegralValidationResult result = targetingIntegral.validateTraq(traqScore);
                    LogUtils.debug("Integral TRAQ " + result.getValidationMessage());
                    if (!result.isValid()) {
                        incrOptout(req, DECISION_TRAQ, impCount);
                        req.getLot49Ext().getOptoutReasons().put(getId(),
                                        "TRAQ check not passed = " + result.getValidationMessage());
                        LogUtils.debug("Integral check on request " + req.getId()
                                        + " FAILED on TRAQ");
                        targetingStatus = false;
                    }
                }
            }

            if (targetingIntegral.isViewability()) {
                ViewabilityDto viewabilityDto = integralInfoReceived.getViewabilityDto();
                if (viewabilityDto == null) {
                    incrOptout(req, DECISION_VIEWABILITY, impCount);
                    req.getLot49Ext().getOptoutReasons().put(getId(),
                                    "No viewability received but integral viewability specified.");
                    LogUtils.debug("Integral check on request " + req.getId()
                                    + " not received Viewability");
                    targetingStatus = false;
                } else {
                    IntegralValidationResult result =
                                    targetingIntegral.validateViewability(viewabilityDto);
                    LogUtils.debug("Integral Viewvability " + result.getValidationMessage());
                    if (!result.isValid()) {
                        incrOptout(req, DECISION_VIEWABILITY, impCount);
                        req.getLot49Ext().getOptoutReasons().put(getId(),
                                        "viewability check not passed = "
                                                        + result.getValidationMessage());
                        LogUtils.debug("Integral check on request " + req.getId()
                                        + " FAILED on Viewability");
                        targetingStatus = false;
                    }
                }
            }
        } else {
            incrOptout(req, DECISION_INTEGRAL, impCount);
            req.getLot49Ext().getOptoutReasons().put(getId(), "general integral problem");
            LogUtils.debug("Integral check on request " + req.getId() + " FAILED not received");
            targetingStatus = false;
        }

        if (targetingStatus) {
            integralInfoReceived.setTargetingStatus("PASS");
            // LogUtils.debug("Integral check on request " + req.getId() + " PASSED");

        } else {
            integralInfoReceived.setTargetingStatus("FAIL");
        }
        req.getLot49Ext().getProviderInfo().put(IntegralService.INTEGRAL_PROVIDER_NAME,
                        integralInfoReceived);

        return targetingStatus;
    }

    public boolean matchTargetingUrls(OpenRtbRequest req, Site site) {
        final long impCount = req.getImp().size();
        if (targetingUrls == null || targetingUrls.size() == 0) {
            return true;
        }

        if (site == null) {
            incrOptout(req, DECISION_URL, impCount);
            req.getLot49Ext().getOptoutReasons().put(getId(),
                            "No Site object received, but targetingUrls specified.");
            return false;
        }
        String url = site.getPage();
        if (url == null) {
            incrOptout(req, DECISION_URL, impCount);
            req.getLot49Ext().getOptoutReasons().put(getId(),
                            "No domain received, but targetingUrls specified.");
            return false;
        }
        // Get rid of https
        url = StringUtils.replace(url, "https://", "http://");
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        try {
            URL urlObj = new URL(url);
            String host = urlObj.getHost().toLowerCase();
            String path = urlObj.getPath();

            for (String domain : targetingUrls.keySet()) {
                if (!host.equals(domain)) {
                    continue;
                }
                Set<String> paths = targetingUrls.get(domain);
                if (paths == null) {
                    return true;
                }
                for (String goodPath : paths) {
                    if (path.startsWith(goodPath)) {

                        return true;
                    }
                }
                // Optimization - we already have host == domain
                // No point to go further in the keySet
                break;
            }
        } catch (MalformedURLException e) {
            incrOptout(req, DECISION_URL, impCount);
            req.getLot49Ext().getOptoutReasons().put(getId(), "URL: " + e.toString());
            return false;
        }
        incrOptout(req, DECISION_URL, impCount);
        req.getLot49Ext().getOptoutReasons().put(getId(), "URL not in whitelist");
        return false;
    }

    private final long loadedOn = BidderCalendar.getInstance().currentTimeMillis();

    @Override
    public boolean isTargetingDeals() {
        if (deals == null || deals.size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String matchDeals(OpenRtbRequest br, Impression imp) {
        if (!isTargetingDeals()) {
            return null;
        }

        // We'll set this now. If this returns
        // false then it has no effect, but if true,
        // downstream methods can use it.
        br.getLot49Ext().setPrivateDeals(true);

        PMP pmp = imp.getPmp();
        if (pmp == null) {
            incrOptout(br, DECISION_PRIVATE_DEAL);
            br.getLot49Ext().getOptoutReasons().put(getId(), "No PMP Object received");

            LogUtils.debug("PMP_" + getId() + " " + br.getId() + ": No PMP Object received");
            return null;
        }
        List<Deal> receivedDeals = pmp.getDeals();
        if (receivedDeals == null || receivedDeals.size() == 0) {
            incrOptout(br, DECISION_PRIVATE_DEAL);
            br.getLot49Ext().getOptoutReasons().put(getId(), "No deals.");
            LogUtils.debug("PMP_" + getId() + " " + br.getId() + ": No deals.");
            return null;
        }

        String exchange = br.getLot49Ext().getAdapter().getName();
        Set<String> myDeals = deals.get(exchange);
        if (myDeals == null || myDeals.size() == 0) {
            incrOptout(br, DECISION_PRIVATE_DEAL);
            br.getLot49Ext().getOptoutReasons().put(getId(),
                            "Not targeting any deals on " + exchange);
            LogUtils.debug("PMP_" + getId() + " " + br.getId() + "Not targeting any deals on "
                            + exchange);
            return null;
        }
        LogUtils.debug("PMP_" + getId() + " " + br.getId() + ": trying to match " + receivedDeals
                        + " to required " + myDeals);
        StringBuilder reasons = new StringBuilder();
        for (Deal gotDeal : receivedDeals) {
            String cur = gotDeal.getBidfloorcur();
            if (cur == null) {
                cur = "usd";
            }
            cur = cur.trim();
            if (cur.length() == 0) {
                cur = "usd";
            }
            if (!cur.toLowerCase().equals("usd")) {
                LogUtils.debug("PMP_" + getId() + " " + br.getId() + "(" + br.getId()
                                + ") : Non-USD " + cur);
                reasons.append("Non-USD: ").append(cur).append(".");
                continue;
            }

            final float floor = gotDeal.getBidfloor();
            final long floorMicro = Utils.cpmToMicro(floor);
            final long bidPrice = getBidPrice(br);
            if (bidPrice < floorMicro) {
                LogUtils.debug("PMP_" + getId() + " " + br.getId() + "(" + br.getId() + "): "
                                + bidPrice + " < " + floorMicro);
                reasons.append("Floor: ").append(bidPrice).append(" < ").append(floorMicro)
                                .append(".");
                continue;
            }

            boolean wadomainOk = false;
            List<String> wadomains = gotDeal.getWadomain();
            if (wadomains != null && wadomains.size() > 0) {
                for (final String wadomain : wadomains) {
                    if (adomain.contains(wadomain)) {
                        wadomainOk = true;
                        break;
                    }
                }
                if (!wadomainOk) {
                    incrOptout(br, DECISION_PRIVATE_DEAL);
                    br.getLot49Ext().getOptoutReasons().put(getId(),
                                    "Domains " + adomain + " not in required  " + wadomains);
                    reasons.append("Domain.");
                    LogUtils.debug("PMP_" + getId() + " " + br.getId() + "(" + br.getId()
                                    + "): Domains " + adomain + " not in required  " + wadomains);
                    continue;
                }
            }

            String id = gotDeal.getId();
            if (myDeals.contains(id)) {
                LogUtils.debug("PMP_" + getId() + " " + br.getId() + "(" + br.getId()
                                + ") Found private deal: " + id);
                return id;
            }

            continue;
        }
        // No matches
        incrOptout(br, DECISION_PRIVATE_DEAL);
        br.getLot49Ext().getOptoutReasons().put(getId(), reasons.toString());

        return null;
    }

    public boolean matchDomainTargeting(OpenRtbRequest br) {
        if (realDomainBlacklist == null && realDomainWhitelist == null) {
            return true;
        }
        int impCount = br.getImp().size();

        Site site = br.getSite();

        if (site == null) {
            incrOptout(br, DECISION_DOMAIN_UNMATCHED, impCount);
            br.getLot49Ext().getOptoutReasons().put(getId(), "Got no Site, but targeting a list.");
            return false;
        }

        String domain = site.getDomain();
        domain = domain == null ? null : domain.trim();
        if (domain == null || domain.length() == 0) {
            incrOptout(br, DECISION_DOMAIN_UNMATCHED, impCount);
            br.getLot49Ext().getOptoutReasons().put(getId(),
                            "Got no domain, but targeting a list.");
            return false;
        }
//        LogUtils.debug(getId() + ": XXX: About to check white/black list.");
        if (this.realDomainWhitelist != null && !realDomainWhitelist.match(domain)) {
            incrOptout(br, DECISION_DOMAIN_UNMATCHED, impCount);
            br.getLot49Ext().getOptoutReasons().put(getId(), "'" + domain + "' not in whitelist.");
            return false;
        }
        if (realDomainBlacklist != null && realDomainBlacklist.match(domain)) {
            incrOptout(br, DECISION_DOMAIN_UNMATCHED, impCount);
            br.getLot49Ext().getOptoutReasons().put(getId(), "'" + domain + "' in blacklist.");
            return false;
        }
        return true;
    }

    @Override
    public boolean canBid1(OpenRtbRequest br) {
        final long impCount = br.getImp().size();
        final Map<String, String> optoutReasons = br.getLot49Ext().getOptoutReasons();

        final Lot49Ext lot49Ext = br.getLot49Ext();

        final Site site = br.getSite();

        // 02.03. Domain required
        if (domainRequired) {
            if (site == null || site.getDomain() == null) {
                incrOptout(br, DECISION_DOMAIN_REQUIRED, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(), "No domain received");
                return false;
            }
        }

        // 02.04. Exchanges
        if (exchanges != null && exchanges.size() > 0) {
            final String receivedExch = lot49Ext.getAdapter().getName();
            if (!exchanges.contains(receivedExch)) {
                incrOptout(br, DECISION_EXCHANGE, impCount);
                optoutReasons.put(getId(), "Exch: " + receivedExch + " not in " + exchanges);
                return false;
            }
        }

        // 02.05. SSPs
        if (ssps != null && ssps.size() > 0) {
            final String receivedSsp = lot49Ext.getSsp();
            if (!ssps.contains(receivedSsp)) {
                incrOptout(br, DECISION_SSP, impCount);
                optoutReasons.put(getId(), "SSP: " + receivedSsp + " not in " + ssps);
                return false;
            }
        }

        final Device device = br.getDevice();

        // 02.06 Device makes/models
        if (deviceMakes != null && deviceMakes.size() > 0) {
            if (device == null) {
                incrOptout(br, DECISION_DEVICE, impCount);
                optoutReasons.put(getId(), "Dev: UNKNOWN not in " + deviceMakes);
                return false;
            }
            final String receivedDevice = device.getMake().toLowerCase();
            if (!deviceMakes.contains(receivedDevice)) {
                incrOptout(br, DECISION_DEVICE, impCount);
                optoutReasons.put(getId(), "Dev: " + receivedDevice + " not in " + deviceMakes);
                return false;
            }
        }

        if (deviceModels != null && deviceModels.size() > 0) {
            if (device == null) {
                incrOptout(br, DECISION_DEVICE, impCount);
                optoutReasons.put(getId(), "DevMod: UNKNOWN not in " + deviceModels);
                return false;
            }
            final String receivedDevice = device.getModel().toLowerCase();
            if (!deviceModels.contains(receivedDevice)) {
                incrOptout(br, DECISION_DEVICE, impCount);
                optoutReasons.put(getId(), "DevMod: " + receivedDevice + " not in " + deviceModels);
                return false;
            }
        }

        if (deviceTypes != null && deviceTypes.size() > 0) {
            if (device == null) {
                incrOptout(br, DECISION_DEVICE, impCount);
                optoutReasons.put(getId(), "DevType: UNKNOWN not in " + deviceTypes);
                return false;
            }
            final Integer receivedDevice = device.getDevicetype();
            if (receivedDevice == null) {
                incrOptout(br, DECISION_DEVICE, impCount);
                optoutReasons.put(getId(), "DevType: UNKNOWN type");
                return false;
            }
            if (!deviceTypes.contains(receivedDevice)) {
                incrOptout(br, DECISION_DEVICE, impCount);
                optoutReasons.put(getId(), "DevType: " + receivedDevice + " not in " + deviceTypes);
                return false;
            }
        }

        // 02.07 OS
        if (oses != null && oses.size() > 0) {
            if (device == null) {
                incrOptout(br, DECISION_OS, impCount);
                optoutReasons.put(getId(), "OS: UNKNOWN not in " + oses);
                return false;
            }
            final String receivedOs = (device.getOs() + " " + device.getOsv()).toLowerCase();
            boolean passed = false;
            for (String os : oses) {
                if (receivedOs.startsWith(os.toLowerCase())) {
                    passed = true;
                    break;
                }
            }
            if (!passed) {
                incrOptout(br, DECISION_OS, impCount);
                optoutReasons.put(getId(), "OS: " + receivedOs + " not in " + oses);
                return false;
            }
        }

        // 02.08. Languages
        Content content = site == null ? null : site.getContent();

        if (languages != null && languages.size() > 0) {
            if (device == null && content == null) {
                incrOptout(br, DECISION_LANGUAGE, impCount);
                optoutReasons.put(getId(), "Lang: UNKNOWN not in " + languages);
                return false;
            }
            final Set<String> receivedLangs = new HashSet<String>();

            if (device != null) {
                String l = device.getLanguage();
                if (l != null) {
                    l = l.toLowerCase();
                    if (l.indexOf("_") > -1) {
                        for (String x : l.split("_")) {
                            if (languages.contains(x)) {
                                receivedLangs.add(x);
                                return true;
                            }
                        }
                    } else {
                        receivedLangs.add(l);
                        if (languages.contains(l)) {
                            return true;
                        }
                    }
                }
            }
            if (content != null) {
                String l = content.getLanguage();
                if (l != null) {
                    l = l.toLowerCase();
                    if (l.indexOf("_") > -1) {
                        for (String x : l.split("_")) {
                            if (languages.contains(x)) {
                                receivedLangs.add(x);
                                return true;
                            }
                        }
                    } else {
                        receivedLangs.add(l);
                        if (languages.contains(l)) {
                            return true;
                        }
                    }
                }
            }

            incrOptout(br, DECISION_LANGUAGE, impCount);
            optoutReasons.put(getId(), "None of " + languages + " found in " + receivedLangs);
            return false;

        }

        // 02.09. Browsers
        if (browsers != null && browsers.size() > 0)

        {
            if (device == null) {
                incrOptout(br, DECISION_BROWSER, impCount);
                optoutReasons.put(getId(), "UA: UNKNOWN not in " + browsers);
                return false;
            }

            final String ua = device.getUa();
            if (ua == null)

            {
                incrOptout(br, DECISION_BROWSER, impCount);
                optoutReasons.put(getId(), "UA: UNKNOWN not in " + browsers);
                return false;
            }

            final String fam = lot49Ext.getBrowserFamily();
            final String name = lot49Ext.getBrowserName();

            if (fam == null && name == null)

            {
                incrOptout(br, DECISION_BROWSER, impCount);
                optoutReasons.put(getId(), "UA: UNKNOWN not in " + browsers);
                return false;
            }

            if (!browsers.contains(name) && !browsers.contains(fam))

            {
                incrOptout(br, DECISION_BROWSER, impCount);
                optoutReasons.put(getId(), "UA: " + name + "/" + fam + " not in " + browsers);
                return false;
            }

        }

        final Geo lot49Geo = br.getLot49Ext().getGeo();

        // 02.10. Geo
        boolean geoOk = true;

        if (geos != null && geos.size() > 0) {
            geoOk = false;
            for (TargetingGeo geo : geos) {
                if (geo.matches(br.getUser().getGeo())) {
                    geoOk = true;
                    break;
                }
                if (geo.matches(br.getDevice().getGeo())) {
                    geoOk = true;
                    break;
                }
                if (geo.matches(lot49Geo)) {
                    geoOk = true;
                    break;
                }
            }
        }

        // If geoOk is false at this point, no point in checking negative geos...
        if (geoOk && (negativeGeos != null && negativeGeos.size() > 0)) {
            for (TargetingGeo geo : negativeGeos) {
                if (geo.matches(br.getUser().getGeo())) {
                    geoOk = false;
                    break;
                }
                if (geo.matches(br.getDevice().getGeo())) {
                    geoOk = false;
                    break;
                }
                if (geo.matches(lot49Geo)) {
                    geoOk = false;
                    break;
                }
            }
        }

        if (geoOk) {
            geoOk = matchOrganizations(lot49Geo);
        }

        if (!geoOk) {
            incrOptout(br, DECISION_GEO, impCount);
            br.getLot49Ext().getOptoutReasons().put(getId(),
                            "No match for " + geos + " in User: " + br.getUser().getGeo()
                                            + ", Dev: " + br.getDevice().getGeo() + ", Lot49: "
                                            + br.getLot49Ext().getGeo());
            return false;
        }

        // 02.11. Domains.

        final boolean domainTargetingOk = matchDomainTargeting(br);
        if (!domainTargetingOk) {
            return false;
        }

        // 02.12. URLs
        if (!matchTargetingUrls(br, site)) {
            return false;
        }

        // 02.13. Categories.
        if (targetingCategories != null && targetingCategories.size() > 0) {
            if (site == null) {
                incrOptout(br, DECISION_CATEGORY, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(),
                                "Got no Site, but targetingCategories");
                return false;
            }
            final List<String> cats = site.getCat();
            final List<String> catsP = site.getPagecat();
            if ((cats == null || cats.size() == 0) && (catsP == null || catsP.size() == 0)) {
                incrOptout(br, DECISION_CATEGORY, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(),
                                "Got no Cat/Pagecat, but targetingCategories");
                return false;
            }

            final Set<String> cats2 = new TreeSet<String>();
            if (cats != null) {
                cats2.addAll(cats);
            }
            if (catsP != null) {
                cats2.addAll(catsP);
            }
            if (Sets.intersection(targetingCategories, cats2).size() == 0) {
                incrOptout(br, DECISION_CATEGORY, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(),
                                "None of " + cats2 + " match " + targetingCategories);
                return false;
            }
        }

        final Map ext = lot49Geo == null ? null : lot49Geo.getExt();
        final Lot49ExtGeo lot49extGeo =
                        ext == null ? null : (Lot49ExtGeo) ext.get(Lot49ExtGeo.GEO_EXT_KEY);

        // 02.14. Hours
        if (targetingHour != null) {
            if (lot49extGeo == null) {
                incrOptout(br, DECISION_HOUR, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(), "Cannot determine hour");
                return false;
            }
            final Integer hour = lot49extGeo.getUserHour();
            if (hour == null) {
                incrOptout(br, DECISION_HOUR, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(), "Cannot determine hour");
                return false;
            }
            if (!targetingHour.check(hour)) {
                incrOptout(br, DECISION_HOUR, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(),
                                "Hour " + hour + " not in " + targetingHour);
                return false;
            }
        }

        // 02.15. DOW
        if (targetingDOW != null) {
            if (lot49extGeo == null) {
                incrOptout(br, DECISION_DAY, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(), "Cannot determine DOW");
                return false;
            }
            final Integer dow = lot49extGeo.getUserDow();
            if (dow == null) {
                incrOptout(br, DECISION_DAY, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(), "Cannot determine DOW");
                return false;
            }
            if (!targetingDOW.check(dow)) {
                incrOptout(br, DECISION_DAY, impCount);
                br.getLot49Ext().getOptoutReasons().put(getId(),
                                "DOW " + dow + " not in " + targetingDOW);
                return false;
            }
        }

        // 02.16. Providers
        if (providerTargeting != null && !providerTargeting.isEmpty()) {
            for (Provider provider : providerTargeting.keySet()) {
                ProviderInfoRequired needed = providerTargeting.get(provider);
                if (needed == null) {
                    continue;
                }
                String provName = provider.getName();
                ProviderInfoReceived got = lot49Ext.getProviderInfo().get(provName);
                if (!provider.match(got, needed)) {
                    incrOptout(br, DECISION_PROVIDER, impCount);
                    br.getLot49Ext().getOptoutReasons().put(getId(),
                                    provName + ": Need: " + needed + "; got: " + got);
                    return false;
                }
            }
        }

        return true;

    }

    public boolean isSegmentLogicAnd() {
        return segmentLogicAnd;
    }

    protected boolean segmentLogicAnd = true;

    /**
     * Algorithm:
     * <p>
     * Return <tt>false</tt> if:
     * </p>
     * <ol>
     * <li>If {@link #getUserSegments() we have targeted segments} and the intersection of it with
     * {@link Lot49ExtRemote#getUserSegments() looked up segments} is empty.</li>
     * </ol>
     * Otherwise, return <tt>true</tt>.
     */
    @Override
    public boolean checkSegments(OpenRtbRequest br, UserSegments userSegments) {
        // Pointless sanity check...
        if (!needCanBid2()) {
            return true;
        }
        if (parsedTargetingSegments == null) {
            return true;
        }
        Set<String> userSegmentsSet;
        if (userSegments == null) {
            userSegmentsSet = new HashSet<String>();
        } else {
            userSegmentsSet = userSegments.getSegmentsSet();
        }

        boolean targetedSegments = parsedTargetingSegments.eval(userSegmentsSet);
        // LogUtils.trace("Eval result for user " + br.getUser().getId() + " " + targetedSegments
        // + " for Ad " + getId() + " expression " + parsedTargetingSegments
        // + " segments " + userSegments);
        if (targetedSegments) {
            return true;
        }
        if (userSegmentsSet.isEmpty()) {
            incrOptout(br, DECISION_NO_USER, br.getImp().size());
            br.getLot49Ext().getOptoutReasons().put(getId(), "User not in UserCache");
        } else {
            incrOptout(br, DECISION_WRONG_USER, br.getImp().size());
            br.getLot49Ext().getOptoutReasons().put(getId(), "User segments don't match: "
                            + userSegments + " vs. " + parsedTargetingSegments);
        }
        return false;

    }

    @Override
    public boolean checkFrequencyCap(OpenRtbRequest br,
                    UserFrequencyCapAttributes userFrequencyCap) {
        if (!isCampaignFrequencyCap() && !isStrategyFrequencyCap()) {
            return true;
        }

        boolean result = true;
        if (isCampaignFrequencyCap()) {
            if (userFrequencyCap.getCampaignBidsCount(this, frequencyCapCampaign.getHours())
                            * getWinRate()
                            + userFrequencyCap.getCampaignImpressionsCount(this,
                                            frequencyCapCampaign.getHours()) >= frequencyCapCampaign
                                                            .getMax()) {
                incrOptout(br, DECISION_FREQ_CAP, br.getImp().size());
                br.getLot49Ext().getOptoutReasons().put(getId(),
                                "Frequency cap reached for Campaign" + getCampaignId());

                return false;
            }

        } else if (isStrategyFrequencyCap()) {
            if (userFrequencyCap.getTargetingStrategyBidsCount(this, frequencyCap.getHours())
                            * getWinRate()
                            + userFrequencyCap.getTargetingStrategyImpressionsCount(this,
                                            frequencyCap.getHours()) >= frequencyCap.getMax()) {
                incrOptout(br, DECISION_FREQ_CAP, br.getImp().size());
                br.getLot49Ext().getOptoutReasons().put(getId(),
                                "Frequency cap reached for TS" + getId());
                return false;
            }
        }

        return result;
    }

    @Override
    public SortedMap<String, SortedMap<String, AtomicLong>> getOptoutsByExchange() {
        return optoutsByExchange;
    }

    @Override
    public void setBidPrice(long n) {
        this.bidPrice = n;
    }

    /**
     * @deprecated
     */
    protected void setBidAmount(long n) {
        setBidPrice(n);
    }

    @Override
    // @ExecutionTimeLimit(limit = 10, unit = TimeUnit.MILLISECONDS)
    public long getBidPrice(OpenRtbRequest req) {
        if (req == null) {
            return this.bidPrice;
        }
        Lot49Ext ext = req.getLot49Ext();
        if (this.bidPriceCalculator != null) {
            return this.bidPriceCalculator.getBidPrice(this, req);
        }
        return this.bidPrice;
    }


    @Override
    public void setBidPriceCalculator(BidPriceCalculator bpc) {
        this.bidPriceCalculator = bpc;
    }

    protected BidPriceCalculator bidPriceCalculator;

    public BidPriceCalculator getBidPriceCalculator() {
        return bidPriceCalculator;
    }

    public boolean isDynamicPricing() {
        return bidPriceCalculator != null;
    }


    @Override
    public double getBidPriceCpm(OpenRtbRequest req) {
        return Utils.microToCpm(getBidPrice(req));
    }

    @Override
    public int getBidProbability() {
        return this.bidProbability;
    }

    private final long getCachebuster() {
        return rnd.nextLong();
    }

    public String getCustomUserData() {
        return customUserData;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    public List<TargetingGeo> getGeos() {
        return geos;
    }

    public List<TargetingGeo> getNegativeGeos() {
        return negativeGeos;
    }


    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Random getRnd() {
        return rnd;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Tag findTagById(final String id) {
        for (Tag tag : tags) {
            if (id.equals(tag.getId())) {
                return tag;
            }
        }
        return null;
    }

    @Deprecated
    @Override
    public Set<String> getUserSegments() {
        throw new UnsupportedOperationException();
    }

    public boolean needUserInfo() {
        return getParsedTargetingSegments() != null
                        || (measurementSegments != null && (abTesting || campaignAbTesting));
    }

    public boolean needFrequencyCap() {
        return frequencyCap != null || frequencyCapCampaign != null;
    }

    public boolean needIntegralInfo() {
        return targetingIntegral != null || isDynamicPricing();
    }

    public boolean needExperimentInfo() {
        return abTesting || campaignAbTesting;
    }

    public boolean isCampaignFrequencyCap() {
        return frequencyCapCampaign != null;
    }

    public boolean isStrategyFrequencyCap() {
        return frequencyCap != null;
    }

    @Override
    public boolean needCanBid2() {
        return needUserInfo() || needFrequencyCap() || needIntegralInfo() || needExperimentInfo()
                        || isDynamicPricing();
    }

    @Override
    public String toString() {
        return "Ad " + getId();
    }

    protected TargetingHour targetingHour;

    @Override
    public TargetingHour getTargetingHour() {
        return targetingHour;
    }

    protected TargetingDOW targetingDOW;

    @Override
    public TargetingDOW getTargetingDOW() {
        return targetingDOW;
    }

    private Set<Pattern> organizations;

    public static final String REGEXP_NON_ALPHANUM_WITH_WILDCARD = "[^A-Za-z0-9 \\*]";

    public void setOrganizations(Collection<String> o) {

        if (o == null || o.size() == 0) {
            this.organizations = null;
            return;
        }
        this.organizations = new HashSet<Pattern>();
        for (String org : o) {
            String origOrg = org;
            if (org == null) {
                continue;
            }
            org = org.trim();
            if (org.length() == 0) {
                continue;
            }
            org = org.toLowerCase();
            org = org.replaceAll(REGEXP_NON_ALPHANUM_WITH_WILDCARD, " ");
            org = Utils.removeRedundantWhitespace(org);
            org = org.trim();
            org = org.replaceAll("\\*", ".*");
            if (!origOrg.equalsIgnoreCase(org)) {
                LogUtils.info(getId() + ": Normalized " + origOrg + " to " + org);
            }
            this.organizations.add(Pattern.compile(org));
        }
    }

    @Override
    public Set<Pattern> getOrganizations() {
        return organizations;
    }


    private final AtomicLong realBids = new AtomicLong(0);
    private final AtomicLong realWins = new AtomicLong(0);
    private AtomicLong realBidAmount = new AtomicLong(0);
    private AtomicLong realSpendAmount = new AtomicLong(0);
    private SortedMap<String, AtomicLong> realBidsByExchange = new TreeMap<String, AtomicLong>();

    public long getRealBids() {
        return this.realBids.get();
    }

    public long getRealWins() {
        return this.realWins.get();
    }

    public SortedMap<String, AtomicLong> getRealBidsByExchange() {
        return realBidsByExchange;
    }

    public long getRealSpendAmount() {
        return realSpendAmount.get();
    }

    public long getRealBidAmount() {
        return realBidAmount.get();
    }

    public void storeAdState() {
        this.realBids.set(this.getBids());
        this.realWins.set(this.getWins());
        this.realSpendAmount.set(this.getSpendAmount());
        this.realBidAmount.set(this.getBidAmount());

        for (String key : this.bidsByExchange.keySet()) {
            this.realBidsByExchange.put(key, this.bidsByExchange.get(key));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Ad)) {
            return false;
        }
        final Ad oAd = (Ad) o;
        return id.equals(oAd.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
