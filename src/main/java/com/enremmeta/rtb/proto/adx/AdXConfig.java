package com.enremmeta.rtb.proto.adx;

import java.util.Map;

import com.enremmeta.rtb.config.Config;
import com.enremmeta.rtb.config.ExchangesConfig;

/**
 * Google (AdX)-specific {@link ExchangesConfig configuration}.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class AdXConfig implements Config {

    public AdXConfig() {
        // TODO Auto-generated constructor stub
    }

    private String nid;

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    private String geoTable;

    public String getGeoTable() {
        return geoTable;
    }

    public void setGeoTable(String geoTable) {
        this.geoTable = geoTable;
    }

    public static final long DEFAULT_TIMEOUT = 100;

    private long timeout = DEFAULT_TIMEOUT;

    long getTimeout() {
        return timeout;
    }

    void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private byte[] encryptionKey;

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public byte[] getIntegrityKey() {
        return integrityKey;
    }

    public void setIntegrityKey(byte[] integrityKey) {
        this.integrityKey = integrityKey;
    }

    private byte[] integrityKey;

    private Map<String, Long> adGroupIdMap;

    public Map<String, Long> getAdGroupIdMap() {
        return adGroupIdMap;
    }

    public void setAdGroupIdMap(Map<String, Long> adGroupIdMap) {
        this.adGroupIdMap = adGroupIdMap;
    }
}
