package com.opendsp.ab;

import java.util.concurrent.Future;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.api.apps.App;
import com.enremmeta.rtb.api.apps.AppData;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.caches.KnownFuture;
import com.enremmeta.rtb.dao.impl.hazelcast.HazelcastService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 * 
 * @author Alex Berg (<a href="mailto:alexberg@gmail.com">alexberg@gmail.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.opendsp.com">OpenDSP</a> 2015. All Rights Reserved.
 *
 */
public class AbTestingApp implements App {

    private static final long serialVersionUID = -2997045813609914780L;

    @Override
    public String getName() {
        return "OpenDSP A/B Testing";
    }

    @Override
    public Future<AppData> getAppData(OpenRtbRequest req) {
        final HazelcastService hzService = Bidder.getInstance().getHazelcastService();
        final HazelcastInstance hzInstance = hzService.getHazelcastInstance();
        final IMap<Object, Object> map = hzInstance.getMap(HazelcastService.MAP_NAME_LONG_LIVED);
        final String buyerUid = req.getUser().getBuyeruid();
        if (buyerUid == null || buyerUid.length() == 0) {
            return KnownFuture.KNOWN_NULL_FUTURE;
        }
        final String key = getCode() + "." + buyerUid;
        final Future retval = map.getAsync(key);
        return (Future<AppData>) retval;
    }

    @Override
    public void processResponse(OpenRtbResponse resp) {

    }

    @Override
    public boolean pass(OpenRtbRequest req, AppData data) {
        // TODO Auto-generated method stub
        return false;
    }

}
