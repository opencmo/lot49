package com.enremmeta.rtb.spi.providers.maxmind;

import com.enremmeta.rtb.config.Config;

/**
 * Configuration for <a href="http://www.maxmind.com">MaxMind</a> geotargeting DB.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class MaxMindConfig implements Config {

    public MaxMindConfig() {
        // TODO Auto-generated constructor stub
    }

    private String city;
    private String domain;
    private String connectionType;
    private String isp;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

}
