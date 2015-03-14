package com.enremmeta.rtb.jersey;

import java.util.Random;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.Utils;

/**
 * Marker interface for any classes that implement Jersey-based REST services here.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public interface JerseySvc {

    static Random RANDOM = new Random(BidderCalendar.getInstance().currentTimeMillis());


    default String getMyCookie(final String cookies) {
        if (cookies != null) {
            final String[] cookieArray = cookies.split("; ");
            for (final String c : cookieArray) {
                final int eqIndexOf = c.indexOf('=');
                if (eqIndexOf > -1) {
                    final String name = c.substring(0, eqIndexOf);
                    if (name.equalsIgnoreCase(USER_ID_COOKIE)) {
                        final String cookieValue = c.substring(eqIndexOf + 1);
                        return cookieValue;
                    }
                } else {
                    LogUtils.error("Malformed cookie: " + c + " in " + cookies);
                }
            }
        }
        return null;
    }



    public static final String AUTH_COOKIE_DU_JOUR = Utils.getId();
    public static final String USER_ID_COOKIE = Bidder.getInstance().getConfig().getUserIdCookie();


}
