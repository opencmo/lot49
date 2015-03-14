package com.enremmeta.rtb.test.utils;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.Lot49Config;

/**
 * Various static utilities for getting test setup. For now the tests are controlled via environment
 * variables, but this can change or depend on the setup (one way in local deployment, another way
 * in CI, etc). This will abstract the methods.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class Lot49TestUtils {

    public Lot49TestUtils() {
        super();

    }

    private Lot49Config config;

    public static final String LOT49_TEST_PROP_CONFIG = "lot49.test.prop.config";

    /**
     * Read {@link Lot49Config config}. This will be based on JVM property variable
     * {@link #LOT49_TEST_ENV_CONFIG}.
     */
    public void loadConfig() throws Exception {
        String configPath = System.getProperty(LOT49_TEST_PROP_CONFIG);
        Bidder.getInstance().parseCommandLineArgs(new String[] {"-c", configPath});
        Bidder.getInstance().loadConfig();
        this.config = Bidder.getInstance().getConfig();
    }

    public void refreshFileAdCache() throws Exception {
        if (this.adCache == null) {
            AdCacheConfig acc = config.getAdCache();
            acc.setTtlMinutes(0);
            adCache = new AdCache(acc);
        }
        adCache.init();
    }

    public AdCache getAdCache() {
        return adCache;
    }

    public void setAdCache(AdCache adCache) {
        this.adCache = adCache;
    }

    private AdCache adCache = null;

    public Lot49Config getConfig() {
        return config;
    }

    public void setConfig(Lot49Config config) {
        this.config = config;
    }
}
