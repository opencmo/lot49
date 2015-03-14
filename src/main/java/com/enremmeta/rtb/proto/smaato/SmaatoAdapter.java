package com.enremmeta.rtb.proto.smaato;

import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ParsedPriceInfo;

/**
 * Adapter for Smaato.
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class SmaatoAdapter implements ExchangeAdapter<OpenRtbRequest, OpenRtbResponse> {

    @Override
    public String getResponseMediaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getWinningPriceMacro() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean localUserMapping() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public OpenRtbRequest convertRequest(OpenRtbRequest request) throws Throwable {
        return request;
    }

    @Override
    public OpenRtbResponse convertResponse(OpenRtbRequest req, OpenRtbResponse o) throws Throwable {
        return o;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getClickMacro() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getClickEncMacro() {
        // TODO Auto-generated method stub
        return null;
    }

}
