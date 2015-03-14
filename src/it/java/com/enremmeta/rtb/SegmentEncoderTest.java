package com.enremmeta.rtb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.DbConfigs;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.config.UserAttributesCacheConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.rtb.proto.adaptv.AdaptvConfig;
import com.enremmeta.rtb.proto.adx.AdXConfig;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchConfig;
import com.enremmeta.rtb.proto.liverail.LiverailConfig;
import com.enremmeta.rtb.proto.openx.OpenXConfig;
import com.enremmeta.rtb.proto.pubmatic.PubmaticConfig;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindConfig;
import com.enremmeta.util.ServiceRunner;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

/**
 * Test {@link SegmentEncoder}. TODO this is a work in progress
 * 
 * This is also a good example of how to do integration tests.
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 */

public class SegmentEncoderTest {

    private static Set<String> segments;
    private static Map<String, Map<String, String>> segmentsMap;

    private static Map<String, byte[]> keys = new HashMap<String, byte[]>();

    private static ServiceRunner serviceRunner;

    @BeforeClass
    public static void setUp() throws Exception {

        Lot49Config config = getTestConfig();

        // Add the main key.
        config.setLogKey(genKey());

        try {
            Bidder.test(config);
            serviceRunner = Bidder.getInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
        }

        segmentsMap = new HashMap<String, Map<String, String>>();
        segmentsMap.put("XXX", new HashMap<String, String>());
        Map<String, String> score1 = new HashMap<String, String>();
        score1.put("score", "12.0");
        segmentsMap.put("37:fp:100", score1);
        Map<String, String> score2 = new HashMap<String, String>();
        score2.put("score", "15.0");
        segmentsMap.put("137:fp:100", score2);
        Map<String, String> score3 = new HashMap<String, String>();
        score3.put("score", "115.0");
        segmentsMap.put("99:fp:200", score3);
        Map<String, String> score4 = new HashMap<String, String>();
        score4.put("score", "0");
        segmentsMap.put("66:fp:200", score4);

        segments = segmentsMap.keySet();

        RedisConnection<String, String> redisCon =
                        getRedisConnection(config.getAdCache().getPacing().getRedis());
        if (redisCon != null) {
            for (String segment : segments) {
                String owner = serviceRunner.getSegmentToOwnerCodec().getOwner(segment);
                System.out.println("Owner of " + segment + " looks to be " + owner);
                if (owner == null) {
                    continue;
                }

                byte[] keyBytes = genKey();
                String keyString = Base64.encodeBase64String(keyBytes);
                if (!keys.containsKey(owner)) {
                    keys.put(owner, keyBytes);
                    redisCon.set(KVKeysValues.OWNER_KEY_PREFIX + owner, keyString);
                }
            }
            redisCon.close();
        }

        serviceRunner.getAdCache().doRun(false);

    }

    // http://stackoverflow.com/questions/20796042/aes-encryption-and-decryption-with-java
    @Test
    public void testInternalEncoder() {
        try {
            byte[] keyBytes = serviceRunner.getConfig().getLogKey();

            String encodedSegments =
                            new InternalSegmentEncoder(new UserSegments(segmentsMap)).getEncoded();
            System.out.println("Encoded " + segments + " to " + encodedSegments);
            String expected = String.join(",", segments);
            String decoded = decrypt(encodedSegments, keyBytes);

            assertEquals(expected, decoded);

        } catch (Throwable t) {
            t.printStackTrace();
            Assert.fail(t.getMessage());
        }
    }

    @Test
    public void testExternalEncoder() {

        try {

            for (String owner : keys.keySet()) {
                byte[] expectedKey = keys.get(owner);
                byte[] actualKey = Bidder.getInstance().getAdCache().getOwnerKey(owner);
                assertNotNull(actualKey);
                Assert.assertArrayEquals(expectedKey, actualKey);
            }

            String encodedSegments =
                            new ExternalSegmentEncoder(new UserSegments(segmentsMap)).getEncoded();
            System.out.println("Encoded " + segments + " to " + encodedSegments);

            List<String> expectedSegments = new ArrayList<String>();;
            for (String segment : segments) {
                String owner = serviceRunner.getSegmentToOwnerCodec().getOwner(segment);
                if (owner == null) {
                    continue;
                } else {
                    expectedSegments.add(segment);
                }
            }

            List<String> actualSegments = new ArrayList<String>();;
            String[] encoded = encodedSegments.split(",");
            for (String segment : encoded) {
                for (String owner : keys.keySet()) {
                    byte[] key = keys.get(owner);
                    try {
                        String decoded = decrypt(segment, key);
                        actualSegments.addAll(Arrays.asList(decoded.split(",")));
                    } catch (Exception e) {
                        // Wrong ownerKey for encrypted segments
                    }
                }
            }

            Collections.sort(expectedSegments);
            Collections.sort(actualSegments);

            assertEquals(String.join(",", expectedSegments), String.join(",", actualSegments));

        } catch (Throwable t) {
            t.printStackTrace();
            Assert.fail(t.getMessage());
        }
    }

    // http://stackoverflow.com/questions/20796042/aes-encryption-and-decryption-with-java
    public static byte[] genKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(SegmentEncoder.ENCRYPTION_ALGORITHM);
        keyGen.init(128);
        SecretKey secKey = keyGen.generateKey();
        return secKey.getEncoded();
    }

    private String decrypt(String cipherTextBase64, byte[] key) throws Exception {
        SecretKey skey = new SecretKeySpec(key, SegmentEncoder.ENCRYPTION_ALGORITHM);
        Cipher aesCipher = Cipher.getInstance(SegmentEncoder.ENCRYPTION_ALGORITHM);
        aesCipher.init(Cipher.DECRYPT_MODE, skey);
        byte[] cipherText = Base64.decodeBase64(cipherTextBase64);
        byte[] bytePlainText = aesCipher.doFinal(cipherText);
        return new String(bytePlainText);
    }

    public static Lot49Config getTestConfig() {

        // Create config
        Lot49Config config = new Lot49Config();
        config.setContainer("none");

        // Deal with Db configs
        RedisServiceConfig rc = new RedisServiceConfig();

        rc.setHost("localhost");
        rc.setPort(6379);
        rc.setTimeoutMillis(1000);
        rc.setFcTtlMilliseconds(100);
        rc.setPoolTtlMinutes(60);
        rc.setPoolSize(2);
        rc.setShortLivedMapTtlSeconds(600);

        config.setDatabases(new DbConfigs());
        DbConfigs dbConfigs = config.getDatabases();
        dbConfigs.setRedises(new HashMap<String, RedisServiceConfig>());
        dbConfigs.getRedises().put("redis", rc);

        config.setUserAttributesCache(new UserAttributesCacheConfig());
        config.getUserAttributesCache().setMapDb("dynUserAttributes");

        // We need adCache for the key-per-owner function.
        config.setAdCache(new AdCacheConfig());
        AdCacheConfig adCacheConfig = config.getAdCache();

        adCacheConfig.setDir("/opt/lot49/ads");
        adCacheConfig.setShortTermDb("redis");
        adCacheConfig.setTtlMinutes(1);
        adCacheConfig.setPacing(new PacingServiceConfig());
        adCacheConfig.getPacing().setMessageTtlMinutes(480);
        adCacheConfig.getPacing().setEffectiveWinRateIfLessThanMin(0.1);
        adCacheConfig.getPacing().setWinRateTtlMinutes(60);
        adCacheConfig.getPacing().setRedis(rc);

        config.setOrchestrator(new OrchestratorConfig());
        OrchestratorConfig orchConfig = config.getOrchestrator();
        orchConfig.setDeployType(LocalOrchestrator.DEPLOY_TYPE);

        config.setExchanges(new ExchangesConfig());
        config.getExchanges().setAdaptv(new AdaptvConfig());
        config.getExchanges().getAdaptv().setAssumeSwfIfVpaid(true);
        config.getExchanges().getAdaptv().setBuyerId("inpagegroupllc");
        config.getExchanges().getAdaptv().setDefaultMaxDuration(30);

        config.getExchanges().setAdx(new AdXConfig());

        byte[] encryptionKey = {39, 64, (byte) 199, 123, (byte) 223, (byte) 216, 125, 24, 71, 120,
                        (byte) 225, 123, (byte) 161, (byte) 128, 4, 23, (byte) 231, 38, 38,
                        (byte) 185, 13, 41, (byte) 176, (byte) 179, (byte) 191, (byte) 243, 88,
                        (byte) 247, (byte) 227, 80, (byte) 209, 105};
        config.getExchanges().getAdx().setEncryptionKey(encryptionKey);
        byte[] integrityKey = {73, (byte) 139, 81, 59, (byte) 221, 62, 64, (byte) 175, 53,
                        (byte) 236, (byte) 190, (byte) 154, 9, 27, (byte) 224, (byte) 132,
                        (byte) 238, 127, 49, (byte) 232, 78, 25, 7, (byte) 146, (byte) 223, 40,
                        (byte) 130, 122, (byte) 215, (byte) 226, (byte) 186, (byte) 200};
        config.getExchanges().getAdx().setIntegrityKey(integrityKey);
        config.getExchanges().getAdx().setGeoTable("/opt/lot49/data/geo/adx/geo-table.csv");;
        config.getExchanges().getAdx().setNid("inpage_group_wopendsp");

        config.getExchanges().setBidswitch(new BidSwitchConfig());
        config.getExchanges().getBidswitch().setSeatId("89");

        config.getExchanges().setLiverail(new LiverailConfig());
        config.getExchanges().getLiverail().setSeatId("113431");

        config.getExchanges().setOpenx(new OpenXConfig());
        config.getExchanges().getOpenx().setEncryptionKey(
                        "6AF5179E373E4BF6B125354C2FCA7679D1523F305B974053B83879826AD1CCFB");
        config.getExchanges().getOpenx().setIntegrityKey(
                        "6503152458AF4CDEA61EF699417FB98965DED58DE8494964BDD721F5E78E6598");
        config.getExchanges().getOpenx().setWinTimeout(600);

        config.getExchanges().setPubmatic(new PubmaticConfig());
        config.getExchanges().getPubmatic().setVcode("bz0yJnR5cGU9MSZjb2RlPTMxNzkmdGw9MTI5NjAw");

        config.setMaxMind(new MaxMindConfig());
        config.getMaxMind().setCity("/opt/lot49/data/geo/maxmind/GeoIP2-City.mmdb");
        config.getMaxMind().setConnectionType(
                        "/opt/lot49/data/geo/maxmind/GeoIP2-Connection-Type.mmdb");
        config.getMaxMind().setDomain("/opt/lot49/data/geo/maxmind/GeoIP2-Domain.mmdb");
        config.getMaxMind().setIsp("/opt/lot49/data/geo/maxmind/GeoIP2-ISP.mmdb");

        return config;
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

}
