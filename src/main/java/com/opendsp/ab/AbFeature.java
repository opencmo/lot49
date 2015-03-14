package com.opendsp.ab;

import java.io.Serializable;

/**
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 * 
 * @author Alex Berg (<a href="mailto:alexberg@gmail.com">alexberg@gmail.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.opendsp.com">OpenDSP</a> 2015. All Rights Reserved.
 *
 */
public class AbFeature implements Serializable {

    private static final long serialVersionUID = 1363368846772513559L;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
