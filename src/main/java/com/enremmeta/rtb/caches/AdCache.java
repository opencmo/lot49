package com.enremmeta.rtb.caches;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.Marker;
import org.codehaus.groovy.control.CompilationFailedException;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.BidPriceCalculator;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.api.Lot49Plugin;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ClientConfig;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.DaoCounters;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.dao.DbService;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.rtb.jersey.AdminSvc;
import com.enremmeta.rtb.sandbox.EnremmetaGroovyClassLoader;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

import groovy.lang.GroovyClassLoader;

/**
 * <p>
 * This is a {@link AdCacheConfig#getTtlMinutes() scheduled} process that, on waking up, well,
 * {@link #run() runs} -- see that method for more info.
 * </p>
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class AdCache implements Runnable {
    private static long runNumber = 0l;

    public static final Format YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");

    public DaoShortLivedMap<String> getFcMap() {
        return fcMap;
    }

    public AdCacheConfig getConfig() {
        return config;
    }

    public static final class BudgetAllocationResult {

        private String msg = "";
    }

    public final class LoadResultHolder {

        private Boolean updated = false;

        private Class<Lot49Plugin> pluginClass;

        /**
         * @return the Updated
         */
        public Boolean getUpdated() {
            return updated;
        }

        /**
         * @param updated
         *            the Updated to set
         */
        public void setUpdated(Boolean updated) {
            this.updated = updated;
        }

        /**
         * @return the pluginClass
         */
        public Class<Lot49Plugin> getPluginClass() {
            return pluginClass;
        }

        /**
         * @param klazz
         *            the pluginClass to set
         */
        public void setPluginClass(Class<Lot49Plugin> klazz) {
            this.pluginClass = klazz;
        }

    }

    public final class ValidationResultHolder {

        private Boolean valid = false;

        private String msg;

        public ValidationResultHolder(Boolean valid, String msg) {
            super();
            this.valid = valid;
            this.msg = msg;
        }

        /**
         * @return the valid
         */
        public Boolean getValid() {
            return valid;
        }

        /**
         * @param valid
         *            the valid to set
         */
        public void setValid(Boolean valid) {
            this.valid = valid;
        }

        /**
         * @return the msg
         */
        public String getMsg() {
            return msg;
        }

        /**
         * @param msg
         *            the msg to set
         */
        public void setMsg(String msg) {
            this.msg = msg;
        }

    }

    final String AD_CONFIG_SCRIPT_PACKAGE = "com.enremmeta.rtb.groovy.tc";

    final String AD_CONFIG_SCRIPT_SUBDIR = AD_CONFIG_SCRIPT_PACKAGE.replaceAll("\\.", "/");

    private final AdCacheConfig config;

    private final Map<String, ClientConfig> clients;

    private final int ttlMinutes;

    private String status = "OK";

    private final RedisClient client;

    /**
     * Only for use in Admin tools.
     */
    public String setBudget(String adId, long amount) throws Lot49Exception {
        final RedisConnection<String, String> redisCon = getRedisConnection();
        try {
            return redisCon.set(KVKeysValues.BUDGET_PREFIX + adId, String.valueOf(amount));
        } finally {
            redisCon.close();
        }
    }

    public String getStatus() {
        return status;
    }

    private static final class AdCollectionHolder {

        private final Map<String, Ad> adMap = new HashMap<String, Ad>();

        private final Map<Ad, String> invalidAds = new HashMap<Ad, String>();

        private final Map<String, String> uncompilableAds = new HashMap<String, String>();

        private final List<Ad> zeroBudgetAds = new ArrayList<Ad>();
        private final List<Ad> ineligibleAds = new ArrayList<Ad>();

        private final List<Ad> blackListedAds = new ArrayList<Ad>();
        private final Map<String, Long> timeoutMap = new HashMap<String, Long>();

        private Ad[] bid1 = new Ad[0];

        private Ad[] bid2 = new Ad[0];

        private Ad[] all = new Ad[0];

    }

    private volatile AdCollectionHolder adCollectionHolder = new AdCollectionHolder();
    private AdCollectionHolder adCollectionHolder2 = new AdCollectionHolder();

    public Ad getAd(String id) {
        return adCollectionHolder.adMap.get(id);
    }

    /**
     * Used for show list of ads with zero budget in the admin tool (see
     * {@link AdminSvc#index(javax.ws.rs.container.AsyncResponse, String)})
     */
    public List<Ad> getZeroBudgetAds() {
        return adCollectionHolder.zeroBudgetAds;
    }

    public List<Ad> getIneligibleAds() {
        return adCollectionHolder.ineligibleAds;
    }

    public List<Ad> getBlackListedAds() {
        return adCollectionHolder.blackListedAds;
    }

    /**
     * Used for show list of invalid ads in the admin tool (see
     * {@link AdminSvc#index(javax.ws.rs.container.AsyncResponse, String)})
     */
    public Map<Ad, String> getInvalidAds() {
        return adCollectionHolder.invalidAds;
    }

    /**
     * Used for show list of uncompilable ads in the admin tool (see
     * {@link AdminSvc#index(javax.ws.rs.container.AsyncResponse, String)})
     */
    public Map<String, String> getUncompilableAds() {
        return adCollectionHolder.uncompilableAds;
    }

    public Map<String, Long> getTimeoutMap() {
        return adCollectionHolder.timeoutMap;
    }

    public AdCache(AdCacheConfig config) throws Lot49Exception {
        super();
        this.config = config;
        this.ttlMinutes = config.getTtlMinutes();

        this.clients = ServiceRunner.getInstance().getConfig().getClients();

        PacingServiceConfig pacingConfig = this.config.getPacing();

        long messageTtl = pacingConfig.getMessageTtlMinutes();
        if (messageTtl <= 0) {
            throw new Lot49Exception(
                            "Expected positive integer as 'messageTtlMinutes' in pacing configuration.");
        }

        this.client = new RedisClient(pacingConfig.getRedis().getHost(),
                        pacingConfig.getRedis().getPort());

    }

    private final static Map<String, Class<Lot49Plugin>> GROOVY_CLASSES =
                    new HashMap<String, Class<Lot49Plugin>>();

    /**
     * Load the groovy class. The contract is such that this
     * {@link LogUtils#error(Object, Throwable) logs an error} and returns <tt>null</tt> if not
     * successful.
     */
    @SuppressWarnings("unchecked")
    private LoadResultHolder load(GroovyClassLoader loader, File f, Boolean forceUpdate)
                    throws CompilationFailedException, IOException, InstantiationException,
                    IllegalAccessException {
        if (!f.exists()) {
            error("Cannot find file " + f);
            return null;
        }

        LoadResultHolder resultHolder = new LoadResultHolder();

        long t0 = getBidderCalendar().currentTimeMillis();
        String checksum = DigestUtils.md5Hex(new FileInputStream(f));
        String msg = "Calculated checksum class from " + f + " in "
                        + (getBidderCalendar().currentTimeMillis() - t0) + " millis";
        // debug(msg);

        Class<Lot49Plugin> groovyClass = null;

        if (!forceUpdate) {
            groovyClass = GROOVY_CLASSES.get(checksum);
        }

        if (runNumber > 1) {
            Utils.noop();
        }
        if (groovyClass == null) {

            t0 = getBidderCalendar().currentTimeMillis();
            if (f.getName().equals("Tag_2184_DemoCreative_1818_DemoStrategy.groovy")) {
                Utils.noop();
            }
            groovyClass = loader.parseClass(f);
            msg = "Loaded class from " + f + " with checksum " + checksum + " in "
                            + (getBidderCalendar().currentTimeMillis() - t0) + " millis";
            warn(msg);
            GROOVY_CLASSES.put(checksum, groovyClass);

            resultHolder.setUpdated(true);
        }
        t0 = getBidderCalendar().currentTimeMillis();

        resultHolder.setPluginClass(groovyClass);

        return resultHolder;
    }

    private static BidderCalendar getBidderCalendar() {
        // TODO this can later be replaced by ThreadLocal version for tests.
        return BidderCalendar.getInstance();
    }

    private Boolean loadTagsByAd(File groovyPkgDir, GroovyClassLoader loader, String adFileName,
                    String adId) {
        String tagFileSuffix = adFileName.substring(adFileName.indexOf(adId) - 1);

        final FilenameFilter tagFilter = new FilenameFilter() {

            @Override
            public final boolean accept(final File dir, final String name) {
                return name.startsWith(Lot49Constants.TAG_FILENAME_PREFIX)
                                && name.endsWith(tagFileSuffix);
            }

        };

        Boolean updated = false;

        final File[] tagFileList = groovyPkgDir.listFiles(tagFilter);
        for (File t : tagFileList) {
            if (loadTag(loader, t)) {
                updated = true;
            }
        }

        return updated;
    }

    private Boolean loadTag(GroovyClassLoader loader, File f) {
        LoadResultHolder resultHolder = null;
        try {
            resultHolder = load(loader, f, false);
        } catch (Throwable t) {
            warn("Error loading " + f, t);
        }
        return resultHolder.getUpdated();
    }

    private Ad loadAd(File groovyPkgDir, GroovyClassLoader loader, File f) {

        String adFileName = f.getName();
        String adId = adFileName.split("_")[1];

        Ad ad = null;
        try {
            long t0 = getBidderCalendar().currentTimeMillis();

            // First we pre-load any changed Tags so that
            // when we load Ads the class loader has the latest classes
            Boolean needAdUpdate = loadTagsByAd(groovyPkgDir, loader, adFileName, adId);

            Class<Lot49Plugin> groovyClass = load(loader, f, needAdUpdate).getPluginClass();
            ad = (Ad) groovyClass.newInstance();
            String msg = "Instantiated object in " + (getBidderCalendar().currentTimeMillis() - t0)
                            + " millis";
            debug(msg);
            if (ad == null) {
                return null;
            }
        } catch (Throwable t) {
            warn("Error loading " + f, t);
            LogUtils.logAdLoadingError(runNumber, f, t.getMessage());
            final String adName = (f == null ? "?" : f.getName());
            adCollectionHolder2.uncompilableAds.put(adName, t.getMessage());
            return null;
        }
        try {
            List<String> problems = ad.validate();
            if (problems != null && problems.size() > 0) {
                StringBuilder msg = new StringBuilder(0);
                for (String problem : problems) {
                    if (msg.length() > 0) {
                        msg.append("; ");
                    }
                    msg.append(problem);
                }
                warn("Errors in " + f + ":\n" + msg);
                adCollectionHolder2.invalidAds.put(ad, msg.toString());
                adCollectionHolder2.adMap.put(ad.getId(), ad);
                LogUtils.logDecision(runNumber, ad, null, Lot49Constants.DECISION_VALIDATION, null,
                                null, msg.toString());
                return null;
            }
            debug("Loaded ad " + ad + " from " + f);
            return ad;
        } catch (Throwable t) {
            LogUtils.logDecision(runNumber, ad, null, Lot49Constants.DECISION_LOADING_ERROR, null,
                            null, t.getMessage());
            adCollectionHolder2.invalidAds.put(ad, t.getMessage());
            return null;
        }
    }

    private final Map<String, BidPriceCalculator> bpcIdToBpc =
                    new HashMap<String, BidPriceCalculator>();

    private BidPriceCalculator loadBidPriceCalculator(File groovyPkgDir, GroovyClassLoader loader,
                    File f) throws Lot49Exception {
        String bpcFileName = f.getName();

        String bpcId = null;

        try {
            bpcId = bpcFileName.split("_")[1].split("\\.")[0];
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new Lot49Exception("Bad name format for " + f);
        }

        BidPriceCalculator bpc = null;
        try {
            long t0 = getBidderCalendar().currentTimeMillis();
            // We don't need to refresh in this version because we load the class and then
            // set it in the ad.
            Class<Lot49Plugin> groovyClass = load(loader, f, false).getPluginClass();
            bpc = (BidPriceCalculator) groovyClass.newInstance();
            String msg = "Instantiated object in " + (getBidderCalendar().currentTimeMillis() - t0)
                            + " millis";
            debug(msg);
            if (bpc != null) {
                if (!bpc.getId().equals(bpcId)) {
                    warn("Error in BidPriceCalculator: code defines " + bpc.getId() + " and file "
                                    + bpcId);
                    return null;
                }
                bpcIdToBpc.put(bpc.getId(), bpc);
            }
        } catch (Throwable t) {
            warn("Error loading " + f, t);
            return null;
        }
        return bpc;
    }

    public void init() throws Lot49Exception {

        String shortTermDbName = config.getShortTermDb();
        LogUtils.info("Using " + shortTermDbName + " for NUrl and BidInFlightInfo maps.");
        shortTermDb = Bidder.getInstance().getDbServiceByName(shortTermDbName);
        nurlMap = shortTermDb.getDaoShortLivedMap(String.class);
        fcMap = shortTermDb.getDaoShortLivedMap(String.class);
        bidInFlightInfoMap = shortTermDb.getDaoShortLivedMap(BidInFlightInfo.class);
        cwrMap = shortTermDb.getDaoShortLivedMap(CacheableWebResponse.class);

        winRateCounters = shortTermDb.getDaoCounters();
        LogUtils.info("Using " + winRateCounters + " for winRateCounters.");

        ScheduledExecutorService exec = Bidder.getInstance().getScheduledExecutor();
        this.scheduledSelf = exec.schedule(this, 1, TimeUnit.SECONDS);
        info("Scheduling first run in 1 second: " + this.scheduledSelf);
    }

    public DbService getShortTermDb() {
        return shortTermDb;
    }

    private DaoShortLivedMap<CacheableWebResponse> cwrMap;
    private DaoCounters winRateCounters;

    public DaoShortLivedMap<CacheableWebResponse> getCwrMap() {
        return cwrMap;
    }

    private DaoShortLivedMap<String> nurlMap;

    private DaoShortLivedMap<String> fcMap;

    private DaoShortLivedMap<BidInFlightInfo> bidInFlightInfoMap;

    public DaoShortLivedMap<String> getNurlMap() {
        return nurlMap;
    }

    public DaoShortLivedMap<BidInFlightInfo> getBidInFlightInfoMap() {
        return bidInFlightInfoMap;
    }

    private DbService shortTermDb;

    public Ad[] getBid1() {
        return adCollectionHolder.bid1;
    }

    public Ad[] getBid2() {
        return adCollectionHolder.bid2;
    }

    public Ad[] getAll() {
        return adCollectionHolder.all;
    }

    private final static FilenameFilter adFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith(Lot49Constants.AD_FILENAME_PREFIX) && name.endsWith(".groovy");
        }

    };


    private final static FilenameFilter bidPriceCalculatorFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith(Lot49Constants.BID_PRICE_CALCULATOR_FILENAME_PREFIX)
                            && name.endsWith(".groovy");
        }

    };

    private final static String formatMicroBudget(long value) {

        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);

        formatter.format("$%1.2f", (float) value / 1000000.);
        formatter.close();
        String retval = sb.toString();

        retval += " (micro$" + value + ")";

        return retval;
    }

    private boolean first = true;

    /**
     * <p>
     * Reserve money from the main budget cache for a given ad. The algorithm is as follows:
     * <ol>
     * </ol>
     * </p>
     *
     * @param redisCon
     *            Connection to the pacing cache.
     * @param ad
     * @return
     * @throws Lot49Exception
     */
    private BudgetAllocationResult reserveMoney(RedisConnection<String, String> redisCon, Ad ad)
                    throws Lot49Exception {
        // How many instances do we have now?

        final BudgetAllocationResult bar = new BudgetAllocationResult();
        StringBuilder barMsg = new StringBuilder();

        Ad prevAd = getAd(ad.getId());

        if (prevAd == null) {
            if (first) {
                barMsg.append(debug(ad, "No last period available, I just started working."));
            } else {
                barMsg.append(warn(ad, "Ad not found for last period."));
            }
        } else {
            prevAd.storeAdState();
            if (prevAd.getSpendAmount() > 0) {

                long prevSpend = winRateCounters.addAndGet(
                                KVKeysValues.PREVIOUS_SPEND_AMOUNT_PREFIX + ad.getId(),
                                prevAd.getSpendAmount());

                if (prevSpend > 0) {
                    barMsg.append(debug(ad, "Spend: Added last period spend "
                                    + prevAd.getSpendAmount() + " to "
                                    + (KVKeysValues.PREVIOUS_SPEND_AMOUNT_PREFIX + ad.getId())
                                    + " for " + prevSpend));
                }
            }
        }

        if (!Bidder.getInstance().getOrchestrator().isBidder()) {
            barMsg.append(debug(ad, "I am not a bidder, done."));
            ad.setBidsToMake(0);
            bar.msg = barMsg.toString();
            return bar;
        }

        String startsOnStr = redisCon.get(KVKeysValues.STARTS_ON_PREFIX + ad.getId());
        final DateTime now = getBidderCalendar().currentDateTime();
        String s = "";
        if (startsOnStr != null) {
            try {
                if (getBidderCalendar().toDateTime(startsOnStr).isAfter(now)) {
                    s = "Starts on date " + startsOnStr + " is after now (" + now + "), bye now.";
                    barMsg.append(warn(ad, s));
                    LogUtils.logDecision(runNumber, ad, null, Lot49Constants.DECISION_DATE, null,
                                    null, s);
                    ad.setBidsToMake(0);
                    bar.msg = barMsg.toString();
                    return bar;
                }
            } catch (IllegalArgumentException iae) {
                s = "Could not parse start datetime " + startsOnStr + ", not starting.";
                barMsg.append(error(ad, s, iae));
                LogUtils.logDecision(runNumber, ad, null, Lot49Constants.DECISION_DATE, null, null,
                                s);
                ad.setBidsToMake(0);
                bar.msg = barMsg.toString();
                return bar;
            }
        }

        String endsOnStr = redisCon.get(KVKeysValues.ENDS_ON_PREFIX + ad.getId());

        final DateTime tomorrow = now.plusDays(1);
        DateTime endsOn = tomorrow;
        boolean asap = false;

        if (endsOnStr != null) {
            if (endsOnStr.equalsIgnoreCase(KVKeysValues.PACING_ASAP)) {
                asap = true;
                barMsg.append(debug(ad, "Ad to end ASAP - greedy strategy."));
            } else {
                try {
                    endsOn = getBidderCalendar().toDateTime(endsOnStr);

                } catch (IllegalArgumentException iae) {
                    endsOn = tomorrow;
                    s = "Could not parse end datetime " + endsOnStr + ", defaulting to " + endsOn;
                    barMsg.append(warn(ad, s, iae));
                }
            }
        } else {
            endsOn = tomorrow;
            barMsg.append(debug(ad,
                            "Could not find end datetime, defaulting to same time tomorrow: "
                                            + tomorrow));
        }

        final String bidPriceKey = KVKeysValues.BID_PRICE_PREFIX + ad.getId();
        String bidPrice = redisCon.get(bidPriceKey);
        if (bidPrice != null && bidPrice.trim().length() > 0) {
            try {
                long bidPriceLong = Long.valueOf(bidPrice);
                barMsg.append(debug(ad, "Read bid price from cache: " + bidPriceLong));
                ad.setBidPrice(bidPriceLong);
            } catch (NumberFormatException a) {
                barMsg.append(error(ad, "Cannot parse bid price: " + bidPrice + " under  "
                                + bidPriceKey + "; will keep " + ad.getBidPrice(null)));
            }
        } else {
            barMsg.append(debug(ad, "Cannot find bid price: under " + bidPriceKey + "; will keep "
                            + ad.getBidPrice(null)));
        }

        final String remainingInStoreStr = redisCon.get(KVKeysValues.BUDGET_PREFIX + ad.getId());
        barMsg.append(debug(ad, "Remaining in pacing cache: " + remainingInStoreStr
                        + ", to end on : " + endsOnStr));

        if (!asap && endsOn.isBefore(now)) {
            s = "Ends on date " + endsOn + " is before now (" + now + "). Good-bye.";
            barMsg.append(warn(ad, s));
            LogUtils.logDecision(runNumber, ad, null, Lot49Constants.DECISION_DATE, null, null, s);
            ad.setBidsToMake(0);
            bar.msg = barMsg.toString();
            return bar;
        }

        boolean unlimited = remainingInStoreStr != null
                        && remainingInStoreStr.equalsIgnoreCase(KVKeysValues.BUDGET_UNLIMITED);
        if (unlimited) {
            barMsg.append(debug(ad, "Setting unlimited budget: " + Long.MAX_VALUE));
        }
        final long remainingInStore = remainingInStoreStr == null ? 0
                        : (unlimited ? Long.MAX_VALUE : Long.valueOf(remainingInStoreStr));

        if (remainingInStore <= 0) {
            s = "No money left: " + formatMicroBudget(remainingInStore);
            barMsg.append(debug(ad, s));
            LogUtils.logDecision(runNumber, ad, null, Lot49Constants.DECISION_BUDGET, null, null,
                            s);
            ad.setBidsToMake(0l);
            bar.msg = barMsg.toString();
            return bar;
        }

        int minsLeft = Minutes.minutesBetween(now, endsOn).getMinutes();
        if (minsLeft < 0) {
            // Can this happen?
            minsLeft = -minsLeft;
        }

        long periodLength = ttlMinutes;
        if (prevAd != null) {
            periodLength = (ad.getLoadedOn() - prevAd.getLoadedOn()) / 1000 / 60;
            barMsg.append(debug(ad, periodLength
                            + " minutes passed since last load, we will use this number."));
        }
        final long refreshPeriodsLeft = minsLeft / periodLength;

        int peerCount = getNumberOfPeers(redisCon);
        final String pacingStrategy = config.getPacing().getBudgetAllocationStrategy();

        if (!unlimited) {
            barMsg.append(debug(ad,
                            "will split budget reservation among  " + peerCount
                                            + " peers found (budget allocation strategy is "
                                            + pacingStrategy + ")"));
        }
        long wins = winRateCounters.get(KVKeysValues.WIN_COUNT_PREFIX + ad.getId());
        long bids = winRateCounters.get(KVKeysValues.BID_COUNT_PREFIX + ad.getId());
        long spendAmount = winRateCounters.get(KVKeysValues.SPEND_AMOUNT_PREFIX + ad.getId());
        long bidAmount = winRateCounters.get(KVKeysValues.BID_AMOUNT_PREFIX + ad.getId());
        long projectedSpend = remainingInStore / peerCount / (asap ? 1 : refreshPeriodsLeft);
        barMsg.append(debug(ad, "Minutes left from " + now + " until " + endsOn + ": "
                        + (asap ? "assuming 1 (ASAP)" : minsLeft) + "; at refresh interval "
                        + periodLength + " this leaves "
                        + (asap ? "assuming 1 (ASAP)" : refreshPeriodsLeft) + ", with " + peerCount
                        + " peers we will allocate "
                        + (unlimited ? (Long.MAX_VALUE + " (UNLIMITED)")
                                        : (formatMicroBudget(remainingInStore) + "/" + peerCount
                                                        + "/" + (asap ? 1 : refreshPeriodsLeft)))
                        + "=" + formatMicroBudget(projectedSpend) + ". "));

        if (prevAd == null) {
            if (first) {
                barMsg.append(debug(ad, "No last period available, I just started working."));
            } else {
                barMsg.append(error(ad, "Ad not found for last period."));
            }
        } else {
            String spendKey = KVKeysValues.PREVIOUS_SPEND_AMOUNT_PREFIX + ad.getId();
            long projectedSpend0 = projectedSpend;
            long lastPeriodProjectedSpend = prevAd.getProjectedSpend();
            long lastPeriodRealSpend = winRateCounters.getAndSet(spendKey, 0);
            long unspentAmountReal = lastPeriodProjectedSpend - lastPeriodRealSpend;

            barMsg.append(debug(ad, "Real unspent amount is " + lastPeriodProjectedSpend + "-"
                            + lastPeriodRealSpend + "=" + unspentAmountReal));
            long unspentAmountAdjusted = unspentAmountReal;
            if (unspentAmountReal < 0) {
                barMsg.append(debug(ad, "Unspent amount is too low: " + unspentAmountReal
                                + ", will adjust to 0"));
                long someoneElsesUnspentAmount =
                                winRateCounters.addAndGet(spendKey, -unspentAmountReal);
                barMsg.append(debug(ad, "Adding " + (-unspentAmountReal)
                                + " to someone else's spend amount: " + someoneElsesUnspentAmount));
                unspentAmountAdjusted = 0;
            }
            projectedSpend += unspentAmountAdjusted;
            barMsg.append(debug(ad, "Unspent amount from last period is " + lastPeriodProjectedSpend
                            + "-" + lastPeriodRealSpend + "=" + unspentAmountAdjusted
                            + " (adjusted from " + unspentAmountReal + "), adding that to "
                            + projectedSpend0 + " for new total of " + projectedSpend));

        }
        ad.setProjectedSpend(projectedSpend);

        double avgWinPrice = 0;
        double winRate = 0;
        if (bids != 0 && wins != 0) {
            avgWinPrice = ((double) spendAmount) / wins;
            barMsg.append(debug(ad, "Average win price=" + spendAmount + "/" + wins + "="
                            + avgWinPrice + ". "));

            double avgBidPrice = ((double) bidAmount) / bids;
            barMsg.append(debug(ad, "Average bid price=" + bidAmount + "/" + bids + "="
                            + avgBidPrice + ". "));

            winRate = ((double) wins) / bids;
            barMsg.append(debug(ad, "Win rate  (wins/bids=" + wins + "/" + bids + ")=" + winRate));
        } else {
            barMsg.append(debug(ad, "Win rate  (wins/bids=" + wins + "/" + bids + ")=" + winRate));
        }
        if (winRate < config.getPacing().getWinRateMin())

        {
            barMsg.append(debug(ad, "Real win rate  (wins/bids=" + wins + "/" + bids + ")="
                            + winRate + " is less than " + config.getPacing().getWinRateMin()
                            + ", set to " + config.getPacing().getEffectiveWinRateIfLessThanMin()
                            + ": "));
            winRate = config.getPacing().getEffectiveWinRateIfLessThanMin();
        }

        long myBidPrice = ad.getBidPrice(null);
        if (myBidPrice == 0 && ad.isDynamicPricing()) {
            barMsg.append(debug(ad, "Found 0 price and dynamic pricing:"));
            long prevBidAmount = prevAd.getBidAmount();
            long prevBids = prevAd.getBids();
            if (prevAd == null || prevBidAmount == 0 || prevBids == 0) {
                myBidPrice = Bidder.getInstance().getConfig().getInitialDynamicPrice();
                barMsg.append(debug(ad, "No amount in previous ad, will just use " + myBidPrice));
            } else {
                myBidPrice = prevBidAmount / bids;
                barMsg.append(debug(ad, "Setting my price for now to average bid from before: "
                                + prevBidAmount + "/" + bids + "=" + myBidPrice));
            }
        }
        long impsNeeded = 0;

        if (avgWinPrice > 0)

        {
            impsNeeded = Math.round(Math.ceil(projectedSpend / avgWinPrice));
            barMsg.append(debug(ad,
                            "Based on projected spend of " + projectedSpend
                                            + ", and average win price " + avgWinPrice
                                            + ",  we want to win " + projectedSpend + "/"
                                            + avgWinPrice + "=" + impsNeeded + " impressions."));
        } else

        {
            impsNeeded = Math.round(Math.ceil(projectedSpend / myBidPrice));
            barMsg.append(debug(ad,
                            "Based on projected spend of " + projectedSpend + ", and bid price "
                                            + myBidPrice + ",  we want to win " + projectedSpend
                                            + "/" + myBidPrice + "=" + impsNeeded
                                            + " impressions."));
        }

        long bidsNeeded = Math.round(Math.ceil(impsNeeded / winRate));
        barMsg.append(debug(ad, "For " + impsNeeded + " impressions at " + winRate
                        + " win rate we need to make " + bidsNeeded + " bids."));

        ad.setBidsToMake(bidsNeeded);
        bar.msg = barMsg.toString();
        return bar;
    }

    private File getReadingFile() {
        String adsDir = config.getDir();
        File f = new File(adsDir, "reading.txt");
        return f;
    }

    private Map<String, byte[]> ownerKeys = null;

    public byte[] getOwnerKey(String owner) {
        if (ownerKeys == null) {
            return null;
        }
        return ownerKeys.get(owner);
    }

    private String refreshOwnerKeys(RedisConnection<String, String> redisCon) {
        String msg = info("Refreshing owner keys");
        List<String> keys = redisCon.keys(KVKeysValues.OWNER_KEY_PREFIX + "*");
        Map<String, byte[]> newKeys = new HashMap<String, byte[]>();
        for (String k : keys) {
            String owner = k.substring(KVKeysValues.OWNER_KEY_PREFIX.length());
            String base64 = redisCon.get(k);
            byte[] key = Base64.decodeBase64(base64);
            LogUtils.info("Key for " + owner + ": " + base64 + " (" + Arrays.asList(key) + ")");
            newKeys.put(owner, key);
        }
        ownerKeys = newKeys;
        return msg;
    }

    private int getNumberOfPeers(RedisConnection<String, String> redisCon) throws Lot49Exception {
        int countOfBidders;
        String numberOfBiddersString = redisCon.get(KVKeysValues.BIDDER_COUNT_KEY);
        if (numberOfBiddersString != null) {
            countOfBidders = Integer.valueOf(numberOfBiddersString);
        } else {
            Orchestrator orchestrator = Bidder.getInstance().getOrchestrator();
            countOfBidders = orchestrator.getNumberOfPeers();
        }
        return countOfBidders;
    }

    private ValidationResultHolder isValidAd(String adId,
                    RedisConnection<String, String> redisCon) {

        StringBuilder logMessage = new StringBuilder();
        String s = "";
        long remainingInStore = 0;

        final String remainingInStoreStr = redisCon.get(KVKeysValues.BUDGET_PREFIX + adId);

        if (remainingInStoreStr != null) {
            if (remainingInStoreStr.equalsIgnoreCase(KVKeysValues.BUDGET_UNLIMITED)) {
                remainingInStore = Long.MAX_VALUE;
            } else {
                remainingInStore = Long.valueOf(remainingInStoreStr);
            }
        }

        if (remainingInStore <= 0) {
            s = "No money left: " + formatMicroBudget(remainingInStore);
            logMessage.append(debug(adId, s));

            return new ValidationResultHolder(false, logMessage.toString());
        }

        final DateTime now = getBidderCalendar().currentDateTime();
        String startsOnStr = redisCon.get(KVKeysValues.STARTS_ON_PREFIX + adId);
        String endsOnStr = redisCon.get(KVKeysValues.ENDS_ON_PREFIX + adId);

        if (startsOnStr != null) {
            try {
                if (getBidderCalendar().toDateTime(startsOnStr).isAfter(now)) {
                    s = "Starts on date " + startsOnStr + " is after now (" + now + "), bye now.";
                    logMessage.append(warn(adId, s));

                    return new ValidationResultHolder(false, logMessage.toString());
                }
            } catch (IllegalArgumentException iae) {
                s = "Could not parse start datetime " + startsOnStr + ", not starting.";
                logMessage.append(error(adId, s, iae));

                return new ValidationResultHolder(false, logMessage.toString());
            }
        }

        final DateTime tomorrow = now.plusDays(1);
        DateTime endsOn = tomorrow;
        boolean asap = false;

        if (endsOnStr != null) {
            if (endsOnStr.equalsIgnoreCase(KVKeysValues.PACING_ASAP)) {
                asap = true;
                logMessage.append(debug(adId, "Ad to end ASAP - greedy strategy."));
            } else {
                try {
                    endsOn = getBidderCalendar().toDateTime(endsOnStr);

                } catch (IllegalArgumentException iae) {
                    endsOn = tomorrow;
                    s = "Could not parse end datetime " + endsOnStr + ", defaulting to " + endsOn;
                    logMessage.append(warn(adId, s, iae));
                }
            }
        } else {
            endsOn = tomorrow;
            logMessage.append(
                            debug(adId, "Could not find end datetime, defaulting to same time tomorrow: "
                                            + tomorrow));
        }

        if (!asap && endsOn.isBefore(now)) {
            s = "Ends on date " + endsOn + " is before now (" + now + "). Good-bye.";
            logMessage.append(warn(adId, s));

            return new ValidationResultHolder(false, logMessage.toString());
        }

        return new ValidationResultHolder(true, logMessage.toString());
    }

    @Deprecated
    private final Map<String, String> tmpAdIdToBpcId = new HashMap<String, String>();

    @Deprecated
    private void tmpPopulateAdIdToBpcId() throws Lot49Exception {
        String url = "jdbc:mysql://db.opendsp.com:3306/opendsp";
        String username = "opendsp2";
        String password = "barabule37";
        System.out.println("Connecting database...");
        Connection con;
        try {
            con = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new Lot49Exception(e);
        }

        try {
            tmpAdIdToBpcId.clear();
            PreparedStatement ps = con.prepareStatement(
                            "SELECT targeting_strategy_id, bid_price_algorithm_id FROM ld_targeting_strategy WHERE NOT ISNULL(bid_price_algorithm_id) AND bid_price_algorithm_id > 0 ");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String adId = String.valueOf(rs.getLong(1));
                String bpcId = String.valueOf(rs.getLong(2));
                tmpAdIdToBpcId.put(adId, bpcId);
            }
            LogUtils.debug("Found the following algorithms for ads: " + tmpAdIdToBpcId);
            rs.close();
            ps.close();
        } catch (SQLException e) {
            LogUtils.error(e);
        }
        try {
            con.close();
        } catch (SQLException e) {
            LogUtils.error(e);
        }
    }



    public List<Ad> loadAdsFromDir(String adsDir, String subDir, Boolean useSanboxClassloader,
                    StringBuilder msg, RedisConnection<String, String> redisCon)
                    throws Lot49Exception {
        // tmpPopulateAdIdToBpcId();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(getReadingFile()));
            bw.write(getBidderCalendar().currentDateString());
            bw.close();
            File wroteFile = new File(adsDir, "wrote.txt");

            int retryCount = 0;
            while (!wroteFile.exists()) {
                if (retryCount > 5) {
                    msg.append(warn("File " + wroteFile
                                    + " does not exist, will skip loading groovy this time around."));
                    return null;
                } else {
                    retryCount++;
                    msg.append(warn("File " + wroteFile + " does not exist, will try again..."));
                    Thread.sleep(1000);
                }
            }

            BufferedReader reader = new BufferedReader(new FileReader(wroteFile));
            msg.append(debug("Last refresh: " + reader.readLine()));
            reader.close();

            final File groovySrcDir = new File(adsDir, "src");
            final File groovyPkgDir = new File(groovySrcDir, subDir).getAbsoluteFile();


            final ClassLoader parent = getClass().getClassLoader();
            final GroovyClassLoader loader;
            if (useSanboxClassloader) {
                loader = new EnremmetaGroovyClassLoader(parent,
                                ServiceRunner.getInstance().getSandboxSecurityManager());
            } else {
                loader = new GroovyClassLoader(parent);
            }
            loader.addClasspath(groovySrcDir.getAbsolutePath());

            // First load calculators.
            final File[] bpcList = groovyPkgDir.listFiles(bidPriceCalculatorFilter);

            if (bpcList == null) {
                msg.append(error("No Bid Price Calculators found in " + groovyPkgDir));
                return null;
            }

            for (File f : bpcList) {

                String bpcFileName = f.getName();
                String bpcId = null;
                try {
                    bpcId = bpcFileName.split("_")[1].split("\\.")[0];
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new Lot49Exception(e);
                }
                if (bpcId != null && !bpcId.isEmpty() && Integer.parseInt(bpcId) > 0) {
                    BidPriceCalculator bpc = loadBidPriceCalculator(groovyPkgDir, loader, f);
                    if (bpc == null) {
                        // Probably an error, in any case, that's been taken care of
                        // upstream...
                        continue;
                    }
                }
            }

            final File[] adFileList = groovyPkgDir.listFiles(adFilter);
            if (adFileList == null) {
                msg.append(error("No Ads found in " + groovyPkgDir));
                return null;
            }

            List<Ad> loadedAds = new ArrayList<Ad>(adFileList.length);

            for (File f : adFileList) {

                String adFileName = f.getName();
                String adId = adFileName.split("_")[1];

                if (adId != null && !adId.isEmpty() && Integer.parseInt(adId) > 0) {
                    ValidationResultHolder validationResult = isValidAd(adId, redisCon);
                    msg.append(validationResult.getMsg());

                    if (getConfig().isValidateBudgetBeforeLoadingAd()
                                    && !validationResult.getValid()) {
                        continue;
                    }
                    if (adId.equals("1818")) {
                        Utils.noop();
                    }
                    Ad ad = loadAd(groovyPkgDir, loader, f);
                    if (ad == null) {
                        // Probably an error, in any case, that's been taken care of
                        // upstream...
                        continue;
                    }
                    String neededBpcId = tmpAdIdToBpcId.get(ad.getId());
                    if (neededBpcId != null) {
                        if (ad.getBidPriceCalculator() == null) {
                            BidPriceCalculator neededBpc = bpcIdToBpc.get(neededBpcId);
                            if (neededBpc == null) {
                                msg.append(error("Expected BidPriceCalculator_" + neededBpcId
                                                + " for " + ad.getId() + ", but got nothing."));
                                continue;
                            }
                            ad.setBidPriceCalculator(neededBpc);
                        } else {
                            msg.append(debug(ad, "Already have " + ad.getBidPriceCalculator()));
                        }
                    }

                    loadedAds.add(ad);
                }
            }

            return loadedAds;

        } catch (Throwable t) {
            msg.append(error("Unexpected error in AdCache.loadAdsFromDir(): " + t, t));
            // t.printStackTrace(); // Already logged
        }
        return null;
    }

    public String refresh(RedisConnection<String, String> redisCon) {
        StringBuilder msg = new StringBuilder();
        if (redisCon == null) {
            msg.append(error("Unexpectedly Redis connection is null???"));
            return msg.toString();
        }
        msg.append(debug("Entering refresh()"));

        msg.append(debug("Starting loading classes..."));
        try {
            String adsDir = config.getDir();

            List<Ad> loadedAds = new ArrayList<Ad>();

            List<Ad> adList = this.loadAdsFromDir(adsDir, AD_CONFIG_SCRIPT_SUBDIR, false, msg,
                            redisCon);
            if (adList != null) {
                loadedAds.addAll(adList);
            }

            if (clients != null && clients.values() != null) {


                for (String clientId : clients.keySet()) {
                    ClientConfig clientConfig = clients.get(clientId);
                    info("Loading scripts for client " + clientId);
                    String clientDir = clientConfig.getDir();
                    String clientSrcDir = clientConfig.getPackageName().replaceAll("\\.", "/");
                    adList = this.loadAdsFromDir(clientDir, clientSrcDir, true, msg, redisCon);
                    if (adList != null) {
                        loadedAds.addAll(adList);
                    }
                }
            }

            final int listLen = loadedAds.size();
            List<Ad> b1 = new ArrayList<Ad>(listLen);
            List<Ad> b2 = new ArrayList<Ad>(listLen);
            List<Ad> a = new ArrayList<Ad>(listLen);

            for (Ad ad : loadedAds) {

                if (ad.getName() == null) {
                    msg.append(warn(ad, "has no name, it's not cool."));
                }

                String desiredNodeId = ad.getNodeId();
                if (desiredNodeId != null) {
                    String myNodeId = Bidder.getInstance().getOrchestrator().getNodeId();
                    if (!desiredNodeId.trim().equalsIgnoreCase(myNodeId)) {
                        LogUtils.logDecision(runNumber, ad, null, Lot49Constants.DECISION_HOST,
                                        null, null,
                                        "I am node " + myNodeId + ", ad required " + desiredNodeId);
                        msg.append(debug(ad, "I am node " + myNodeId + ", ad required "
                                        + desiredNodeId));
                        adCollectionHolder2.ineligibleAds.add(ad);
                        adCollectionHolder2.adMap.put(ad.getId(), ad);
                        continue;
                    }
                }

                // TODO make this into a map and move it somewhere else
                // before budget
                if (ad.getBidProbability() <= 0) {
                    LogUtils.logDecision(runNumber, ad, null, Lot49Constants.DECISION_PROBABILITY,
                                    null, null, "Probability " + ad.getBidProbability() + "<= 0");
                    msg.append(debug(ad, "has 0 probability."));
                    adCollectionHolder2.ineligibleAds.add(ad);
                    adCollectionHolder2.adMap.put(ad.getId(), ad);
                    continue;
                }
                String messageKey = KVKeysValues.PACING_LOG_PREFIX + ad.getId();
                try {
                    long messageTtlMins = this.config.getPacing().getMessageTtlMinutes();

                    long tooRange = getBidderCalendar().currentTimeMillis()
                                    - messageTtlMins * 60 * 1000;

                    msg.append(debug(ad, "ZREMRANGEBYSCORE -inf " + tooRange));
                    redisCon.zremrangebyscore(messageKey, "-inf", "" + tooRange);
                } catch (Throwable t) {
                    msg.append(warn("Error in ZREMRANGEBYSCORE for " + messageKey, t));
                }
                try {

                    BudgetAllocationResult bar = reserveMoney(redisCon, ad);
                    final long ts = getBidderCalendar().currentTimeMillis();
                    StringBuilder redisMsg = new StringBuilder();
                    redisMsg.append("Time: ").append(getBidderCalendar().currentDateString())
                                    .append("\n");

                    redisMsg.append("Node: ")
                                    .append(Bidder.getInstance().getOrchestrator().getNodeId())
                                    .append("\n");
                    redisMsg.append("Unix time:").append(ts).append("\n");
                    redisMsg.append("Bidder: ")
                                    .append((Bidder.getInstance().getOrchestrator().isBidder()
                                                    ? "YES" : "NO"))
                                    .append("\n");
                    redisMsg.append("Message: ").append(bar.msg).append("\n");

                    try {
                        msg.append(debug(ad, "ZADD " + messageKey + " " + ts + " ..."));
                        redisCon.zadd(messageKey, ts, redisMsg.toString());
                    } catch (Throwable t) {
                        msg.append(warn("Error in ZADD for " + messageKey, t));
                    }

                    msg.append(bar.msg);

                } catch (Throwable t) {
                    msg.append(warn(ad, "Cannot allocate money", t));
                    adCollectionHolder2.zeroBudgetAds.add(ad);
                    adCollectionHolder2.adMap.put(ad.getId(), ad);
                    msg.append(debug(ad, "====================================================="));
                    continue;
                }

                if (ad.getRemainingBidsToMake() <= 0) {
                    msg.append(info(ad, "no money left."));
                    adCollectionHolder2.zeroBudgetAds.add(ad);
                    adCollectionHolder2.adMap.put(ad.getId(), ad);
                    msg.append(info(ad, "====================================================="));
                    continue;
                }

                if (adCollectionHolder.timeoutMap.get(ad.getId()) != null) {
                    msg.append(info(ad, "black listed because of timeouts."));
                    adCollectionHolder2.blackListedAds.add(ad);
                    adCollectionHolder2.adMap.put(ad.getId(), ad);
                    msg.append(info(ad, "====================================================="));
                    continue;
                }

                a.add(ad);

                final boolean needCanBid2 = ad.needCanBid2();
                msg.append(info(ad, "needs user cache: " + needCanBid2));
                if (needCanBid2) {
                    b2.add(ad);
                } else {
                    b1.add(ad);
                }

            }

            adCollectionHolder2.bid1 = b1.toArray(adCollectionHolder2.bid1);
            adCollectionHolder2.bid2 = b2.toArray(adCollectionHolder2.bid2);
            adCollectionHolder2.all = a.toArray(adCollectionHolder2.all);
            msg.append(info("Eligible ad count: " + a.size() + ":"));
            for (Ad ad : adCollectionHolder2.all) {
                msg.append(info("\t" + ad.getId() + ": " + ad));
                adCollectionHolder2.adMap.put(ad.getId(), ad);
            }
            String statusMsg = null;
            if (adCollectionHolder2.all.length == 0) {
                statusMsg = "Nothing-to-run";
            } else {
                statusMsg = "Have-" + adCollectionHolder2.all.length + "-ads";
            }

            msg.append(info("New all: " + Arrays.asList(adCollectionHolder2.all)));
            msg.append(info("Old all: " + Arrays.asList(adCollectionHolder.all)));
            AdCollectionHolder oldHolder = replaceHolders();

            StringBuilder curMsgAll = new StringBuilder();
            StringBuilder curMsg = new StringBuilder();
            final long ts = getBidderCalendar().currentTimeMillis();

            for (final Ad ad : oldHolder.adMap.values()) {
                // First let's write tag decisions and be done with it.
                for (Tag tag : ad.getTags()) {
                    Map<String, SortedMap<String, AtomicLong>> tagDecisions =
                                    tag.getOptoutsByExchange();
                    for (String xch : tagDecisions.keySet()) {
                        Map<String, AtomicLong> tagReasonsByExchange = tagDecisions.get(xch);
                        for (String reason : tagReasonsByExchange.keySet()) {

                            long count = tagReasonsByExchange.get(reason).get();
                            if (count > 0) {
                                LogUtils.logTagDecision(runNumber, tag, xch, reason, count,
                                                tag.getImpressionsConsidered(xch));
                            }
                        }
                    }
                }

                long count = 0;
                SortedMap<String, SortedMap<String, AtomicLong>> optoutMap =
                                ad.getOptoutsByExchange();
                for (String exchange : optoutMap.keySet()) {
                    for (AtomicLong v : optoutMap.get(exchange).values()) {
                        count += v.get();
                    }
                }
                curMsg = new StringBuilder();
                String messageKey = KVKeysValues.PACING_LOG_PREFIX + ad.getId();

                if (count != 0) {
                    StringBuilder optoutMsg;
                    String decisionMsgPrefix = "Decision: ";
                    String hkey = KVKeysValues.DECISION_DISPLAY_PREFIX + ad.getId();
                    StringBuilder countStr = new StringBuilder(0);
                    long requiredBidsCount = ad.getOriginalBidsToMake();
                    if (requiredBidsCount > 0) {
                        LogUtils.logDecision(runNumber, ad, null,
                                        Lot49Constants.DECISION_BIDS_NEEDED, requiredBidsCount,
                                        null, null);
                    }
                    for (String exchange : optoutMap.keySet()) {
                        optoutMsg = new StringBuilder();
                        long totalOptoutCount = 0;
                        countStr = new StringBuilder(0);
                        long pacingOptoutCount = 0;
                        long reqCount = ad.getRequestCount(exchange);
                        if (reqCount == 0) {
                            continue;
                        }
                        SortedMap<String, AtomicLong> curOptoutMap = optoutMap.get(exchange);
                        for (String reason : curOptoutMap.keySet()) {
                            long optoutCount = curOptoutMap.get(reason).get();

                            if (reason.equals(Lot49Constants.DECISION_PACING)) {
                                pacingOptoutCount += optoutCount;
                            }
                            if (optoutCount > 0) {
                                LogUtils.logDecision(runNumber, ad, exchange, reason, optoutCount,
                                                reqCount, reason);
                                totalOptoutCount += optoutCount;
                                if (countStr.length() > 0) {
                                    countStr.append("+");
                                }
                                countStr.append(optoutCount);
                                String hfield = exchange + "/" + reason;
                                optoutMsg.append(info(ad, decisionMsgPrefix + " Optout: " + hfield
                                                + "\t" + optoutCount + "\t" + reqCount));
                                redisCon.hincrby(hkey, hfield, optoutCount);
                            }
                        }
                        if (countStr.length() == 0) {
                            countStr.append(exchange).append(": 0");
                        }
                        countStr.append("=").append(totalOptoutCount);

                        long bidsCount = ad.getBids(exchange);

                        if (bidsCount > 0) {
                            optoutMsg.append(debug(ad, decisionMsgPrefix + " Bids\t" + bidsCount
                                            + "\t" + reqCount));
                            LogUtils.logDecision(runNumber, ad, exchange,
                                            Lot49Constants.DECISION_BIDS_MADE, bidsCount, reqCount,
                                            null);
                            if (countStr.length() > 0) {
                                countStr.append("; ");
                            }
                            countStr.append(reqCount).append("-").append(bidsCount).append("=")
                                            .append(reqCount - bidsCount);
                            String hfield = exchange + "/bids";
                            redisCon.hincrby(hkey, hfield, bidsCount);
                        }

                        if (pacingOptoutCount > 0 && bidsCount > 0
                                        && (bidsCount != requiredBidsCount)) {
                            if (countStr.length() > 0) {
                                countStr.append("; ");
                            }
                            countStr.append("Required bids: ").append(requiredBidsCount)
                                            .append(", bids made: ").append(bidsCount);
                        }

                        String hfield = exchange + "/requests";
                        redisCon.hincrby(hkey, hfield, reqCount);
                        LogUtils.logDecision(runNumber, ad, exchange,
                                        Lot49Constants.DECISION_BIDS_POSSIBLE,
                                        reqCount - totalOptoutCount + pacingOptoutCount, reqCount,
                                        null);
                        if (reqCount - bidsCount != totalOptoutCount) {
                            if (countStr.length() > 0) {
                                countStr.append(": FAIL!");
                            }
                        }

                        if (countStr.length() > 0) {
                            optoutMsg.append(info(ad, decisionMsgPrefix + "Sanity check: "
                                            + countStr.toString()));
                        }
                        if (ad.getBids() != 0 || totalOptoutCount != 0) {
                            curMsg.append(optoutMsg.toString());
                        }
                    }
                }

                final String adId = ad.getId();
                final String winsKey = KVKeysValues.WIN_COUNT_PREFIX + adId;
                final String bidsKey = KVKeysValues.BID_COUNT_PREFIX + adId;
                final String spendAmountKey = KVKeysValues.SPEND_AMOUNT_PREFIX + adId;
                final String bidAmountKey = KVKeysValues.BID_AMOUNT_PREFIX + adId;
                final String winRateStartedKey = KVKeysValues.WIN_RATE_STARTED_TS + ad.getId();

                long winRateStarted = winRateCounters.get(winRateStartedKey);

                long winRateTtl = config.getPacing().getWinRateTtlMinutes();
                long curTime = getBidderCalendar().currentTimeMillis();
                if (winRateTtl > 0) {
                    long diff = (curTime - winRateStarted) / 1000 / 60;
                    boolean timeToReset = diff > winRateTtl;
                    curMsg.append(info(ad,
                                    "Win rate reset at " + new Date(winRateStarted) + ", " + diff
                                                    + " minutes ago; TTL is " + winRateTtl
                                                    + ", time to reset: " + timeToReset));
                    if (timeToReset) {
                        winRateCounters.getAndSet(winsKey, 0);
                        winRateCounters.getAndSet(bidsKey, 0);
                        winRateCounters.getAndSet(bidAmountKey, 0);
                        winRateCounters.getAndSet(spendAmountKey, 0);
                        winRateCounters.set(winRateStartedKey, curTime);

                        curMsg.append(info(ad, "Win rate reset to 0."));
                    }
                }

                String date = YYYYMMDD.format(getBidderCalendar().currentDate());

                if (ad.getWins() > 0) {

                    long overWins = ad.getWins() - ad.getRealWins();
                    if (overWins > 0) {
                        Ad currentAd = getAd(ad.getId());
                        if (currentAd != null) {
                            currentAd.incrWins(overWins);
                        } else {
                            overWins = 0;
                        }
                    }

                    long wins = winRateCounters.addAndGet(winsKey, ad.getWins() - overWins);
                    curMsg.append(info(ad,
                                    "Adding " + ad.getWins() + " to " + winsKey + ": " + wins));

                    redisCon.hincrby(KVKeysValues.WIN_COUNT_DISPLAY_PREFIX + ad.getId(), date,
                                    wins);
                }

                if (ad.getBids() > 0) {

                    long overBids = ad.getBids() - ad.getRealBids();
                    long overBidAmount = ad.getBidAmount() - ad.getRealBidAmount();
                    if (overBids > 0) {
                        long bidPrice = overBidAmount / overBids;

                        Ad currentAd = getAd(ad.getId());

                        if (currentAd != null) {
                            for (String key : ad.getRealBidsByExchange().keySet()) {
                                long overBidsByExchange = ad.getBidsByExchange().get(key)
                                                .longValue()
                                                - ad.getRealBidsByExchange().get(key).longValue();

                                while (overBidsByExchange > 0 && currentAd.haveBidsToMake()) {
                                    currentAd.incrBids(key, bidPrice);
                                    overBidsByExchange--;
                                }

                                if (overBidsByExchange > 0) {
                                    // TODO Not clear what to do in this case. OverBids will be
                                    // lost.
                                }
                            }
                        } else {
                            overBids = 0;
                            overBidAmount = 0;
                        }
                    }

                    long bids = winRateCounters.addAndGet(bidsKey, ad.getBids() - overBids);
                    curMsg.append(info(ad,
                                    "Adding " + ad.getBids() + " to " + bidsKey + ": " + bids));

                    redisCon.hincrby(KVKeysValues.BID_COUNT_DISPLAY_PREFIX + ad.getId(), date,
                                    bids);

                    long bidAmount = winRateCounters.addAndGet(bidAmountKey,
                                    ad.getBidAmount() - overBidAmount);

                    redisCon.hincrby(KVKeysValues.BID_AMOUNT_DISPLAY_PREFIX + ad.getId(), date,
                                    bidAmount);
                    curMsg.append(info(ad, "Adding " + (ad.getBidAmount() - overBidAmount) + " to "
                                    + bidAmountKey + ": " + bidAmount));

                }

                long lastSpendAmount = ad.getSpendAmount();

                if (lastSpendAmount > 0) {

                    long overSpendAmount = ad.getSpendAmount() - ad.getRealSpendAmount();
                    if (overSpendAmount > 0) {
                        Ad currentAd = getAd(ad.getId());
                        if (currentAd != null) {
                            currentAd.incrSpendAmount(overSpendAmount);
                            lastSpendAmount = ad.getRealSpendAmount();
                        }
                    }

                    long runningTotalSpendAmount =
                                    winRateCounters.addAndGet(spendAmountKey, lastSpendAmount);
                    curMsg.append(info(ad,
                                    "Spend: Adding " + lastSpendAmount + " to " + spendAmountKey
                                                    + " for running total for last period: "
                                                    + runningTotalSpendAmount));
                    long dateSpend = redisCon.hincrby(
                                    KVKeysValues.SPEND_AMOUNT_DISPLAY_PREFIX + ad.getId(), date,
                                    lastSpendAmount);
                    curMsg.append(info(ad, "Spend: Adding " + lastSpendAmount + " to spend for "
                                    + date + " for current total of " + dateSpend));
                    long newBudget = redisCon.decrby(KVKeysValues.BUDGET_PREFIX + ad.getId(),
                                    lastSpendAmount);
                    curMsg.append(info(ad,
                                    "Spend: Subtracting recent spend " + lastSpendAmount + " from "
                                                    + KVKeysValues.BUDGET_PREFIX + ad.getId()
                                                    + ", remains: " + newBudget));
                }


                StringBuilder redisMsg = new StringBuilder();
                redisMsg.append("Time: ").append(getBidderCalendar().currentDateString())
                                .append("\n");
                redisMsg.append("Node: ").append(Bidder.getInstance().getOrchestrator().getNodeId())
                                .append("\n");
                redisMsg.append("Unix time:").append(ts).append("\n");
                redisMsg.append("Bidder: ").append(
                                (Bidder.getInstance().getOrchestrator().isBidder() ? "YES" : "NO"))
                                .append("\n");
                redisMsg.append("Message: ").append(curMsg).append("\n");

                try {
                    curMsg.append(info(ad, "ZADD " + messageKey + " " + ts + " ..."));
                    redisCon.zadd(messageKey, ts, redisMsg.toString());
                } catch (Throwable t) {
                    curMsg.append(error("Error executing ZADD for " + messageKey
                                    + " key and message " + redisMsg.toString(), t));
                }
                msg.append(info(ad, "====================================================="));
                curMsgAll.append(curMsg.toString());
            }

            msg.append(curMsgAll.toString());
            msg.append(refreshOwnerKeys(redisCon));

            msg.append(setStatus(statusMsg));
            msg.append(info("Refreshed ad cache, loaded " + adCollectionHolder.bid1.length
                            + " ads with no user info needed, and " + adCollectionHolder.bid2.length
                            + " with user needed."));

            return msg.toString();

        } catch (Throwable t) {
            msg.append(error("Unexpected error in AdCache.refresh(): " + t, t));
            // t.printStackTrace(); // already logged
        }
        return msg.toString();

    }

    private final AdCollectionHolder replaceHolders() {
        AdCollectionHolder retval = adCollectionHolder;
        this.adCollectionHolder = adCollectionHolder2;
        this.adCollectionHolder2 = new AdCollectionHolder();
        return retval;
    }

    private final static String logPrefix() {
        return getBidderCalendar().currentDate() + " AdCache: ";
    }

    private final static String error(String s) {
        String retval = logPrefix() + s;
        LogUtils.error(retval);
        return retval + "\n";
    }

    private final static String error(String s, Throwable t) {
        String retval = logPrefix() + s;
        LogUtils.error(retval, t);
        return retval + "\n";
    }

    private final static String warn(String s) {
        String retval = logPrefix() + s;
        LogUtils.warn("\t" + retval);
        return retval + "\n";
    }

    private final static String warn(String s, Throwable t) {
        String retval = logPrefix() + s;
        LogUtils.warn("\t" + retval, t);
        return retval + "\n";
    }

    private final static String info(String s) {
        String retval = logPrefix() + s;
        LogUtils.info("\t\t" + retval);
        return retval + "\n";
    }

    private final static String info(Marker m, String s) {
        String retval = logPrefix() + s;
        LogUtils.info(m, "\t\t" + retval);
        return retval + "\n";
    }

    private final static String debug(String s) {
        String retval = logPrefix() + s;
        LogUtils.debug("\t\t\t" + retval);
        return retval + "\n";
    }

    private final static String debug(Marker m, String s) {
        String retval = logPrefix() + s;
        LogUtils.debug(m, "\t\t\t" + retval);
        return retval + "\n";
    }

    private ScheduledFuture scheduledSelf;

    private final static String error(Ad ad, String s) {
        return error("Ad " + ad.getId() + ": " + s);
    }

    private final static String error(Ad ad, String s, Throwable t) {
        return error(ad.getId(), s, t);
    }

    private final static String error(String adId, String s, Throwable t) {
        return error("Ad " + adId + ": " + s, t);
    }

    private final static String warn(Ad ad, String s) {
        return warn(ad.getId(), s);
    }

    private final static String warn(String adId, String s) {
        return warn("Ad " + adId + ": " + s);
    }

    private final static String warn(Ad ad, String s, Throwable t) {
        return warn(ad.getId(), s, t);
    }

    private final static String warn(String adId, String s, Throwable t) {
        return warn("Ad " + adId + ": " + s, t);
    }

    private final static String info(Ad ad, String s) {
        String me = Bidder.getInstance().getOrchestrator().isBidder() ? "BIDDER" : "NON-BIDDER";
        return info(me + ": Ad " + ad.getId() + ": " + s);
    }

    private final static String debug(Ad ad, String s) {
        return debug((ad == null ? null : ad.getId()), s);
    }

    private final static String debug(String adId, String s) {
        return debug("Ad " + adId + ": " + s);
    }

    /**
     * In order to run from
     * {@link AdminSvc#refreshAdCache(javax.ws.rs.container.AsyncResponse, String)} or ...
     */
    public synchronized boolean cancel() {
        if (isRunning.get()) {
            return false;
        }
        if (scheduledSelf != null) {
            final boolean retval = scheduledSelf.cancel(true);
            scheduledSelf = null;
            return retval;
        } else {
            return true;
        }

    }

    private synchronized String rescheduleSelf() {
        if (ttlMinutes <= 0) {
            return "Not scheduling: TTL " + ttlMinutes;
        }

        final int delay = 1;
        final TimeUnit timeUnit = TimeUnit.MINUTES;
        ScheduledExecutorService exec = Bidder.getInstance().getScheduledExecutor();
        this.scheduledSelf = exec.schedule(this, delay, timeUnit);
        String msg = info("Scheduling another run in " + delay + " " + timeUnit + ": "
                        + this.scheduledSelf);

        return msg;
    }

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private final RedisConnection<String, String> getRedisConnection() throws Lot49Exception {
        final RedisServiceConfig redisConf = this.config.getPacing().getRedis();
        final String redisHost = redisConf.getHost();
        final int redisPort = redisConf.getPort();
        String hostPort = redisHost + ":" + redisPort;
        info("Connecting to pacing store at " + hostPort + ".");

        final RedisConnection<String, String> redisCon = client.connect();
        if (redisCon == null) {
            throw new Lot49Exception("Error connecting to Redis at " + hostPort);
        }
        redisCon.setTimeout(redisConf.getTimeoutMillis(), TimeUnit.MILLISECONDS);
        return redisCon;
    }

    private String setStatus(String s) {
        this.status = s;
        return debug("Setting status from '" + status + "' to '" + s + "'");

    }

    /**
     * <h1>Ad cache refresh</h1>This method is run every minute. It can also be forced explicitly to
     * run, usually in the debug phase. We do guarantee, however, that only one instance of this
     * method is running. It goes through a number of housekeeping tasks.
     * <h2>Ad refresh</h2> This is one of the main tasks. It does the following:
     *
     * <h2>Refresh ads</h2> This only runs every {@link AdCacheConfig#getTtlMinutes() ttl} minutes.
     * It goes through the following steps:
     * <ol>
     *
     * </ol>
     *
     *
     *
     * @param scheduled
     *            whether the run was {@link AdCacheConfig#getTtlMinutes() scheduled} or explicit
     *            (say, via invocation from
     *            {@link AdminSvc#refreshAdCache(javax.ws.rs.container.AsyncResponse, String)}).
     * @return log messages to print (in case of explicit invocation).
     */
    public String doRun(boolean scheduled) {
        final long t0 = getBidderCalendar().currentTimeMillis();
        StringBuilder msg = new StringBuilder();
        msg.append(info("Entering AdCache.doRun(" + scheduled + ")"));

        if (!isRunning.compareAndSet(false, true)) {
            msg.append(info("Already running, will exit this time."));
            return msg.toString();
        }
        boolean refreshAds = false;
        if (scheduled) {
            if (runNumber % ttlMinutes == 0) {
                refreshAds = true;
            }
        } else {
            refreshAds = true;
        }
        runNumber++;
        msg.append(info("This is run number " + runNumber + "; ttlMinutes: " + ttlMinutes
                        + "; refreshAds: " + refreshAds));

        RedisConnection<String, String> redisCon = null;
        try {
            redisCon = getRedisConnection();
            if (refreshAds) {
                msg.append(refresh(redisCon));
            }

            File readingFile = getReadingFile();
            LogUtils.info("Removing " + readingFile + ": " + readingFile.delete());

        } catch (Throwable e) {
            msg.append(error("AdCache.run(): Unexpected error: " + e, e));
            if (e.getCause() != null) {
                msg.append(error("AdCache.run(): Caused by: " + e.getCause(), e.getCause()));
            }

            e.printStackTrace(System.out);
            e.printStackTrace(System.err);
        } finally {
            if (redisCon != null) {
                try {
                    redisCon.close();
                    msg.append(debug("Closed " + redisCon));
                } catch (Throwable t) {
                    msg.append(error("Error closing " + redisCon, t));
                }
            }

            final long t1 = getBidderCalendar().currentTimeMillis();
            final long elapsed = (t1 - t0) / 1000;
            msg.append(debug("Exiting AdCache.run(), spent " + elapsed + " seconds."));
            if (scheduled) {
                first = false;
                msg.append(rescheduleSelf());
            } else {
                info("Unscheduled run, not setting first run to false and not rescheduling.");
            }
            isRunning.set(false);

        }
        return msg.toString();
    }

    /**
     * Same as <tt>doRun(true)</tt>
     *
     * @see #doRun(boolean)
     */
    @Override
    public void run() {
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            doRun(true);
        } catch (Throwable t) {
            LogUtils.error(t);
        } finally {
            first = false;
        }
    }

}
