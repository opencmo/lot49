package com.enremmeta.rtb.dao;

import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

import com.enremmeta.rtb.caches.CacheObject;

/**
 * @deprecated
 * 
 * 
 *             Level1 cache - something in memory.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface L1Cache<T> {

    Future<CacheObject<T>> get(String key);

    long getTtl();

    void put(String key, CacheObject<T> value);

    Lock lock(String key);
}
