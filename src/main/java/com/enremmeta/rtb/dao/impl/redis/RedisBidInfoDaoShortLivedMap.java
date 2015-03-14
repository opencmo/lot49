package com.enremmeta.rtb.dao.impl.redis;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.dao.JsonFuture;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lambdaworks.redis.RedisAsyncConnection;

public class RedisBidInfoDaoShortLivedMap extends RedisDaoShortLivedMap<BidInFlightInfo> {

    public RedisBidInfoDaoShortLivedMap(RedisService svc) {
        super(svc);
    }

    @Override
    public void putAsync(String key, BidInFlightInfo value) {
        long t0 = BidderCalendar.getInstance().currentTimeMillis();
        String valStr;
        try {
            valStr = Utils.MAPPER.writeValueAsString(value);
            LogUtils.debug("Dao: " + getClass().getName() + ":  putAsync(" + key + ", " + valStr
                            + ")");
            getRedisService().getConnection().set(key, valStr);
            setExpiry(key);
        } catch (JsonProcessingException e) {
            LogUtils.error(e);
        }
        LogUtils.trace("Time to putAsync(" + key + "): " + (System.currentTimeMillis() - t0));

    }

    @Override
    public void putAsync(String key, BidInFlightInfo value, Long milliseconds)
                    throws Lot49Exception {

    }

    @Override
    public BidInFlightInfo replace(String key, BidInFlightInfo value) {
        long t0 = BidderCalendar.getInstance().currentTimeMillis();
        try {
            final String valStr = Utils.MAPPER.writeValueAsString(value);
            final Future<String> f = getRedisService().getConnection().getset(key, valStr);

            final String json = f.get();
            LogUtils.debug("Dao: " + getClass().getName() + ":  replace(" + key + ", " + valStr
                            + "): " + json);
            BidInFlightInfo retval = null;
            if (json != null) {
                retval = Utils.MAPPER.readValue(json, BidInFlightInfo.class);
            }
            setExpiry(key);
            return retval;
        } catch (InterruptedException e) {
            LogUtils.error(e);
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            LogUtils.error(e);
            return null;
        } catch (JsonProcessingException e) {
            LogUtils.error(e);
            return null;
        } catch (IOException e) {
            LogUtils.error(e);
            return null;
        } finally {
            LogUtils.trace("Time to replace(" + key + "): " + (System.currentTimeMillis() - t0));
        }
    }

    @Override
    public Future<BidInFlightInfo> getAsync(String key) {
        Future<String> f = getRedisService().getConnection().get(key);
        JsonFuture<BidInFlightInfo> retval =
                        new JsonFuture<BidInFlightInfo>(f, BidInFlightInfo.class);
        return retval;
    }

    @Override
    public BidInFlightInfo get(String key) {
        Future<BidInFlightInfo> f = getAsync(key);
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
    public void put(String key, BidInFlightInfo value) {
        long t0 = BidderCalendar.getInstance().currentTimeMillis();
        putAsync(key, value);
        LogUtils.trace("Time to put(" + key + "): " + (System.currentTimeMillis() - t0));

    }

    // TODO this is not atomic!
    @Override
    public BidInFlightInfo remove(String key) throws Lot49Exception {
        RedisAsyncConnection<String, String> con = this.getRedisService().getConnection();
        Future<String> f = con.get(key);
        Future<BidInFlightInfo> f2 = new RedisJsonFuture<BidInFlightInfo>(f, BidInFlightInfo.class,
                        getRedisService());
        con.del(key);
        try {
            return f2.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Lot49Exception(e);
        } catch (ExecutionException e) {
            throw new Lot49Exception(e.getCause());
        }
    }

}
