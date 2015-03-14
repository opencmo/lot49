package com.enremmeta.rtb.api;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;

public interface BidPriceCalculator extends Lot49Plugin {
    long getBidPrice(Ad ad, OpenRtbRequest req);

}
