package com.enremmeta.util;

import java.nio.charset.Charset;
import java.util.concurrent.Future;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;

/**
 * Delimited layout.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 *
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014-2015. All Rights
 *         Reserved. 
 *
 */
@Plugin(name = "DelimLayout", category = "Core", elementType = "layout", printObject = true)
public final class DelimLayout extends AbstractStringLayout implements EnremmetaConstants {

    public DelimLayout() {
        this(LOG_DELIMITER, LOG_LIST_DELIMITER, LOG_NULL_CHAR, false, 0);
    }

    /**
     * What to write instead of null (e.g., in some cases it's convenient to log "-", just like
     * NGINX does).
     */
    public String getNullChar() {
        return this.nullChar;
    }

    public DelimLayout(char delimiter, char secondaryDelimiter, String nullChar) {
        this(delimiter, secondaryDelimiter, nullChar, false, 0);
    }

    /**
     *
     * @param delimiter
     *            pretty obvious
     * @param secondaryDelimiter
     *            delimiter for collections
     * @param nullChar
     *            what to replace <tt>null</tt> with.
     * @param waitForFutures
     *            whether to wait for {@link Future} objects to finish {@link Future#get() getting}
     *            the underlying object.
     */
    public DelimLayout(final char delimiter, final char secondaryDelimiter, final String nullChar,
                    final boolean waitForFutures, final long waitForFuturesTimeoutMillis) {
        super(Charset.forName("UTF-8"));
        this.delimiter = delimiter;
        this.secondaryDelimiter = secondaryDelimiter;
        this.nullChar = nullChar;
        this.waitForFutures = waitForFutures;
        this.waitForFuturesTimeoutMillis = waitForFuturesTimeoutMillis;
    }

    private final boolean waitForFutures;
    private final long waitForFuturesTimeoutMillis;
    private final char delimiter;
    private final char secondaryDelimiter;
    private String nullChar;

    @PluginFactory
    public static DelimLayout createLayout(final @PluginAttribute("delimiter") char delimiter,
                    final @PluginAttribute("secondaryDelimiter") char secondaryDelimiter,
                    final @PluginAttribute("nullChar") String nullChar,
                    final @PluginAttribute("waitForFutures") boolean waitForFutures,
                    final @PluginAttribute("waitForFuturesTimeoutMillis") long waitForFuturesTimeoutMillis) {
        return new DelimLayout(delimiter, secondaryDelimiter, nullChar, waitForFutures,
                        waitForFuturesTimeoutMillis);
    }

    /**
     * Formats the event as an Object that can be serialized.
     *
     * @param event
     *            The Logging Event.
     * @return The formatted event.
     */
    @Override
    public String toSerializable(LogEvent event) {
        final Message m = event.getMessage();
        StringBuilder sb = new StringBuilder();
        if (m != null) {
            if (m instanceof ObjectMessage) {
                final ObjectMessage om = (ObjectMessage) m;
                final Object objs[] = om.getParameters();
                if (objs.length == 1) {
                    Utils.delimFormat(sb, delimiter, secondaryDelimiter, nullChar, waitForFutures,
                                    waitForFuturesTimeoutMillis, objs[0]);
                } else {
                    Utils.delimFormat(sb, delimiter, secondaryDelimiter, nullChar, waitForFutures,
                                    waitForFuturesTimeoutMillis, objs);
                }
            } else if (m instanceof FormattedMessage) {
                sb.append(((FormattedMessage) m).getFormattedMessage());
            } else if (m instanceof ParameterizedMessage) {
                final ParameterizedMessage pm = (ParameterizedMessage) m;
                final Object objs[] = pm.getParameters();
                if (objs.length == 1) {
                    Utils.delimFormat(sb, delimiter, secondaryDelimiter, nullChar, waitForFutures,
                                    waitForFuturesTimeoutMillis, objs[0]);
                } else {
                    Utils.delimFormat(sb, delimiter, secondaryDelimiter, nullChar, waitForFutures,
                                    waitForFuturesTimeoutMillis, objs);
                }
            } else if (m instanceof SimpleMessage) {
                final SimpleMessage sm = (SimpleMessage) m;
                final Object[] objs = sm.getParameters();
                sb.append(sm.getFormattedMessage());
            }
        }

        if (sb.length() != 0) {
            sb.append("\n");
        }

        return sb.toString();

    }
}
