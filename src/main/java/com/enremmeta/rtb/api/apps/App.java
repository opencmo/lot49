package com.enremmeta.rtb.api.apps;

import java.io.Serializable;
import java.util.concurrent.Future;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;

/**
 * Interface for a third-party app.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public interface App extends Serializable {
    default String getCode() {
        return getClass().getPackage().getName();
    }

    String getName();

    Future<AppData> getAppData(OpenRtbRequest req);

    /* Can have side effects!!!! */
    boolean pass(OpenRtbRequest req, AppData data);

    void processResponse(OpenRtbResponse resp);
}
