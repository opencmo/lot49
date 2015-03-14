package com.enremmeta.rtb.proto.testexchange;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.spotxchange.ExchangeAdapterImpl;

public class Lot49InternalAuctionExchangeAdapter
                extends ExchangeAdapterImpl<OpenRtbRequest, OpenRtbResponse> {

    @Override
    public String getResponseMediaType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return Lot49Constants.EXCHANGE_LOT49_INTERNAL_AUCTION;
    }

    @Override
    public String getWinningPriceMacro() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean localUserMapping() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OpenRtbRequest convertRequest(OpenRtbRequest req) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public OpenRtbResponse convertResponse(OpenRtbRequest req, OpenRtbResponse resp)
                    throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClickMacro() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClickEncMacro() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getName();
    }

}
