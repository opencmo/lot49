package com.enremmeta.rtb.spi.providers.skyhook;

import java.util.List;

import com.enremmeta.util.Jsonable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FullGridEntryBean implements Jsonable {
    private List<Integer> categories;

    public List<Integer> getCategories() {
        return categories;
    }

    @JsonProperty("ActiveForResidentialHours")
    private int activeForResidentialHours;

    public int getActiveForResidentialHours() {
        return activeForResidentialHours;
    }

    public void setActiveForResidentialHours(int activeForResidentialHours) {
        this.activeForResidentialHours = activeForResidentialHours;
    }

    public int getActiveForBusinesslHours() {
        return activeForBusinesslHours;
    }

    public void setActiveForBusinesslHours(int activeForBusinesslHours) {
        this.activeForBusinesslHours = activeForBusinesslHours;
    }

    @JsonProperty("ActiveForBusinessHours")
    private int activeForBusinesslHours;

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
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
