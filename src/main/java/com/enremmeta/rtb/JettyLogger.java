package com.enremmeta.rtb;

import org.eclipse.jetty.util.log.Logger;

/**
 * An attempt to override Jetty's logging to wherever it is.
 * 
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public final class JettyLogger implements Logger {

    public JettyLogger() {
        super();
    }

    private final org.apache.logging.log4j.Logger delegate = LogUtils.jettyLogger;

    @Override
    public String getName() {
        return LogUtils.LOG_JETTY;
    }

    @Override
    public void warn(String msg, Object... args) {
        delegate.warn(msg, args);

    }

    @Override
    public void warn(Throwable thrown) {
        delegate.warn(thrown);

    }

    @Override
    public void warn(String msg, Throwable thrown) {
        delegate.warn(msg, thrown);

    }

    @Override
    public void info(String msg, Object... args) {
        delegate.info(msg, args);

    }

    @Override
    public void info(Throwable thrown) {
        delegate.info(thrown);

    }

    @Override
    public void info(String msg, Throwable thrown) {
        delegate.info(msg, thrown);

    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void debug(String msg, Object... args) {
        delegate.debug(msg, args);

    }

    @Override
    public void debug(String msg, long value) {
        delegate.debug(msg, value);

    }

    @Override
    public void debug(Throwable thrown) {
        delegate.debug(thrown);

    }

    @Override
    public void debug(String msg, Throwable thrown) {
        delegate.debug(msg, thrown);

    }

    @Override
    public Logger getLogger(String name) {
        return this;
    }

    @Override
    public void ignore(Throwable ignored) {

    }
}
