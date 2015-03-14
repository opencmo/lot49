package com.enremmeta.rtb.config;

import java.util.Map;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;

public class DbConfigs implements Config {
    public DbConfig findDbConfig(String name) throws Lot49Exception {
        HazelcastServiceConfig hzConfig = hazelcasts.get(name);
        RedisServiceConfig redisConfig = redises.get(name);
        DynamoDBServiceConfig dynConfig = dynamodbs.get(name);
        AerospikeDBServiceConfig aeroConfig = aerospikes.get(name);

        if (hzConfig != null) {
            if (redisConfig != null || dynConfig != null || aeroConfig != null) {
                throw new Lot49Exception("Duplicate DB " + name);
            }
            return hzConfig;
        } else if (redisConfig != null) {
            if (dynConfig != null || aeroConfig != null) {
                throw new Lot49Exception("Duplicate DB " + name);
            }
            return redisConfig;
        } else if (aeroConfig != null) {
            if (dynConfig != null) {
                throw new Lot49Exception("Duplicate DB " + name);
            }
            return aeroConfig;
        }
        return dynConfig;
    }

    private Map<String, HazelcastServiceConfig> hazelcasts;
    private Map<String, RedisServiceConfig> redises;
    private Map<String, DynamoDBServiceConfig> dynamodbs;
    private Map<String, MockDbServiceConfig> mockdbs;
    private Map<String, AerospikeDBServiceConfig> aerospikes;

    public Map<String, MockDbServiceConfig> getMockdbs() {
        return mockdbs;
    }

    public void setMockdbs(Map<String, MockDbServiceConfig> mockdbs) {
        this.mockdbs = mockdbs;
    }

    public Map<String, HazelcastServiceConfig> getHazelcasts() {
        return hazelcasts;
    }

    public void setHazelcasts(Map<String, HazelcastServiceConfig> hazelcasts) {
        this.hazelcasts = hazelcasts;
    }

    public Map<String, RedisServiceConfig> getRedises() {
        return redises;
    }

    public void setRedises(Map<String, RedisServiceConfig> redises) {
        this.redises = redises;
    }

    public Map<String, DynamoDBServiceConfig> getDynamodbs() {
        return dynamodbs;
    }

    public void setDynamos(Map<String, DynamoDBServiceConfig> dynamodbs) {
        this.dynamodbs = dynamodbs;
    }

    /**
     * @return the aerospikes
     */
    public Map<String, AerospikeDBServiceConfig> getAerospikes() {
        return aerospikes;
    }

    /**
     * @param aerospikes
     *            the aerospikes to set
     */
    public void setAerospikes(Map<String, AerospikeDBServiceConfig> aerospikes) {
        this.aerospikes = aerospikes;
    }

}
