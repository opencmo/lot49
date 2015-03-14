package com.enremmeta.rtb.api.proto.openrtb.lot49;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.RtbBean;

/**
 * This holds the subscription data.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public final class Lot49SubscriptionData implements RtbBean {

    private final Map<String, List<Lot49SubscriptionServiceName>> map = new HashMap<>();

    public static enum Lot49SubscriptionServiceName {
        INTEGRAL, LOTAME
    }

    public void addSubscriptionData(String clientId,
                    List<Lot49SubscriptionServiceName> subscriptionServiceNames) {
        map.put(clientId, subscriptionServiceNames);
    }

    public List<Lot49SubscriptionServiceName> getSubscriptionData(String clientId) {
        return map.get(clientId);
    }

    public boolean isAllowedService(String clientId, Lot49SubscriptionServiceName... serviceNames) {
        List<Lot49SubscriptionServiceName> subscriptionServiceNames = getSubscriptionData(clientId);
        return subscriptionServiceNames != null
                        && subscriptionServiceNames.containsAll(Arrays.asList(serviceNames));
    }

}
