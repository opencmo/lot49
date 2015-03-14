package com.enremmeta.rtb.dao.impl.redis;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.enremmeta.rtb.dao.JsonFuture;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lambdaworks.redis.RedisAsyncConnection;

public class RedisCacheableWebResponseDaoShortLivedMap
                extends RedisDaoShortLivedMap<CacheableWebResponse> {

    public RedisCacheableWebResponseDaoShortLivedMap(RedisService svc) {
        super(svc);

    }

    @Override
    public void putAsync(String key, CacheableWebResponse value) {
        String valStr;
        try {
            valStr = Utils.MAPPER.writeValueAsString(value);
            getRedisService().getConnection().set(key, valStr);
            setExpiry(key);
        } catch (JsonProcessingException e) {
            LogUtils.error(e);
        }

    }

    @Override
    public void putAsync(String key, CacheableWebResponse value, Long milliseconds)
                    throws Lot49Exception {

    }

    @Override
    public CacheableWebResponse replace(String key, CacheableWebResponse value)
                    throws Lot49Exception {
        try {
            final String valStr = Utils.MAPPER.writeValueAsString(value);
            Future<String> f = getRedisService().getConnection().getset(key, valStr);
            String json = getSafe(f);

            CacheableWebResponse retval = null;
            if (json != null) {
                Utils.MAPPER.readValue(json, CacheableWebResponse.class);
            }
            setExpiry(key);
            return retval;
        } catch (IOException e) {
            LogUtils.error(e);
            return null;
        }
    }

    @Override
    public Future<CacheableWebResponse> getAsync(String key) throws Lot49Exception {
        Future<String> f = getRedisService().getConnection().get(key);
        RedisJsonFuture<CacheableWebResponse> retval = new RedisJsonFuture<CacheableWebResponse>(f,
                        CacheableWebResponse.class, getRedisService());
        return retval;
    }

    @Override
    public CacheableWebResponse get(String key) throws Lot49Exception {
        Future<CacheableWebResponse> f = getAsync(key);
        try {
            return f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            LogUtils.error(e);
            return null;
        }
    }

    @Override
    public void put(String key, CacheableWebResponse value) {
        put(key, value);

    }

    // TODO this is not atomic!
    @Override
    public CacheableWebResponse remove(String key) {
        RedisAsyncConnection<String, String> con = this.getRedisService().getConnection();
        Future<String> f = con.get(key);
        Future<CacheableWebResponse> f2 =
                        new JsonFuture<CacheableWebResponse>(f, CacheableWebResponse.class);
        con.del(key);
        try {
            return f2.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            LogUtils.error(e);
            return null;
        }
    }

}
