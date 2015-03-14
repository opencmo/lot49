package com.enremmeta.rtb.dao.impl.redis;

import java.util.concurrent.Future;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.lambdaworks.redis.RedisAsyncConnection;

public class RedisStringDaoShortLivedMap extends RedisDaoShortLivedMap<String> {

    public RedisStringDaoShortLivedMap(RedisService svc) {
        super(svc);

    }

    @Override
    public void putAsync(String key, String value) {
        LogUtils.debug("putAsync(" + key + "," + value + ")");
        getRedisService().getConnection().set(key, value);
        setExpiry(key);
    }

    @Override
    public void putAsync(String key, String value, Long milliseconds) {
        LogUtils.debug("putAsync(" + key + "," + value + ")");
        getRedisService().getConnection().set(key, value);
        setExpiry(key, milliseconds);
    }

    @Override
    public String replace(String key, String value) throws Lot49Exception {
        Future<String> f = getRedisService().getConnection().getset(key, value);
        setExpiry(key);
        return getSafe(f);

    }

    @Override
    public Future<String> getAsync(String key) {
        return getRedisService().getConnection().get(key);
    }

    @Override
    public String get(String key) throws Lot49Exception {
        Future<String> f = getAsync(key);

        return getSafe(f);

    }

    @Override
    public void put(String key, String value) {
        putAsync(key, value);
    }

    // TODO this is not atomic!
    @Override
    public String remove(String key) throws Lot49Exception {
        RedisAsyncConnection<String, String> con = this.getRedisService().getConnection();
        Future<String> f = con.get(key);
        con.del(key);

        return getSafe(f);

    }
}
