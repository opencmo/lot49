package com.enremmeta.rtb.proto.liverail;

import com.enremmeta.rtb.config.Config;
import com.enremmeta.rtb.config.ExchangesConfig;

/**
 * OpenX-specific {@link ExchangesConfig configuration}.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class LiverailConfig implements Config {

    private String seatId;

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

}
