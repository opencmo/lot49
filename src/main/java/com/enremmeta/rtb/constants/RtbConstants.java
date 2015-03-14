package com.enremmeta.rtb.constants;

import com.enremmeta.rtb.proto.brx.BrxRtb095.Api;

/**
 * Useful constants in RTB. This class only defines those constants that are found in documentation,
 * whether OpenRTB or exchange-specific. Any constant that is defined by Lot49 project is in
 * {@link Lot49Constants}.
 * 
 * @see IABCategories
 * 
 * @see Lot49Constants
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 * 
 *
 */
public interface RtbConstants {
    /**
     * Linear ad - see OpenRTB specification.
     */
    public static final int LINEARITY_LINEAR = 1;

    /**
     * Non-linear ad - see OpenRTB specification.
     */
    public static final int LINEARITY_NON_LINEAR = 2;

    /**
     * Section
     */
    public static final int API_VPAID_1 = 1;
    public static final int API_VPAID_2 = 2;
    public static final int API_MRAID = 3;
    public static final int API_ORMMA = 4;

    // BRX???
    public static final int BR_HTML5_1_0_VALUE = Api.BR_HTML5_1_0_VALUE;
    public static final int BR_HTML5_2_0_VALUE = Api.BR_HTML5_2_0_VALUE;

    /**
     * Section 6.7 of http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf
     */
    public static final int VIDEO_PROTOCOL_VAST_1 = 1;
    public static final int VIDEO_PROTOCOL_VAST_2 = 2;
    public static final int VIDEO_PROTOCOL_VAST_3 = 3;
    public static final int VIDEO_PROTOCOL_VAST_WRAPPER_1 = 4;
    public static final int VIDEO_PROTOCOL_VAST_WRAPPER_2 = 5;
    public static final int VIDEO_PROTOCOL_VAST_WRAPPER_3 = 6;

    /**
     * Section 6.8 of http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf
     */
    public static final int VIDEO_PLAYBACK_AUTO_SOUND_ON = 1;
    public static final int VIDEO_PLAYBACK_AUTO_SOUND_OFF = 2;
    public static final int VIDEO_PLAYBACK_CLICK_TO_PLAY = 3;
    public static final int VIDEO_PLAYBACK_MOUSEOVER = 4;

    /**
     * Section 6.10 of http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf
     */
    public static final int CONNECTION_TYPE_UNKNOWN = 0;

    public static final int CONNECTION_TYPE_ETHERNET = 1;
    public static final int CONNECTION_TYPE_WIFI = 2;
    public static final int CONNECTION_TYPE_CELL_UNKNOWN = 3;
    public static final int CONNECTION_TYPE_CELL_2G = 4;
    public static final int CONNECTION_TYPE_CELL_3G = 5;
    public static final int CONNECTION_TYPE_CELL_4G = 6;

    /**
     * Section 6.13 of http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf
     */
    public static final int CONTENT_CONTEXT_VIDEO = 1;
    public static final int CONTENT_CONTEXT_GAME = 2;
    public static final int CONTENT_CONTEXT_MUSIC = 3;
    public static final int CONTENT_CONTEXT_APP = 4;
    public static final int CONTENT_CONTEXT_TEXT = 5;
    public static final int CONTENT_CONTEXT_OTHER = 6;
    public static final int CONTENT_CONTEXT_UNKNOWN = 7;

    /**
     * Section 6.16 of http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf
     */
    public static final int DEVICE_TYPE_MOBILE_TABLET = 1;
    public static final int DEVICE_TYPE_PC = 2;
    public static final int DEVICE_TYPE_CONNECTED_TV = 3;
    public static final int DEVICE_TYPE_PHONE = 4;
    public static final int DEVICE_TYPE_TABLET = 5;
    public static final int DEVICE_TYPE_CONNECTED_DEVICE = 6;
    public static final int DEVICE_TYPE_STB = 7;

    /**
     * Based on OpenX: http://docs.openx.com/ad_exchange_adv/#rtb_user.html
     */
    public static final int ETHNICITY_AFRICAN_AMERICAN = 0;
    public static final int ETHNICITY_ASIAN = 1;
    public static final int ETHNICITY_HISPANIC = 2;
    public static final int ETHNICITY_WHITE = 3;
    public static final int ETHNICITY_OTHER = 4;

    public static final String MARITAL_DIVORCED = "D";
    public static final String MARITAL_MARRIED = "M";
    public static final String MARITAL_SINGLE = "S";

    /**
     * Not an RTB constant...
     */
    public static final int AUCTION_TYPE_UNKNOWN = 0;
    /**
     * See http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-3.pdf, 3.2.1
     */
    public static final int AUCTION_TYPE_FIRST_PRICE = 1;
    public static final int AUCTION_TYPE_SECOND_PRICE_PLUS = 2;
    public static final int AUCTION_TYPE_FIXED_PRICE = 3;
}
