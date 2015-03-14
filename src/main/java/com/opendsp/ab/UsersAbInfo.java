package com.opendsp.ab;

import java.util.HashMap;
import java.util.Map;

import com.enremmeta.rtb.api.apps.AppData;

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
public class UsersAbInfo implements AppData {
    /**
     * 
     */
    private static final long serialVersionUID = -2273909440761004822L;

    private String adId;

    private Map<AbFeature, AbValue> map = new HashMap<AbFeature, AbValue>();
}
