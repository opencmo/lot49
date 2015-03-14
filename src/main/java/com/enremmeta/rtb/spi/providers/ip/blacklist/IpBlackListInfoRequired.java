package com.enremmeta.rtb.spi.providers.ip.blacklist;

import com.enremmeta.rtb.spi.providers.ProviderInfoRequired;

/**
 * Created by amiroshn on 4/26/2016.
 */
public class IpBlackListInfoRequired implements ProviderInfoRequired {

    private boolean found;


    public IpBlackListInfoRequired() {
        super();
    }

    public IpBlackListInfoRequired(boolean found) {
        super();
        this.found = found;
    }


    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
