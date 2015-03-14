package com.enremmeta.util;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.joda.time.DateTime;

import com.enremmeta.rtb.AwsOrchestrator;
import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.LogExceptionHandler;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49SubscriptionData;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AerospikeDBServiceConfig;
import com.enremmeta.rtb.config.DbConfigs;
import com.enremmeta.rtb.config.DynamoDBServiceConfig;
import com.enremmeta.rtb.config.HazelcastServiceConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.UserAttributesCacheConfig;
import com.enremmeta.rtb.config.UserCacheConfig;
import com.enremmeta.rtb.config.UserSegmentsCacheConfig;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;
import com.enremmeta.rtb.dao.DbService;
import com.enremmeta.rtb.dao.impl.aerospike.AerospikeDBService;
import com.enremmeta.rtb.dao.impl.collections.CollectionsDbService;
import com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBService;
import com.enremmeta.rtb.dao.impl.hazelcast.HazelcastService;
import com.enremmeta.rtb.dao.impl.redis.RedisService;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.sandbox.SandboxSecurityManager;
import com.enremmeta.rtb.spi.codecs.NoopSegmentToOwnerCodec;
import com.enremmeta.rtb.spi.codecs.SegmentToOwnerCodec;
import com.enremmeta.rtb.spi.providers.Provider;
import com.enremmeta.rtb.spi.providers.ProviderConfig;
import com.enremmeta.rtb.spi.providers.ProviderFactory;
import com.enremmeta.rtb.spi.providers.integral.IntegralConfig;
import com.enremmeta.rtb.spi.providers.integral.IntegralService;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindConfig;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindFacade;

/**
 * Implements some common services for classes that are intended to have <tt>main()</tt>, such as
 * command-line parsing, etc.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *         </p>
 */
public abstract class ServiceRunner {

    public static ServiceRunner getInstance() {
        return runner;
    }

    protected static ServiceRunner runner;

    public AdCache getAdCache() {
        return adCache;
    }

    protected Options opts;
    protected CommandLine cl;
    private HazelcastService hazelcastService;
    private MaxMindFacade maxMind;
    private Orchestrator orchestrator;

    private DaoMapOfUserAttributes userAttributesCacheService;

    private DaoMapOfUserSegments userSegmentsCacheService;

    public DaoMapOfUserAttributes getUserAttributesCacheService() {
        return userAttributesCacheService;
    }

    public DaoMapOfUserSegments getUserSegmentsCacheService() {
        return userSegmentsCacheService;
    }

    protected SandboxSecurityManager sandboxSecurityManager;
    protected AdCache adCache;
    protected Lot49Config config = null;

    private String configFilename;
    private ScheduledExecutorService scheduledExecutor;
    private ExecutorService executor;

    private IntegralService integralService;

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    protected String getConfigFilename() {
        return configFilename;
    }

    public SandboxSecurityManager getSandboxSecurityManager() {
        return sandboxSecurityManager;
    }

    public void initExecutors() {

        int poolSize = config.getExecutorThreadPoolSize();
        this.scheduledExecutor = new ScheduledThreadPoolExecutor(poolSize);
        ScheduledThreadPoolExecutor stpe = ((ScheduledThreadPoolExecutor) scheduledExecutor);
        stpe.setKeepAliveTime(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        RejectedExecutionHandler reh = new RejectedExecutionHandler() {

            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                String errMsg = "rejectedExecution in " + r + ", " + executor;
                LogUtils.error(errMsg);
                throw new RuntimeException(errMsg);
            }
        };
        stpe.setRejectedExecutionHandler(reh);

        this.executor = new ThreadPoolExecutor(poolSize, Integer.MAX_VALUE, Integer.MAX_VALUE,
                        NANOSECONDS, new LinkedBlockingQueue<Runnable>());
        ThreadPoolExecutor tpe = ((ThreadPoolExecutor) executor);
        tpe.setKeepAliveTime(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        tpe.setRejectedExecutionHandler(reh);

    }

    protected ServiceRunner() {
        super();
        opts = new Options();
        opts.addOption("c", true, "Config file.");
    }

    public CommandLine getCl() {
        return cl;
    }

    public void initProviders() throws Lot49Exception {
        Map<String, Map> providersConfig = config.getProviders();
        if (providersConfig == null) {
            LogUtils.init("No providers configured.");
            return;
        }
        for (final String provName : providersConfig.keySet()) {
            Map provConfMap = providersConfig.get(provName);
            ProviderConfig provConf = new ProviderConfig(provConfMap);
            if (!provConf.isEnabled()) {
                LogUtils.init("Provider '" + provName + "' is not enabled, skipping.");
                continue;
            }
            LogUtils.init("Initializing " + provName);

            final Provider prov = ProviderFactory.getProvider(this, provName, provConfMap);

            if (prov.isInitAsync()) {
                final long t0 = BidderCalendar.getInstance().currentTimeMillis();
                SimpleCallback cb = new SimpleCallback() {

                    @Override
                    public void done(Throwable t) {
                        if (t == null) {
                            long elapsed = (BidderCalendar.getInstance().currentTimeMillis() - t0)
                                            / 1000;
                            providers.put(prov.getName(), prov);
                            LogUtils.init("Successfully initialized provider '" + provName + "' in "
                                            + elapsed + " seconds.");
                        } else {
                            LogUtils.error("Error initializing provider '" + provName + "'", t);
                        }
                    }
                };
                prov.initAsync(cb);
            } else {
                prov.init();
            }

            providers.put(prov.getName(), prov);
        }
    }

    private final Map<String, Provider> providers = new HashMap<String, Provider>();

    public Map<String, Provider> getProviders() {
        return providers;
    }

    public MaxMindFacade getMaxMind() {
        return this.maxMind;
    }

    public void initGeo() throws IOException {
        MaxMindConfig c = config.getMaxMind();
        this.maxMind = new MaxMindFacade(c);
    }

    public void initOrchestrator() throws Lot49Exception {
        OrchestratorConfig orchConfig = this.config.getOrchestrator();
        String deployType = orchConfig.getDeployType();
        if (deployType == null) {
            deployType = this.config.getDeploy();
        }
        switch (deployType) {
            case LocalOrchestrator.DEPLOY_TYPE:
                this.orchestrator = new LocalOrchestrator(orchConfig);
                break;
            case AwsOrchestrator.DEPLOY_TYPE:
                if (orchConfig == null) {
                    throw new Lot49Exception("Expected orchestrator configuration object.");
                }
                this.orchestrator = new AwsOrchestrator(orchConfig);
                break;
            default:
                throw new Lot49Exception("Unknown deploy type: " + deployType);

        }
    }

    public void initLogging() throws Lot49Exception {
        LogExceptionHandler.init();
    }

    public Options getOpts() {
        return opts;
    }

    public Orchestrator getOrchestrator() {
        return orchestrator;
    }

    private final static String getRolloverEnforcerId() {
        return "scheduleLogRolloverEnforcer_" + Utils.getId();
    }

    private long lastRolloverEnforcerRan = 0;

    /**
     * We schedule a process to run every minute, that, when it detects that an hour is rolled over
     * (the {@link DateTime#getMinuteOfHour() minute of hour} is 0), writes an entry into every log
     * with mostly NULLs. This will force the roll over process to work.
     *
     * @see <a href=
     *      "http://stackoverflow.com/questions/19304165/time-based-triggering-policy-in-log4j2">
     *      Remko Popma's comment on Stackoverflow</a>
     */
    // TODO there has to be a better way with the metadata driven stuff2
    public void scheduleLogRolloverEnforcer() {

        final Runnable command = new Runnable() {

            @Override
            public void run() {
                try {
                    long elapsed = BidderCalendar.getInstance().currentTimeMillis()
                                    - lastRolloverEnforcerRan;
                    if (elapsed < 30 * 60 * 1000) {
                        return;
                    }
                    LogUtils.init("scheduleLogRolloverEnforcer() pushing empty line into logs (previous run at "
                                    + new Date(lastRolloverEnforcerRan));
                    OpenRtbRequest req = new OpenRtbRequest();
                    Bid bid = new Bid();
                    bid.setId(getRolloverEnforcerId());
                    req.getLot49Ext().setTest(true);
                    req.setId(getRolloverEnforcerId());
                    final List<Impression> imps = new ArrayList<Impression>();
                    req.setImp(imps);
                    final Impression imp = new Impression();
                    imps.add(imp);
                    imp.setId(getRolloverEnforcerId());
                    final Banner banner = new Banner();
                    imp.setBanner(banner);

                    LogUtils.logRequest(req, false, 0);
                    LogUtils.logRequest(req, true, 0);
                    LogUtils.logBid(req, 0, getRolloverEnforcerId(), "pushkin", "pushkin", bid, 0,
                                    0, null, null);
                    LogUtils.logWin("", "", getRolloverEnforcerId(), "", "", "", "", (double) 0.0,
                                    0l, 0l, 0l, "", "", "", "", "", null, "", new Long(0), "", null,
                                    false, getRolloverEnforcerId(), "", "", "", "", "", false,
                                    false, null);
                    LogUtils.logWin("", "", getRolloverEnforcerId(), "", "", "", "", (double) 0.0,
                                    0l, 0l, 0l, "", "", "", "", "", null, "", new Long(0), "", null,
                                    true, getRolloverEnforcerId(), "", "", "", "", "", false, false,
                                    null);

                    LogUtils.logLost(new Long(0), getRolloverEnforcerId(), "", "", "", new Long(0),
                                    new Long(0), null, null, null, null, null, null, null,
                                    getRolloverEnforcerId(), null);
                    LogUtils.logImpression(null, null, getRolloverEnforcerId(), null, null, null,
                                    0l, null, null, null, null, null, null, null, 0, null, null,
                                    getRolloverEnforcerId(), null, null, null, null, null, false,
                                    null, null, null, false, null, null);
                    LogUtils.logClick(null, null, getRolloverEnforcerId(), null, null, null, null,
                                    null, null, null, null, null, null, null,
                                    getRolloverEnforcerId(), null, null, null);
                    lastRolloverEnforcerRan = BidderCalendar.getInstance().currentTimeMillis();
                } catch (Throwable t) {
                    LogUtils.error("Error in scheduleLogRolloverEnforcer()", t);
                }

            }
        };
        scheduledExecutor.scheduleWithFixedDelay(command, 0, 1, TimeUnit.MINUTES);
    }

    public void initAdapters() throws Lot49Exception {
        boolean errors = false;
        LogUtils.init("Initializing exchange adapters.");
        for (String name : ExchangeAdapterFactory.getAllExchangeAdapterNames()) {
            try {
                final ExchangeAdapter adapter = ExchangeAdapterFactory.getExchangeAdapter(name);
                LogUtils.init(name + "... OK: " + adapter.toString());
            } catch (Throwable e) {
                LogUtils.error("Error initializing adapter for " + name, e);
                errors = true;
            }
        }
        if (errors) {
            throw new Lot49Exception("Could not initialize all adapters.");
        }
    }

    public void setCl(CommandLine cl) {
        this.cl = cl;
    }

    public HazelcastService getHazelcastService() {
        return hazelcastService;
    }

    private Map<String, DbService> dbServices = new HashMap<String, DbService>();

    private AtomicLong counter = new AtomicLong(0);

    public String getNextId() {
        return Utils.getId();
    }

    private SegmentToOwnerCodec segmentToOwnerCodec;

    public SegmentToOwnerCodec getSegmentToOwnerCodec() {
        return segmentToOwnerCodec;
    }

    public void initCodecs() throws Lot49Exception {
        String klassName = getConfig().getSegmentToOwnerCodecClassname();
        try {
            Class klazz = Class.forName(klassName);
            if (SegmentToOwnerCodec.class.isAssignableFrom(klazz)) {
                this.segmentToOwnerCodec = (SegmentToOwnerCodec) klazz.newInstance();
            } else {
                LogUtils.warn(klassName + " is not a subclass of " + SegmentToOwnerCodec.class
                                + " defaulting to Noop");
                this.segmentToOwnerCodec = new NoopSegmentToOwnerCodec();

            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LogUtils.warn(e);
            this.segmentToOwnerCodec = new NoopSegmentToOwnerCodec();
        }
    }

    /**
     * Initialize a {@link DbService} for every item in {@link DbConfigs}, and throw an exception
     * the first time an error happens.
     *
     * @throws Lot49Exception
     */
    public void initDbServices() throws Lot49Exception {

        DbConfigs dbConfigs = getConfig().getDatabases();

        // TODO first make it work with all existing ones (dynamo, hazel)
        // TODO second of course we need to make this be able to load
        // dynamically like SPI
        Map<String, RedisServiceConfig> redisConfigs = dbConfigs.getRedises();
        if (redisConfigs != null) {
            for (String key : redisConfigs.keySet()) {
                RedisServiceConfig redisConfig = redisConfigs.get(key);
                RedisService redisService = new RedisService();
                if (redisConfig.isEnabled()) {
                    redisService.init(redisConfig);
                    dbServices.put(key, redisService);
                    LogUtils.init("Initialized Redis service " + " with " + redisService);
                } else {
                    LogUtils.init("Service " + key + " not enabled");
                }
            }
        }

        Map<String, DynamoDBServiceConfig> dynConfigs = dbConfigs.getDynamodbs();
        if (dynConfigs != null) {
            for (String key : dynConfigs.keySet()) {
                DynamoDBServiceConfig dynConfig = dynConfigs.get(key);
                DynamoDBService dynService = new DynamoDBService();
                if (dynConfig.isEnabled()) {
                    try {
                        dynService.init(dynConfig);
                    } catch (Exception e) {
                        LogUtils.init("Error initializing DynamoDB service " + e);
                        continue;
                    }
                    dbServices.put(key, dynService);
                    LogUtils.init("Initialized DynamoDB service " + key + " with " + dynConfig);
                } else {
                    LogUtils.init("Service " + key + " not enabled");
                }
            }
        }

        Map<String, AerospikeDBServiceConfig> aeroConfigs = dbConfigs.getAerospikes();
        if (aeroConfigs != null) {
            for (String key : aeroConfigs.keySet()) {
                AerospikeDBServiceConfig aeroConfig = aeroConfigs.get(key);
                AerospikeDBService aeroService = new AerospikeDBService();
                if (aeroConfig.isEnabled()) {
                    try {
                        aeroService.init(aeroConfig);
                    } catch (Exception e) {
                        LogUtils.init("Error initializing Aerospike service " + e);
                        continue;
                    }
                    dbServices.put(key, aeroService);
                    LogUtils.init("Initialized Aerospike service " + key + " with " + aeroConfig);
                } else {
                    LogUtils.init("Service " + key + " not enabled");
                }
            }
        }

        Map<String, HazelcastServiceConfig> hzConfigs = dbConfigs.getHazelcasts();
        if (hzConfigs != null) {
            for (String key : hzConfigs.keySet()) {
                HazelcastServiceConfig hzConfig = hzConfigs.get(key);
                LogUtils.init(key + " enabled: " + hzConfig.isEnabled());

                if (hzConfig.isEnabled()) {
                    HazelcastService hzService = new HazelcastService(this, hzConfig);
                    hzService.init(hzConfig);
                    dbServices.put(key, hzService);
                    LogUtils.init("Initialized Hazelcast service " + key + " with " + hzConfig);
                } else {
                    LogUtils.init("Service " + key + " not enabled");
                }
            }
        }

        dbServices.put(COLLECTIONS_DB_SERVICE, new CollectionsDbService());

        final Runnable command = new Runnable() {

            @Override
            public void run() {
                try {
                    String msg = "Queues sizes. Main Executor: "
                                    + ((ThreadPoolExecutor) executor).getQueue().size()
                                    + ", ScheduledExecutor: "
                                    + ((ThreadPoolExecutor) scheduledExecutor).getQueue().size();

                    Map<String, DynamoDBServiceConfig> dynConfigs = dbConfigs.getDynamodbs();
                    if (dynConfigs != null) {
                        for (String key : dynConfigs.keySet()) {
                            DynamoDBServiceConfig dynConfig = dynConfigs.get(key);
                            // DynamoDBService dynService;
                            if (dynConfig.isEnabled()) {
                                // dynService = (DynamoDBService) dbServices.get(key);
                                msg += ", DynamoDB " + key /* + ": " + dynService.getQueueSize() */;
                            }
                        }
                    }
                    LogUtils.debug(msg);
                } catch (Throwable t) {
                    LogUtils.error("Error in getting queues sizes", t);
                }

            }
        };
        scheduledExecutor.scheduleWithFixedDelay(command, 5, 5, TimeUnit.SECONDS);
    }

    public final static String COLLECTIONS_DB_SERVICE = "collections";

    public final DbService getDbServiceByName(String name) {
        // This is a performance no-no but this only is supposed to happen at
        // startup.
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        boolean pastMe = false;
        String requester = "Someone ";
        for (int i = stackTraceElements.length - 1; i >= 0; i--) {
            StackTraceElement ste = stackTraceElements[i];
            if (pastMe) {
                requester = ste.getFileName() + ":" + ste.getLineNumber() + " ("
                                + ste.getClassName() + "." + ste.getMethodName() + "() )";
                break;
            }
            if (ste.getClassName().equals(ServiceRunner.class.getName())) {
                pastMe = true;
            }
        }
        LogUtils.init(requester + " requested DbService " + name);
        return dbServices.get(name);
    }

    public void initSecurityManager() {
        if (config.getSecurityManagerConfig() != null) {
            sandboxSecurityManager = new SandboxSecurityManager(config.getSecurityManagerConfig());
            System.setSecurityManager(sandboxSecurityManager);
        } else {
            LogUtils.init("Security manager is not configured.");
        }
    }

    public void initCaches() throws Lot49Exception {

        this.adCache = new AdCache(config.getAdCache());
        adCache.init();

        UserCacheConfig ucConfig = config.getUserCache();
        UserAttributesCacheConfig uaConfig = config.getUserAttributesCache();
        userAttributesCacheService =
                        dbServices.get(uaConfig.getMapDb()).getDaoMapOfUserAttributes();
        LogUtils.init("Implementation of UserAttributesCache used: "
                        + userAttributesCacheService.getClass());
        UserSegmentsCacheConfig usConfig = config.getUserSegmentsCache();
        userSegmentsCacheService =
                        dbServices.get(usConfig.getSegmentsDb()).getDaoMapOfUserSegments();
        LogUtils.init("Implementation of UserSegmentsCache used: "
                        + userSegmentsCacheService.getClass());
    }

    public void setOpts(Options opts) {
        this.opts = opts;
    }

    public Lot49Config getConfig() {
        return config;
    }

    public void parseCommandLineArgs(String[] argv) throws Lot49Exception {
        CommandLineParser parser = new PosixParser();
        try {
            cl = parser.parse(opts, argv);
        } catch (ParseException e) {
            usage();
        }
        configFilename = cl.getOptionValue("c");
        if (cl != null) {
            System.out.println("Using configuration: " + configFilename);
        }
    }

    protected void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ant", opts);
        System.exit(1);
    }

    /**
     * Intended for use in tests - this has no effect if loaded after initial startup.
     *
     * @param config
     *            the configuration
     */
    public void setConfig(Lot49Config config) {
        this.config = config;
    }

    public Lot49Config loadConfig() throws Lot49Exception {

        LogUtils.init("Entering loadConfig()");
        if (configFilename == null) {
            System.out.println("Config filename not specified, trying environment variable "
                            + KVKeysValues.ENV_LOT49_CONFIG_FILE);
            configFilename = System.getenv(KVKeysValues.ENV_LOT49_CONFIG_FILE);
            if (configFilename == null) {
                throw new Lot49Exception(
                                "Config not specified either in the command-line or in the environment variable, bailing out.");
            }
        }
        try {
            this.config = (Lot49Config) Utils.loadConfig(configFilename, Lot49Config.class);

            return this.config;
        } finally {
            LogUtils.init("Exiting loadConfig(), got: " + this.config);
        }
    }

    public void updateSubcriptionData() {
        if (this.config != null && this.config.getClients() != null) {
            Lot49SubscriptionData subscriptionData = new Lot49SubscriptionData();

            this.config.getClients().forEach((clientId, clientConfig) -> {
                List<Lot49SubscriptionData.Lot49SubscriptionServiceName> subscriptions =
                                clientConfig.getSubscriptions();
                subscriptionData.addSubscriptionData(clientId, subscriptions);
            });
            this.config.setSubscriptionData(subscriptionData);
        }
    }

    public IntegralService getIntegralService() {
        return integralService;
    }

    public void initIntegral() {
        IntegralConfig integralConfig = config.getIntegral();
        if (integralConfig != null) {
            LogUtils.init("Integral endpoint : " + integralConfig.getEndpoint());
            integralService = new IntegralService(integralConfig);
        } else {
            LogUtils.init("Integral is not configurated");
        }
    }

}
