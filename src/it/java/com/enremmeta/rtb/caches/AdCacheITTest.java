package com.enremmeta.rtb.caches;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.util.BidderCalendar;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

public class AdCacheITTest {
    /*
     * @BeforeClass public static void beforeAll() { System.out.println("@BeforeClass: start Bidder"
     * ); String configFile = System.getProperty("user.dir") +
     * "/testit/resources/config/lot49.testit.json"; String[] argv = { "-c", configFile };
     * 
     * Map<String, String> env = new HashMap<String, String>(System.getenv());
     * env.put(KVKeysValues.ENV_DYNAMO_ENDPOINT,"dynamodb.us-east-1.amazonaws.com");
     * env.put("NODE_ID", "test_node");
     * 
     * setEnv(env);
     * 
     * Bidder.main(argv); System.out.println("@BeforeClass: Bidder started"); }
     */

    @Test
    public void testCalendar() {
        BidderCalendar c1 = BidderCalendar.getInstance();
        BidderCalendar c2 = BidderCalendar.getInstance();

        System.out.println("c1 currentTimeMillis: " + c1.currentTimeMillis());
        System.out.println("c2 currentTimeMillis: " + c2.currentTimeMillis());

        c1.addHours(5);

        System.out.println("c1 currentTimeMillis: " + c1.currentTimeMillis());
        System.out.println("c2 currentTimeMillis: " + c2.currentTimeMillis());
    }

    @Test
    public void testAdCache() {

        try {

            System.out.println("###### Start Bidder ######");
            Map<String, String> env = new HashMap<String, String>(System.getenv());
            env.put(KVKeysValues.ENV_DYNAMO_ENDPOINT, "dynamodb.us-east-1.amazonaws.com");
            env.put("NODE_ID", "test_node");
            env.put("ADS_HOME", System.getProperty("user.dir"));

            setEnv(env);

            String configFile = System.getProperty("user.dir")
                            + "/src/it/resources/config/lot49.testit.adcache.json";
            String[] argv = {"-c", configFile};

            Bidder.main(argv);

            Thread.sleep(5 * 1000);

            System.out.println("###### Bidder started ######");

            Bidder bidder = (Bidder) Bidder.getInstance();

            assertNotNull(bidder);

            String adId = "78";
            String addTestAdDataMsg = addTestAdDataInRedison(bidder.getConfig().getAdCache(), adId);
            System.out.println("AdCache addTestAdDataInRedison: \n" + addTestAdDataMsg);

            long prevBudget = 0;
            long currentBudget =
                            Long.valueOf(getCurrentBudget(bidder.getConfig().getAdCache(), adId));
            System.out.println("Start budget: " + currentBudget);

            AdCache adCache = bidder.getAdCache();

            // while (currentBudget > 0 || (currentBudget - prevBudget) != 0) {
            while (currentBudget > 0) {

                BidderCalendar.getInstance().addMinutes(1);

                Ad ad = adCache.getAd(adId);

                long remainingBidsToMake = ad.getRemainingBidsToMake();
                Random rand = new Random();

                int bidsToMake = rand.nextInt(10) + 1;
                // long bidsToMake = Math.round(Math.ceil(remainingBidsToMake * bidder.getConfig()
                // .getAdCache().getPacing().getEffectiveWinRateIfLessThanMin()));
                // bidsToMake = (bidsToMake > 0 ? (1 + (int) (Math.random() * ((bidsToMake - 1) +
                // 1)))
                // : 0);
                // bidsToMake = (int) (Math.random() * ((bidsToMake - 1) + 1));

                long bidPrice = ad.getBidPrice(null);

                ad.setBidsToMake(bidsToMake);
                ad.incrSpendAmount(bidsToMake * bidPrice);
                System.out.println("\nremainingBidsToMake: " + remainingBidsToMake);
                System.out.println("bidsToMake: " + bidsToMake);
                System.out.println("bidPrice: " + bidPrice);
                System.out.println("spendAmount: " + ad.getSpendAmount());

                String msg = adCache.doRun(false);
                prevBudget = currentBudget;
                currentBudget = Long
                                .valueOf(getCurrentBudget(bidder.getConfig().getAdCache(), adId));
                System.out.println(msg);
                System.out.println("#########  Current budget: " + currentBudget
                                + "  ##########################################################");
            }

            assertFalse(currentBudget < 0);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private String getCurrentBudget(AdCacheConfig config, String adId) {

        final String bidBudgetKey = KVKeysValues.BUDGET_PREFIX + adId;

        String budget = "0";
        RedisConnection<String, String> redisCon = null;
        try {
            redisCon = getRedisConnection(config);

            budget = redisCon.get(bidBudgetKey);

        } catch (Lot49Exception e) {
            budget = "-1";
        } finally {
            if (redisCon != null) {
                redisCon.close();
            }
        }
        return budget;
    }

    private String addTestAdDataInRedison(AdCacheConfig config, String adId) {

        final String bidBudgetKey = KVKeysValues.BUDGET_PREFIX + adId;
        final String bidStartKey = KVKeysValues.STARTS_ON_PREFIX + adId;
        final String bidEndKey = KVKeysValues.ENDS_ON_PREFIX + adId;
        final String bidPriceKey = KVKeysValues.BID_PRICE_PREFIX + adId;
        final String prevSpendAmountKey = KVKeysValues.PREVIOUS_SPEND_AMOUNT_PREFIX + adId;
        final String spendAmountKey = KVKeysValues.SPEND_AMOUNT_PREFIX + adId;
        final String winsKey = KVKeysValues.WIN_COUNT_PREFIX + adId;
        final String bidsKey = KVKeysValues.BID_COUNT_PREFIX + adId;
        // final String bidAmountKey = KVKeysValues.BID_AMOUNT_PREFIX + adId;
        final String winRateStartedKey = KVKeysValues.WIN_RATE_STARTED_TS + adId;

        StringBuilder sb = new StringBuilder();
        RedisConnection<String, String> redisCon = null;

        try {
            redisCon = getRedisConnection(config);
            int budgetAmount = 120000;
            // int priceAmount = 3000;

            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
            final DateTime now = DateTime.now(DateTimeZone.UTC);
            String startStr = now.minusDays(0).toString(dtf);
            String endStr = now.plusDays(6).toString(dtf);

            redisCon.del(bidBudgetKey, bidStartKey, bidEndKey, prevSpendAmountKey, spendAmountKey,
                            winRateStartedKey, winsKey, bidsKey);

            sb.append("\nSet " + bidBudgetKey + " to " + budgetAmount + ": "
                            + redisCon.set(bidBudgetKey, String.valueOf(budgetAmount)));
            sb.append("\nSet " + bidStartKey + " to " + startStr + ": "
                            + redisCon.set(bidStartKey, startStr));
            sb.append("\nSet " + bidEndKey + " to " + endStr + ": "
                            + redisCon.set(bidEndKey, endStr));
            // sb.append("\nSet " + bidPriceKey + " to " + priceAmount + ": " +
            // redisCon.set(bidPriceKey, String.valueOf(priceAmount)));
            sb.append("\n");

        } catch (Lot49Exception e) {
            sb.append("ERROR: " + e.getMessage());
        } finally {
            if (redisCon != null) {
                redisCon.close();
            }
        }
        return sb.toString();
    }

    private final RedisConnection<String, String> getRedisConnection(AdCacheConfig config)
                    throws Lot49Exception {
        final PacingServiceConfig pacingConf = config.getPacing();
        final RedisServiceConfig redisConf = pacingConf.getRedis();
        final String redisHost = redisConf.getHost();
        final int redisPort = redisConf.getPort();
        String hostPort = redisHost + ":" + redisPort;
        final RedisClient client = new RedisClient(redisHost, redisPort);
        final RedisConnection<String, String> redisCon = client.connect();
        if (redisCon == null) {
            throw new Lot49Exception("Error connecting to Redis at " + hostPort);
        }
        redisCon.setTimeout(redisConf.getTimeoutMillis(), TimeUnit.MILLISECONDS);
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
