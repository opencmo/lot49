package com.enremmeta.rtb.dao.impl.redis;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.caches.RedisFetchOp;
import com.enremmeta.rtb.dao.L2Cache;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.codec.Utf8StringCodec;

/**
 * Redis implementation.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class RedisL2Cache<T> implements L2Cache<T> {
    private final RedisServiceConfig redisConfig;
    // private final RedisCodec<String, ConnectionType> codec;
    final RedisFetchOp<T> fetchOp;

    public RedisL2Cache(final RedisServiceConfig redisConfig, final RedisFetchOp<T> fetchOp) {
        super();
        this.fetchOp = fetchOp;
        this.redisConfig = redisConfig;
        // this.codec = codec;
        poolSize = redisConfig.getPoolSize();
        poolTtlMinutes = redisConfig.getPoolTtlMinutes();
        varzas = new RedisAsyncConnection[poolSize];

        refreshPool();
    }

    private final int poolSize;

    private final int poolTtlMinutes;

    private int curVarzaPtr = 0;

    /**
     * Completely threadunsafe.
     */
    public int getCurVarzaPtr() {
        return curVarzaPtr;
    }

    private final RedisAsyncConnection<String, String>[] varzas;

    private RedisAsyncConnection<String, String> getConnection() {
        curVarzaPtr = ((++curVarzaPtr) % poolSize);
        return varzas[curVarzaPtr];
    }

    @Override
    public Future<T> get(String key) {
        final RedisAsyncConnection<String, String> con = getConnection();
        final Future<T> retval = fetchOp.fetch(con, key);
        return retval;
    }

    @Override
    public void refreshPool() {
        final String redisHost = redisConfig.getHost();
        final int redisPort = redisConfig.getPort();
        LogUtils.info("Refreshing connection pool for " + redisHost + ":" + redisPort
                        + " (pool TTL: " + poolTtlMinutes + " minutes; poolSize: " + poolSize
                        + " connections; timeout: " + redisConfig.getTimeoutMillis()
                        + " milliseconds).");
        RedisClient client = new RedisClient(redisHost, redisPort);
        int closedCnt = 0;
        for (int i = 0; i < poolSize; i++) {
            final RedisAsyncConnection<String, String> newCon =
                            client.connectAsync(new Utf8StringCodec());
            newCon.setTimeout(redisConfig.getTimeoutMillis(), TimeUnit.MILLISECONDS);
            final RedisAsyncConnection<String, String> oldCon = varzas[i];
            varzas[i] = newCon;
            if (oldCon != null) {
                oldCon.close();
                closedCnt++;
            }
        }
        LogUtils.info("Closed " + closedCnt + " connections.");
        Bidder.getInstance().getScheduledExecutor().schedule(this, this.poolTtlMinutes,
                        TimeUnit.MINUTES);
    }

    @Override
    public void close() {
        for (int i = 0; i < poolSize; i++) {
            final RedisAsyncConnection<String, String> oldCon = varzas[i];
            if (oldCon != null) {
                oldCon.close();
            }
        }
    }
}
