package com.enremmeta.rtb.proto.testexchange;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.spotxchange.ExchangeAdapterImpl;

public class Test2ExchangeAdapter extends ExchangeAdapterImpl<OpenRtbRequest, OpenRtbResponse> {

    @Override
    public String getResponseMediaType() {
        return "text/html";
    }

    @Override
    public String getName() {
        return Lot49Constants.EXCHANGE_TEST2;
    }

    @Override
    public String getWinningPriceMacro() {
        return "{WP}";
    }

    @Override
    public boolean localUserMapping() {

        return false;
    }

    @Override
    public OpenRtbRequest convertRequest(OpenRtbRequest req) throws Throwable {
        return req;
    }

    @Override
    public OpenRtbResponse convertResponse(OpenRtbRequest req, OpenRtbResponse resp)
                    throws Throwable {
        return resp;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {
        return new ParsedPriceInfo(10, 10000, 20000);
    }

    @Override
    public String getClickMacro() {
        return "{CLICK}";
    }

    @Override
    public String getClickEncMacro() {
        return "{CLICK_ENC}";
    }

}
