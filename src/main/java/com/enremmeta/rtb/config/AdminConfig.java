package com.enremmeta.rtb.config;

import com.enremmeta.rtb.jersey.AdminSvc;
import com.enremmeta.rtb.jersey.OpsSvc;
import com.enremmeta.rtb.proto.ExchangeAdapter;

/**
 * Config for the admin section.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class AdminConfig implements Config {

    private String exchangeClickSimulatorUrl;

    /**
     * A URL that can be set up that can be used in
     * {@link AdminSvc#debug2(String, String, String, String, String, javax.servlet.http.HttpServletRequest, javax.ws.rs.container.AsyncResponse, String)
     * debugging} as a value for an {@link ExchangeAdapter#getClickMacro() exchange click macro}. We
     * do not make this service a part of {@link OpsSvc} so as not to create confusion.
     */
    public String getExchangeClickSimulatorUrl() {
        return exchangeClickSimulatorUrl;
    }

    public void setExchangeClickSimulatorUrl(String exchangeClickSimulatorUrl) {
        this.exchangeClickSimulatorUrl = exchangeClickSimulatorUrl;
    }

    public AdminConfig() {
        // TODO Auto-generated constructor stub
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;

    private String username;

    /**
     * If <tt>null</tt> or empty we assume this means auth is turned off.
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String testDataDir;

    public String getTestDataDir() {
        return testDataDir;
    }

    public void setTestDataDir(String testDataDir) {
        this.testDataDir = testDataDir;
    }

}
