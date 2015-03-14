package com.enremmeta.rtb.dao.impl.redis;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.dao.DaoCounters;
import com.lambdaworks.redis.RedisAsyncConnection;

public class RedisDaoCounters extends RedisDaoImpl<Long> implements DaoCounters {

    public RedisDaoCounters(RedisService svc) {
        super(svc);
    }

    @Override
    public long get(String key) throws Lot49Exception {
        Future<String> f = getRedisService().getConnection().get(key);
        String s;
        try {
            s = f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Lot49Exception(e);
        } catch (ExecutionException e) {
            throw new Lot49Exception(e);
        }
        if (s == null) {
            return 0;
        }
        return Long.parseLong(s);
    }

    @Override
    public void set(String key, long val) {
        getRedisService().getConnection().set(key, String.valueOf(val));

    }

    @Override
    public long getAndSet(String key, long val) throws Lot49Exception {
        Future<String> f = getRedisService().getConnection().getset(key, String.valueOf(val));
        String s;
        try {
            s = f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Lot49Exception(e);
        } catch (ExecutionException e) {
            throw new Lot49Exception(e);
        }
        if (s == null) {
            return 0;
        }

        return Long.parseLong(s);

    }

    @SuppressWarnings("unused")
    @Override
    public long addAndGet(String key, long val) throws Lot49Exception {
        RedisAsyncConnection<String, String> rac = getRedisService().getConnection();

        Future<Long> f = rac.incrby(key, val);

        if (f == null) {
            return 0;
        } else {
            return getSafe(f);
        }
    }
}
