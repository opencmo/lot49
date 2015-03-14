/**
 * 
 */
package com.enremmeta.rtb.dao;

import java.util.concurrent.Future;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.UserAttributes;

/**
 * DAO interface for {@link UserAttributes}
 * 
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */

public interface DaoMapOfUserAttributes {
    public Future<UserAttributes> getAsync(String uid);

    public void putAsync(String uid, UserAttributes value);

    public void updateImpressionsHistoryAsync(Ad ad, String uid);

}
