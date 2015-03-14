package com.enremmeta.rtb.config;

import java.util.Map;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.SegmentEncoder;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49SubscriptionData;
import com.enremmeta.rtb.jersey.AdminSvc;
import com.enremmeta.rtb.spi.codecs.DefaultSegmentToOwnerCodec;
import com.enremmeta.rtb.spi.providers.integral.IntegralConfig;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindConfig;

/**
 * Config bean implementing main configuration (and comprising others).
 * 
 * @see Bidder
 * 
 * @author <a href="mailto:grisha@alum.mit.edu">Gregory Golberg (grisha@alum.mit.edu)</a>
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 * 
 */
public class Lot49Config implements Config {

    public static byte[] DEFAULT_LOG_KEY =
                    {-55, 75, 28, -16, 42, 109, 6, 124, -99, -71, 109, -74, 111, 56, 52, -94};

    private byte[] logKey = null; // DEFAULT_LOG_KEY;

    public static final long DEFAULT_INITIAL_DYNAMIC_PRICE = 5000;

    private long initialDynamicPrice = DEFAULT_INITIAL_DYNAMIC_PRICE;

    public long getInitialDynamicPrice() {
        return initialDynamicPrice;
    }

    public void setInitialDynamicPrice(long initialDynamicPrice) {
        this.initialDynamicPrice = initialDynamicPrice;
    }

    /**
     * AES key with which data that only we have access to in logs will be encrypted. If this is
     * <tt>null</tt>, no encryption.
     * 
     * @see SegmentEncoder
     */
    public byte[] getLogKey() {
        return logKey;
    }

    public void setLogKey(byte[] logKey) {
        this.logKey = logKey;
    }

    // TODO this should go into config and replaced by Noop - that is more
    // proper;
    public static final String DEFAULT_SEGMENT_TO_OWNER_CODEC_CLASSNAME =
                    DefaultSegmentToOwnerCodec.class.getName();

    private String segmentToOwnerCodecClassname = DEFAULT_SEGMENT_TO_OWNER_CODEC_CLASSNAME;

    public String getSegmentToOwnerCodecClassname() {
        return segmentToOwnerCodecClassname;
    }

    public void setSegmentToOwnerCodecClassname(String segmentToOwnerCodecClassname) {
        this.segmentToOwnerCodecClassname = segmentToOwnerCodecClassname;
    }

    public static final boolean DEFAULT_REMOVE_TAG_ON_FIRST_NURL_REQUEST = false;

    private boolean removeTagOnFirstNurlRequest = DEFAULT_REMOVE_TAG_ON_FIRST_NURL_REQUEST;

    public boolean isRemoveTagOnFirstNurlRequest() {

        return removeTagOnFirstNurlRequest;
    }

    public void setRemoveTagOnFirstNurlRequest(boolean removeTagOnFirstNurlRequest) {
        this.removeTagOnFirstNurlRequest = removeTagOnFirstNurlRequest;
    }

    public static final long DEFAULT_DNS_CACHE_TTL_SECONDS = 60;

    private long dnsCacheTtlSeconds = DEFAULT_DNS_CACHE_TTL_SECONDS;

    public long getDnsCacheTtlSeconds() {
        return dnsCacheTtlSeconds;
    }

    public void setDnsCacheTtlSeconds(long dnsCacheTtlSeconds) {
        this.dnsCacheTtlSeconds = dnsCacheTtlSeconds;
    }

    public static final long DEFAULT_TIMEOUT_THRESHOLD = 20;
    private long timeoutThresholdMillis = DEFAULT_TIMEOUT_THRESHOLD;

    /**
     * URL to favicon.ico
     */
    private String favicon;

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public long getTimeoutThresholdMillis() {
        return timeoutThresholdMillis;
    }

    public void setTimeoutThresholdMillis(long timeoutThresholdMillis) {
        this.timeoutThresholdMillis = timeoutThresholdMillis;
    }

    private Map<String, Map> providers;

    private Map<String, ClientConfig> clients;

    public Map<String, ClientConfig> getClients() {
        return clients;
    }

    private Lot49SubscriptionData subscriptionData;

    public void setClients(Map<String, ClientConfig> clients) {
        this.clients = clients;
    }

    public Map<String, Map> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, Map> providers) {
        this.providers = providers;
    }

    public OrchestratorConfig getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(OrchestratorConfig orchestrator) {
        this.orchestrator = orchestrator;
    }

    private DbConfigs databases;

    public DbConfigs getDatabases() {
        return databases;
    }

    public void setDatabases(DbConfigs databases) {
        this.databases = databases;
    }

    private OrchestratorConfig orchestrator;

    private AdCacheConfig adCache;
    private MagicConfig magic;

    public MagicConfig getMagic() {
        return magic;
    }

    public void setMagic(MagicConfig magic) {
        this.magic = magic;
    }

    // somaxconn
    private int jettyAcceptQueueSize = 65535;

    public int getJettyAcceptQueueSize() {
        return jettyAcceptQueueSize;
    }

    public void setJettyAcceptQueueSize(int jettyAcceptQueueSize) {
        this.jettyAcceptQueueSize = jettyAcceptQueueSize;
    }

    private String container = "jetty";

    private int maxCookieAge = DEFAULT_MAX_COOKIE_AGE;

    /**
     * Cookie TTL.
     * 
     * @see #getCookieDomain()
     * 
     * @see #getUserIdCookie()
     */
    public int getMaxCookieAge() {
        return maxCookieAge;
    }

    public void setMaxCookieAge(int maxCookieAge) {
        this.maxCookieAge = maxCookieAge;
    }

    public static final int DEFAULT_MAX_COOKIE_AGE = 60 * 60 * 24 * 30;

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    private String adConfigScriptDir;

    private String baseMicroputTrackingUrl;

    private String statsUrl;

    public String getStatsUrl() {
        return statsUrl == null ? getBaseUrl() : statsUrl;
    }

    public void setStatsUrl(String statsUrl) {
        this.statsUrl = statsUrl;
    }

    private String baseUrl = null;

    private String cookieDomain;

    @Deprecated
    private boolean debug = false;

    @Deprecated
    public String getDeploy() {
        return deploy;
    }

    @Deprecated
    public void setDeploy(String deploy) {
        this.deploy = deploy;
    }

    private String deploy;

    private ExchangesConfig exchanges;

    private int executorThreadPoolSize;

    private String host;

    private boolean jettyTracing;

    private int keepAliveTimeoutSeconds = 75;

    private MaxMindConfig maxMind;

    private int port;

    private boolean timeoutBidProcessing;

    private UserCacheConfig userCache;

    private UserSegmentsCacheConfig userSegmentsCache;

    private UserAttributesCacheConfig userAttributesCache;

    private IntegralConfig integral;

    private int experimentDataTtlDays;

    public UserCacheConfig getUserCache() {
        return userCache;
    }

    public void setUserCache(UserCacheConfig userCache) {
        this.userCache = userCache;
    }

    private String userIdCookie;

    private long winTimeoutSeconds;

    public AdCacheConfig getAdCache() {
        return adCache;
    }

    public String getAdConfigScriptDir() {
        return adConfigScriptDir;
    }

    public String getBaseMicroputTrackingUrl() {
        return baseMicroputTrackingUrl;
    }

    /**
     * URL you'd like to show to the world (e.g., in
     * {@link AdminSvc#auth(javax.ws.rs.container.AsyncResponse, String, String)} <b>without</b> the
     * last leading slash. If not supplied, it will be http://{@link #getHost()}: {@link #getPort()}
     */
    public String getBaseUrl() {
        if (baseUrl == null) {
            return "http://" + getHost() + ":" + getPort();
        }
        return baseUrl;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    /**
     * Exchange-specific sections.
     */
    public ExchangesConfig getExchanges() {
        return exchanges;
    }

    public int getExecutorThreadPoolSize() {
        return executorThreadPoolSize;
    }

    public String getHost() {
        return host;
    }

    /**
     * See <a href="http://aws.amazon.com/blogs/aws/elb-idle-timeout-control/">Amazon ELB
     * timeout</a> is 60 seconds; it is advisable to keep this longer.
     */
    public int getKeepAliveTimeoutSeconds() {
        return keepAliveTimeoutSeconds;
    }

    private HazelcastServiceConfig hazelcast;

    public HazelcastServiceConfig getHazelcast() {
        return hazelcast;
    }

    public void setHazelcast(HazelcastServiceConfig hazelcast) {
        this.hazelcast = hazelcast;
    }

    public MaxMindConfig getMaxMind() {
        return maxMind;
    }

    public int getPort() {
        return port;
    }

    private AdminConfig admin;

    public AdminConfig getAdmin() {
        return admin;
    }

    public void setAdmin(AdminConfig admin) {
        this.admin = admin;
    }

    /**
     * Main cookie whose value is what we consider our User ID.
     * 
     * @see User#getBuyeruid()
     */
    public String getUserIdCookie() {
        return userIdCookie;
    }

    /**
     * Number, in seconds, after which we can assume that we have lost the auction.
     * 
     * @see Bidder#getExecutor()
     */
    public long getWinTimeoutSeconds() {
        return winTimeoutSeconds;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isJettyTracing() {
        return jettyTracing;
    }

    public boolean isTimeoutBidProcessing() {
        return timeoutBidProcessing;
    }

    public void setAdCache(AdCacheConfig adCache) {
        this.adCache = adCache;
    }

    public void setAdConfigScriptDir(String adConfigScriptDir) {
        this.adConfigScriptDir = adConfigScriptDir;
    }

    public void setBaseMicroputTrackingUrl(String baseMicroputTrackingUrl) {
        this.baseMicroputTrackingUrl = baseMicroputTrackingUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setExchanges(ExchangesConfig exchanges) {
        this.exchanges = exchanges;
    }

    public void setExecutorThreadPoolSize(int executorThreadPoolSize) {
        this.executorThreadPoolSize = executorThreadPoolSize;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setJettyTracing(boolean jettyTracing) {
        this.jettyTracing = jettyTracing;
    }

    public void setKeepAliveTimeoutSeconds(int keepAliveTimeout) {
        this.keepAliveTimeoutSeconds = keepAliveTimeout;
    }

    public void setMaxMind(MaxMindConfig maxMind) {
        this.maxMind = maxMind;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeoutBidProcessing(boolean timeoutBidProcessing) {
        this.timeoutBidProcessing = timeoutBidProcessing;
    }

    public void setUserIdCookie(String userIdCookie) {
        this.userIdCookie = userIdCookie;
    }

    public void setWinTimeoutSeconds(long winTimeout) {
        this.winTimeoutSeconds = winTimeout;
    }

    private Map<String, AppConfig> apps;

    public Map<String, AppConfig> getApps() {
        return apps;
    }

    public void setApps(Map<String, AppConfig> apps) {
        this.apps = apps;
    }

    public UserAttributesCacheConfig getUserAttributesCache() {
        return userAttributesCache;
    }

    public void setUserAttributesCache(UserAttributesCacheConfig userAttributesCache) {
        this.userAttributesCache = userAttributesCache;
    }

    public IntegralConfig getIntegral() {
        return integral;
    }

    public void setIntegral(IntegralConfig integral) {
        this.integral = integral;
    }

    public int getExperimentDataTtlDays() {
        return experimentDataTtlDays;
    }

    public void setExperimentDataTtlDays(int experimentDataTtlDays) {
        this.experimentDataTtlDays = experimentDataTtlDays;
    }


    private SecurityManagerConfig securityManagerConfig;

    public SecurityManagerConfig getSecurityManagerConfig() {
        return securityManagerConfig;
    }

    public void setSecurityManagerConfig(SecurityManagerConfig securityManagerConfig) {
        this.securityManagerConfig = securityManagerConfig;
    }

    public UserSegmentsCacheConfig getUserSegmentsCache() {
        return userSegmentsCache;
    }

    public void setUserSegmentsCache(UserSegmentsCacheConfig userSegmentsCache) {
        this.userSegmentsCache = userSegmentsCache;
    }

    public Lot49SubscriptionData getSubscriptionData() {
        return subscriptionData;
    }

    public void setSubscriptionData(Lot49SubscriptionData subscriptionData) {
        this.subscriptionData = subscriptionData;
    }
}
