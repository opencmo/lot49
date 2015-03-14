package com.enremmeta.rtb.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User segments data
 * 
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */

public class UserSegments {
    private Map<String, Map<String, String>> userSegmentsMap;

    public UserSegments(Map<String, Map<String, String>> userSegmentsMap) {
        this.userSegmentsMap = userSegmentsMap;
    }

    public UserSegments() {
        this.userSegmentsMap = new HashMap<String, Map<String, String>>();
    }

    public Map<String, Map<String, String>> getUserSegmentsMap() {
        return userSegmentsMap;
    }

    public void setUserSegmentsMap(Map<String, Map<String, String>> userSegmentsMap) {
        this.userSegmentsMap = userSegmentsMap;
    }

    public Set<String> getSegmentsSet() {
        return userSegmentsMap.keySet();
    }

}
