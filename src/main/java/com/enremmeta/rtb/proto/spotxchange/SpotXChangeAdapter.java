package com.enremmeta.rtb.proto.spotxchange;

import javax.ws.rs.core.MediaType;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.util.Utils;

/**
 * SpotXChange adapter.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class SpotXChangeAdapter extends ExchangeAdapterImpl<OpenRtbRequest, OpenRtbResponse> {

    public SpotXChangeAdapter() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public BidChoiceAlgorithm getBidChoiceAlgorithm() {
        return BidChoiceAlgorithm.LRU;
    }

    @Override
    public final long getDefaultTimeout() {
        return 300;
    }

    @Override
    public final String getWinningPriceMacro() {
        return "$MBR";
    }

    @Override
    public boolean localUserMapping() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public OpenRtbRequest convertRequest(OpenRtbRequest req) {
        final User user = req.getUser();
        if (user != null) {
            parseUserId(user.getBuyeruid(), req);
        }
        req.getLot49Ext().setAdapter(this);
        return req;
    }

    @Override
    public String getName() {
        return Lot49Constants.EXCHANGE_SPOTXCHANGE;
    }

    @Override
    public String getSeat(Ad ad) {
        return "spotxseat";
    }

    @Override
    public OpenRtbResponse convertResponse(OpenRtbRequest req, OpenRtbResponse o) throws Exception {
        return o;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) {
        Double wpDouble = Double.valueOf(winningPriceString);
        wpDouble = wpDouble * Utils.microToCpm(bidMicros);
        final long wpMicro = Utils.cpmToMicro(wpDouble);
        return new ParsedPriceInfo();
    }

    @Override
    public String getClickMacro() {
        return "";
    }

    @Override
    public String getClickEncMacro() {
        return "";
    }

    public static final String RESPONSE_MEDIA_TYPE = MediaType.APPLICATION_JSON;

    @Override
    public String getResponseMediaType() {
        return RESPONSE_MEDIA_TYPE;
    }
}
