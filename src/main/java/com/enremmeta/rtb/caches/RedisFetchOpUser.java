package com.enremmeta.rtb.caches;

import java.util.Set;
import java.util.concurrent.Future;

import com.lambdaworks.redis.RedisAsyncConnection;

public class RedisFetchOpUser implements RedisFetchOp<Set<String>> {

    @Override
    public Future<Set<String>> fetch(RedisAsyncConnection<String, String> con, String key) {
        return con.smembers(key);
    }

}
