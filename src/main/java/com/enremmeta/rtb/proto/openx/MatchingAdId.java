package com.enremmeta.rtb.proto.openx;

import java.util.Map;

import com.enremmeta.util.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @see <a href="http://docs.openx.com/ad_exchange_adv/#openrtb_auctionresults.html">http://docs.
 *      openx.com/ad_exchange_adv/#openrtb_auctionresults.html</a>
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class MatchingAdId {

    public MatchingAdId(Map m) {
        super();
        this.adHeight = (Integer) m.get("ad_height");
        this.adWidth = (Integer) m.get("ad_width");
        this.campaignId = (Integer) m.get("campaign_id");
        this.creativeId = (Integer) m.get("creative_id");
        this.placementId = (Integer) m.get("placement_id");

    }

    public MatchingAdId(final int campaignId, final int placementId, final int creativeId) {
        super();
        this.campaignId = campaignId;
        this.creativeId = creativeId;
        this.placementId = placementId;

    }

    public MatchingAdId() {
        // TODO Auto-generated constructor stub
    }

    @JsonProperty("campaign_id")
    private int campaignId;

    @JsonProperty("placement_id")
    private int placementId;

    @JsonProperty("creative_id")
    private int creativeId;

    @JsonProperty("ad_height")
    private int adHeight;

    @JsonProperty("ad_width")
    private int adWidth;

    private Map deal;

    public int getAdHeight() {
        return adHeight;
    }

    public void setAdHeight(int adHeight) {
        this.adHeight = adHeight;
    }

    public int getAdWidth() {
        return adWidth;
    }

    public void setAdWidth(int adWidth) {
        this.adWidth = adWidth;
    }

    public Map getDeal() {
        return deal;
    }

    public void setDeal(Map deal) {
        this.deal = deal;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public int getPlacementId() {
        return placementId;
    }

    public void setPlacementId(int placementId) {
        this.placementId = placementId;
    }

    public int getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(int creativeId) {
        this.creativeId = creativeId;
    }

    @Override
    public String toString() {
        return "{" + Utils.toStringKeyValue("campaign_id", campaignId) + ", "
                        + Utils.toStringKeyValue("placement_id", placementId) + ","
                        + Utils.toStringKeyValue("creative_id", creativeId) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof MatchingAdId)) {
            return false;
        }
        MatchingAdId that = (MatchingAdId) o;
        return creativeId == that.creativeId && campaignId == that.campaignId
                        && placementId == that.placementId;
    }

}
