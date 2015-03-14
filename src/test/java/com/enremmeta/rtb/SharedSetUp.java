package com.enremmeta.rtb;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;

import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.spi.PowerMockPolicy;
import org.powermock.mockpolicies.MockPolicyClassLoadingSettings;
import org.powermock.mockpolicies.MockPolicyInterceptionSettings;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.AdImpl;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.TagImpl;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.config.SecurityManagerConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisService;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.proto.adaptv.AdaptvConfig;
import com.enremmeta.rtb.proto.adx.AdXConfig;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchConfig;
import com.enremmeta.rtb.proto.liverail.LiverailConfig;
import com.enremmeta.rtb.proto.openx.OpenXConfig;
import com.enremmeta.rtb.proto.pubmatic.PubmaticConfig;
import com.enremmeta.util.ServiceRunner;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

public class SharedSetUp implements PowerMockPolicy {

    @Override
    public void applyClassLoadingPolicy(MockPolicyClassLoadingSettings settings) {
        settings.addFullyQualifiedNamesOfClassesToLoadByMockClassloader(
                        ServiceRunner.class.getName());
    }

    @Override
    public void applyInterceptionPolicy(MockPolicyInterceptionSettings settings) {
        // isn't used
    }

    public static ServiceRunner createServiceRunnerMock() {
        PowerMockito.mockStatic(ServiceRunner.class);

        ServiceRunner serviceRunnerSimpleMock =
                        PowerMockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.doReturn(configMock).when(serviceRunnerSimpleMock).getConfig();

        return serviceRunnerSimpleMock;
    }

    /**
     * Creates mock for Bidder. Don't forget add ServiceRunner and Bidder classes
     * into @PrepareForTest() annotation.
     * 
     * @return mock for Bidder
     */
    public static Bidder createBidderMock() {
        PowerMockito.mockStatic(ServiceRunner.class);

        Bidder bidderMock = PowerMockito.mock(Bidder.class, Mockito.CALLS_REAL_METHODS);
        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        PowerMockito.when(ServiceRunner.getInstance()).thenReturn(bidderMock);
        Mockito.doReturn(configMock).when(bidderMock).getConfig();

        return bidderMock;
    }

    /**
     * Creates mock for class inherited from ExchangeAdapter, that will be returned by method
     * ExchangeAdapterFactory.getExchangeAdapter(). Don't forget add ExchangeAdapterFactory and
     * appropriate class inherited from ExchangeAdapter into @PrepareForTest() annotation.
     * 
     * @param exchangeName
     *            name of exchange adapter
     * @param defaultAnswer
     *            default answer for unstubbed methods
     * @return mock object for exchange adapter
     */
    @SuppressWarnings("rawtypes")
    public static ExchangeAdapter createExchangeAdapterMock(String exchangeName,
                    Answer<Object> defaultAnswer) {
        ExchangeAdapter adapterMock = null;

        if (exchangeName != null) {
            Map<String, Class<? extends ExchangeAdapter>> adapterMap =
                            Whitebox.getInternalState(ExchangeAdapterFactory.class, "adapterMap");
            Class<? extends ExchangeAdapter> adapterClass = adapterMap.get(exchangeName);

            if (adapterClass != null) {
                PowerMockito.mockStatic(ExchangeAdapterFactory.class);

                adapterMock = PowerMockito.mock(adapterClass, defaultAnswer);
                PowerMockito.when(ExchangeAdapterFactory.getExchangeAdapter(exchangeName))
                                .thenReturn(adapterMock);
            }
        }

        return adapterMock;
    }

    public static Ad createAdMock(String id, String name, String desc) {
        Ad adMock = Mockito.mock(AdImpl.class);

        Mockito.doReturn(id).when(adMock).getId();
        Mockito.doReturn(name).when(adMock).getName();
        Mockito.doReturn(desc).when(adMock).getDesc();

        Mockito.doCallRealMethod().when(adMock).setBidPrice(anyLong());
        Mockito.doCallRealMethod().when(adMock).getBidPrice(any(OpenRtbRequest.class));
        Mockito.doCallRealMethod().when(adMock).getBidPriceCpm(any(OpenRtbRequest.class));

        Mockito.doCallRealMethod().when(adMock).setBidProbability(anyInt());
        Mockito.doCallRealMethod().when(adMock).getBidProbability();

        return adMock;
    }

    public static Tag createTagMock(String id, String name, String desc) {
        Tag tagMock = Mockito.mock(TagImpl.class);

        Mockito.doReturn(id).when(tagMock).getId();
        Mockito.doReturn(name).when(tagMock).getName();
        Mockito.doReturn(desc).when(tagMock).getDesc();

        return tagMock;
    }

    public static AdCacheConfig createAdCacheConfig() {
        RedisServiceConfig redisServiceConfig = createRedisServiceConfig();

        AdCacheConfig adCacheConfig = new AdCacheConfig();
        adCacheConfig.setPacing(new PacingServiceConfig());
        adCacheConfig.getPacing().setRedis(redisServiceConfig);

        return adCacheConfig;
    }

    public static RedisServiceConfig createRedisServiceConfig() {
        RedisServiceConfig redisServiceConfig = new RedisServiceConfig();
        redisServiceConfig.setHost("221.34.157.44");
        redisServiceConfig.setPort(3000);
        redisServiceConfig.setShortLivedMapTtlSeconds(60);
        redisServiceConfig.setPoolSize(4);

        return redisServiceConfig;
    }

    public static RedisService createRedisServiceMock() throws Exception {
        RedisServiceConfig redisServiceConfig = createRedisServiceConfig();

        RedisService redisServiceMock = PowerMockito.mock(RedisService.class);
        PowerMockito.when(redisServiceMock, "getConfig").thenReturn(redisServiceConfig);

        return redisServiceMock;
    }

    /**
     * Creates mock for RedisClient. Don't forget to add class where RedisClient must be created
     * into @PrepareForTest() annotation.
     * 
     * @return mock for RedisClient
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static RedisClient createRedisClientMock() throws Exception {
        RedisClient redisClientMock = PowerMockito.mock(RedisClient.class);
        RedisConnection<String, String> redisConnectionMock =
                        PowerMockito.mock(RedisConnection.class);

        PowerMockito.when(redisClientMock.connect()).thenReturn(redisConnectionMock);
        PowerMockito.whenNew(RedisClient.class).withAnyArguments().thenReturn(redisClientMock);

        return redisClientMock;
    }

    public static SecurityManagerConfig createSecurityManagerConfig() {
        SecurityManagerConfig config = new SecurityManagerConfig();

        config.setPackageBlackList(
                        Arrays.asList("sun", "com.enremmeta.rtb.caches", "com.enremmeta.rtb.cli",
                                        "com.enremmeta.rtb.config", "com.enremmeta.rtb.dao",
                                        "com.enremmeta.rtb.impl", "com.enremmeta.rtb.jersey",
                                        "com.enremmeta.rtb.magic", "com.enremmeta.rtb.sandbox"));
        config.setPackageWhiteList(Arrays.asList());
        config.setClassBlackList(Arrays.asList("java.lang.Thread"));
        config.setClassWhiteList(Arrays.asList());

        return config;
    }

    public static final byte[] ENCRIPTION_KEY = new byte[] {39, 64, (byte) 199, 123, (byte) 223,
                    (byte) 216, 125, 24, 71, 120, (byte) 225, 123, (byte) 161, (byte) 128, 4, 23,
                    (byte) 231, 38, 38, (byte) 185, 13, 41, (byte) 176, (byte) 179, (byte) 191,
                    (byte) 243, 88, (byte) 247, (byte) 227, 80, (byte) 209, 105};

    public static final byte[] INTEGRITY_KEY = new byte[] {73, (byte) 139, 81, 59, (byte) 221, 62,
                    64, (byte) 175, 53, (byte) 236, (byte) 190, (byte) 154, 9, 27, (byte) 224,
                    (byte) 132, (byte) 238, 127, 49, (byte) 232, 78, 25, 7, (byte) 146, (byte) 223,
                    40, (byte) 130, 122, (byte) 215, (byte) 226, (byte) 186, (byte) 200};

    public static AsyncResponse getFakeAsynkResponseObject() {
        return new AsyncResponse() {

            @Override
            public void setTimeoutHandler(TimeoutHandler handler) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean setTimeout(long time, TimeUnit unit) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean resume(Throwable response) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean resume(Object response) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Map<Class<?>, Collection<Class<?>>> register(Object callback,
                            Object... callbacks) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Map<Class<?>, Collection<Class<?>>> register(Class<?> callback,
                            Class<?>... callbacks) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Collection<Class<?>> register(Object callback) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Collection<Class<?>> register(Class<?> callback) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isSuspended() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isDone() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isCancelled() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean cancel(Date retryAfter) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean cancel(int retryAfter) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean cancel() {
                // TODO Auto-generated method stub
                return false;
            }
        };
    }


    public static class Ad_1001001_fake extends AdImpl {

        public Ad_1001001_fake() throws Lot49Exception {
            super();

        }

    }

    public static class Ad_1001001_mock extends AdImpl {

        public Ad_1001001_mock() throws Lot49Exception {
            super();

        }

        @Override
        public boolean canBid1(OpenRtbRequest br) {
            return true;
        }
    }

    public static final Geo GEO_MARKER = new Geo("Bratislava", "Bratislava Stare Mesto", "81100",
                    "Bratislava", "Slovakia");
    public static final String IP_MARKER = "192.30.252.131";

    public static void prepareExchanges(ExchangesConfig exchangesConfig) {

        AdaptvConfig adaptvConfigMock = Mockito.mock(AdaptvConfig.class);
        Mockito.when(adaptvConfigMock.getBuyerId()).thenReturn("buyerId");

        LiverailConfig liverailConfigMock = Mockito.mock(LiverailConfig.class);
        Mockito.when(liverailConfigMock.getSeatId()).thenReturn("seatId");

        AdXConfig adXConfig = new AdXConfig();
        prepareAdxConfigSecurity(adXConfig);

        PubmaticConfig pubmaticConfigMock = Mockito.mock(PubmaticConfig.class);
        Mockito.when(pubmaticConfigMock.getVcode()).thenReturn("vcode");

        BidSwitchConfig bidSwitchConfigMock = Mockito.mock(BidSwitchConfig.class);
        Mockito.when(bidSwitchConfigMock.getSeatId()).thenReturn("seatId");

        OpenXConfig openXConfig = new OpenXConfig();
        prepareOpenXConfigSecurity(openXConfig);
        exchangesConfig.setAdaptv(adaptvConfigMock);
        exchangesConfig.setLiverail(liverailConfigMock);
        exchangesConfig.setAdx(adXConfig);
        exchangesConfig.setPubmatic(pubmaticConfigMock);
        exchangesConfig.setBidswitch(bidSwitchConfigMock);
        exchangesConfig.setOpenx(openXConfig);
    }

    private static void prepareAdxConfigSecurity(AdXConfig xConfig) {
        xConfig.setEncryptionKey(SharedSetUp.ENCRIPTION_KEY);
        xConfig.setIntegrityKey(SharedSetUp.INTEGRITY_KEY);
    }

    private static void prepareOpenXConfigSecurity(OpenXConfig xConfig) {
        xConfig.setEncryptionKey("ENCRIPTION_KEY_STRING");
        xConfig.setIntegrityKey("INTEGRITY_KEY_STRING");
    }

    public static class Tag_2002002_tagMarker_1001001_fake extends TagImpl {

        private static AtomicInteger initCallCounter = new AtomicInteger(0);

        public static int getInitCallCounter() {
            return initCallCounter.get();
        }

        public static void setInitCallCounter(int value) {
            initCallCounter.set(value);
        }

        public Tag_2002002_tagMarker_1001001_fake(Ad ad) throws Lot49Exception {
            super(ad);
        }


        @Override
        public String getTagTemplate(OpenRtbRequest req, Impression imp, Bid bid) {
            return "<div>test tag</div>";
        }


        @Override
        public void init() {
            initCallCounter.incrementAndGet();
        }

        @Override
        public List<String> getMimes() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public static final String USER_AGENT =
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";

    public static final String AD_GROOVE_SAMPLE = "package com.enremmeta.rtb.groovy.tc\n"
                    + "import com.enremmeta.rtb.api.AdImpl\n"
                    + "import com.enremmeta.rtb.constants.Lot49Constants\n"
                    + "class Ad_78_testMyIPnewAdservice2 extends AdImpl {" + "void init() {\n"
                    + "adomain = [\"myip.io\"]\n"
                    + "iurl = \"http://creative.us.cf.opendsp.com/creatives/creative_openDSP_myIpN01_300_250/myip300x250_n.swf\"\n"
                    + "bidAmount = 100000;" + "tags = [\n"
                    + "new com.enremmeta.rtb.Tag_178_MYIP_78_testMyIPnewAdservice2(this)" + "]"
                    + "}" + "}\n";

    public static final String TAG_GROOVE_SAMPLE = "package com.enremmeta.rtb.groovy.tc\n"
                    + "import com.enremmeta.rtb.*\n" + "import com.enremmeta.rtb.api.*\n"
                    + "import com.enremmeta.rtb.api.proto.openrtb.*\n"
                    + "class Tag_178_MYIP_78_testMyIPnewAdservice2 extends TagImpl {\n"
                    + "public Tag_178_MYIP_78_testMyIPnewAdservice2(Ad ad) throws Lot49Exception { super(ad); }\n"
                    + "@Override public String getTagTemplate(OpenRtbRequest req, Impression imp, Bid bid) { return null; }\n"
                    + "@Override public void init() { banner = true; }\n" + "}\n";

    public static File createTempGroovyFile(TemporaryFolder tempFolder, String scriptSubdir,
                    String fileName, String groovyContent) throws IOException {
        if (scriptSubdir == null) {
            scriptSubdir = "";
        }
        if (groovyContent == null) {
            groovyContent = "";
        }

        File groovySrcDir = new File(tempFolder.getRoot(), "src");
        if (!groovySrcDir.exists()) {
            groovySrcDir.mkdir();
            groovySrcDir.setWritable(true);
            groovySrcDir.setReadable(true);
        }

        File groovyPkgDir = new File(groovySrcDir, scriptSubdir).getAbsoluteFile();
        Path pathToFile = Paths.get(groovyPkgDir.getAbsolutePath());
        Files.createDirectories(pathToFile);

        File groovyFile = new File(groovyPkgDir, fileName + ".groovy");
        groovyFile.createNewFile();

        FileWriter writer = new FileWriter(groovyFile);
        try {
            writer.write(groovyContent);
        } finally {
            writer.close();
        }

        return groovyFile;
    }

    /**
     * Json serializer that throws exception
     */
    public static class ThrowExceptionSerializer<T> extends JsonSerializer<T> {
        @Override
        public void serialize(T value, JsonGenerator jgen, SerializerProvider provider)
                        throws IOException, JsonProcessingException {
            throw new RuntimeException("Serialization exception"); /// generates
                                                                   /// JsonProcessingException
        }
    }

    /**
     * Json deserializer that throws exception
     */
    public static class ThrowExceptionDeserializer<T> extends JsonDeserializer<T> {
        @Override
        public T deserialize(JsonParser parser, DeserializationContext context)
                        throws IOException, JsonProcessingException {
            throw new IOException("Deserialization exception");
        }
    }
}
