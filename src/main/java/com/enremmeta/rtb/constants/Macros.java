package com.enremmeta.rtb.constants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import com.enremmeta.rtb.api.TagImpl;
import com.enremmeta.rtb.jersey.StatsSvc;
import com.enremmeta.rtb.proto.ExchangeAdapter;

/**
 * Macros to be substituted in
 * {@link TagImpl#getTag(com.enremmeta.rtb.proto.openrtb.OpenRtbRequest, com.enremmeta.rtb.proto.openrtb.Impression, com.enremmeta.rtb.proto.openrtb.Bid, boolean)}
 * .
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public interface Macros extends Lot49Constants {

    /**
     * Is substituted with the URL to Lot49
     * {@link StatsSvc#impression(UriInfo, String, String, String, String, String, String, String, String, String, long, String, int, String, String, String, String, String, String, String, HttpServletRequest, String, javax.ws.rs.core.HttpHeaders, String, String)
     * impression handler}. It will automatically include the appropriate exchange's
     * {@link ExchangeAdapter#getWinningPriceMacro() winning price macro}.
     */
    public static final String MACRO_LOT49_IMPRESSION = "__LOT49_IMPRESSION__";

    public static final String MACRO_LOT49_IMPRESSION_ENC = "__LOT49_IMPRESSION_ENC__";

    public static final String MACRO_LOT49_PROUST_REST = "__LOT49_PROUST_REST__";
    public static final String MACRO_LOT49_PROUST_SYNC = "__LOT49_PROUST_SYNC__";

    /**
     * Is substituted with the URL to Lot49
     * {@link StatsSvc#click(javax.ws.rs.core.UriInfo, String, String, String, String, String, String, String, String, String, String, String, String, javax.servlet.http.HttpServletRequest, String, String)
     * click handler}. The appropriate exchange-specific macro is automatically included and placed
     * in the right place in the expanded URL <b>unless</b> it is explicitly specified elsewhere in
     * the tag template.
     */
    public static final String MACRO_LOT49_CLICK = "__LOT49_CLICK__";

    /**
     * Like {@link #MACRO_LOT49_CLICK}, but encoded.
     */
    public static final String MACRO_LOT49_CLICK_ENC = "__LOT49_CLICK_ENC__";

    /**
     * Like {@link #MACRO_LOT49_CLICK_ENC} but encoded yet again.
     */
    public static final String MACRO_LOT49_CLICK_ENC_ENC = "__LOT49_CLICK_ENC_ENC__";

    /**
     * See
     * {@link com.enremmeta.rtb.api.Tag#getTag(com.enremmeta.rtb.proto.openrtb.OpenRtbRequest, com.enremmeta.rtb.proto.openrtb.Impression, com.enremmeta.rtb.proto.openrtb.Bid, boolean)}
     * for substitution rules.
     */
    public static final String MACRO_LOT49_CLICK_CHAIN_ENC = "__LOT49_CLICK_CHAIN_ENC__";

    /**
     * Is substituted with the appropriate encoded click macro from the exchange.
     * <p>
     * If present in the
     * {@link com.enremmeta.rtb.api.Tag#getTagTemplate(com.enremmeta.rtb.proto.openrtb.OpenRtbRequest, com.enremmeta.rtb.proto.openrtb.Impression, com.enremmeta.rtb.proto.openrtb.Bid)
     * tag template}, any of the instances of {@link #MACRO_LOT49_CLICK}/
     * {@link #MACRO_LOT49_CLICK_ENC} will not automatically include this.
     * </p>
     */
    public static final String MACRO_LOT49_EXCHANGE_CLICK_ENC = "__LOT49_EXCHANGE_CLICK_ENC__";

    /**
     * Is substituted with the appropriate click macro from the exchange.
     */
    public static final String MACRO_LOT49_EXCHANGE_CLICK = "__LOT49_EXCHANGE_CLICK__";

    public static final String MACRO_LOT49_VAST_EXTENSIONS = "<Extensions></Extensions>";

}
