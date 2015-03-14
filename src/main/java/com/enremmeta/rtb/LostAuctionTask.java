package com.enremmeta.rtb;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.util.BidderCalendar;

/**
 * A task scheduled for
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class LostAuctionTask extends TimerTask {
    public static final String CANCELLATION_REASON_AUTHORITATIVE_LOSS = "loss";
    public static final String CANCELLATION_REASON_AUTHORITATIVE_ERROR = "error";
    public static final String CANCELLATION_REASON_AUTHORITATIVE_ASSUMED = "assumed";

    public static final BidInFlightInfo CANCELLATION_REASON_AUTHORITATIVE_LOSS_INFO =
                    new BidInFlightInfo(CANCELLATION_REASON_AUTHORITATIVE_LOSS);

    public static final BidInFlightInfo CANCELLATION_REASON_AUTHORITATIVE_ERROR_INFO =
                    new BidInFlightInfo(CANCELLATION_REASON_AUTHORITATIVE_ERROR);

    public static final BidInFlightInfo CANCELLATION_REASON_AUTHORITATIVE_ASSUMED_INFO =
                    new BidInFlightInfo(CANCELLATION_REASON_AUTHORITATIVE_ASSUMED);

    @Override
    public void run() {
        try {
            final String bidId = this.bidInfo.getBidId();
            final String brId = this.bidInfo.getRequestId();
            final String msgPrefix = "Executing scheduled " + this + " for bid ID " + bidId;
            try {
                BidInFlightInfo bif = cancelLostAuctionTask(this.key,
                                CANCELLATION_REASON_AUTHORITATIVE_ASSUMED_INFO);
                if (bif != null) {
                    final String reason = bif.getReason();
                    if (reason != null) {
                        LogUtils.debug("LostAuctionTask: " + this.bidInfo.getAdId()
                                        + ": Lost because of " + reason);

                    } else if (bif.getWinPrice() != null) {
                        Ad ad = Bidder.getInstance().getAdCache().getAd(this.bidInfo.getAdId());
                        long winPrice = bif.getWinPrice();
                        if (ad == null) {
                            LogUtils.error("LostAuctionTask: Cannot find ad "
                                            + this.bidInfo.getAdId());
                        } else {
                            LogUtils.info("LostAuctionTask: " + ad.getId() + " won for " + winPrice
                                            + " (bid was " + bif.getBidPriceMicros() + ")");

                        }

                    } else {
                        LogUtils.info("LostAuctionTask: assumed loss, my info is: " + bidInfo
                                        + " / Cached info is: " + bif);
                        if (!bidInfo.getAdId().equals(bif.getAdId())
                                        || !bidInfo.getBidId().equals(bif.getBidId())
                                        || bidInfo.getBidPriceMicros() != bif.getBidPriceMicros()) {
                            LogUtils.error("LostAuctionTask: Retrieved info " + bidInfo
                                            + " not equal to expected " + bif);
                        }
                        LogUtils.logLost(bif.getBidCreatedOnTimestamp(), bidId,
                                        bif.getImpressionId(), bif.getCampaignId(),
                                        bif.getCreativeId(), bif.getBidPriceMicros(), -1l,
                                        CANCELLATION_REASON_AUTHORITATIVE_ASSUMED,
                                        bidInfo.getExchange(), null, null, null, null, bif.getSsp(),
                                        brId, bif.getInstanceId());
                    }
                }
            } catch (Throwable t) {
                LogUtils.error("LostAuctionTask: " + t.getMessage(), t);
            }
        } catch (Throwable t2) {
            LogUtils.error("LostAuctionTask: " + t2.getMessage(), t2);
        }
    }

    private final BidInFlightInfo bidInfo;

    /**
     * Cancel the task - usually because we won the bid; but sometimes because we got another
     * explicit notification that we lost, so we don't need the timeout.
     * 
     * @see ExchangeAdapter#trueWinOnNurlOrImpression()
     */
    public static BidInFlightInfo cancelLostAuctionTask(String key, BidInFlightInfo replacement)
                    throws Lot49Exception {
        DaoShortLivedMap<BidInFlightInfo> map =
                        Bidder.getInstance().getAdCache().getBidInFlightInfoMap();

        final Object result = map.replace(key, replacement);

        LogUtils.info("LostAuctionTask: cancelLostAuctionTask(" + key + ", " + replacement + "): "
                        + result);
        if (result == null) {
            return new BidInFlightInfo("cancelLostAuctionTask() BidInFlightInfo for key " + key
                            + " not found");
        } else {
            if (result instanceof BidInFlightInfo) {
                return (BidInFlightInfo) result;
            } else if (result instanceof String) {
                return new BidInFlightInfo((String) result);
            } else if (result instanceof Long) {
                return new BidInFlightInfo((Long) result);
            } else {
                LogUtils.error("Expected BidInFlightInfo or String, received: " + result);
                return null;
            }
        }
    }

    public LostAuctionTask(final BidInFlightInfo bidInfo, final String key) {
        super();
        this.bidInfo = bidInfo;
        this.key = key;
    }

    private final String key;

    @Override
    public String toString() {
        return "LostAuctionTask(" + bidInfo + ")";
    }

    private static final Map<String, LostAuctionTask> bidsInFlight =
                    new HashMap<String, LostAuctionTask>();

    /**
     * Assume a bid is lost when {@link Lot49Config#getWinTimeoutSeconds() winTimeout} seconds have
     * passed.
     * 
     * @param info
     *            info of bid that we lost
     * @param key
     *            key
     * @param winTimeout
     *            timeout
     * 
     * @throws Lot49Exception
     */
    public static void scheduleLostAuctionTask(BidInFlightInfo info, String key, long winTimeout)
                    throws Lot49Exception {
        long t0 = BidderCalendar.getInstance().currentTimeMillis();
        final LostAuctionTask task = new LostAuctionTask(info, key);

        DaoShortLivedMap<BidInFlightInfo> map = null;
        map = Bidder.getInstance().getAdCache().getBidInFlightInfoMap();
        map.putAsync(key, info);
        LogUtils.info("LostAuctionTask: put(" + key + "," + info);
        Bidder.getInstance().getScheduledExecutor().schedule(task, winTimeout, TimeUnit.SECONDS);
        LogUtils.trace("Time to scheduleLostAuctionTask(" + key + "): "
                        + (BidderCalendar.getInstance().currentTimeMillis() - t0));

    }
}
