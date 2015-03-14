package com.enremmeta.rtb.jersey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.enremmeta.rtb.BidCandidate;
import com.enremmeta.rtb.BidCandidateManager;
import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.PMP;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49CustomData;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49SubscriptionData;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchAdapter;
import com.enremmeta.rtb.proto.brx.BrxAdapter;
import com.enremmeta.rtb.proto.brx.BrxRtb095;
import com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Ext;
import com.enremmeta.rtb.proto.liverail.LiverailAdapter;
import com.enremmeta.rtb.proto.openx.OpenXAdapter;
import com.enremmeta.rtb.proto.pubmatic.PubmaticAdapter;
import com.enremmeta.rtb.proto.smaato.SmaatoAdapter;
import com.enremmeta.rtb.proto.spotxchange.SpotXChangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Test1ExchangeAdapter;
import com.enremmeta.rtb.spi.providers.Provider;
import com.enremmeta.rtb.spi.providers.ProviderInfoReceived;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.google.protos.adx.NetworkBid;

/**
 * Class providing most services for REST auction requests (using Jersey).
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 *
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 */
@Path(Lot49Constants.ROOT_PATH_AUCTIONS)
public class AuctionsSvc implements JerseySvc, Lot49Constants {

    private static Bid getBid(Ad ad, OpenRtbRequest req) {
        final List<Impression> imps = req.getImp();

        final List<Bid> potentialBids = new ArrayList<Bid>(ad.getTags().size());
        if (imps == null || imps.size() == 0) {
            ad.incrOptout(req, Lot49Constants.DECISION_NO_IMPRESSIONS);
            LogUtils.debug("No impressions in " + req.getId());
            return null;
        }

        // Here we'll just quit as long as we have a bid for one impression.
        for (final Impression imp : imps) {

            // 02.03. Private deal
            PMP pmp = imp.getPmp();

            String matchedDeal = null;
            if (ad.isTargetingDeals()) {
                matchedDeal = ad.matchDeals(req, imp);
                if (matchedDeal == null) {
                    continue;
                }
            } else if (pmp != null && pmp.getDeals() != null && pmp.getDeals().size() > 0) {
                ad.incrOptout(req, Lot49Constants.DECISION_PRIVATE_DEAL);
                req.getLot49Ext().getOptoutReasons().put(ad.getId(),
                                "Request is for a private deal");
                continue;

            } else {
                String cur = imp.getBidfloorcur();
                if (cur == null) {
                    cur = "usd";
                }
                if (cur.trim().equals("")) {
                    cur = "usd";
                }
                if (!cur.toLowerCase().equals("usd")) {
                    ad.incrOptout(req, Lot49Constants.DECISION_FLOOR);
                    req.getLot49Ext().getOptoutReasons().put(ad.getId(), "Bid floor currency " + cur

                                    + " at impression " + imp.getId());
                    continue;
                }

                final Float floor = imp.getBidfloor();

                if (!ad.isDynamicPricing() && floor != null) {
                    double cpmBid = ad.getBidPriceCpm(req);
                    if (floor > cpmBid) {

                        ad.incrOptout(req, Lot49Constants.DECISION_FLOOR);
                        req.getLot49Ext().getOptoutReasons().put(ad.getId(), "Bid floor " + floor
                                        + ">" + cpmBid + " at impression " + imp.getId());
                        continue;
                    }
                }

            }
            StringBuilder reasons = new StringBuilder(0);

            final List<Tag> tags = ad.getTags();
            for (Tag tag : tags) {
                String reasonNotOk = tag.canBid(req, imp);
                if (reasonNotOk != null) {
                    LogUtils.trace("Tag " + tag + " not bidding on " + req.getId() + " " + req + " "
                                    + imp + " because " + reasonNotOk);
                }
                if (reasonNotOk == null) {
                    Bid bid = tag.getBid(req, imp);
                    if (ad.isTargetingDeals()) {
                        LogUtils.debug("PMP_" + ad.getId() + " " + req.getId() + " Bidding!");
                        bid.setDealid(matchedDeal);
                    }
                    potentialBids.add(bid);
                } else {

                    if (ad.isTargetingDeals()) {
                        LogUtils.debug("PMP_" + ad.getId() + " " + req.getId() + ": " + reasons);
                    }

                    if (reasons.length() > 0) {
                        reasons.append("; ");
                    }
                    reasons.append(tag.getId()).append(": ").append(reasonNotOk);
                }
            }
            if (potentialBids.size() == 0) {
                ad.incrOptout(req, Lot49Constants.DECISION_TAG);
                req.getLot49Ext().getOptoutReasons().put(ad.getId(),
                                "Out of " + tags.size() + " tags: " + reasons.toString());
            } else {
                break;
            }
        }

        if (potentialBids.size() == 0)

        {
            return null;
        }

        final int bidIdx =
                        potentialBids.size() == 1 ? 0 : Utils.RANDOM.nextInt(potentialBids.size());

        final Bid bid = potentialBids.get(bidIdx);
        return bid;

    }

    /**
     * Main entry point (after the appropriate {@link ExchangeAdapter} has done its thing). This is
     * where most of logic occurs. The logic is as follows:
     * <ol>
     * </ol>
     */

    final static void onBidRequestDelegate(final JerseySvc instance, final String debug,
                    final ExchangeAdapter exchangeAdapter, final AsyncResponse response,
                    final OpenRtbRequest req, final String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip) {
        try {
            boolean isDebug = Utils.isTrue(debug);
            if (isDebug) {
                LogUtils.warn("Request with debug sent");
                req.getLot49Ext().setDebug(true);
            }

            final long enterTime = BidderCalendar.getInstance().currentTimeMillis();
            final long timeout = (req.getTmax() > 0 ? req.getTmax()
                            : exchangeAdapter.getDefaultTimeout()) - TIMEOUT_THRESHOLD;

            exchangeAdapter.fillPlatformInfoFromUa(req);
            final OpenRtbResponse resp = new OpenRtbResponse();

            final Device dev = req.getDevice();
            final Lot49Ext lot49Ext = req.getLot49Ext();

            // set permissions for clients
            lot49Ext.setSubscriptionData(Bidder.getInstance().getConfig().getSubscriptionData());

            if (dev != null) {
                lot49Ext.setGeo(Bidder.getInstance().getMaxMind().getGeo(dev.getIp()));
            }

            final Map<String, Provider> providers = Bidder.getInstance().getProviders();

            for (final String providerName : providers.keySet()) {
                final Provider provider = providers.get(providerName);
                if (!provider.isEnabled()) {
                    continue;
                }
                final ProviderInfoReceived info = provider.getProviderInfo(req);
                if (info != null) {
                    lot49Ext.getProviderInfo().put(provider.getName(), info);
                }
            }

            lot49Ext.setxForwardedFor(xff);
            lot49Ext.setRealIpHeader(xrip);
            lot49Ext.setRemoteHost(servletRequest.getRemoteHost());
            lot49Ext.setRemotePort(servletRequest.getRemotePort());
            lot49Ext.setRemoteAddr(servletRequest.getRemoteAddr());

            lot49Ext.setNoBid(true);

            final AdCache tCache = Bidder.getInstance().getAdCache();
            final boolean lot49Test = lot49Ext.isLot49Test();

            if (lot49Test) {
                LogUtils.trace(req.getId()
                                + ": This is a Lot49 test! This is just a test! Had this been a real emergency, I would have yelled and ran out of the studio");
            }

            final Ad[] all = lot49Test ? lot49Ext.getAll() : tCache.getAll();
            boolean needUserInfo = false;
            boolean needIntegralInfo = false;
            boolean needExperimentData = false;
            boolean needFrequencyCapData = false;

            if (all == null || all.length == 0) {
                lot49Ext.getOptoutReasons().put("ALL", "No ads here -- refreshing ad cache.");
                LogUtils.trace("No ads here -- refreshing ad cache.");
                LogUtils.logRequest(req, false, 0);
                response.resume(exchangeAdapter.getOptoutBuilder(req)
                                .header("x-lot49-optout", tCache.getStatus()).build());
                return;
            }

            resp.setId(req.getId());

            final String bidResponseId = ServiceRunner.getInstance().getNextId();
            resp.setBidid(bidResponseId);
            resp.setCur("USD");

            final SeatBid seatBid = new SeatBid();
            resp.getSeatbid().add(seatBid);

            final List<Bid> bids = new ArrayList<Bid>();
            seatBid.setBid(bids);

            final BidCandidateManager bcMgr = new BidCandidateManager(req, resp, response, timeout);
            String userId = null;

            userId = req.getUser().getBuyeruid();
            String modUid = lot49Ext.getModUid();

            if (userId == null || userId.length() == 0) {
                lot49Ext.setForceCookieResync(true);
            }

            final ScheduledExecutorService scheduledExecutor =
                            Bidder.getInstance().getScheduledExecutor();

            String cookieModUidMsg = "UserId: Cookie " + userId + " and mod_uid " + modUid;
            if ((userId != null && modUid == null) || (userId == null && modUid != null)) {
                LogUtils.warn(cookieModUidMsg);
            }

            for (final Ad ad : all) {
                if (req.getImp() == null || req.getImp().size() == 0) {
                    LogUtils.error("No impressions in " + req.getId());
                    ResponseBuilder optoutBuilder = exchangeAdapter.getOptoutBuilder(req);
                    response.resume(optoutBuilder.header("x-lot49-optout", "No impressions")
                                    .build());
                    return;
                }
                long impCount = req.getImp().size();
                if (impCount > 1) {
                    LogUtils.debug("For bid request " + req.getId() + ", impression count: "
                                    + impCount);
                }
                ad.incrRequestCount(exchangeAdapter.getName(), impCount);

                try {

                    if (modUid == null && ad.needCanBid2()) {
                        // Cannot do lookup
                        ad.incrOptout(req, Lot49Constants.DECISION_USER_UNKNOWN, impCount);
                        lot49Ext.getOptoutReasons().put(ad.getId(), cookieModUidMsg);
                        continue;
                    }

                    if (!ad.canBid1(req)) {
                        continue;
                    }
                    Bid bid = getBid(ad, req);
                    if (bid == null) {
                        // Nothing to bid..
                        continue;
                    }

                    // 4. Budget check
                    if (!ad.haveBidsToMake()) {
                        ad.incrOptout(req, Lot49Constants.DECISION_PACING, impCount);
                        lot49Ext.getOptoutReasons().put(ad.getId(),
                                        "Bids to make remaining: " + ad.getRemainingBidsToMake());
                        continue;
                    }

                    if (!needUserInfo) {
                        needUserInfo = ad.needUserInfo();
                        if (needUserInfo) {
                            long beforeGetUserSegments =
                                            BidderCalendar.getInstance().currentTimeMillis();
                            LogUtils.trace(req.getId() + ": User Segments needed at least for "
                                            + ad);
                            bcMgr.requestUserSegments(modUid);
                            LogUtils.trace("Time to get User Segments Future for " + modUid + ": "
                                            + (BidderCalendar.getInstance().currentTimeMillis()
                                                            - beforeGetUserSegments));
                        }
                    }

                    if (!needIntegralInfo) {
                        needIntegralInfo = ad.needIntegralInfo() && (lot49Ext
                                        .getSubscriptionData() == null
                                        || lot49Ext.getSubscriptionData().isAllowedService(
                                                        ad.getClientId(),
                                                        Lot49SubscriptionData.Lot49SubscriptionServiceName.INTEGRAL));
                        if (needIntegralInfo) {
                            LogUtils.trace(req.getId() + ": Integral info needed at least for "
                                            + ad);
                            Site site = req.getSite();
                            if (site == null) {
                                ad.incrOptout(req, DECISION_INTEGRAL_URL, impCount);
                                req.getLot49Ext().getOptoutReasons().put(req.getId(),
                                                "No Site object received, but targeting integral specified.");
                                continue;
                            }

                            String url = site.getPage();
                            if (url == null) {
                                ad.incrOptout(req, DECISION_INTEGRAL_URL, impCount);
                                req.getLot49Ext().getOptoutReasons().put(req.getId(),
                                                "No domain received, but targeting integral specified.");
                                continue;
                            }
                            bcMgr.requestIntegralInfo(url);
                        }
                    }
                    if (modUid != null) {
                        if (!needExperimentData) {
                            needExperimentData = ad.needExperimentInfo();
                            if (needExperimentData) {
                                bcMgr.putUnderExperiment();
                                bcMgr.requestUserAttributes(modUid);
                                LogUtils.trace(req.getId()
                                                + ": Experiment Data needed at least for " + ad);
                            } else if (!needFrequencyCapData) {
                                needFrequencyCapData = ad.needFrequencyCap();
                                if (needFrequencyCapData) {
                                    bcMgr.requestUserAttributes(modUid);
                                    LogUtils.trace(req.getId()
                                                    + ": Frequency Cap needed at least for " + ad);
                                }
                            }
                        }


                    }

                    BidCandidate bc = new BidCandidate(bcMgr, ad, bid);
                    bcMgr.add(bc);
                } catch (Throwable adThrowable) {
                    LogUtils.error("Error evaluating ad " + ad, adThrowable);
                    ad.incrOptout(req, Lot49Constants.DECISION_EVALUATION_ERROR, impCount);
                }
            }

            LogUtils.trace("For " + req.getId() + ", considered " + Arrays.asList(all)
                            + ", candidates: " + bcMgr.getCandidates());

            if (bcMgr.getCandidateCount() > 0) {
                final long scheduleTime = BidderCalendar.getInstance().currentTimeMillis();
                @SuppressWarnings("unused")
                final long timeBeforeSchedule = scheduleTime - enterTime;

                @SuppressWarnings("rawtypes")
                final long delay = needUserInfo || needIntegralInfo || needExperimentData
                                || needFrequencyCapData ? 10 : 1;
                final ScheduledFuture bcmFuture = scheduledExecutor.scheduleWithFixedDelay(bcMgr,
                                delay, delay, TimeUnit.MILLISECONDS);
                bcMgr.setFutureSelf(bcmFuture);

            } else {
                LogUtils.trace(req.getId() + ": No eligible ads.");
                LogUtils.logRequest(req, false, 0);
                ResponseBuilder optoutBuilder = exchangeAdapter.getOptoutBuilder(req);
                optoutBuilder = optoutBuilder.header("x-lot49-optout", "No-one-eligible");
                optoutBuilder = Utils.addOptoutHeaders(lot49Ext, optoutBuilder);
                response.resume(optoutBuilder.build());
            }

        } catch (final Throwable t) {
            LogUtils.error("Error in AuctionSvc.onBidRequestDelegate()", t);
            response.resume(Response.serverError().build());

        }
    }

    private final static long TIMEOUT_THRESHOLD =
                    Bidder.getInstance().getConfig().getTimeoutThresholdMillis();

    protected final boolean isTimeout(final ExchangeAdapter exchangeAdapter,
                    final OpenRtbRequest req) {
        final long timePassed = BidderCalendar.getInstance().currentTimeMillis()
                        - req.getLot49Ext().getTimestamp();
        final long timeout = exchangeAdapter.getDefaultTimeout();
        final boolean isTimeout = timePassed > timeout - TIMEOUT_THRESHOLD;
        if (isTimeout) {
            LogUtils.debug("Bid timing out: " + timePassed + " passed out of " + timeout);
        }
        return isTimeout;
    }

    @POST
    @Path("adx_optout")
    @Produces("application/octet-stream")
    public void onFakeAdx(@Suspended final AsyncResponse response,
                    final @QueryParam("time") Integer time) {
        NetworkBid.BidResponse.Builder respBuilder = NetworkBid.BidResponse.newBuilder();
        respBuilder.setProcessingTimeMs(time);
        NetworkBid.BidResponse result = respBuilder.build();
        ResponseBuilder optoutBuilder = Response.ok(result);
        response.resume(optoutBuilder.build());
    }

    @POST
    @Path(Lot49Constants.EXCHANGE_ADX)
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public void onAdx(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    final NetworkBid.BidRequest req,
                    @HeaderParam("x-forwarded-for") final String xff,
                    final @QueryParam("debug") String debug,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip) {

        try {
            AdXAdapter adapter = new AdXAdapter();
            final OpenRtbRequest req2 = adapter.convertRequest(req);
            LogUtils.logRawRequest(req2, req, uriInfo);
            onBidRequestDelegate(this, debug, adapter, response, req2, xff, servletRequest, xrip);
        } catch (Throwable t) {
            LogUtils.error("Error parsing request from AdX: " + t.getMessage(), t);
            response.resume(Response.serverError().build());
            return;
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(Lot49Constants.EXCHANGE_ADAPTV)
    public void onAdaptv(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    final OpenRtbRequest req, @HeaderParam("x-forwarded-for") final String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("debug") String debug) {

        final AdaptvAdapter adapter = new AdaptvAdapter();
        final OpenRtbRequest req2 = adapter.convertRequest(req);
        LogUtils.logRawRequest(req2, req, uriInfo);
        onBidRequestDelegate(this, debug, adapter, response, req2, xff, servletRequest, xrip);
    }

    @POST
    @Path(Lot49Constants.EXCHANGE_LIVERAIL)
    // @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void onLiverail(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    final OpenRtbRequest req, final @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("debug") String debug) {

        try {
            final LiverailAdapter adapter = new LiverailAdapter();
            final OpenRtbRequest req2 = adapter.convertRequest(req);
            LogUtils.logRawRequest(req2, req, uriInfo);
            onBidRequestDelegate(this, debug, adapter, response, req2, xff, servletRequest, xrip);
        } catch (Throwable t) {
            LogUtils.error("Error parsing request from LiveRail: " + t.getMessage(), t);
            response.resume(Response.serverError().build());
            return;
        }

    }

    @POST
    @Path(Lot49Constants.EXCHANGE_PUBMATIC)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void onPubmatic(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    final OpenRtbRequest req, final @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("debug") String debug) {

        try {
            PubmaticAdapter adapter = new PubmaticAdapter();
            final OpenRtbRequest req2 = adapter.convertRequest(req);
            LogUtils.logRawRequest(req2, req, uriInfo);

            onBidRequestDelegate(this, debug, adapter, response, req2, xff, servletRequest, xrip);
        } catch (Throwable t) {
            LogUtils.error("Error parsing request from Pubmatic: " + t.getMessage(), t);
            response.resume(Response.serverError().build());
            return;
        }
    }

    @POST
    @Path(Lot49Constants.EXCHANGE_BRX)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void onBrx(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    BrxRtb095.BidRequest req1, @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("debug") String debug) {
        final Ext ext1 = req1.getExt();
        // Quickly check if just ping...
        if (ext1 != null && ext1.getIsPing()) {
            LogUtils.info("Replying to ping from BRX");
            response.resume(Response.status(Status.NO_CONTENT).header("x-lot49-optout", "Ping-pong")
                            .build());
            return;
        }
        try {

            final BrxAdapter adapter = new BrxAdapter();
            final OpenRtbRequest req2 = adapter.convertRequest(req1);
            LogUtils.logRawRequest(req2, req1, uriInfo);

            onBidRequestDelegate(this, debug, adapter, response, req2, xff, servletRequest, xrip);
        } catch (Throwable t) {
            LogUtils.error("Error parsing request from BrX: " + t.getMessage(), t);
            response.resume(Response.serverError().build());
            return;
        }
    }

    @POST
    @Path(Lot49Constants.EXCHANGE_SPOTXCHANGE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void onSpotX(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    final OpenRtbRequest req, final @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("debug") String debug) {

        final SpotXChangeAdapter adapter = new SpotXChangeAdapter();
        try {
            final OpenRtbRequest req2 = adapter.convertRequest(req);
            LogUtils.logRawRequest(req2, req, uriInfo);
            AuctionsSvc.onBidRequestDelegate(this, debug, adapter, response, req2, xff,
                            servletRequest, xrip);
        } catch (Throwable t) {
            LogUtils.error("Error parsing request from SpotXChange: " + t.getMessage(), t);
            response.resume(Response.serverError().build());
            return;
        }
    }

    /**
     * We actually keep our data stored in OpenX by using the RTB data URL mechanism (
     * http://docs.openx.com/ad_exchange_adv/#rtb_sync_userdata_rtbdataurl .html); and ignoring, for
     * now, cookie syncing.
     *
     * @see User#getCustomdata()
     * @see Lot49CustomData
     * @see <a href="http://docs.openx.com/ad_exchange_adv">OpenX documentation</a>.
     */
    @POST
    @Path(Lot49Constants.EXCHANGE_OPENX)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void onOpenX(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    OpenRtbRequest req, @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("debug") String debug) {

        OpenXAdapter adapter;
        OpenRtbRequest req2;
        try {
            adapter = new OpenXAdapter();
            req2 = adapter.convertRequest(req);
            LogUtils.logRawRequest(req2, req, uriInfo);
        } catch (Throwable t) {
            LogUtils.error("Error parsing request from OpenX: " + t.getMessage(), t);

            response.resume(Response.serverError().build());
            return;
        }

        onBidRequestDelegate(this, debug, adapter, response, req2, xff, servletRequest, xrip);
    }

    @POST
    @Path(Lot49Constants.EXCHANGE_TEST1)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void onTest(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    OpenRtbRequest req, @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip) {

        try {
            final Test1ExchangeAdapter adapter = new Test1ExchangeAdapter();
            req = adapter.convertRequest(req);
            onBidRequestDelegate(this, "true", adapter, response, req, xff, servletRequest, xrip);
        } catch (Throwable t) {
            LogUtils.error(t);
            response.resume(Response.serverError().build());
            return;
        }
    }

    @POST
    @Path(Lot49Constants.EXCHANGE_BIDSWITCH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void onBidSwitch(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    final OpenRtbRequest req, @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("debug") String debug) {

        final OpenRtbRequest req2;

        try {
            final BidSwitchAdapter adapter = new BidSwitchAdapter();
            req2 = adapter.convertRequest(req);
            LogUtils.logRawRequest(req2, req, uriInfo);

            onBidRequestDelegate(this, debug, adapter, response, req2, xff, servletRequest, xrip);
        } catch (Throwable t) {
            LogUtils.error("Error parsing request from Bidswitch: " + t.getMessage(), t);
            response.resume(Response.serverError().build());
            return;
        }
    }

    @POST
    @Path(Lot49Constants.EXCHANGE_SMAATO)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void onSmaato(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    OpenRtbRequest req, @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("debug") String debug) {

        OpenRtbRequest req2;

        try {
            final SmaatoAdapter adapter = new SmaatoAdapter();
            req2 = adapter.convertRequest(req);
            LogUtils.logRawRequest(req2, req, uriInfo);
            onBidRequestDelegate(this, debug, adapter, response, req2, xff, servletRequest, xrip);
        } catch (Throwable t) {
            LogUtils.error("Error parsing request from Smaato: " + t.getMessage(), t);
            response.resume(Response.serverError().build());
            return;
        }

    }
}
