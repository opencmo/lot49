package com.enremmeta.rtb.spi.providers.ip.blacklist;

import com.enremmeta.rtb.spi.providers.ProviderInfoReceived;

/**
 * Created by amiroshn on 4/22/2016.
 */
public class IpBlackListInfoReceived implements ProviderInfoReceived {

    private static final long serialVersionUID = -7936656146105408113L;

    private boolean found;

    public IpBlackListInfoReceived(boolean found) {
        this.found = found;
    }


    public boolean isFound() {
        return found;
    }

    @Override
    public String toString() {
        return "IpBlackListInfoReceived{ found=" + found + '}';
    }
}
