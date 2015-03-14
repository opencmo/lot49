package com.enremmeta.rtb.proto.bidswitch;

import com.enremmeta.rtb.config.Config;

/**
 * Configuration for BidSwitch
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class BidSwitchConfig implements Config {

    public BidSwitchConfig() {
        // TODO Auto-generated constructor stub
    }

    private String seatId;

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }
}
