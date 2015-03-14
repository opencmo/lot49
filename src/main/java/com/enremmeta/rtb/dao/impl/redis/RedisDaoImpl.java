package com.enremmeta.rtb.dao.impl.redis;

import java.util.concurrent.Future;

import com.enremmeta.rtb.Lot49Exception;

public abstract class RedisDaoImpl<T> {

    public RedisDaoImpl(RedisService svc) {
        super();
        this.svc = svc;

    }

    private final RedisService svc;

    public RedisService getRedisService() {
        return svc;
    }

    public T getSafe(Future<T> f) throws Lot49Exception {
        return RedisJsonFuture.getSafe(f, svc);
    }

}
