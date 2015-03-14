/**
 * 
 */
package com.enremmeta.rtb.config;

import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;

/**
 * Config for {@link DaoMapOfUserAttributes}
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 * 
 */

public class UserAttributesCacheConfig implements Config {
    private String mapDb;

    public String getMapDb() {
        return mapDb;
    }

    public void setMapDb(String mapDb) {
        this.mapDb = mapDb;
    }
}
