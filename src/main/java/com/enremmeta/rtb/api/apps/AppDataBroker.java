package com.enremmeta.rtb.api.apps;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.opendsp.ab.AbTestingApp;

/**
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class AppDataBroker {
    private AppDataBroker() {
        super();
    }

    private static AppDataBroker INSTANCE = new AppDataBroker();

    public AppDataBroker getInstance() {
        return INSTANCE;
    }

    public Map<App, Future<AppData>> getAppData(OpenRtbRequest req) {
        final Map<String, Future<AppData>> map = new HashMap<String, Future<AppData>>();
        // Hardcode the app
        final AbTestingApp app = new AbTestingApp();
        // map.put(.getName(), null);
        // return map;
        return null;
    }
}
