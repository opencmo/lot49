package com.enremmeta.util;

import java.io.Serializable;

/**
 * Marker interface denoting whether we know this object can easily get converted to JSON.
 * 
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public interface Jsonable extends Serializable {

    default String normalize(String x) {
        if (x == null) {
            return null;
        }
        x = x.trim();
        if (x.length() == 0) {
            return null;
        }
        return x.toLowerCase();
    }

}
