package com.enremmeta.rtb.dao.impl.redis;

import com.enremmeta.rtb.config.DbConfig;
import com.lambdaworks.redis.RedisConnection;

public class RedisServiceConfig extends DbConfig {

    public RedisServiceConfig() {
        // TODO Auto-generated constructor stub
    }

    private String host;
    private int port;
    private long fcTtlMilliseconds = 100;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    private int poolTtlMinutes = 5;

    /**
     * How often to refresh the pool (close all connections and open new ones), in minutes.
     */
    public int getPoolTtlMinutes() {
        return poolTtlMinutes;
    }

    public void setPoolTtlMinutes(int poolTtlMinutes) {
        this.poolTtlMinutes = poolTtlMinutes;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static final int DEFAULT_POOL_SIZE = 128;

    private int poolSize = DEFAULT_POOL_SIZE;

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    private long timeoutMillis = 1000;

    /**
     * Timeout (see {@link RedisConnection#setTimeout(long, java.util.concurrent.TimeUnit)}).
     */
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public long getFcTtlMilliseconds() {
        return fcTtlMilliseconds;
    }

    public void setFcTtlMilliseconds(long fcTtlMilliseconds) {
        this.fcTtlMilliseconds = fcTtlMilliseconds;
    }
}
