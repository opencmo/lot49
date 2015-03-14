package com.enremmeta.rtb.dao.impl.aerospike;

import com.aerospike.client.async.AsyncClient;
import com.aerospike.client.async.AsyncClientPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.config.AerospikeDBServiceConfig;
import com.enremmeta.rtb.config.DbConfig;
import com.enremmeta.rtb.dao.DaoCache;
import com.enremmeta.rtb.dao.DaoCacheLoader;
import com.enremmeta.rtb.dao.DaoCounters;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.dao.DbService;

public class AerospikeDBService implements DbService {

    private AsyncClient client;
    private AerospikeDBServiceConfig config;
    private AsyncClientPolicy policy;

    @Override
    public void init(DbConfig config) throws Lot49Exception {
        this.config = (AerospikeDBServiceConfig) config;
        WritePolicy writePolicy = new WritePolicy();
        writePolicy.expiration = 300;

        Policy readPolicy = new Policy();

        policy = new AsyncClientPolicy();
        policy.readPolicyDefault = readPolicy;
        policy.writePolicyDefault = writePolicy;
        policy.asyncMaxCommands = 300;
        policy.asyncSelectorThreads = 1;
        policy.asyncSelectorTimeout = 10;
        policy.failIfNotConnected = true;
        client = new AsyncClient(policy, this.config.getHost(), this.config.getPort());
    }

    @Override
    public <T> DaoShortLivedMap<T> getDaoShortLivedMap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> DaoShortLivedMap<T> getDaoShortLivedMap() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    private AerospikeDaoMapOfUserAttributes daoMapOfUserAttributes = null;

    @Override
    public synchronized DaoMapOfUserAttributes getDaoMapOfUserAttributes() {
        if (daoMapOfUserAttributes == null) {
            daoMapOfUserAttributes = new AerospikeDaoMapOfUserAttributes(this);
        }
        return daoMapOfUserAttributes;
    }

    private AerospikeDaoMapOfUserSegments daoMapOfUserSegments = null;

    @Override
    public synchronized DaoMapOfUserSegments getDaoMapOfUserSegments() {
        if (daoMapOfUserSegments == null) {
            daoMapOfUserSegments = new AerospikeDaoMapOfUserSegments(this);
        }
        return daoMapOfUserSegments;
    }

    public AsyncClient getClient() {
        return client;
    }

    public void setClient(AsyncClient client) {
        this.client = client;
    }

    /**
     * @return the policy
     */
    public AsyncClientPolicy getPolicy() {
        return policy;
    }

    /**
     * @param policy
     *            the policy to set
     */
    public void setPolicy(AsyncClientPolicy policy) {
        this.policy = policy;
    }

    /**
     * @return the config
     */
    public AerospikeDBServiceConfig getConfig() {
        return config;
    }

    /**
     * @param config
     *            the config to set
     */
    public void setConfig(AerospikeDBServiceConfig config) {
        this.config = config;
    }

}
