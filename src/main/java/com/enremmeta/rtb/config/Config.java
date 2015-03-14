package com.enremmeta.rtb.config;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.util.Jsonable;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Marker_interface_pattern">Marker</a> interface for various
 * beans holding configuration information.
 * </p>
 * Conventions:
 * <ul>
 * <li>The fields' meaning is described in the Javadoc for the getter, not for the field; for
 * example, to find out what <tt>foo</tt> field in configuration means, look at documentation for
 * <tt>getFoo()</tt>.</li>
 * <li>The default meaning of a field is defined in the corresponding constant named with
 * <tt>DEFAULT_</tt> prefix and further using of upper-case-underscore-delimited convention (not
 * camel case). It is recommended that the setter is written to substitute this value for null or
 * empty string, and to log it.</li>
 * <li>When a value is effectively an enum, it is suggested that possible values are enumerated, for
 * example, as
 * <ul>
 * <li>{@link PacingServiceConfig#DEFAULT_BUDGET_ALLOCATION_STRATEGY}</li>
 * <li>{@link PacingServiceConfig#DEFAULT_MESSAGE_TTL_MINUTES}</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * <p>
 * Partly these conventions are done so that we may write a simple tool to generate config file with
 * all possible values.
 * </p>
 * 
 * @author <a href="mailto:grisha@alum.mit.edu">Gregory Golberg (grisha@alum.mit.edu)</a>
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 * 
 *
 */
public interface Config extends Jsonable {
    @JsonAnySetter
    public default void handleUnknown(String key, Object value) {
        LogUtils.warn("Config " + getClass().getName() + ": Unknown key " + key + " with value "
                        + value);
    }
}
