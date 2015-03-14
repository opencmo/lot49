package com.enremmeta.rtb.proto.openx;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @see <a href="http://docs.openx.com/ad_exchange_adv/openrtb_auctionresults.html">http://docs.
 *      openx.com/ad_exchange_adv/openrtb_auctionresults.html</a>
 *      {auction_id=48e314c9-587c-4cde-ab9d-b4898efa694b, results=[{matching_ad_id={campaign_id=0,
 *      placement_id=0, creative_id=0}, status=3, loss_reason=undefined, error_reason=timeout,
 *      winning_bid_micros=undefined, clearing_price_micros=undefined}]}
 * 
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class AuctionResultMessage {

    public AuctionResultMessage() {
        // TODO Auto-generated constructor stub
    }

    @JsonProperty("auction_id")
    private String auctionId;

    private List<AuctionResult> results;

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public List<AuctionResult> getResults() {
        return results;
    }

    public void setResults(List<AuctionResult> results) {
        this.results = results;
    }

    public String toString() {
        StringBuilder retval = new StringBuilder();
        retval.append("{ \"auction_id\" : \"").append(auctionId).append("\", results=[");
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) {
                retval.append(". ");
            }
            retval.append(results.get(i).toString());
        }
        retval.append("]}");
        return retval.toString();
    }
}
