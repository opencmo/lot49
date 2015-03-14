package com.enremmeta.rtb.proto.openx;

import com.enremmeta.util.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {auction_id=48e314c9-587c-4cde-ab9d-b4898efa694b, results=[{matching_ad_id={campaign_id=0,
 * placement_id=0, creative_id=0}, status=3, loss_reason=undefined, error_reason=timeout,
 * winning_bid_micros=undefined, clearing_price_micros=undefined}]}
 * 
 * @see <a href="http://docs.openx.com/ad_exchange_adv/#openrtb_auctionresults.html">http://docs.
 *      openx.com/ad_exchange_adv/#openrtb_auctionresults.html</a>
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class AuctionResult {

    public AuctionResult() {
        // TODO Auto-generated constructor stub
    }

    @JsonProperty("matching_ad_id")
    private MatchingAdId matchingAdId;

    /**
     * @see #getStatus()
     */
    private int status;

    @JsonProperty("loss_reason")
    private String lossReason;

    public static final String LOSS_REASON_DISQUALIFICATION = "disqualification";

    public static final String LOSS_REASON_PRICE = "price";

    /**
     * An error occurred while establishing the HTTP connection.
     */
    public static final String ERROR_CONNECT = "ERROR_CONNECT";

    /**
     * There was an error resolving the endpoint domain.
     */
    public static final String ERROR_NXDOMAIN = "ERROR_NXDOMAIN";

    /**
     * An error occurred while parsing the response.
     */
    public static final String ERROR_PARSE = "ERROR_PARSE";

    /**
     * An error occurred while receiving the response.
     */
    public static final String ERROR_RECEIVE = "ERROR_RECEIVE";

    /**
     * Some other unknown error occurred.
     */
    public static final String ERROR_UNKNOWN = "ERROR_UNKNOWN";

    /**
     * The original bid was not received (it timed out).
     */
    public static final String ERROR_TIMEOUT = "TIMEOUT"; // sic!

    public static final int STATUS_WIN = 1;

    public static final int STATUS_LOSS = 2;

    public static final int STATUS_ERROR = 3;

    @JsonProperty("error_reason")
    private String errorReason;

    public MatchingAdId getMatchingAdId() {
        return matchingAdId;
    }

    public void setMatchingAdId(MatchingAdId matchingAdId) {
        this.matchingAdId = matchingAdId;
    }

    /**
     * 1: win; 2: loss; 3: error
     * 
     * @return
     */
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * If {@link #getStatus()} is 2
     */
    public String getLossReason() {
        return lossReason;
    }

    public void setLossReason(String lossReason) {
        this.lossReason = lossReason;
    }

    /**
     * If {@link #getStatus()} is 3
     */
    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    @JsonProperty("winning_bid_micros")
    private String winningBidMicros;

    /**
     * Winning bid in micro-dollars.
     */
    public String getWinningBidMicros() {
        return winningBidMicros;
    }

    public void setWinningBidMicros(String winningBidMicros) {
        this.winningBidMicros = winningBidMicros;
    }

    /**
     * Clearing price, in micro-dollars,
     */
    public String getClearingPriceMicros() {
        return clearingPriceMicros;
    }

    public void setClearingPriceMicros(String clearingPriceMicros) {
        this.clearingPriceMicros = clearingPriceMicros;
    }

    @JsonProperty("clearing_price_micros")
    private String clearingPriceMicros;

    public String toString() {
        String retval = "{" + Utils.toStringKeyValue("matching_ad_id", matchingAdId) + ", "
                        + Utils.toStringKeyValue("status", status) + ", "
                        + Utils.toStringKeyValue("loss_reason", lossReason) + ", "
                        + Utils.toStringKeyValue("error_reason", errorReason) + ", "
                        + Utils.toStringKeyValue("clearing_price_micros", clearingPriceMicros)
                        + ", " + Utils.toStringKeyValue("winning_bid_micros", winningBidMicros)
                        + "}";
        return retval;
    }
}
