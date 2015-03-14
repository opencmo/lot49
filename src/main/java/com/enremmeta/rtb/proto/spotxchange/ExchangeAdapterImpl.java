package com.enremmeta.rtb.proto.spotxchange;

import com.enremmeta.rtb.proto.ExchangeAdapter;

/**
 * For the sake of {@link #toString()}.
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public abstract class ExchangeAdapterImpl<RequestType extends Object, ResponseType extends Object>
                implements ExchangeAdapter<RequestType, ResponseType> {

    public ExchangeAdapterImpl() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public String toString() {
        return "ExchangeAdapter for " + getName() + ", aggregator: " + isAggregator()
                        + "; NURL required: " + isNurlRequired()
                        + "; true win on NUrl or impression: " + trueWinOnNurlOrImpression()
                        + "; default timeout: " + getDefaultTimeout() + "; local user mapping: "
                        + localUserMapping() + "; response media type: " + getResponseMediaType()
                        + "; winning price macro: " + getWinningPriceMacro() + "; click macro: "
                        + getClickMacro();
    }

}
