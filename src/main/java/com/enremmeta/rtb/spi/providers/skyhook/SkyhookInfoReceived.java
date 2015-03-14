package com.enremmeta.rtb.spi.providers.skyhook;

import java.util.Set;

import com.enremmeta.rtb.spi.providers.ProviderInfoReceived;

public class SkyhookInfoReceived implements ProviderInfoReceived {

    /**
     * 
     */
    private static final long serialVersionUID = 6714442317084202131L;

    /**
     * @param cats
     * @param businessActive
     * @param residentActive
     */
    public SkyhookInfoReceived(Set<Integer> cats, boolean businessActive, boolean residentActive) {
        super();
        this.cats = cats;
        this.businessActive = businessActive;
        this.residentActive = residentActive;
    }

    private Set<Integer> cats;

    private boolean businessActive;
    private boolean residentActive;

    public Set<Integer> getCats() {
        return cats;
    }

    public void setCats(Set<Integer> cats) {
        this.cats = cats;
    }

    public boolean isBusinessActive() {
        return businessActive;
    }

    public void setBusinessActive(boolean businessActive) {
        this.businessActive = businessActive;
    }

    public boolean isResidentActive() {
        return residentActive;
    }

    public void setResidentActive(boolean residentActive) {
        this.residentActive = residentActive;
    }

    @Override
    public String toString() {
        return "{ \"skyhook\" : { \"resident\" : " + isResidentActive() + ",  \"businesss\" : "
                        + isBusinessActive() + ", \"cats\" : "
                        + (cats == null ? "[]" : cats.toString()) + "}";
    }
}
