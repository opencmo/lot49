package com.enremmeta.rtb.dao.impl.collections;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.dao.DaoCache;
import com.enremmeta.rtb.dao.DaoCacheLoader;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

/**
 * {@link DaoCache} implementation based on Guava.
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class CollectionsCache<T> implements DaoCache {

    private final LoadingCache<String, T> cache;

    private final DaoCacheLoader<T> loader;

    public CollectionsCache(final DaoCacheLoader<T> loader, final T nullValue, long ttl,
                    long maxItems) {
        super();
        this.loader = loader;
        cache = CacheBuilder.newBuilder().expireAfterWrite(ttl, TimeUnit.SECONDS)
                        .refreshAfterWrite(ttl, TimeUnit.SECONDS).maximumSize(maxItems)
                        .build(new CacheLoader<String, T>() {

                            @Override
                            public T load(String key) throws Exception {
                                final long loadBegin =
                                                BidderCalendar.getInstance().currentTimeMillis();
                                T retval = loader.load(key).get();
                                if (retval == null) {
                                    retval = nullValue;
                                }
                                final long loadTime =
                                                BidderCalendar.getInstance().currentTimeMillis()
                                                                - loadBegin;
                                LogUtils.trace("Time for " + loader + " to load " + key + " is "
                                                + loadTime + " ms, got: " + retval);
                                return retval;
                            }

                            // https://code.google.com/p/guava-libraries/wiki/CachesExplained#Size-based_Eviction
                            @Override
                            public ListenableFuture<T> reload(String key, T oldValue)
                                            throws Exception {
                                LogUtils.trace("In " + this + ".reload(" + key + ")");
                                ListenableFutureTask<T> task =
                                                ListenableFutureTask.create(new Callable<T>() {
                                                    @Override
                                                    public T call() throws Exception {
                                                        final Future<T> f = loader.load(key);
                                                        T t = f.get();
                                                        return t;
                                                    }
                                                });
                                ServiceRunner.getInstance().getExecutor().execute(task);
                                return task;
                            }

                        });
        this.ttl = ttl;
    }

    private final long ttl;

    @Override
    public T get(String key) {
        try {

            T retval = cache.get(key);
            LogUtils.trace("Collections cache for " + key + ": " + retval);
            return retval;
        } catch (ExecutionException e) {
            LogUtils.error(e);
            return null;
        }
    }

    @Override
    public Future<T> getAsync(String key) {
        // try {
        Future<T> retval = loader.load(key);
        LogUtils.trace("Collections cache (ASYNC) for " + key + ": " + retval);
        return retval;
        // } catch (ExecutionException e) {
        // LogUtils.error(e);
        // return KnownFuture.KNOWN_NULL_FUTURE;
        // }
    }

    @Override
    public long getTtl() {
        return this.ttl;
    }

}
