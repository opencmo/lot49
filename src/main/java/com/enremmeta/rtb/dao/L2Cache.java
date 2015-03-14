package com.enremmeta.rtb.dao;

import java.util.concurrent.Future;

import com.enremmeta.rtb.LogUtils;

/**
 * @deprecated
 * 
 *             Level2 Cache -- something further away.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 *
 */
public interface L2Cache<T> extends Runnable {

    Future<T> get(String key);

    /**
     * Disconnect and reconnect.
     */
    public void refreshPool();

    public void close();

    @Override
    default void run() {
        try {
            refreshPool();
        } catch (final Throwable t) {
            LogUtils.error(t);
        }
    }

}
