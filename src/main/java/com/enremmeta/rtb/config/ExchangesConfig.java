package com.enremmeta.rtb.config;

import com.enremmeta.rtb.proto.adaptv.AdaptvConfig;
import com.enremmeta.rtb.proto.adx.AdXConfig;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchConfig;
import com.enremmeta.rtb.proto.liverail.LiverailConfig;
import com.enremmeta.rtb.proto.openx.OpenXConfig;
import com.enremmeta.rtb.proto.pubmatic.PubmaticConfig;

/**
 * Exchange-specific implementation of a {@link Lot49Config} section. See each particular
 * configuration for more documentation. You will need it when integrating with the exchange,
 * otherwise you won't.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class ExchangesConfig implements Config {

    public LiverailConfig getLiverail() {
        return liverail;
    }

    private PubmaticConfig pubmatic;

    public PubmaticConfig getPubmatic() {
        return pubmatic;
    }

    public void setPubmatic(PubmaticConfig pubmatic) {
        this.pubmatic = pubmatic;
    }

    private AdXConfig adx;

    public AdXConfig getAdx() {
        return adx;
    }

    public void setAdx(AdXConfig adx) {
        this.adx = adx;
    }

    public void setLiverail(LiverailConfig liverail) {
        this.liverail = liverail;
    }

    private LiverailConfig liverail;

    public ExchangesConfig() {
        // TODO Auto-generated constructor stub
    }

    private OpenXConfig openx;

    private BidSwitchConfig bidswitch;

    public BidSwitchConfig getBidswitch() {
        return bidswitch;
    }

    public void setBidswitch(BidSwitchConfig bidswitch) {
        this.bidswitch = bidswitch;
    }

    public OpenXConfig getOpenx() {
        return openx;
    }

    public void setOpenx(OpenXConfig openx) {
        this.openx = openx;
    }

    private AdaptvConfig adaptv = new AdaptvConfig();

    public AdaptvConfig getAdaptv() {
        return adaptv;
    }

    public void setAdaptv(AdaptvConfig adaptv) {
        this.adaptv = adaptv;
    }
}
