package com.enremmeta.rtb.dao;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.config.DbConfig;

/**
 * Service providing one or more of the following:
 * 
 * <ul>
 * <li>{@link DaoCounters}</li>
 * <li>{@link DaoMap}</li>
 * <li>{@link DaoShortLivedMap}</li>
 * <li>{@link DaoMapOfUserAttributes}</li>
 * <li>{@link DaoMapOfUserSegments}</li>
 * 
 * <li></li>
 * </ul>
 * 
 * If the service does not provide any of the above services, the appropriate getter for it
 * <b>MUST</b> throw {@link UnsupportedOperationException}.
 * 
 * @see DbConfig
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface DbService {

    void init(DbConfig config) throws Lot49Exception;

    <T> DaoShortLivedMap<T> getDaoShortLivedMap(Class<T> type);

    <T> DaoShortLivedMap<T> getDaoShortLivedMap();

    <T> DaoCacheLoader<T> getDaoCacheLoader();

    <T> DaoCache<T> getDaoCache(DaoCacheLoader<T> loader, T nullValue, long ttl, long maxItems);

    DaoCounters getDaoCounters();

    DaoMapOfUserAttributes getDaoMapOfUserAttributes();

    DaoMapOfUserSegments getDaoMapOfUserSegments();
}
