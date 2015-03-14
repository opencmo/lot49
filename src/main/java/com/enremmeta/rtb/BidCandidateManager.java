package com.enremmeta.rtb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtRemote;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoReceived;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;

/**
 * Manages the passing or failing of eligible {@link Ad}s and their {@link Tag}s for a given bid
 * request, and sending the response.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public final class BidCandidateManager implements Runnable, Lot49Constants {

    /**
     * Constructor
     *
     * @param bReq
     *            Current BidRequest
     * @param bResp
     *            BidResponse to fill in
     * @param asyncResp
     *            AsyncResponse to send
     * @param timeout
     *            Exchange-specific timeout after which to either decline to bid or send whatever we
     *            have ready and stop trying.
     * 
     * @see OpenRtbRequest#getTmax()
     * @see ExchangeAdapter#getDefaultTimeout()
     */
    public BidCandidateManager(final OpenRtbRequest bReq, final OpenRtbResponse bResp,
                    final AsyncResponse asyncResp, final long timeout) {
        super();
        this.startTime = BidderCalendar.getInstance().currentTimeMillis();
        this.bReq = bReq;
        this.bResp = bResp;
        this.asyncResp = asyncResp;
        this.timeout = timeout;
        this.integralInfoReceived = new IntegralInfoReceived();

    }

    public final OpenRtbRequest getBidRequest() {
        return bReq;
    }

    @SuppressWarnings("rawtypes")
    private ScheduledFuture futureSelf;

    @SuppressWarnings("rawtypes")
    public void setFutureSelf(ScheduledFuture futureSelf) {
        this.futureSelf = futureSelf;
    }

    private final long timeout;

    private final long startTime;

    private final OpenRtbRequest bReq;
    private final OpenRtbResponse bResp;
    private final AsyncResponse asyncResp;

    private AtomicBoolean running = new AtomicBoolean(false);

    private int runCount = 0;

    private long timeSpent;

    private IntegralInfoReceived integralInfoReceived = null;

    private Future<UserAttributes> userAttributesFuture = null;
    private UserAttributes userAttributes = null;
    private UserExperimentAttributes experimentData = null;

    private boolean isUnderExperiment = false;
    private boolean shouldSaveUserAttributes = false;

    private Future<UserSegments> userSegmentsFuture = null;
    private UserSegments userSegments = null;

    public final void requestUserSegments(String uid) {
        if (userSegmentsFuture == null) {
            userSegmentsFuture =
                            ServiceRunner.getInstance().getUserSegmentsCacheService().getAsync(uid);
        }
    }

    public Future<UserSegments> getUserSegmentsFuture() {
        return userSegmentsFuture;
    }

    public UserSegments getUserSegments() {
        return userSegments;
    }

    public void setUserSegments(UserSegments userSegments) {
        this.userSegments = userSegments;
    }

    public void setUserAttributes(UserAttributes userAttributes) {
        this.userAttributes = userAttributes;
        this.experimentData = userAttributes.getUserExperimentData();
    }

    public UserAttributes getUserAttributes() {
        return userAttributes;
    }

    public final void requestUserAttributes(String uid) {
        if (userAttributesFuture == null) {
            userAttributesFuture = ServiceRunner.getInstance().getUserAttributesCacheService()
                            .getAsync(uid);
            shouldSaveUserAttributes = true;
        }
    }

    public final Future<UserAttributes> getUserAttributesFuture() {
        return userAttributesFuture;
    }

    public final void putUnderExperiment() {
        isUnderExperiment = true;
    }

    public final void requestIntegralInfo(String url) {
        Bidder.getInstance().getIntegralService().checkUrl(url, integralInfoReceived);
    }

    public IntegralInfoReceived getIntegralInfoReceived() {
        return integralInfoReceived;
    }

    /**
     * Runs periodically, scheduled by {@link Bidder#getExecutor()}.
     */
    @Override
    public void run() {
        StringBuilder bcmPath = new StringBuilder();
        try {
            ++runCount;
            bcmPath.append("Run ").append(runCount).append(". ");
            if (!running.compareAndSet(false, true)) {
                trace("BidCandidateManager.run(): " + Thread.currentThread().getName()
                                + " finds that " + this + " is already running: " + running);
                bcmPath.append("Already running, returning. ");
                return;
            }
            int doneCount = 0;
            int passedCount = 0;
            int failedCount = 0;
            for (BidCandidate bc : candidates) {
                if (bc.passed()) {
                    doneCount++;
                    passedCount++;
                } else if (bc.failed()) {
                    doneCount++;
                    failedCount++;
                }
            }

            int candidateCount = candidates.size();
            timeSpent = (BidderCalendar.getInstance().currentTimeMillis() - startTime);
            boolean timedOut = timeSpent >= this.timeout;

            bcmPath.append("Done: " + doneCount + "/" + candidateCount + "; passed: " + passedCount
                            + "; failed: " + failedCount + "; elapsed: " + timeSpent
                            + "; timed out: " + timedOut + " (" + candidates.toString() + ")");
            bReq.getLot49Ext().setProcessingTime(timeSpent);
            if (doneCount == candidateCount || timedOut) {
                LogUtils.logRequest(this.bReq, false, candidateCount);

                setBids(bReq, bResp);
                final ExchangeAdapter adapter = bReq.getLot49Ext().getAdapter();
                Lot49Ext lot49Ext = bReq.getLot49Ext();

                if (lot49Ext.isNoBid()) {
                    trace("No bid for " + bReq.getId());
                    ResponseBuilder builder = adapter.getOptoutBuilder(bReq);
                    builder = Utils.addOptoutHeaders(lot49Ext, builder);
                    asyncResp.resume(builder.build());
                    bcmPath.append("AsyncResp resumed with no content. ");
                } else {

                    try {
                        Object bidResponse = adapter.convertResponse(bReq, bResp);

                        ResponseBuilder builder = Response.ok(bidResponse);
                        builder = adapter.setHeaders(bResp, builder);
                        builder = Utils.addOptoutHeaders(lot49Ext, builder);

                        final Response resp = builder.build();
                        asyncResp.resume(resp);

                        LogUtils.logResponse(bReq, bResp, bidResponse);
                        bcmPath.append("AsyncResp resumed with response. ");
                    } catch (Exception e) {
                        LogUtils.error("Error converting response, this sucks. ", e);
                        throw e;
                    }

                }
                if (futureSelf != null) {
                    boolean cancelingFutureSelf = futureSelf.cancel(false);
                    bcmPath.append("Canceling self: ").append(cancelingFutureSelf).append(". ");
                }
            }
        } catch (Throwable e) {
            LogUtils.error("BidCandidateManager.run(): (" + this + "): Unexpected error", e);
            boolean cancelingFutureSelf = futureSelf.cancel(false);
            LogUtils.debug(LogUtils.MARKER_WINLOSS,
                            "Canceling future self: " + cancelingFutureSelf + ": " + e);
            asyncResp.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build());
            bcmPath.append("AsyncResp resumed with Error. ");
        } finally {
            bcmPath.append("Done.");
            running.set(false);
            trace(bcmPath.toString());
        }
    }

    public void add(BidCandidate bc) {
        candidates.add(bc);
    }

    @Override
    public String toString() {
        return "BidCandidateManager@" + hashCode() + ": started at " + startTime + "; timeout: "
                        + timeout + "; running: " + running;
    }

    private final List<BidCandidate> candidates = new ArrayList<BidCandidate>(128);

    public int getCandidateCount() {
        return candidates.size();
    }

    public List<BidCandidate> getCandidates() {
        return candidates;
    }

    public BidCandidate getCandidate(int idx) {
        return candidates.get(idx);
    }

    private boolean postProcessBid(OpenRtbRequest req, Ad ad, Bid bid, boolean multiple) {
        final long postProcessBidEnterTime = BidderCalendar.getInstance().currentTimeMillis();
        final Lot49Ext ext = req.getLot49Ext();
        final ExchangeAdapter adapter = ext.getAdapter();
        final String exchange = adapter.getName();
        final String ssp = adapter.isAggregator() ? req.getLot49Ext().getSsp() : exchange;
        final BidInFlightInfo bidInFlightInfo = new BidInFlightInfo(ad.getId(), req, bid,
                        Utils.cpmToMicro(bid.getPrice()), postProcessBidEnterTime, exchange, ssp);

        if (isUnderExperiment && ad.checkMeasurementSegments(userSegments)) {
            final long impCount = req.getImp().size();
            String modUid = bReq.getLot49Ext().getModUid();
            if (ad.doCampaignAbTesting()) {
                if (experimentData.getStatusForCampaign(ad) == null) {
                    if (Utils.RANDOM.nextDouble() < ad.getCampaignAbTestingShare()) {
                        if (Utils.RANDOM.nextDouble() < ad.getCampaignAbTestingControlShare()) {
                            experimentData.setStatusControlForCampaign(ad);
                        } else {
                            experimentData.setStatusTestForCampaign(ad);
                        }
                    } else {
                        experimentData.setStatusNotExperimentForCampaign(ad);
                    }
                    LogUtils.logExperimentCampaign(req, modUid, ad, experimentData);
                }
            }

            if (ad.doTargetingStrategyAbTesting()) {
                if (experimentData.getStatusForCampaign(ad) == null) {
                    experimentData.setStatusNotExperimentForCampaign(ad);
                    LogUtils.logExperimentCampaign(req, modUid, ad, experimentData);
                }
                experimentData.setExperimentVersion(ad, "2");
                if (experimentData.isStatusNotExperimentForCampaign(ad)) {
                    if (experimentData.getStatusForTargetingStrategy(ad) == null) {
                        if (Utils.RANDOM.nextDouble() < ad.getAbTestingControlShare()) {
                            experimentData.setStatusControlForTargetingStrategy(ad);
                        } else {
                            experimentData.setStatusTestForTargetingStrategy(ad);
                        }
                        LogUtils.logExperimentTargetingStrategy(req, modUid, ad, experimentData);
                    }
                }
            }
            if (ad.doAbTesting() && (experimentData.isStatusControlForCampaign(ad)
                            || experimentData.isStatusControlForTargetingStrategy(ad))) {
                LogUtils.trace("User " + modUid + " in Control set of Ad " + ad.getId() + " "
                                + experimentData.getStatusForTargetingStrategy(ad) + " or campaign "
                                + ad.getCampaignId() + " "
                                + experimentData.getStatusForCampaign(ad));
                ad.incrOptout(bReq, DECISION_EXPERIMENT_CONTROL_SET, impCount);
                LogUtils.logBid(req, impCount, req.getId(), req.getUser().getBuyeruid(), modUid,
                                bid, -1, -1, adapter, Lot49Constants.BID_OUTCOME_CONTROL);
                req.getLot49Ext().getOptoutReasons()
                                .put(ad.getId(), "User in control set of campaign "
                                                + ad.getCampaignId()
                                                + " or in one of TS on this camapign");
                return false;
            }
        }

        if (userAttributes != null) {
            if (ad.isCampaignFrequencyCap()) {
                userAttributes.getUserFrequencyCap().updateBidsHistoryForCampaign(ad);
            }
            if (ad.isStrategyFrequencyCap()) {
                userAttributes.getUserFrequencyCap().updateBidsHistoryForTargetingStrategy(ad);
            }
        }

        String bidInFlightKey;
        if (multiple) {
            bidInFlightKey = KVKeysValues.BID_PREFIX + bid.getId();
        } else {
            bidInFlightKey = KVKeysValues.BID_REQUEST_PREFIX + req.getId();
        }
        final String bidInFlightFinal = bidInFlightKey;
        // This is because we are about to do a few putAsyncs and we are not
        // sure
        // they are that async...
        final ServiceRunner runner = ServiceRunner.getInstance();
        runner.getExecutor().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    LostAuctionTask.scheduleLostAuctionTask(bidInFlightInfo, bidInFlightFinal,
                                    adapter.getWinTimeout());
                    if (adapter.isNurlRequired()) {
                        DaoShortLivedMap<String> nurlMap = runner.getAdCache().getNurlMap();
                        final String tag = ext.getBidIdToTagText().get(bid.getId());
                        final String nurlId = (String) bid.getHiddenAttributes()
                                        .remove(KVKeysValues.NURL_PREFIX);
                        final String nurlKey = KVKeysValues.NURL_PREFIX + nurlId;
                        nurlMap.putAsync(nurlKey, tag);

                    }
                } catch (Lot49Exception e) {
                    LogUtils.error(e);
                }
            }

        });

        ad.setLastBidTime(postProcessBidEnterTime);
        ad.incrBids(exchange, Utils.cpmToMicro(bid.getPrice()));

        final long postProcessBidTime =
                        BidderCalendar.getInstance().currentTimeMillis() - postProcessBidEnterTime;
        LogUtils.trace("Time to postprocess bid: " + postProcessBidTime);
        return true;
    }

    public void setBids(OpenRtbRequest req, OpenRtbResponse resp) {
        final long impCount = req.getImp().size();
        final long curTime = BidderCalendar.getInstance().currentTimeMillis();
        final List<SeatBid> seatBids = resp.getSeatbid();
        List<Bid> bidsToReturn = null;
        // Candidates to consider for the case of RANDOM algorithm
        final List<BidCandidate> candsToConsider = new ArrayList<BidCandidate>(candidates.size());
        // Passed BidCandidates
        final List<BidCandidate> candsPassed = new ArrayList<BidCandidate>(candidates.size());
        SeatBid seatBid = null;
        if (seatBids != null && !seatBids.isEmpty()) {
            // Pick the first one.
            seatBid = resp.getSeatbid().get(0);
            bidsToReturn = seatBid.getBid();
        } else {
            LogUtils.info("Unexpected: Empty seatBid: " + seatBid);
            return;
        }

        int failCnt = 0;
        int timedOutCnt = 0;

        final ExchangeAdapter adapter = bReq.getLot49Ext().getAdapter();

        // Current maximum price
        long maxBidPrice = -1;
        // Bid from an Ad having currently least recent bid
        Bid lruBid = null;
        // BidCandidate having currently least recent bid
        BidCandidate lruCand = null;

        String modUid = bReq.getLot49Ext().getModUid();
        for (BidCandidate c : candidates) {
            final Ad ad = c.getAd();

            if (c.passed()) {
                candsPassed.add(c);
            } else {
                // NOT passed yet.
                // Refund budget.
                // c.getAd().unallocateBid();
                if (c.failed()) {
                    // Explicitly failed.
                    failCnt++;
                } else {
                    // If not passed but not explicitly failed - means timed
                    // out.
                    timedOutCnt++;
                    final String optout = "Timed out: " + (curTime - startTime) + " OR " + timeSpent
                                    + ", timeout: " + this.timeout + "; run count: " + runCount
                                    + "; for request: " + bReq.getId();
                    if (ad.doAbTesting() && !c.isExperimentInfoCompleted()) {
                        ad.incrOptout(bReq, DECISION_TIMEOUT_EXPERIMENT_STATUS, impCount);
                        req.getLot49Ext().getOptoutReasons().put(ad.getId(),
                                        optout + "; Experiment status cache not received");
                    } else if (!c.isFcInfoCompleted()) {
                        ad.incrOptout(bReq, DECISION_TIMEOUT_FC, impCount);
                        req.getLot49Ext().getOptoutReasons().put(ad.getId(),
                                        optout + "; FC data not received");
                    } else if (!c.isIntegralCompleted()) {
                        ad.incrOptout(bReq, DECISION_TIMEOUT_INTEGRAL, impCount);
                        req.getLot49Ext().getOptoutReasons().put(ad.getId(),
                                        optout + "; Inteegral data not received");
                    } else if (!c.isUserInfoCompleted()) {
                        ad.incrOptout(bReq, DECISION_TIMEOUT_USERDATA, impCount);
                        req.getLot49Ext().getOptoutReasons().put(ad.getId(),
                                        optout + "; User data not received");
                    } else {
                        ad.incrOptout(bReq, DECISION_TIMEOUT_UNKNOWN, impCount);
                        req.getLot49Ext().getOptoutReasons().put(ad.getId(),
                                        optout + "; some data not received");
                    }
                }
            }
        }

        // TODO it is potentially dangerous to put this stuff into Lot49Ext to
        // pass on to BidPriceCalculator for 2 reasons:
        // 1. This does not check subscriptions
        // 2. BidPriceCalculator can invoke setters - this should be forbidden by security model.
        Lot49ExtRemote lot49ExtRemote = req.getLot49Ext().getLot49ExtRemote();
        if (integralInfoReceived != null) {
            if (integralInfoReceived.isCompleted()) {
                lot49ExtRemote.setIntegralInfoReceived(integralInfoReceived);
            } else {
                integralInfoReceived.cancelIntegralRequest();
            }
        }
        if (userSegmentsFuture != null) {
            LogUtils.debug("done/canceled: " + userSegmentsFuture.isDone() + "/"
                            + userSegmentsFuture.isCancelled());
            if (userSegmentsFuture.isDone() && !userSegmentsFuture.isCancelled()) {
                try {
                    lot49ExtRemote.setUserSegments(userSegmentsFuture.get());
                } catch (InterruptedException e) {
                    LogUtils.error(e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    LogUtils.error(e);
                }
            } else {
                userSegmentsFuture.cancel(false);
            }
        }

        if (userAttributesFuture != null) {
            if (userAttributesFuture.isDone()) {
                try {
                    lot49ExtRemote.setUserAttributes(userAttributesFuture.get());
                } catch (InterruptedException e) {
                    LogUtils.error(e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    LogUtils.error(e);
                }
            } else {
                userAttributesFuture.cancel(false);
            }
        }

        if (candsPassed.size() > 0) {
            LogUtils.debug("Got " + candsPassed.size() + " candidates for request " + req.getId()
                            + " on user " + modUid + ":" + candsPassed.toString());
        }

        List<Impression> imps = req.getImp();
        for (BidCandidate c : candsPassed) {
            final Ad ad = c.getAd();
            final Bid bid = c.getBid();

            long bidPriceLong = 0;
            try {
                bidPriceLong = ad.getBidPrice(req);
                c.setBidPrice(bidPriceLong);
            } catch (Throwable t) {
                LogUtils.error("Error getting bid price from " + ad.getId() + ": " + t.getMessage(),
                                t);
                ad.incrOptout(bReq, DECISION_EVALUATION_ERROR, impCount);
                req.getLot49Ext().getOptoutReasons().put(ad.getId(), DECISION_EVALUATION_ERROR);
                continue;

            }
            final Float bidPrice = new Float(Utils.microToCpm(bidPriceLong));
            // We may still be lower than floor at this point.
            if (ad.isDynamicPricing()) {
                boolean ok = true;
                boolean found = false;
                for (Impression imp : imps) {
                    if (imp.getId().equals(bid.getImpid())) {
                        found = true;
                        if (bidPrice < imp.getBidfloor()) {
                            ad.incrOptout(bReq, DECISION_FLOOR, impCount);
                            ok = false;
                            break;
                        }
                    }
                }
                if (!found) {
                    LogUtils.error("Impression ID " + bid.getImpid()
                                    + " does not correspond to any impression in request");
                    continue;
                }
                if (!ok) {
                    continue;
                }
            }
            bid.setPrice(bidPrice);
            req.getLot49Ext().getBidRequestIdToAdObject().put(bReq.getId(), ad);

            if (maxBidPrice == -1) {
                // First time
                maxBidPrice = bidPriceLong;
                candsToConsider.add(c);
            } else if (bidPriceLong == maxBidPrice) {
                // Same price as current max price, add to consideration
                candsToConsider.add(c);
            } else if (bidPriceLong > maxBidPrice) {
                // Eject previous candidates and add this one
                for (BidCandidate optoutCandidate : candsToConsider) {
                    final Ad optoutAd = optoutCandidate.getAd();
                    final Bid optoutBid = optoutCandidate.getBid();
                    LogUtils.logBid(req, timeSpent, req.getId(), req.getUser().getBuyeruid(),
                                    req.getLot49Ext().getModUid(), bid, candsToConsider.size(), -1,
                                    null, Lot49Constants.BID_OUTCOME_INTERNAL_AUCTION_LOSS);
                    LogUtils.logLost(curTime, optoutBid.getId(), optoutBid.getImpid(), ad.getId(),
                                    optoutBid.getCrid(), maxBidPrice, bidPriceLong,
                                    Lot49Constants.BID_OUTCOME_INTERNAL_AUCTION_LOSS, null,
                                    Lot49Constants.BID_OUTCOME_INTERNAL_AUCTION_LOSS, null, null,
                                    null, null, req.getId(),
                                    Bidder.getInstance().getOrchestrator().getNodeId());
                    optoutAd.incrOptout(bReq, DECISION_INTERNAL_AUCTION, impCount);
                    req.getLot49Ext().getOptoutReasons().put(optoutAd.getId(),
                                    "MAX strategy used and " + bidPriceLong + ">" + maxBidPrice);

                }
                maxBidPrice = bidPriceLong;
                candsToConsider.clear();
                candsToConsider.add(c);
            } else {
                // Optout this one
                final Ad optoutAd = c.getAd();
                final Bid optoutBid = c.getBid();
                LogUtils.logBid(req, timeSpent, req.getId(), req.getUser().getBuyeruid(),
                                req.getLot49Ext().getModUid(), bid, candsToConsider.size(), -1,
                                null, Lot49Constants.BID_OUTCOME_INTERNAL_AUCTION_LOSS);
                LogUtils.logLost(curTime, optoutBid.getId(), optoutBid.getImpid(), ad.getId(),
                                optoutBid.getCrid(), maxBidPrice, bidPriceLong,
                                Lot49Constants.BID_OUTCOME_INTERNAL_AUCTION_LOSS, null,
                                Lot49Constants.BID_OUTCOME_INTERNAL_AUCTION_LOSS, null, null, null,
                                null, req.getId(),
                                Bidder.getInstance().getOrchestrator().getNodeId());
                optoutAd.incrOptout(bReq, DECISION_INTERNAL_AUCTION, impCount);
                req.getLot49Ext().getOptoutReasons().put(optoutAd.getId(),
                                "MAX strategy used and " + bidPriceLong + ">" + maxBidPrice);

            }

            if (candsToConsider.size() > 0) {
                StringBuilder msg = new StringBuilder();
                msg.append("[");
                for (BidCandidate xxx : candsToConsider) {
                    msg.append(" ").append(xxx.getAd().getId()).append(":")
                                    .append(xxx.getBidPrice());
                }
                msg.append("]");
                LogUtils.debug("INTERNAL AUCTION: CURRENT STATE: " + msg.toString());
            }

            // TODO
            seatBid.setSeat(adapter.getSeat(ad));
        }
        final String exchange = adapter.getName();
        final String ssp = adapter.isAggregator() ? req.getLot49Ext().getSsp() : exchange;

        if (candsToConsider.size() > 0) {
            final int rndIdx = Utils.RANDOM.nextInt(candsToConsider.size());
            for (int j = 0; j < candsToConsider.size(); j++) {

                final BidCandidate curCand = candsToConsider.get(j);
                final Ad curAd = curCand.getAd();
                if (j == rndIdx) {
                    final Bid randBid = curCand.getBid();

                    if (postProcessBid(req, curAd, randBid, false)) {
                        req.getLot49Ext().setNoBid(false);
                        bidsToReturn.add(randBid);
                    }
                    if (candsToConsider.size() > 1) {
                        LogUtils.debug("INTERNAL AUCTION: Chose MAX(" + rndIdx + ") bid "
                                        + curAd.getId() + " out of " + candsToConsider.size() + ": "
                                        + candsToConsider);
                    }
                } else {
                    curAd.incrOptout(bReq, DECISION_INTERNAL_AUCTION, impCount);
                    req.getLot49Ext().getOptoutReasons().put(curAd.getId(), "MAX strategy used and "
                                    + rndIdx + " was chosen from " + candsToConsider);
                }
            }
        }


        timeSpent = curTime - startTime;
        trace("Time spent in decision: " + timeSpent);

        for (final Bid bidToLog : bidsToReturn) {
            LogUtils.logBid(req, timeSpent, req.getId(), req.getUser().getBuyeruid(),
                            req.getLot49Ext().getModUid(), bidToLog, candidates.size(),
                            bidsToReturn.size(), adapter, Lot49Constants.BID_OUTCOME_SUBMITTED);
        }
        if (shouldSaveUserAttributes) {
            ServiceRunner.getInstance().getUserAttributesCacheService().putAsync(modUid,
                            userAttributes);
        }
        if (bidsToReturn.size() > 0) {
            LogUtils.logRequest(req, true, candidates.size());
        }
        trace("Out of " + candidates.size() + ": " + candsPassed.size() + " passed, " + failCnt
                        + " failed, " + timedOutCnt + " timed out, " + bidsToReturn.size()
                        + " bids.");
    }

    private final String getPrefix() {
        return "BidCandidateManager(" + bReq.getId() + "," + bReq.getLot49Ext().getModUid() + "): ";
    }

    private void trace(String s) {
        LogUtils.trace(getPrefix() + s);
    }

}
