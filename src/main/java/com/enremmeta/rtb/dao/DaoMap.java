package com.enremmeta.rtb.dao;

import java.util.concurrent.Future;

import com.enremmeta.rtb.Lot49Exception;

/**
 * A basic map.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface DaoMap<T> {
    public void put(String key, T value);

    /**
     * Atomic guarantee
     */
    public T replace(String key, T value) throws Lot49Exception;

    public Future<T> getAsync(String key) throws Lot49Exception;

    public void putAsync(String key, T value) throws Lot49Exception;

    public void putAsync(String key, T value, Long milliseconds) throws Lot49Exception;

    public T get(String key) throws Lot49Exception;

    public T remove(String key) throws Lot49Exception;

    Boolean isExpired(String key);
}
