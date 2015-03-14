package com.enremmeta.rtb.dao.impl.redis;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.Lot49RuntimeException;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.enremmeta.rtb.config.DbConfig;
import com.enremmeta.rtb.dao.DaoCache;
import com.enremmeta.rtb.dao.DaoCacheLoader;
import com.enremmeta.rtb.dao.DaoCounters;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.dao.DbService;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisException;
import com.lambdaworks.redis.codec.Utf8StringCodec;

/**
 * Redis service.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class RedisService implements DbService, Runnable {

    @Override
    public void run() {
        try {
            refreshPool();
        } catch (final Throwable t) {
            LogUtils.error(t);
        }
    }

    private RedisServiceConfig config;

    private RedisClient client;

    @Override
    public void init(DbConfig config) throws Lot49Exception {
        try {
            this.config = (RedisServiceConfig) config;
        } catch (ClassCastException cce) {
            throw new Lot49Exception("Expected RedisServiceConfig class, got " + config.getClass(),
                            cce);
        }

        client = new RedisClient(this.config.getHost(), this.config.getPort());

        poolSize = this.config.getPoolSize();
        poolTtlMinutes = this.config.getPoolTtlMinutes();
        varzas = new RedisAsyncConnection[poolSize];
        refreshPool();

    }

    private int poolSize;

    private int poolTtlMinutes;

    private int curVarzaPtr = 0;

    /**
     * Completely threadunsafe.
     */
    public int getCurVarzaPtr() {
        return curVarzaPtr;
    }

    // TODO connection should be differnt depending on type.
    private RedisAsyncConnection<String, String>[] varzas;

    // We'll call this object but we'll know for now it's
    // String
    RedisAsyncConnection<String, String> getConnection() {
        curVarzaPtr = ((++curVarzaPtr) % poolSize);
        RedisAsyncConnection<String, String> con = varzas[curVarzaPtr];
        if (con == null) {
            throw new Lot49RuntimeException("Could not get connection to " + config.getHost() + ":"
                            + config.getPort());
        } else {
            return con;
        }
    }

    public void refreshPool() {
        final String redisHost = config.getHost();
        final int redisPort = config.getPort();
        LogUtils.info("Refreshing connection pool for " + redisHost + ":" + redisPort
                        + " (pool TTL: " + poolTtlMinutes + " minutes; poolSize: " + poolSize
                        + " connections; timeout: " + config.getTimeoutMillis()
                        + " milliseconds).");

        int closedCnt = 0;
        int openCnt = 0;
        for (int i = 0; i < poolSize; i++) {
            try {
                final RedisAsyncConnection<String, String> newCon =
                                client.connectAsync(new Utf8StringCodec());
                openCnt++;
                newCon.setTimeout(config.getTimeoutMillis(), TimeUnit.MILLISECONDS);
                final RedisAsyncConnection<String, String> oldCon = varzas[i];
                varzas[i] = newCon;
                if (oldCon != null) {
                    oldCon.close();
                    closedCnt++;
                }
            } catch (RedisException re) {
                Throwable t = re;
                Throwable c = re.getCause();
                if (c != null && c instanceof ConnectException) {
                    t = c;
                }
                LogUtils.error("Error refreshing connection " + i + " to " + redisHost + ":"
                                + redisPort, t);
            }
        }
        LogUtils.info("Closed " + closedCnt + " connections, opened " + openCnt + " connections.");
        Bidder.getInstance().getScheduledExecutor().schedule(this, this.poolTtlMinutes,
                        TimeUnit.MINUTES);
    }

    public void close() {
        for (int i = 0; i < poolSize; i++) {
            final RedisAsyncConnection<String, String> oldCon = varzas[i];
            if (oldCon != null) {
                oldCon.close();
            }
        }
    }

    @Override
    public <T> DaoShortLivedMap<T> getDaoShortLivedMap(Class<T> type) {
        if (type.equals(String.class)) {
            return (DaoShortLivedMap<T>) new RedisStringDaoShortLivedMap(this);
        } else if (type.equals(BidInFlightInfo.class)) {
            return (DaoShortLivedMap<T>) new RedisBidInfoDaoShortLivedMap(this);
        } else if (type.equals(CacheableWebResponse.class)) {
            return (DaoShortLivedMap<T>) new RedisCacheableWebResponseDaoShortLivedMap(this);
        } else {
            throw new UnsupportedOperationException("Cannot deal with " + type.getName());
        }
    }

    RedisServiceConfig getConfig() {
        return this.config;
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
        return new RedisDaoCounters(this);
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
