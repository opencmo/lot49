package com.enremmeta.rtb.dao;

import java.util.concurrent.Future;

import com.enremmeta.rtb.api.UserSegments;

/**
 * DAO interface for access to {@link UserSegments}
 * 
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */


public interface DaoMapOfUserSegments {
    public Future<UserSegments> getAsync(String uid);
}
