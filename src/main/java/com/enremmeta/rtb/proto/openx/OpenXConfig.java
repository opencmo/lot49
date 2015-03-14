package com.enremmeta.rtb.proto.openx;

import com.enremmeta.rtb.config.Config;
import com.enremmeta.rtb.config.ExchangesConfig;

/**
 * OpenX-specific {@link ExchangesConfig configuration}.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class OpenXConfig implements Config {

    public OpenXConfig() {
        // TODO Auto-generated constructor stub
    }

    private long winTimeout;

    public long getWinTimeout() {
        return winTimeout;
    }

    public void setWinTimeout(long winTimeout) {
        this.winTimeout = winTimeout;
    }

    private String encryptionKey;

    private String integrityKey;

    /**
     * Encryption key required to decrypt winning price. See your OpenX account manager to get one.
     * 
     * @see OpenXAdapter#parse(String, long)
     */
    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    /**
     * Integrity key required to decrypt winning price. See your OpenX account manager to get one.
     * 
     * @see OpenXAdapter#parse(String, long)
     */
    public String getIntegrityKey() {
        return integrityKey;
    }

    public void setIntegrityKey(String integrityKey) {
        this.integrityKey = integrityKey;
    }

}
