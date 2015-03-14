package com.enremmeta.rtb.dao.impl.redis;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.lambdaworks.redis.RedisAsyncConnection;

public abstract class RedisDaoShortLivedMap<T> extends RedisDaoImpl<String>
                implements DaoShortLivedMap<T> {
    public RedisDaoShortLivedMap(RedisService svc) {
        super(svc);
        this.ttl = getRedisService().getConfig().getShortLivedMapTtlSeconds();
    }

    private final long ttl;

    protected void setExpiry(String key) {
        LogUtils.debug(getClass().getName() + ":  setExpiry(" + key + ", " + this.ttl + ")");
        RedisService rs = this.getRedisService();
        RedisAsyncConnection<String, String> con = rs.getConnection();
        con.expire(key, this.ttl);
    }

    protected void setExpiry(String key, long milliseconds) {
        LogUtils.debug(getClass().getName() + ":  setExpiry(" + key + ", " + milliseconds + ")");
        RedisService rs = this.getRedisService();
        RedisAsyncConnection<String, String> con = rs.getConnection();
        con.pexpire(key, milliseconds);
    }

    public Boolean isExpired(String key) {
        RedisService rs = this.getRedisService();
        RedisAsyncConnection<String, String> con = rs.getConnection();
        Future<Long> pttl = con.pttl(key);
        try {
            Long timeLeft = pttl.get();
            return timeLeft <= 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }
}
