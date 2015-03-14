package com.enremmeta.rtb.proto.testexchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.spotxchange.ExchangeAdapterImpl;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;

/**
 * Adapter for a Test exchange, useful for various internal testing.
 * 
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class Test1ExchangeAdapter extends ExchangeAdapterImpl<OpenRtbRequest, OpenRtbResponse> {

    @Override
    public String getSampleWinningPrice() {
        return "12.34";
    }

    public Test1ExchangeAdapter() {
        super();
    }

    public ResponseBuilder setHeaders(OpenRtbResponse bResp, ResponseBuilder builder) {
        ResponseBuilder retval = builder;
        for (SeatBid sb : bResp.getSeatbid()) {
            for (Bid bid : sb.getBid()) {
                String absNurl = bid.getNurl();
                String relNurl = absNurl.replace(
                                ServiceRunner.getInstance().getConfig().getStatsUrl() + "/", "");
                retval = builder.header("X-Lot49-Nurl", relNurl);
            }
        }
        return retval;
    }

    @Override
    public final long getDefaultTimeout() {
        return 100;
    }

    @Override
    public final String getWinningPriceMacro() {
        return "%%${AUCTION_PRICE}%%";
    }

    @Override
    public final OpenRtbRequest convertRequest(final OpenRtbRequest req) throws Throwable {
        final Lot49Ext lot49Ext = req.getLot49Ext();
        lot49Ext.setAdapter(this);
        lot49Ext.setLot49Test(true);

        final AdCache adCache = Bidder.getInstance().getAdCache();

        List<Ad> bid2 = new ArrayList<Ad>();
        bid2.addAll(Arrays.asList(adCache.getBid2()));
        List<Ad> all = new ArrayList<Ad>();
        all.addAll(Arrays.asList(adCache.getAll()));

        for (final Ad ad : adCache.getZeroBudgetAds()) {
            ad.setBidsToMake(1000000);
            if (ad.needCanBid2()) {
                bid2.add(ad);
            }
            all.add(ad);
        }

        for (final Ad ad : adCache.getIneligibleAds()) {
            ad.setBidsToMake(1000000);
            if (ad.needCanBid2()) {
                bid2.add(ad);
            }
            all.add(ad);
        }

        LogUtils.debug("For test setting all ads to " + all);
        LogUtils.debug("For test setting bid2 ads to " + bid2);
        lot49Ext.setAll(all.toArray(adCache.getAll()));
        lot49Ext.setBid2(bid2.toArray(adCache.getBid2()));

        final User user = req.getUser();
        if (user != null) {
            String origBuyerUid = user.getBuyeruid();
            if (origBuyerUid != null) {
                lot49Ext.setReceivedBuyerUid(origBuyerUid);

                if (origBuyerUid.length() == Lot49Constants.MOD_UID_COOKIE_LENGTH_MIN - 2
                                || origBuyerUid.length() == Lot49Constants.MOD_UID_COOKIE_LENGTH_MAX
                                                - 2) {
                    // Will assume it's missing trailing ==, as we have seen in
                    // logs
                    origBuyerUid += "==";
                }
                final String decodedBuyerUid = Utils.cookieToLogModUid(origBuyerUid);
                user.setBuyeruid(decodedBuyerUid);
            }
        }

        final Map ext = req.getExt();
        if (ext != null) {
            final String ssp = (String) ext.get("ssp");
            lot49Ext.setSsp(ssp);
        }
        return req;
    }

    public String getName() {
        return Lot49Constants.EXCHANGE_TEST1;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {
        final Double winningPriceCpm = 100.;

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
    public boolean trueWinOnNurlOrImpression() {
        return true;
    }

    public static final String RESPONSE_MEDIA_TYPE = MediaType.APPLICATION_JSON;

    @Override
    public String getResponseMediaType() {
        return RESPONSE_MEDIA_TYPE;
    }

    @Override
    public boolean localUserMapping() {
        return false;
    }

    @Override
    public OpenRtbResponse convertResponse(OpenRtbRequest req, OpenRtbResponse resp)
                    throws Throwable {
        return resp;
    }
}
