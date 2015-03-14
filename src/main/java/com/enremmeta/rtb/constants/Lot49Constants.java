package com.enremmeta.rtb.constants;

import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.util.Utils;

/**
 * Constants for Lot49. Not sure if this class even needs to be here, as opposed to
 * {@link Lot49Config}. Everything that is standard should be in {@link RtbConstants}.
 * 
 * @see Lot49Config
 * 
 * @see RtbConstants
 * 
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *
 */
public interface Lot49Constants {


    String MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH = "application/x-shockwave-flash";
    String MEDIA_TYPE_APPLICATION_JAVASCRIPT = "application/x-javascript";
    String MEDIA_TYPE_VIDEO_FLV = "video/x-flv";
    String MEDIA_TYPE_VIDEO_MP4 = "video/mp4";
    public static final String LOT49_VERSION_KEY = "lot49version";
    public static final String LOT49_VERSION_VALUE = "v16";

    // Tag decisions
    String TAG_DECISION_SSL_REQUIRED = "SSL required";
    String TAG_DECISION_REQUEST_DISALLOWS_MARKUP = "Markup disallowed";
    String TAG_DECISION_NOT_VIDEO = "Not video";
    String TAG_DECISION_VIDEO_LINEARITY = "Linearity mismatch";
    String TAG_DECISION_DIMENSIONS = "Dimensions mismatch";
    String TAG_DECISION_DURATION = "Duration mismatch";
    String TAG_DECISION_MIME = "Mime mismatch";
    String TAG_DECISION_PROTOCOL = "Protocol mismatch";
    String TAG_DECISION_API = "API mismatch";
    String TAG_DECISION_NOT_BANNER = "Not banner";

    // Phase 1: Ad loading
    String DECISION_LOADING_ERROR = "01.01.Ad loading error";
    String DECISION_VALIDATION = "01.02.Validation";
    String DECISION_DATE = "01.03.Date";
    String DECISION_PROBABILITY = "01.04.Probability";
    String DECISION_BUDGET = "01.05.Budget";
    String DECISION_HOST = "01.06.Host";
    String DECISION_BIDS_NEEDED = "01.99.Bids needed";

    // Phase 2
    String DECISION_EVALUATION_ERROR = "02.01.Ad evaluation error";
    String DECISION_NO_IMPRESSIONS = "02.02.No impressions";
    String DECISION_USER_UNKNOWN = "02.03.User required but unknown";

    String DECISION_DOMAIN_REQUIRED = "02.04.Domain required";
    String DECISION_EXCHANGE = "02.05.Exchange";
    String DECISION_SSP = "02.06.SSP";
    String DECISION_DEVICE = "02.07.Device";
    String DECISION_PROVIDER = "02.08.Provider";
    String DECISION_OS = "02.09.OS";
    String DECISION_LANGUAGE = "02.10.Language";
    String DECISION_BROWSER = "02.11.Browser";
    String DECISION_GEO = "02.12.Geo";
    String DECISION_DOMAIN_UNMATCHED = "02.13.Domain not matched";
    String DECISION_URL = "02.14.URL";
    String DECISION_CATEGORY = "02.15.Contextual";
    String DECISION_HOUR = "02.16.Hour";
    String DECISION_DAY = "02.17.Day";

    // Phase 3
    String DECISION_FLOOR = "03.01.Floor";
    String DECISION_TAG = "03.02.Tag";
    String DECISION_PRIVATE_DEAL = "03.03.Private deal";

    // Phase 4
    String DECISION_PACING = "04.01.Pacing";

    // Phase 5
    String DECISION_WRONG_USER = "05.01.Wrong user";
    String DECISION_FREQ_CAP = "05.02.Frequency cap";
    String DECISION_INTEGRAL = "05.03.Integral general";
    String DECISION_INTEGRAL_URL = "05.04.Integral adsafe url needed";
    String DECISION_VIEWABILITY = "05.05.Integral Viewability";
    String DECISION_BRANDSAFETY = "05.06.Integral Brand safety";
    String DECISION_TRAQ = "05.07.Integral TRAQ Score";
    String DECISION_NO_USER = "05.08.User not in UserCache";
    String DECISION_IP_BLACKLISTED = "05.09.Device is null or IP is null or blacklisted";

    // Phase 6
    String DECISION_TIMEOUT_USERDATA = "06.01.Timeout User Data";
    String DECISION_TIMEOUT_INTEGRAL = "06.02.Timeout Integral";
    String DECISION_TIMEOUT_FC = "06.03.Timeout FC";
    String DECISION_TIMEOUT_EXPERIMENT_STATUS = "06.04.Timeout Experiment status";
    String DECISION_TIMEOUT_UNKNOWN = "06.05.General timeout";

    // Phase 7
    String DECISION_INTERNAL_AUCTION = "07.01.Internal auction";

    // Phase 8
    String DECISION_EXPERIMENT_CONTROL_SET = "08.01.User in Control Set";

    // Phase 9
    String DECISION_BIDS_MADE = "09.01.Bids made";
    String DECISION_BIDS_POSSIBLE = "09.02.Bids possible";

    String BID_OUTCOME_INTERNAL_AUCTION_LOSS = "internal auction loss";

    String BID_OUTCOME_CONTROL = "control";
    String BID_OUTCOME_SUBMITTED = "submitted";
    /**
     * How to pass our {@link Macros#MACRO_LOT49_CLICK} downstream in case
     * {@link ExchangeAdapter#isMacrosInNurl() macros are only passed after a NUrl} call.
     */
    public static final String QUERY_STRING_LOT49_CLICK_THROUGH_MACRO = "myct";

    /**
     * @see Tag#getNUrl(com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest,
     *      com.enremmeta.rtb.api.proto.openrtb.Bid, String)
     */
    public static final String QUERY_STRING_EXCHANGE_CLICK_THROUGH_MACRO = "ct";

    /**
     * @see Tag#getNUrl(com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest,
     *      com.enremmeta.rtb.api.proto.openrtb.Bid, String)
     */
    public static final String QUERY_STRING_EXCHANGE_CLICK_THROUGH_ENCODED_MACRO = "cte";

    public static final String NAME = "Lot49";

    public static final String AD_FILENAME_PREFIX = "Ad_";
    public static final String BID_PRICE_CALCULATOR_FILENAME_PREFIX = "BidPriceCalculator_";

    public static final String TAG_FILENAME_PREFIX = "Tag_";

    public static final String ROOT_PATH_AUCTIONS = "/auction";
    public static final String ROOT_PATH_STATS = "/stats";
    public static final String ROOT_PATH_ADMIN = "/admin";

    public static final String REL_PATH_DEBUG_NURL = "debugNurl";

    public static final String DEFAULT_PROUST_PATH_RELATIVE = "proust";
    public static final String DEFAULT_PROUST_PATH_ABSOLUTE =
                    ROOT_PATH_STATS + "/" + DEFAULT_PROUST_PATH_RELATIVE;

    public static final String DEFAULT_TEST_PATH_RELATIVE = "test";
    public static final String DEFAULT_IMPRESSION_PATH_RELATIVE = "imp";
    public static final String DEFAULT_IMPRESSION_PATH_ABSOLUTE =
                    ROOT_PATH_STATS + "/" + DEFAULT_IMPRESSION_PATH_RELATIVE;

    public static final String DEFAULT_REDIR_PATH_RELATIVE = "r";

    public static final String DEFAULT_REDIR_PATH_ABSOLUTE =
                    ROOT_PATH_STATS + "/" + DEFAULT_REDIR_PATH_RELATIVE;
    /**
     * NUrl - Notice URL.
     */
    public static final String DEFAULT_NURL_PATH_RELATIVE = "nurl";
    public static final String DEFAULT_NURL_PATH_ABSOLUTE =
                    ROOT_PATH_STATS + "/" + DEFAULT_NURL_PATH_RELATIVE;

    public static final String DEFAULT_PIXEL_PATH_RELATIVE = "pixel";
    public static final String DEFAULT_PIXEL_PATH_ABSOLUTE =
                    ROOT_PATH_STATS + "/" + DEFAULT_PIXEL_PATH_RELATIVE;

    public static final String DEFAULT_CLICK_PATH_RELATIVE = "clk";
    public static final String DEFAULT_CLICK_PATH_ABSOLUTE =
                    ROOT_PATH_STATS + "/" + DEFAULT_CLICK_PATH_RELATIVE;

    public static final String EXCHANGE_SPOTXCHANGE = "spotxchange";
    public static final String EXCHANGE_OPENX = "openx";
    public static final String EXCHANGE_BIDSWITCH = "bidswitch";
    public static final String EXCHANGE_TEST1 = "test1";
    public static final String EXCHANGE_TEST2 = "test2";
    public static final String EXCHANGE_SMAATO = "smaato";

    public static final String AUCTION_RESULTS_PATH_OPENX = EXCHANGE_OPENX + "/results";
    public static final String EXCHANGE_LOT49_INTERNAL_AUCTION = "lot49";

    /**
     * Adap.tv
     */
    public static final String EXCHANGE_ADAPTV = "adaptv";

    /**
     * Google AdX
     */
    public static final String EXCHANGE_ADX = "adx";

    public static final String AUCTION_RESULTS_PATH_ADX = EXCHANGE_ADX + "/results";
    /**
     * Brightroll
     */
    public static final String EXCHANGE_BRX = "brx";

    public static final String EXCHANGE_PUBMATIC = "pubmatic";

    public static final String AUCTION_RESULTS_PATH_PUBMATIC = EXCHANGE_PUBMATIC + "/results";

    public static final String EXCHANGE_LIVERAIL = "liverail";

    // public static final Map<String, String> EXCHANGE_IMPRESSION_PATHS = new
    // HashMap<String, String>() {
    // {
    // // put(EXCHANGE_OPENX, Lot49Constants.IMPRESSION_PATH_OPENX);
    // }
    // };

    /**
     * Cookie prefix for identifying synchronized {@link User#getBuyeruid() buyer UID}s that are
     * already in the "decoded" cookie format. In other words, if the UID
     */
    public static final String COOKIE_PREFIX_A = "XODSPX";

    /**
     * Reference value for tests. Based on real NGINX values.
     * 
     * @see Utils#logToCookieModUid(String)
     * 
     * @see Utils#cookieToLogModUid(String)
     */
    public static final String TEST_MOD_UID_COOKIE_1 = "CtQF91SMxKx6nR1XWfrKAg";

    public static final String TEST_MOD_UID_COOKIE_2 = "CtQF91R09xIreF7QED9HAg";
    public static final String TEST_MOD_UID_COOKIE_3 = "rB4An1Z7gksDfE3HQu0UAg";
    public static final String TEST_MOD_UID_COOKIE_4 = "CtQFF91UMZ3hzIFvYseEJA";

    public static final int MOD_UID_COOKIE_LENGTH_MIN = 22;
    public static final int MOD_UID_COOKIE_LENGTH_MAX = 25;
    /**
     * Reference value for tests
     * 
     * @see Utils#logToCookieModUid(String)
     * 
     * @see Utils#cookieToLogModUid(String)
     */
    public static final String TEST_MOD_UID_LOG_1 = "F705D40AACC48C54571D9D7A02CAFA59";

    public static final String TEST_MOD_UID_LOG_2 = "F705D40A12F77454D05E782B02473F10";
    public static final String TEST_MOD_UID_LOG_3 = "9F001EAC4B827B56C74D7C030214ED42";
    public static final String TEST_MOD_UID_LOG_4 = "1705D40A9D3154DD6F81CCE12484C762";

    public static final int MOD_UID_LOG_LENGTH = TEST_MOD_UID_LOG_1.length();

    public static final int NURL_STANDART = 0;
    public static final int NURL_ONLY_WIN_NOTIFICATION = 1;
    public static final int NURL_ONLY_TAG = 2;

}
