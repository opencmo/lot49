package com.enremmeta.rtb.spi.providers;

import java.util.HashMap;
import java.util.Map;

import com.enremmeta.rtb.config.Config;
import com.enremmeta.util.Utils;

public class ProviderConfig implements Config {

    protected Map getMap() {
        return map;
    }

    private Map map = new HashMap();

    protected boolean toBool(String key) {
        return Utils.isTrue(key);

    }

    public ProviderConfig(Map map) {
        super();
        this.map = map;

    }

    private boolean enabled = false;

    public boolean isEnabled() {
        return toBool("enabled");
    }

    public void setEnabled(boolean enabled) {
        this.map.put("enabled", true);
    }

}
