package com.enremmeta.rtb.proto;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.OutputInterceptor;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.DbConfigs;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.rtb.jersey.AuctionsSvc;
import com.enremmeta.rtb.jersey.protobuf.ProtobufMessageReader;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

@Ignore
public abstract class AdaptersBaseTest {

    protected static ServiceRunner serviceRunner;
    protected static Lot49Config lot49Config;

    protected static File getTestDataFile(String name) {
        String cwd = System.getProperty("user.dir");
        File f = new File(cwd, "src/it/resources/data/" + name);
        return f;
    }

    @BeforeClass
    public static void setUp() throws Throwable {
        Map<String, String> env = new HashMap<String, String>(System.getenv());
        env.put(KVKeysValues.ENV_DYNAMO_ENDPOINT, "dynamodb.us-east-1.amazonaws.com");
        env.put("NODE_ID", "test_node");
        env.put("ADS_HOME", System.getProperty("user.dir"));

        setEnv(env);

        String configFile = System.getProperty("user.dir")
                        + "/src/it/resources/config/lot49.testit.adcache.json";

        lot49Config = (Lot49Config) Utils.loadConfig(configFile, Lot49Config.class);
        if (lot49Config == null) {
            lot49Config.setContainer("none");

            RedisServiceConfig rc = new RedisServiceConfig();

            rc.setHost("localhost");
            rc.setPort(6379);
            rc.setTimeoutMillis(1000);
            rc.setFcTtlMilliseconds(100);
            rc.setPoolTtlMinutes(60);
            rc.setPoolSize(2);
            rc.setShortLivedMapTtlSeconds(600);

            DbConfigs dbConfigs = lot49Config.getDatabases();
            dbConfigs.setRedises(new HashMap<String, RedisServiceConfig>());
            dbConfigs.getRedises().put("redis", rc);

            // We need adCache for the key-per-owner function.
            lot49Config.setAdCache(new AdCacheConfig());
            AdCacheConfig adCacheConfig = lot49Config.getAdCache();

            adCacheConfig.setShortTermDb("redis");
            adCacheConfig.setTtlMinutes(1);
            adCacheConfig.setPacing(new PacingServiceConfig());
            adCacheConfig.getPacing().setMessageTtlMinutes(480);
            adCacheConfig.getPacing().setEffectiveWinRateIfLessThanMin(0.1);
            adCacheConfig.getPacing().setWinRateTtlMinutes(60);
            adCacheConfig.getPacing().setRedis(rc);
        }

        try {
            Bidder.test(lot49Config);
            serviceRunner = Bidder.getInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
        }

        serviceRunner.getAdCache().doRun(false);

        startServerJetty();

    }

    private static void startServerJetty() throws Throwable {
        Server server = new Server();
        ServerConnector con = new ServerConnector(server);
        con.setAcceptQueueSize(lot49Config.getJettyAcceptQueueSize());
        con.setPort(lot49Config.getPort());
        con.setIdleTimeout(lot49Config.getKeepAliveTimeoutSeconds() * 1000);
        con.setSoLingerTime(0);
        ServletContextHandler context =
                        new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        ServletHolder sh = context.addServlet(ServletContainer.class, "/*");

        String svcPkgs = AuctionsSvc.class.getPackage().getName() + ";"
                        + OpenRtbRequest.class.getPackage().getName() + ";"
                        + ProtobufMessageReader.class.getPackage().getName();
        sh.setInitParameter(ServerProperties.PROVIDER_PACKAGES,
                        "com.fasterxml.jackson.jaxrs.json;" + svcPkgs);
        sh.setInitOrder(1);
        sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        sh.setInitParameter(ServerProperties.MOXY_JSON_FEATURE_DISABLE, "true");

        sh.setInitParameter(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, "true");

        if (lot49Config.isJettyTracing()) {
            sh.setInitParameter(ServerProperties.TRACING, "ALL");
            sh.setInitParameter(ServerProperties.TRACING_THRESHOLD, "VERBOSE");
        }

        server.start();
        con.start();
        System.out.flush();
        System.setOut(new PrintStream(new OutputInterceptor()));
        System.setErr(new PrintStream(new OutputInterceptor()));

        AdXAdapter a = (AdXAdapter) ExchangeAdapterFactory.getExchangeAdapter("adx");
        a.parse("VbkSGAAB2AsKaReKAAIZn67UIYViBeAxU1_hpw", 0);

    }

    public static RedisConnection<String, String> getRedisConnection(RedisServiceConfig config)
                    throws Lot49Exception {
        final String redisHost = config.getHost();
        final int redisPort = config.getPort();
        String hostPort = redisHost + ":" + redisPort;
        final RedisClient client = new RedisClient(redisHost, redisPort);
        final RedisConnection<String, String> redisCon = client.connect();
        if (redisCon == null) {
            throw new Lot49Exception("Error connecting to Redis at " + hostPort);
        }
        redisCon.setTimeout(config.getTimeoutMillis(), TimeUnit.MILLISECONDS);
        return redisCon;
    }

    protected static void setEnv(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                            .getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv =
                            (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
