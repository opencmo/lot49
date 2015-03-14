package com.enremmeta.rtb.caches;

import java.util.concurrent.Future;

import com.lambdaworks.redis.RedisAsyncConnection;

/**
 * Functional interface defining the Redis operation to be used (because we don't necessarily want
 * <tt>GET</tt>, we could want <tt>SMEMBERS</tt>).
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface RedisFetchOp<T> {
    Future<T> fetch(RedisAsyncConnection<String, String> con, String key);
}
