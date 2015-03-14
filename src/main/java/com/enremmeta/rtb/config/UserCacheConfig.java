package com.enremmeta.rtb.config;

/**
 * 
 * 
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */

public class UserCacheConfig implements Config {

    public UserCacheConfig() {
        // TODO Auto-generated constructor stub
    }

    private String segmentDb;

    public String getSegmentDb() {
        return segmentDb;
    }

    public void setSegmentDb(String segmentDb) {
        this.segmentDb = segmentDb;
    }

}
