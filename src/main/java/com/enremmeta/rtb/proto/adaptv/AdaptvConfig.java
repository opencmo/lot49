package com.enremmeta.rtb.proto.adaptv;

import com.enremmeta.rtb.config.Config;
import com.enremmeta.rtb.config.ExchangesConfig;

/**
 * AdapTV-specific {@link ExchangesConfig configuration}.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class AdaptvConfig implements Config {

    private String buyerId;

    public String getBuyerId() {
        return buyerId;
    }

    public void setPartnerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public AdaptvConfig() {
        // TODO Auto-generated constructor stub
    }

    private boolean assumeSwfIfVpaid = true;

    public boolean isAssumeSwfIfVpaid() {
        return assumeSwfIfVpaid;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public void setAssumeSwfIfVpaid(boolean assumeSwfIfVpaid) {
        this.assumeSwfIfVpaid = assumeSwfIfVpaid;
    }

    private int defaultMaxDuration = 0;

    public int getDefaultMaxDuration() {
        return defaultMaxDuration;
    }

    public void setDefaultMaxDuration(int defaultMaxDuration) {
        this.defaultMaxDuration = defaultMaxDuration;
    }


}
