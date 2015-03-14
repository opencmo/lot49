package com.enremmeta.rtb.proto.adx;

import java.util.ArrayList;
import java.util.List;

/**
 * AdX-specific targeting
 * 
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class AdXTargeting {
    private List<Long> adGroupIds = new ArrayList<Long>(1);

    public List<Long> getAdGroupIds() {
        return adGroupIds;
    }

    public void setAdGroupId(List<Long> adGroupIds) {
        this.adGroupIds = adGroupIds;
    }
}
