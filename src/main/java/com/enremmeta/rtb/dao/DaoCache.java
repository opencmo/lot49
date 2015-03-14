package com.enremmeta.rtb.dao;

import java.util.concurrent.Future;

/**
 * Cache - objects get evicted after a {@link #getTtl() ttl} and are fetched asynchronously}.
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface DaoCache<T> {
    T get(String value);

    Future<T> getAsync(String value);

    long getTtl();
}
