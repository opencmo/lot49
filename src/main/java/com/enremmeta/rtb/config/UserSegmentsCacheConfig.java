package com.enremmeta.rtb.config;

import com.enremmeta.rtb.dao.DaoMapOfUserSegments;

/**
 * Config for {@link DaoMapOfUserSegments}
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 * 
 */

public class UserSegmentsCacheConfig implements Config {

    private String segmentsDb;

    public String getSegmentsDb() {
        return segmentsDb;
    }

    public void setSegmentsDb(String segmentsDb) {
        this.segmentsDb = segmentsDb;
    }

}
