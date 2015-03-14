package com.enremmeta.rtb.dao.impl.hazelcast;

import java.util.concurrent.Future;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.hazelcast.core.IMap;

public class HzDaoShortLivedMap<T> implements DaoShortLivedMap<T> {

    private final IMap<String, T> imap;

    public HzDaoShortLivedMap(IMap<String, T> imap) {
        super();
        this.imap = imap;
    }

    @Override
    public void put(String key, T value) {
        imap.put(key, value);
    }

    @Override
    public T replace(String key, T value) {
        return imap.replace(key, value);
    }

    @Override
    public Future<T> getAsync(String key) {
        return imap.getAsync(key);
    }

    @Override
    public void putAsync(String key, T value) {
        imap.putAsync(key, value);
    }

    @Override
    public void putAsync(String key, T value, Long milliseconds) throws Lot49Exception {

    }

    @Override
    public T get(String key) {
        return imap.get(key);
    }

    @Override
    public T remove(String key) {
        return imap.remove(key);
    }

    @Override
    public Boolean isExpired(String key) {
        return null;
    }
}
