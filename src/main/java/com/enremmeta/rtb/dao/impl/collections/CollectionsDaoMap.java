package com.enremmeta.rtb.dao.impl.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.caches.KnownFuture;
import com.enremmeta.rtb.dao.DaoMap;

/**
 * 
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class CollectionsDaoMap<T> implements DaoMap<T> {

    public CollectionsDaoMap() {
        super();
    }

    private Map<String, T> map = new HashMap<String, T>();

    @Override
    public void put(String key, T value) {
        map.put(key, value);
    }

    @Override
    public T get(String key) {
        return map.get(key);
    }

    @Override
    public Future<T> getAsync(String key) {
        T val = map.get(key);
        KnownFuture<T> f = new KnownFuture<T>(val);
        return f;
    }

    // TODO true async with client
    @Override
    public void putAsync(String key, T value) {
        put(key, value);
    }

    @Override
    public void putAsync(String key, T value, Long milliseconds) throws Lot49Exception {

    }

    @Override
    public T remove(String k) {
        return map.remove(k);
    }

    @Override
    public Boolean isExpired(String key) {
        return null;
    }

    @Override
    public T replace(String k, T value) {
        return map.replace(k, value);
    }

}
