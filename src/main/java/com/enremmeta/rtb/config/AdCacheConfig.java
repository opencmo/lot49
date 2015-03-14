package com.enremmeta.rtb.config;

import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.dao.DbService;

/**
 * Config for {@link AdCache}.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class AdCacheConfig implements Config {

    private boolean validateBudgetBeforeLoadingAd = true;

    /**
     * Should be validated budget, start and end dates before loading ad from the groovy script.
     * 
     * @return the validateBudgetBeforeLoadingAd
     */
    public boolean isValidateBudgetBeforeLoadingAd() {
        return validateBudgetBeforeLoadingAd;
    }

    /**
     * @param validateBudgetBeforeLoadingAd
     *            the validateBudgetBeforeLoadingAd to set
     */
    public void setValidateBudgetBeforeLoadingAd(boolean validateBudgetBeforeLoadingAd) {
        this.validateBudgetBeforeLoadingAd = validateBudgetBeforeLoadingAd;
    }

    /**
     * {@link DbService} implementing {@link DbService#getDaoShortLivedMap()} or
     * {@link DbService#getDaoShortLivedMap(Class)}
     */
    public String getShortTermDb() {
        return shortTermDb;
    }

    public void setShortTermDb(String shortTermDb) {
        this.shortTermDb = shortTermDb;
    }

    private String shortTermDb;

    public static final int DEFAULT_DOMAIN_LIST_MAX_SIZE = 4096;

    private int domainListMaxSize = DEFAULT_DOMAIN_LIST_MAX_SIZE;

    /**
     * Maximum configured size of domain white and black list, per {@link com.enremmeta.rtb.api.Ad}.
     * If not specified, defaults to {@link #DEFAULT_DOMAIN_LIST_MAX_SIZE}.
     */
    public int getDomainListMaxSize() {
        return domainListMaxSize;
    }

    public void setDomainListMaxSize(int domainListMaxSize) {
        this.domainListMaxSize = domainListMaxSize;
    }

    public static final int DEFAULT_URL_LIST_MAX_SIZE = 4096;

    private int urlListMaxSize = DEFAULT_URL_LIST_MAX_SIZE;

    /**
     * Maximum size of {@link com.enremmeta.rtb.api.Ad#getTargetingUrls() URL list}, per
     * {@link com.enremmeta.rtb.api.Ad}. If not specified, defaults to
     * {@link #DEFAULT_URL_LIST_MAX_SIZE}.
     */
    public int getUrlListMaxSize() {
        return urlListMaxSize;
    }

    public void setUrlListMaxSize(int urlListMaxSize) {
        this.urlListMaxSize = urlListMaxSize;
    }

    private int ttlMinutes;

    public int getTtlMinutes() {
        return ttlMinutes;
    }

    private PacingServiceConfig pacing;

    public PacingServiceConfig getPacing() {
        return pacing;
    }

    public void setPacing(PacingServiceConfig pacingConfig) {
        this.pacing = pacingConfig;
    }

    public void setTtlMinutes(int ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public AdCacheConfig() {
        // TODO Auto-generated constructor stub
    }

    private String dir;
}
