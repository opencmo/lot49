package com.enremmeta.rtb.caches;

/**
 * @deprecated
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class CacheStats {

    public void reset() {
        l1Misses = 0;
        l1Hits = 0;
        l1StaleReturns = 0;
        l1NullReturns = 0;
        l2TripsForMiss = 0;
        l2TripsForStale = 0;
        l2Fetched = 0;
        cacheRequests = 0;
    }

    private int cacheRequests;
    private int l1Misses;
    private int l1Hits;
    private int l1StaleReturns;
    private int l1NullReturns;
    private int l2TripsForMiss;
    private int l2TripsForStale;
    private int l2Fetched;

    public int getCacheRequests() {
        return cacheRequests;
    }

    public void incrCacheRequests() {
        cacheRequests++;
    }

    public int getL1Misses() {
        return l1Misses;
    }

    public void incrL2Fetched() {
        this.l2Fetched++;
    }

    public int getL2Fetched() {
        return this.l2Fetched;
    }

    public void incrL1Misses() {
        this.l1Misses++;
    }

    public int getL1Hits() {
        return l1Hits;
    }

    public void incrL1Hits() {
        this.l1Hits++;
    }

    public int getL1StaleReturns() {
        return l1StaleReturns;
    }

    public void incrL1StaleReturns() {
        this.l1StaleReturns++;
    }

    public int getL1NullReturns() {
        return l1NullReturns;
    }

    public void incrL1NullReturns() {
        this.l1NullReturns++;
    }

    public int getL2TripsForMiss() {
        return l2TripsForMiss;
    }

    public void incrL2TripsForMiss() {
        this.l2TripsForMiss++;
    }

    public int getL2TripsForStale() {
        return l2TripsForStale;
    }

    public void incrL2TripsForStale() {
        this.l2TripsForStale++;
    }
}
