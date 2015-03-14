package com.enremmeta.rtb;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.jersey.StatsSvc;
import com.enremmeta.util.Jsonable;

/**
 * Encapsulates information about a bid in flight - which may be lost or won, and the lost or won
 * (or assumed lost) information may be handled on a different machine.
 * 
 * To be used in {@link LostAuctionTask}.
 * 
 * @see LogUtils#logWin(String, String, String, String, String, String, String, double, long, long,
 *      long, String, String, String, String, String, HttpServletRequest, String, Long, String, URI,
 *      boolean, String, String, String, String, String, String, boolean, boolean, String)
 * 
 * @see LogUtils#logLost(Long, String, String, String, String, Long, Long, String, String, String,
 *      String, HttpServletRequest, String, String, String, String)
 * 
 * @see StatsSvc#resultsOpenX(javax.ws.rs.core.UriInfo, String,
 *      com.enremmeta.rtb.proto.openx.AuctionResultMessage, String, HttpServletRequest, String,
 *      javax.ws.rs.core.HttpHeaders)
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public final class BidInFlightInfo implements Serializable, Jsonable {

    private Long winPrice;

    public Long getWinPrice() {
        return winPrice;
    }

    public void setWinPrice(Long winPrice) {
        this.winPrice = winPrice;
    }

    public BidInFlightInfo(Long winPrice) {
        this.winPrice = winPrice;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3889303824932135794L;

    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * To be used by JSON only
     */
    public BidInFlightInfo() {
        super();
    }

    public BidInFlightInfo(String reason) {
        super();
        this.reason = reason;
    }

    private Map<String, String> logValues = new HashMap<String, String>();

    public BidInFlightInfo(String adId, OpenRtbRequest req, Bid bid, long bidPriceMicros,
                    long bidCreatedOnTimestamp, String exchange, String ssp) {
        super();
        this.adId = adId;
        this.requestId = req.getId();
        this.exchange = exchange;
        this.ssp = ssp;
        this.bidId = bid.getId();
        this.impressionId = bid.getImpid();
        this.campaignId = bid.getCid();
        this.creativeId = bid.getCrid();
        this.bidPriceMicros = bidPriceMicros;
        this.bidCreatedOnTimestamp = bidCreatedOnTimestamp;
        this.winPrice = null;

        Site site = req.getSite();
        if (site != null) {
            this.logValues.put("domain", site.getDomain());
            this.logValues.put("url", site.getPage());
        }
    }

    public Map<String, String> getLogValues() {
        return logValues;
    }

    public void setLogValues(Map<String, String> logValues) {
        this.logValues = logValues;
    }

    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    private String adId;

    private String exchange;

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getExchange() {
        return exchange;
    }

    public String getSsp() {
        return ssp;
    }

    private String ssp;

    public void setSsp(String ssp) {
        this.ssp = ssp;
    }

    private String bidId;

    private String impressionId;

    private String campaignId;

    private String creativeId;

    private long bidPriceMicros;

    private long bidCreatedOnTimestamp;

    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public String getImpressionId() {
        return impressionId;
    }

    public void setImpressionId(String impressionId) {
        this.impressionId = impressionId;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(String creativeId) {
        this.creativeId = creativeId;
    }

    public long getBidPriceMicros() {
        return bidPriceMicros;
    }

    public void setBidPriceMicros(long bidPriceMicros) {
        this.bidPriceMicros = bidPriceMicros;
    }

    public long getBidCreatedOnTimestamp() {
        return bidCreatedOnTimestamp;
    }

    public void setBidCreatedOnTimestamp(long bidCreatedOnTimestamp) {
        this.bidCreatedOnTimestamp = bidCreatedOnTimestamp;
    }

    private String instanceId = Bidder.getInstance().getOrchestrator().getNodeId();

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        if (reason != null) {
            return "BidInFlightInfo(" + reason + ")";
        }
        if (winPrice != null) {
            return "BidInFlightInfo(Win price: " + winPrice + ")";

        }
        return "BidInFlightInfo(Ad ID: " + adId + "; Bid ID: " + bidId + "; impression ID: "
                        + impressionId + "; campaign ID: " + campaignId + "; creative ID: "
                        + creativeId + "; bid price micro$" + bidPriceMicros + "; created on "
                        + bidCreatedOnTimestamp + "; node ID: " + instanceId + ")";
    }

}
