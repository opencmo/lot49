package com.enremmeta.rtb.caches;

import java.io.Serializable;

import com.enremmeta.util.BidderCalendar;

/**
 * A wrapper for an object to be cached in a cache. In addition to the actual {@link #getObject()
 * payload} it stores information on the last fetch time. The reason for this, instead of using
 * local cache's TTL-based eviction policy, is that we don't want to evict an object; we want to
 * fetch a new copy without stampede, and, in the meantime, keep returning a stale one.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 * 
 *
 */
public class CacheObject<T> implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4456317487538510408L;

    public CacheObject(T value) {
        super();
        this.object = value;
        this.fetchedTime = BidderCalendar.getInstance().currentTimeMillis();
    }

    public CacheObject() {
        super();
        this.fetchedTime = 0;
    }

    private boolean isNull = false;

    public boolean isNull() {
        return this.isNull;
    }

    public void setNull() {
        this.isNull = true;
    }

    public long getFetchedTime() {
        return fetchedTime;
    }

    public void setFetchedTimeNow() {
        this.fetchedTime = BidderCalendar.getInstance().currentTimeMillis();
    }

    public void setFetchedTime(long fetchedTime) {
        this.fetchedTime = fetchedTime;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
        if (object != null) {
            this.isNull = false;
        }
    }

    private long fetchedTime;

    private T object;

}
