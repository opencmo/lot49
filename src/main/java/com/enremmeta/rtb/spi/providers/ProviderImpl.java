package com.enremmeta.rtb.spi.providers;

import java.util.Map;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.util.ServiceRunner;

/**
 * 
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public abstract class ProviderImpl implements Provider {

    private final ServiceRunner runner;

    public ProviderImpl(ServiceRunner runner, Map configMap) {
        super();
        this.runner = runner;
    }

    public void info(Object msg) {
        if (runner == null) {
            System.out.println(msg);
        } else {
            LogUtils.info(msg);
        }
    }

    public void error(Object msg, Throwable t) {
        if (runner == null) {
            System.out.println(msg);
            LogUtils.error(t);
        } else {
            LogUtils.error(msg, t);
        }
    }
}
