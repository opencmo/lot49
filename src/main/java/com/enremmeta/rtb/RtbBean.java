package com.enremmeta.rtb;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.util.Jsonable;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Marker interface denoting an entity that is part of OpenRTB request or response.
 * 
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public interface RtbBean extends Jsonable {

    /**
     * If an unknown key is encountered, log a warning.
     */
    @JsonAnySetter
    public default void handleUnknown(String key, Object value) {
        String s = "";
        if (this instanceof OpenRtbRequest) {
            Lot49Ext ext = ((OpenRtbRequest) this).getLot49Ext();
            if (ext != null) {
                ExchangeAdapter a = ext.getAdapter();
                if (a != null) {
                    s = "(" + a.getName() + ")";
                } else {
                    s = "(???)";
                }
            }
        }
        LogUtils.warn("RtbBean " + s + getClass().getName() + ": Unknown key " + key
                        + " with value " + value);

    }

}
