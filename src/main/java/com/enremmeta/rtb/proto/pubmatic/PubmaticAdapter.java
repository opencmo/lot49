package com.enremmeta.rtb.proto.pubmatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.LostAuctionTask;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.jersey.StatsSvc;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.spotxchange.ExchangeAdapterImpl;
import com.enremmeta.util.Utils;

/**
 * Adapter for Pubmatic exchange.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class PubmaticAdapter extends ExchangeAdapterImpl<OpenRtbRequest, OpenRtbResponse> {
    private final PubmaticConfig config;

    private final String vcode;

    @Override
    public String getPartnerInitiatedSyncUrl(String myUserId) {
        return "https://image2.pubmatic.com/AdServer/Pug?vcode=" + vcode + "&piggybackCookie="
                        + myUserId;
    }

    public PubmaticAdapter() throws Lot49Exception {
        super();
        this.config = Bidder.getInstance().getConfig().getExchanges().getPubmatic();
        String vcodeTmp = this.config.getVcode();
        if (vcodeTmp == null) {
            throw new Lot49Exception("Missing required configuration field 'vcode'.");
        }
        vcodeTmp = vcodeTmp.trim();
        if (vcodeTmp.length() == 0) {
            throw new Lot49Exception("Missing required configuration field 'vcode'.");
        }
        vcode = vcodeTmp;
    }

    @Override
    public long getDefaultTimeout() {
        return 100;
    }

    @Override
    public String getWinningPriceMacro() {
        return "${AUCTION_PRICE}";
    }

    @Override
    public boolean localUserMapping() {
        return true;
    }

    @Override
    public boolean trueWinOnNurlOrImpression() {
        // Per conversation with them
        // [11/16/15, 6:01:18 PM] Mariam Parwez: the macro should be
        // ${AUCTION_PRICE} not {PUBMATIC_SECOND_PRICE} it should be the same as
        // our phase 1
        // [11/16/15, 6:01:45 PM] Mariam Parwez: are you not seeing any spend on
        // your side?
        // [11/16/15, 6:02:33 PM] Gregory Golberg: ah ok now I had another
        // question though… the win that comes in via wli and the impression -
        // would those be equivalent?
        // [11/16/15, 6:02:35 PM] Gregory Golberg: in other words
        // [11/16/15, 6:03:10 PM] Gregory Golberg: does it matter whether I
        // record the spend off the impression macro or off the wli extension ?
        // [11/16/15, 6:04:34 PM] Mariam Parwez: yeah you should record off
        // impression macro
        // [11/16/15, 6:05:20 PM] Mariam Parwez: i will have to double check but
        // i dont belive the wli will give you the bid price the winner paid--
        // it will give you the price they bid
        // [11/16/15, 6:05:37 PM] Mariam Parwez: but since these can be second
        // price auctions, the winner just pays 1 cent more than the 2nd highest
        // bid
        // [11/16/15, 6:08:50 PM] Mariam Parwez: if its not 2nd price auction,
        // which I tihnk vadim said was the case for your PMP deal
        // [11/16/15, 6:09:48 PM] Mariam Parwez: you should still calculate
        // spend off the impression macro
        // [11/16/15, 6:21:55 PM] Gregory Golberg: sorry not sure - I should add
        // a cent to the auction_price or auction_price is already the win
        // price?
        // [11/16/15, 6:22:02 PM] Gregory Golberg: (b/c we have both pmp and non
        // pmp
        // [11/16/15, 6:22:23 PM] Mariam Parwez: no you dont need to add a cent,
        // but you should not use WLI
        // [11/16/15, 6:22:33 PM] Mariam Parwez: you should use the impression
        // macro
        // [11/16/15, 6:22:40 PM] Gregory Golberg: aha
        // [11/16/15, 6:22:41 PM] Gregory Golberg: ok
        // [11/16/15, 6:22:44 PM] Gregory Golberg: and WLI is for what?
        // [11/16/15, 6:23:07 PM] Mariam Parwez: it will give you insight into
        // when your bid is too low what others are bidding on it
        // [11/16/15, 6:23:24 PM] Mariam Parwez: in case next time you want the
        // impression you will have a better chance of winning it
        // [11/16/15, 6:25:54 PM] Gregory Golberg: got it thanks!
        return true;
    }

    @Override
    public OpenRtbRequest convertRequest(OpenRtbRequest req) throws Throwable {
        Lot49Ext lot49Ext = req.getLot49Ext();
        lot49Ext.setAdapter(this);
        final User user = req.getUser();
        if (user != null) {
            parseUserId(user.getBuyeruid(), req);
        }
        return req;
    }

    private void parseWinLossInfo(OpenRtbRequest req) {
        Map ext = req.getExt();
        if (ext == null) {
            return;
        }

        Object wliMap = ext.get("wli");
        if (wliMap == null) {
            return;
        }

        PubmaticWinLossObject wli = null;
        try {
            wli = new PubmaticWinLossObject(wliMap);

        } catch (Lot49Exception e) {
            LogUtils.error("Error parsing wli", e);
            return;
        }
        LogUtils.info("Got WLI " + wli);
        String brId = wli.getrId();
        List<PubmaticBidObject> binfos = wli.getbInfo();

        if (binfos == null) {
            return;
        }
        BidInFlightInfo bif = null;
        for (final PubmaticBidObject binfo : binfos) {
            try {
                int status = binfo.getStatus();
                PubmaticWinLossStatus statusObj = PubmaticWinLossStatus.get(status);
                String statusStr = getName() + "_" + status + "_" + statusObj.name().toLowerCase();
                switch (statusObj) {
                    case WIN:
                        double wpCpm = wli.getwBid();
                        long wpMicros = Utils.cpmToMicro(wpCpm);
                        boolean trueWin = !trueWinOnNurlOrImpression();
                        boolean suspicious = false;
                        String pmBidId = null;
                        String myBidId = null;

                        if (trueWin) {
                            bif = StatsSvc.handleWinLossError("PubmaticWinLossStatus", this, null,
                                            brId, null, null, wpMicros);
                            if (bif == null) {
                                suspicious = true;
                            } else {
                                myBidId = bif.getBidId();
                                pmBidId = binfo.getbId();
                                if (pmBidId == null) {
                                    suspicious = true;
                                    LogUtils.error("Received null bid ID in WLI " + wli
                                                    + ", ours is " + myBidId);
                                } else {
                                    if (!pmBidId.equals(myBidId)) {
                                        LogUtils.error("Received bid ID in WLI " + wli + ": "
                                                        + pmBidId + ", ours is " + myBidId);

                                    }
                                }
                            }
                        }
                        LogUtils.logWin(req.getUser().getBuyeruid(), getName(),
                                        pmBidId == null ? pmBidId : myBidId,
                                        bif == null ? null : bif.getImpressionId(),
                                        bif == null ? null : bif.getAdId(),
                                        bif == null ? null : bif.getCreativeId(),
                                        String.valueOf(wpCpm), wpCpm, wpMicros,
                                        bif == null ? null : bif.getBidPriceMicros(),
                                        bif == null ? null : bif.getBidCreatedOnTimestamp(), null,
                                        null, null, null, null, null, null, 0l,
                                        req.getLot49Ext().getModUid(), null, trueWin, brId,
                                        getName(), null, null, null, null, false, suspicious,
                                        bif == null ? null : bif.getInstanceId());
                        break;
                    case TIMEOUT:
                    case INCOMPLETE:
                    case INVALID_BID_PRICE:
                    case ADOMAIN_BLOCKED:
                    case ADOMAIN_MISSING:
                    case CRID_MISSING:
                    case CREATIVE_BLOCKED:
                    case BID_PRICE_DECRYPTION_FAILED:
                    case FLOOR:
                    case REQUEST_ID_MISMATCH:
                    case SSL_EXPECTED:
                    case CURRENCY_INVALID:
                        bif = StatsSvc.handleWinLossError("PubmaticWinLossStatus", this, null, brId,
                                        null,
                                        LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_ERROR,
                                        null);
                        LogUtils.logLost(bif == null ? null : bif.getBidCreatedOnTimestamp(),
                                        bif == null ? null : bif.getBidId(),
                                        bif == null ? null : bif.getImpressionId(),
                                        bif == null ? null : bif.getCampaignId(),
                                        bif == null ? null : bif.getCreativeId(),
                                        bif == null ? null : bif.getBidPriceMicros(), null,
                                        LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_ERROR,
                                        getName(), statusStr, null, null, null, getName(), brId,
                                        bif == null ? null : bif.getInstanceId());

                        break;
                    case OUTBID:
                        statusStr = "outbid";
                        StatsSvc.handleWinLossError("PubmaticWinLossStatus", this, null, brId, null,
                                        LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_LOSS,
                                        null);
                        LogUtils.logLost(bif == null ? null : bif.getBidCreatedOnTimestamp(),
                                        bif == null ? null : bif.getBidId(),
                                        bif == null ? null : bif.getImpressionId(),
                                        bif == null ? null : bif.getCampaignId(),
                                        bif == null ? null : bif.getCreativeId(),
                                        bif == null ? null : bif.getBidPriceMicros(),
                                        Utils.cpmToMicro(wli.getwBid()),
                                        LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_LOSS,
                                        getName(), "outbid", null, null, null, getName(), brId,
                                        bif == null ? null : bif.getInstanceId());

                        break;

                    default:
                        LogUtils.error("Unknown status: " + status);
                }
            } catch (Lot49Exception e) {
                LogUtils.error("Error parsing wli", e);

            }

        }
    }

    @Override
    public String getClickMacro() {
        return "${AUCTION_CLICKTRACK_URL}";
    }

    @Override
    public String getClickEncMacro() {
        return "${AUCTION_CLICKTRACK_URL}";
    }

    @Override
    public OpenRtbResponse convertResponse(OpenRtbRequest req, OpenRtbResponse resp)
                    throws Throwable {
        return resp;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {
        Double wpCpm = Double.parseDouble(winningPriceString);
        long wpMicros = Utils.cpmToMicro(wpCpm);
        return new ParsedPriceInfo(wpCpm, wpMicros, bidMicros);
    }

    @Override
    public final String getSeat(Ad ad) {
        return ad.getAdvertiserId();
    }

    @Override
    public String getName() {
        return Lot49Constants.EXCHANGE_PUBMATIC;
    }

    /**
     * Specific requirements (from communications with Pubmatic):
     * <ul>
     * <li><tt>adomain</tt> must include only ONE domain</li>
     * <li><tt>iurl</tt> is required</li>
     * </ul>
     */
    @Override
    public List<String> validateAd(Ad ad) {
        final List<String> retval = new ArrayList<String>();
        final String prefix = "Ad " + ad.getId() + " not suited for " + getName() + ": ";
        final List<String> adomain = ad.getAdomain();
        if (adomain == null || adomain.size() != 1) {
            retval.add(prefix + "adomain must be present and include only one entry.");
        }

        final String iUrl = ad.getIurl();
        if (iUrl == null || !(iUrl.startsWith("http://") || iUrl.startsWith("https://"))) {
            retval.add(prefix + "iurl is required to be a valid URL, instead: " + iUrl);
        }

        if (retval.size() > 0) {
            return retval;
        }
        return null;

    }

    public static final String RESPONSE_MEDIA_TYPE = MediaType.APPLICATION_JSON;

    @Override
    public String getResponseMediaType() {
        return RESPONSE_MEDIA_TYPE;
    }

    /**
     * Default behavior is not OK here in case of private deal IDs.
     */
    @Override
    public ResponseBuilder getOptoutBuilder(OpenRtbRequest req) {
        if (req.getLot49Ext().isPrivateDeals()) {
            OpenRtbResponse resp = new OpenRtbResponse();
            Map<String, Integer> ext = new HashMap<String, Integer>();
            ext.put("dnbr", 1);
            resp.setExt(ext);
            ResponseBuilder builder = Response.ok(resp);
            return builder;
        } else {
            return Response.status(Status.NO_CONTENT).header("x-lot49-optout",
                            Bidder.getInstance().getAdCache().getStatus());
        }
    }

}
