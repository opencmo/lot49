package com.enremmeta.rtb.proto.liverail;

import javax.ws.rs.core.MediaType;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.Lot49RuntimeException;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.Macros;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.util.Utils;

public class LiverailAdapter implements ExchangeAdapter<OpenRtbRequest, OpenRtbResponse> {

    private final LiverailConfig config;
    private final String seatId;

    public LiverailAdapter() {
        super();
        final Lot49Config conf = Bidder.getInstance().getConfig();
        this.config = conf.getExchanges() == null ? new LiverailConfig()
                        : (conf.getExchanges().getLiverail() == null ? new LiverailConfig()
                                        : conf.getExchanges().getLiverail());
        this.seatId = config.getSeatId();
        if (this.seatId == null) {
            throw new Lot49RuntimeException("seatId is null");
        }

    }

    @Override
    public String getSeat(Ad ad) {
        return this.seatId;
    }

    @Override
    public String getResponseMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    @Override
    public String getName() {
        return Lot49Constants.EXCHANGE_LIVERAIL;
    }

    @Override
    public String getWinningPriceMacro() {
        return "$WINNING_PRICE";
    }

    @Override
    public boolean localUserMapping() {

        return false;
    }

    @Override
    public OpenRtbRequest convertRequest(OpenRtbRequest request) throws Throwable {
        request.getLot49Ext().setAdapter(this);
        return request;
    }

    public static final String EXTENSION_TEMPLATE_PART_1 =
                    "<Extensions><Extension type=\"LR-Pricing\"><Price model=\"CPM\" currency=\"USD\" source=\"Lot49\">";
    public static final String EXTENSION_TEMPLATE_PART_2 = "</Price></Extension></Extensions>";

    @Override
    public OpenRtbResponse convertResponse(OpenRtbRequest req, OpenRtbResponse resp)
                    throws Throwable {
        final Bid bid = resp.getSeatbid().get(0).getBid().get(0);

        final String vast = bid.getAdm();
        final String ext = EXTENSION_TEMPLATE_PART_1 + bid.getPrice() + EXTENSION_TEMPLATE_PART_2;
        final String lrVast = vast.replace(Macros.MACRO_LOT49_VAST_EXTENSIONS, ext);
        bid.setAdm(lrVast);
        return resp;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {
        double winPrice = bidMicros * Double.valueOf(winningPriceString);
        long winPriceLong = new Double(winPrice).longValue();
        return new ParsedPriceInfo(Utils.microToCpm(winPriceLong), winPriceLong, bidMicros);
    }

    @Override
    public String getClickMacro() {

        return null;
    }

    @Override
    public String getClickEncMacro() {

        return null;
    }

}
