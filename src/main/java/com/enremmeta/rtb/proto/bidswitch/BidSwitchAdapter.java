package com.enremmeta.rtb.proto.bidswitch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.Lot49RuntimeException;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.PMP;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.spotxchange.ExchangeAdapterImpl;
import com.enremmeta.util.Utils;

/**
 * Adapter for BidSwitch.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 * 
 *         </p>
 */
public class BidSwitchAdapter extends ExchangeAdapterImpl<OpenRtbRequest, OpenRtbResponse> {

    private final String seatId;

    @Override
    public String getSampleWinningPrice() {
        return "12.37";
    }

    public BidSwitchAdapter(BidSwitchConfig bsConf) {
        super();
        this.config = bsConf;
        if (config == null) {
            throw new Lot49RuntimeException("Expected BidSwitch configuration object.");
        }
        this.seatId = config.getSeatId();
        if (this.seatId == null) {
            throw new Lot49RuntimeException(
                            "Expected seatId object in BidSwitch configuration object.");
        }
    }

    public BidSwitchAdapter() {
        super();
        final Lot49Config conf = Bidder.getInstance().getConfig();
        final ExchangesConfig exchConf = conf.getExchanges();
        if (exchConf == null) {
            throw new Lot49RuntimeException("Expected ExchangeConfiguration object.");
        }
        config = exchConf.getBidswitch();
        if (config == null) {
            throw new Lot49RuntimeException("Expected BidSwitch configuration object.");
        }
        this.seatId = config.getSeatId();
        if (this.seatId == null) {
            throw new Lot49RuntimeException(
                            "Expected seatId object in BidSwitch configuration object.");
        }
    }

    private final BidSwitchConfig config;

    @Override
    public final boolean isAggregator() {
        return true;
    }

    @Override
    public final long getDefaultTimeout() {
        return 100;
    }

    @Override
    public final String getWinningPriceMacro() {
        return "${AUCTION_PRICE}";
    }

    @Override
    public final boolean localUserMapping() {
        return false;
    }

    @Override
    public final OpenRtbRequest convertRequest(final OpenRtbRequest req) throws Throwable {
        final Lot49Ext lot49Ext = req.getLot49Ext();
        lot49Ext.setAdapter(this);

        final User user = req.getUser();
        if (user != null) {
            parseUserId(user.getBuyeruid(), req);
        }

        Site site = req.getSite();
        if (site != null) {
            String domain = site.getDomain();
            if (domain != null) {
                if (domain.startsWith("www.")) {
                    domain = domain.substring(4);
                    site.setDomain(domain);
                }
            } else {
                String page = site.getPage();
                if (page != null && page.trim().length() > 0) {
                    try {
                        if (page.startsWith("//")) {
                            page = "http:" + page;
                        } else if (!page.startsWith("http://") && !page.startsWith("https://")) {
                            page = "http://" + page;
                        }

                        domain = new URL(page).getHost();
                        if (domain.startsWith("www.")) {
                            domain = domain.substring(4);
                        }
                        site.setDomain(domain);

                    } catch (MalformedURLException e) {
                        if (!Utils.validateDomain(page)) {
                            trace("Malformed URL " + page + " in request");
                        }
                    }
                }
            }
        }

        final Map ext = req.getExt();
        if (ext != null) {
            final String ssp = (String) ext.get("ssp");
            lot49Ext.setSsp(ssp);
        }

        PMP pmp = req.getPmp();
        if (pmp != null) {
            for (Impression imp : req.getImp()) {
                PMP impPmp = imp.getPmp();
                if (impPmp == null) {
                    imp.setPmp(pmp);
                }
            }
        }
        return req;
    }

    public String getName() {
        return Lot49Constants.EXCHANGE_BIDSWITCH;
    }

    @Override
    public String getPartnerInitiatedSyncUrl(String myUserId) {
        return "http://x.bidswitch.net/sync?dsp_id=89&expires=" + this.seatId + "&user_id="
                        + myUserId;
    }

    @Override
    public OpenRtbResponse convertResponse(final OpenRtbRequest req, final OpenRtbResponse resp)
                    throws Exception {
        Map ext = resp.getExt();
        if (ext == null) {
            ext = new HashMap();
            resp.setExt(ext);
        }
        ext.put("protocol", "2.6");

        final List<String> wseat = req.getWseat();

        if (wseat != null && wseat.size() > 0) {
            for (final SeatBid seatBid : resp.getSeatbid()) {
                seatBid.setSeat(wseat.get(0));
            }
        }

        return resp;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {
        final Double winningPriceCpm = Double.parseDouble(winningPriceString);

        final long wpMicros = Utils.cpmToMicro(winningPriceCpm);
        ParsedPriceInfo ppi = new ParsedPriceInfo(winningPriceCpm, wpMicros, bidMicros);
        return ppi;
    }

    @Override
    public boolean isNurlRequired() {
        return true;
    }

    @Override
    public boolean isMacrosInNurl() {
        return true;
    }

    @Override
    public String getClickMacro() {
        return "${CLICK_URL}";
    }

    @Override
    public String getClickEncMacro() {
        return "${CLICK_URL:URLENCODE}";
    }

    @Override
    public final String getSeat(Ad ad) {
        return ad.getAdvertiserId();
    }

    @Override
    public boolean trueWinOnNurlOrImpression() {
        return true;
    }

    public static final String RESPONSE_MEDIA_TYPE = MediaType.APPLICATION_JSON;

    @Override
    public String getResponseMediaType() {
        return RESPONSE_MEDIA_TYPE;
    }
}
