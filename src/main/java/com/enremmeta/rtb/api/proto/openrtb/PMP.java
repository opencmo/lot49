package com.enremmeta.rtb.api.proto.openrtb;

import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;

/**
 * OpenRTB PMP (Private Marketplace) object.
 * 
 * @see <a href="http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-3.pdf">OpenRTB
 *      2.3 (section 3.2.17)</a>
 * 
 * @see Deal
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class PMP implements RtbBean {

    public PMP() {
        // TODO Auto-generated constructor stub
    }

    private int private_auction = -1;

    private List<Deal> deals;

    private Map ext;

    public int getPrivate_auction() {
        return private_auction;
    }

    public void setPrivate_auction(int private_auction) {
        this.private_auction = private_auction;
    }

    public List<Deal> getDeals() {
        return deals;
    }

    public void setDeals(List<Deal> deals) {
        this.deals = deals;
    }

    public Map getExt() {
        return ext;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }
}
