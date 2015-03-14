package com.enremmeta.rtb.dao.impl.hazelcast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TagDescription;
import com.enremmeta.rtb.AwsOrchestrator;
import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.LostAuctionTask;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.caches.CacheObject;
import com.enremmeta.rtb.caches.CacheObjectSerializer;
import com.enremmeta.rtb.config.DbConfig;
import com.enremmeta.rtb.config.HazelcastServiceConfig;
import com.enremmeta.rtb.dao.DaoCache;
import com.enremmeta.rtb.dao.DaoCacheLoader;
import com.enremmeta.rtb.dao.DaoCounters;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.dao.DbService;
import com.enremmeta.rtb.jersey.StatsSvc;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;

/**
 * Shared caches across all instances - as Hazelcast. This will provide the following maps:
 * <ol>
 * <li>{@link #getShortLivedMap() short-lived map} -- evictions based on
 * {@link HazelcastServiceConfig#getShortLivedMapTtlSeconds() TTL}. Used for things such as storing
 * state</li>
 * </ol>
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class HazelcastService implements DbService {

    private final IdGenerator idGenerator;
    private final HazelcastServiceConfig config;

    public HazelcastService(ServiceRunner runner, HazelcastServiceConfig config)
                    throws Lot49Exception {
        this(runner, config, false);
    }

    private static final AtomicLong BYTES_WRITTEN = new AtomicLong(0);

    public static final void incrBytesWritten(long l) {
        BYTES_WRITTEN.incrementAndGet();
    }

    public static final long resetBytesWritten() {
        return BYTES_WRITTEN.getAndSet(0);
    }

    public HazelcastService(ServiceRunner runner, HazelcastServiceConfig config, boolean noInit)
                    throws Lot49Exception {
        super();
        this.config = config;
        makeHazelConfig(runner.getOrchestrator());

        if (hazelConfig != null) {
            final MapConfig mapConfigShort = new MapConfig();
            mapConfigShort.setBackupCount(0);
            mapConfigShort.setTimeToLiveSeconds((int) config.getShortLivedMapTtlSeconds());
            mapConfigShort.setName(MAP_NAME_SHORT_LIVED);
            hazelConfig.addMapConfig(mapConfigShort);

            final MapConfig mapConfigLong = makeLongLivedMapConfig(MAP_NAME_LONG_LIVED);
            hazelConfig.addMapConfig(mapConfigLong);
        }
        LogUtils.info("Configuring Hazelcast with " + hazelConfig);

        if (!noInit) {
            if (this.config.isEmbedded()) {
                hazelcastInstance = Hazelcast.newHazelcastInstance(hazelConfig);
            } else {
                hazelcastInstance = HazelcastClient.newHazelcastClient(hazelClientConfig);
            }

            shortLivedMap = hazelcastInstance.getMap(MAP_NAME_SHORT_LIVED);
            idGenerator = hazelcastInstance.getIdGenerator(ID);
            LogUtils.info("Initialized Hazelcast.");
        } else {
            hazelcastInstance = null;
            shortLivedMap = null;
            idGenerator = null;
        }
    }

    private Config hazelConfig;
    private ClientConfig hazelClientConfig;

    public void makeHazelConfig(Orchestrator orch) throws Lot49Exception {

        // Map configuration
        if (config.isEmbedded()) {
            hazelConfig = new Config();
            hazelConfig.setProperty("hazelcast.logging.type", "log4j2");
            // Network configuration.
            String deploy = orch.getDeployType();
            NetworkConfig networkConfig = new NetworkConfig();
            hazelConfig.setNetworkConfig(networkConfig);
            if (deploy.equals(AwsOrchestrator.DEPLOY_TYPE)) {
                final String region = this.config.getAwsRegion();
                if (region == null) {
                    throw new Lot49Exception("Hazelcast configuration: awsRegion required.");
                }
                LogUtils.info("Hazelcast configuration: awsRegion: " + region);
                final int connTimeoutSettings = this.config.getConnTimeoutSeconds();
                LogUtils.info("Hazelcast configuration: connTimeoutSettings: "
                                + connTimeoutSettings);

                final String skey = config.getAwsSecretKey();
                final String akey = config.getAwsAccessKey();
                final AwsConfig awsConfig = new AwsConfig().setEnabled(true).setAccessKey(akey)
                                .setSecretKey(skey).setConnectionTimeoutSeconds(connTimeoutSettings)
                                .setRegion(region);
                final String tagKey = this.config.getAwsTagKey();
                final String tagValue = this.config.getAwsTagValue();

                if (tagKey == null) {
                    if (tagValue != null) {
                        throw new Lot49Exception(
                                        "Hazelcast configuration: If awsTagValue is specified, awsTagKey also must be.");
                    } else {
                        LogUtils.info("Hazelcast configuration: no tags specified.");
                    }
                } else {
                    if (tagValue == null) {
                        throw new Lot49Exception(
                                        "Hazelcast configuration: If awsTagKey is specified, awsTagValue also must be.");
                    } else {
                        LogUtils.info("Hazelcast configuration: AWS tag key: " + tagKey);
                        LogUtils.info("Hazelcast configuration: AWS tag value: " + tagValue);
                        awsConfig.setTagKey(tagKey).setTagValue(tagValue);
                        // Here's a little trick that we may or may not need,
                        // but...
                        // It has come from PaulM's experience that tags may not
                        // immediately attach
                        // to the instance, and if our bootstrap.sh runs too
                        // early
                        // then this may be an issue.
                        LogUtils.info("Hazelcast configuration: is tag based, so waiting until self will have the tags!");

                        final AmazonEC2Client client =
                                        new AmazonEC2Client(new BasicAWSCredentials(akey, skey));

                        final long t0 = BidderCalendar.getInstance().currentTimeMillis();
                        boolean foundTags = false;
                        while (true) {
                            if (foundTags) {
                                break;
                            }
                            final String nodeId =
                                            Bidder.getInstance().getOrchestrator().getNodeId();
                            if ((BidderCalendar.getInstance().currentTimeMillis() - t0)
                                            / 1000 > connTimeoutSettings) {
                                String msg = connTimeoutSettings
                                                + " seconds passed, abandoning attempt to get tags for "
                                                + nodeId;
                                throw new Lot49Exception(msg);
                            }
                            LogUtils.info("Attempting to get tags for " + nodeId);

                            DescribeTagsResult descTagsRes =
                                            client.describeTags(new DescribeTagsRequest()
                                                            .withFilters(new Filter("resource-id")
                                                                            .withValues(nodeId)));
                            final List<TagDescription> tags = descTagsRes.getTags();
                            if (tags.size() > 0) {
                                LogUtils.info("Received tags: " + tags);
                                for (TagDescription tagDesc : tags) {
                                    final String tagKey2 = tagDesc.getKey();
                                    final String tagValue2 = tagDesc.getValue();
                                    LogUtils.info("Comparing required key <" + tagKey
                                                    + "> to received <" + tagKey2 + ">.");

                                    if (!tagKey.equals(tagKey2)) {
                                        continue;
                                    }
                                    LogUtils.info("Comparing required value <" + tagValue
                                                    + "> to received <" + tagValue2 + ">.");

                                    if (!tagValue.equals(tagValue2)) {
                                        throw new Lot49Exception("For " + nodeId
                                                        + " got expected tag " + tagKey
                                                        + " but value " + tagValue2 + " instead of "
                                                        + tagValue);
                                    }
                                    // Good to go

                                    foundTags = true;
                                    break;
                                }
                            }
                            if (foundTags) {
                                break;
                            }
                            try {
                                Thread.sleep(500);

                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }

                final String sg = this.config.getAwsSecurityGroupName();
                if (sg != null) {
                    LogUtils.info("Hazelcast configuration: AWS security group: " + sg);
                    awsConfig.setSecurityGroupName(sg);
                }

                JoinConfig joinConfig = new JoinConfig().setAwsConfig(awsConfig);
                joinConfig.setMulticastConfig(new MulticastConfig().setEnabled(false));
                networkConfig.setJoin(joinConfig);
                hazelConfig.setProperty("hazelcast.icmp.enabled", "true");
            } else if (deploy.equals("local")) {
                // networkConfig.setInterfaces(ifcConfig);
                JoinConfig joinConfig = new JoinConfig();
                joinConfig.setMulticastConfig(new MulticastConfig().setEnabled(false));

                joinConfig.setTcpIpConfig(
                                new TcpIpConfig().addMember("127.0.0.1").setEnabled(true));

                networkConfig.setJoin(joinConfig).setPortAutoIncrement(true);
                networkConfig.setPortAutoIncrement(true).setJoin(new JoinConfig()
                                .setTcpIpConfig(new TcpIpConfig().addMember("127.0.0.1"))
                                .setMulticastConfig(new MulticastConfig().setEnabled(false)));

            } else {
                throw new Lot49Exception("Unknown deploy.");
            }

            final String mgmtUrl = this.config.getManagementUrl();
            if (mgmtUrl != null) {
                LogUtils.info("Hazelcast configuration: Management center URL: " + mgmtUrl);
                hazelConfig.setManagementCenterConfig(
                                new ManagementCenterConfig().setEnabled(true).setUrl(mgmtUrl));
            }
            SerializerConfig serializerConfig = new SerializerConfig();
            serializerConfig.setClass(CacheObjectSerializer.class).setTypeClass(CacheObject.class);
            hazelConfig.setSerializationConfig(
                            new SerializationConfig().addSerializerConfig(serializerConfig));

        } else {
            // Not embedded

            hazelClientConfig = new ClientConfig();

            String groupName = config.getGroupName();
            if (groupName != null && !groupName.trim().equals("")) {
                groupName = groupName.trim();
                String groupPassword = config.getGroupPassword();
                if (groupPassword == null || groupPassword.trim().equals("")) {
                    throw new Lot49Exception(
                                    "Expected 'groupPassword' in case of not embedded Hazelcast configuration and group specified.");
                }
                groupPassword = groupPassword.trim();
                hazelClientConfig.getGroupConfig().setName(groupName).setPassword(groupPassword);
            }

            List<String> addresses = config.getAddresses();
            if (addresses == null || addresses.isEmpty()) {
                throw new Lot49Exception(
                                "Expected at least one address in 'addresses' in case of not embedded Hazelcast configuration.");

            }
            for (String addr : addresses) {
                hazelClientConfig.getNetworkConfig().addAddress(addr);
            }
            SerializerConfig serializerConfig = new SerializerConfig();
            serializerConfig.setClass(CacheObjectSerializer.class).setTypeClass(CacheObject.class);
            hazelClientConfig.setSerializationConfig(
                            new SerializationConfig().addSerializerConfig(serializerConfig));
        }

    }

    private MapConfig makeLongLivedMapConfig(String name) {
        MapConfig mapConfigLong = new MapConfig();
        mapConfigLong.setBackupCount(0);
        mapConfigLong.setEvictionPolicy(com.hazelcast.config.EvictionPolicy.LFU);
        MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
        maxSizeConfig.setMaxSizePolicy(MaxSizePolicy.USED_HEAP_PERCENTAGE);
        maxSizeConfig.setSize(config.getHeapPercentage());
        mapConfigLong.setMaxSizeConfig(maxSizeConfig);
        mapConfigLong.setName(MAP_NAME_LONG_LIVED);
        return mapConfigLong;
    }

    // hazelConfig.addMapConfig(mapConfigLong);

    public HazelcastServiceConfig getConfig() {
        return config;
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    /**
     * For persisting items short term, e.g.,
     * {@link StatsSvc#nurl(UriInfo, String, String, String, String, String, String, String, String, String, String, long, String, String, String, String, String, String, HttpServletRequest, String, String, String, String, String)
     * NUrl contents}, {@link LostAuctionTask}s, etc.) without any need for backing store.
     */
    public IMap<Object, Object> getShortLivedMap() {
        return shortLivedMap;
    }

    public long getNextId() {
        // TODO -overflow?
        return idGenerator.newId();
    }

    public final static String ID = "HZ_ID";

    public final static String MAP_NAME_LONG_LIVED = "longLivedMap";

    public final static String MAP_NAME_SHORT_LIVED = "shortLivedMap";

    private final HazelcastInstance hazelcastInstance;

    private final IMap<Object, Object> shortLivedMap;

    @Override
    public void init(DbConfig config) throws Lot49Exception {

    }

    private final Map<Class<?>, DaoShortLivedMap<? extends Object>> slMapOfMaps =
                    new HashMap<Class<?>, DaoShortLivedMap<? extends Object>>();

    @Override
    public synchronized <T> DaoShortLivedMap<T> getDaoShortLivedMap(Class<T> type) {
        DaoShortLivedMap<T> map = (DaoShortLivedMap<T>) slMapOfMaps.get(type);
        if (map == null) {
            IMap<String, T> imap = hazelcastInstance.getMap(MAP_NAME_SHORT_LIVED);
            map = new HzDaoShortLivedMap<T>(imap);
            slMapOfMaps.put(type, map);

        }
        return map;
    }

    @Override
    public synchronized <T> DaoShortLivedMap<T> getDaoShortLivedMap() {
        IMap<String, T> imap = hazelcastInstance.getMap(MAP_NAME_SHORT_LIVED);
        DaoShortLivedMap<T> map = new HzDaoShortLivedMap<T>(imap);
        return map;
    }

    @Override
    public <T> DaoCacheLoader<T> getDaoCacheLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> DaoCache<T> getDaoCache(DaoCacheLoader<T> loader, T nullValue, long ttl,
                    long maxItems) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DaoCounters getDaoCounters() {
        return new HzDaoCounters(this);
    }

    @Override
    public DaoMapOfUserAttributes getDaoMapOfUserAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DaoMapOfUserSegments getDaoMapOfUserSegments() {
        throw new UnsupportedOperationException();
    }
}
