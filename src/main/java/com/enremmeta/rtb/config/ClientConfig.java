package com.enremmeta.rtb.config;

import java.util.List;

import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49SubscriptionData;

/**
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class ClientConfig implements Config {
    private String cookieDomain;
    private String baseUrl;
    private String statsUrl;

    private String packageName;

    private String dir;

    private List<Lot49SubscriptionData.Lot49SubscriptionServiceName> subscriptions;

    public List<Lot49SubscriptionData.Lot49SubscriptionServiceName> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(
                    List<Lot49SubscriptionData.Lot49SubscriptionServiceName> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getStatsUrl() {
        return statsUrl;
    }

    public void setStatsUrl(String statsUrl) {
        this.statsUrl = statsUrl;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
