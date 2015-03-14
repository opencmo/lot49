package com.enremmeta.rtb;

import com.enremmeta.rtb.caches.AdCache;

/**
 * Shutdown hook.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public final class ShutdownHook extends Thread {

    public ShutdownHook() {
        super();
        setName("Lot49ShutdownHook");
    }

    @Override
    public void run() {
        try {
            AdCache adCache = Bidder.getInstance().getAdCache();
        } finally {
            final String msg = "Lot49 has left the building.";
            System.out.println(msg);
            LogUtils.info(msg);
        }
    }

}
