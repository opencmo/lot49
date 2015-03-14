package com.enremmeta.rtb.dao;

import com.enremmeta.rtb.Lot49Exception;

/**
 * Counters - set of atomic operations on some named {@link Long longs}.
 * 
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface DaoCounters {

    long get(String key) throws Lot49Exception;

    void set(String key, long val) throws Lot49Exception;

    long getAndSet(String key, long val) throws Lot49Exception;

    long addAndGet(String key, long val) throws Lot49Exception;
}
