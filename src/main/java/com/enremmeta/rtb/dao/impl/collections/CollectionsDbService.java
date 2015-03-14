package com.enremmeta.rtb.dao.impl.collections;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.config.DbConfig;
import com.enremmeta.rtb.dao.DaoCache;
import com.enremmeta.rtb.dao.DaoCacheLoader;
import com.enremmeta.rtb.dao.DaoCounters;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.dao.DbService;

public class CollectionsDbService implements DbService {

    public CollectionsDbService() {
        super();

    }

    // private final CollectionsShortLivedMap<T> shortLivedMap = new
    // CollectionsShortLivedMap<T>();

    @Override
    public synchronized <T> DaoShortLivedMap<T> getDaoShortLivedMap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> DaoShortLivedMap<T> getDaoShortLivedMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init(DbConfig config) throws Lot49Exception {

    }

    @Override
    public <T> DaoCacheLoader<T> getDaoCacheLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> DaoCache<T> getDaoCache(final DaoCacheLoader<T> loader, T nullValue, long ttl,
                    long maxItems) {
        return new CollectionsCache(loader, nullValue, ttl, maxItems);
    }

    @Override
    public DaoCounters getDaoCounters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DaoMapOfUserAttributes getDaoMapOfUserAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DaoMapOfUserSegments getDaoMapOfUserSegments() {
        throw new UnsupportedOperationException();
    }
}
