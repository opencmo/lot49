package com.enremmeta.rtb.spi.providers.integral;

import com.enremmeta.rtb.config.Config;

public class IntegralConfig implements Config {

    private static final long serialVersionUID = 4886521906406888105L;

    private String host;
    private Integer port;
    private String clientId;
    private Integer maxConnPerRoute;

    public IntegralConfig() {

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getEndpoint() {
        return host + ":" + port;
    }

    public Integer getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(Integer maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }

}
