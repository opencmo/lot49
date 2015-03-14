package com.enremmeta.rtb.spi.providers.skyhook;

import java.util.Map;

import com.enremmeta.rtb.spi.providers.ProviderConfig;

public class SkyhookConfig extends ProviderConfig {

    public SkyhookConfig(Map map) {
        super(map);
        this.fullGrid = (String) map.get("fullGrid");
        this.skyhook = (String) map.get("skyhook");
        this.skyhookMap = (String) map.get("skyhookMap");
    }

    private String fullGrid;

    private String skyhook;

    private String skyhookMap;

    public String getSkyhookMap() {
        return skyhookMap;
    }

    public void setSkyhookMap(String skyhookMap) {
        this.skyhookMap = skyhookMap;
    }

    public String getFullGrid() {
        return fullGrid;
    }

    public void setFullGrid(String fullGrid) {
        this.fullGrid = fullGrid;
    }

    public String getSkyhook() {
        return skyhook;
    }

    public void setSkyhook(String skyhook) {
        this.skyhook = skyhook;
    }
}
