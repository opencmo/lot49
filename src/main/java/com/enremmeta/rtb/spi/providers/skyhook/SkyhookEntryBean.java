package com.enremmeta.rtb.spi.providers.skyhook;

import com.enremmeta.util.Jsonable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SkyhookEntryBean implements Jsonable {
    @JsonProperty("ip_address")
    private String ipAddress;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getGridId() {
        return gridId;
    }

    public void setGridId(long gridId) {
        this.gridId = gridId;
    }

    @JsonProperty("grid_id")
    private long gridId;
}
