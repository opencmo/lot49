package com.enremmeta.rtb.proto;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.CorruptedUserIdException;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.ExchangeTargeting;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.constants.RtbConstants;
import com.enremmeta.rtb.jersey.StatsSvc;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchAdapter;
import com.enremmeta.util.Utils;

import net.sf.uadetector.OperatingSystem;
import net.sf.uadetector.ReadableDeviceCategory;
import net.sf.uadetector.ReadableDeviceCategory.Category;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentFamily;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.VersionNumber;
import net.sf.uadetector.service.UADetectorServiceFactory;

/**
 * Interface defining connector to an exchange.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public interface ExchangeAdapter<RequestType, ResponseType> {

    public default void parseUserId(String userId, OpenRtbRequest req) {
        final Lot49Ext lot49Ext = req.getLot49Ext();
        User user = req.getUser();
        if (user == null) {
            user = new User();
            req.setUser(user);
        }
        final String receivedUserId = userId;
        String modUid = null;
        if (userId == null) {
            return;
        }
        if (userId.startsWith("COOKIEMONSTER_V2_")) {
            userId = userId.replace("COOKIEMONSTER_V2_", "");
        } else if (userId.startsWith("B64") && userId.endsWith("B64")) {
            userId = userId.substring(3, userId.length() - 3);
        } else if (userId.startsWith("B65")) {
            int i = 0;
            int start = userId.lastIndexOf("fp") - 1;
            String seg = null;
            if (start > -1) {
                for (i = start; i > 0; i--) {
                    if (!Character.isDigit(userId.charAt(i))) {
                        break;
                    }
                }
                i++;
                seg = userId.substring(i);
                userId = userId.substring(3, i);
            }
        } else if (userId.startsWith("HEX0")) {
            userId = userId.replace("HEX0", "");
        } else if (userId.startsWith("HEX1")) {
            userId = userId.replace("HEX1", "");
        } else if (userId.startsWith("odsp=")) {
            userId = userId.replace("odsp=", "");
        } else if (userId.startsWith("aaaa=")) {
            userId = userId.replace("aaaa=", "");
        } else if (userId.startsWith("XX")) {
            userId = userId.substring(2);
        }

        boolean parsed = false;
        String origWarning = "";
        try {
            new BigInteger(userId, 16);
            modUid = userId;
            userId = Utils.logToCookieModUid(modUid);
            parsed = true;
        } catch (NumberFormatException nfe) {
            // parsed remains false.
        } catch (CorruptedUserIdException cuie) {
            // parsed remains false.
            origWarning = "UserId: Cannot parse " + modUid;
        }
        if (!parsed) {
            if (userId.startsWith("XX")) {
                userId = userId.substring(2);
            } else if (userId.startsWith("%22")) {
                userId = userId.substring(3);
            }
            try {
                modUid = Utils.cookieToLogModUid(userId);
            } catch (CorruptedUserIdException cuie) {
                lot49Ext.setForceCookieReset(true);
                lot49Ext.setForceCookieResync(true);
                if (origWarning.length() == 0) {
                    LogUtils.warn("UserId: Cannot parse " + userId, cuie);
                } else {
                    LogUtils.warn(origWarning);
                }
            }
        }

        LogUtils.trace("UserId: Parsing result. " + "Received: " + receivedUserId + " | "
                        + "Parsed: " + userId + " | " + "Decoded: " + modUid);

        lot49Ext.setReceivedBuyerUid(receivedUserId);
        lot49Ext.setModUid(modUid);
        user.setBuyeruid(userId);
    }

    public default Map<String, Object> makeExchangeSpecificInstructionsMap() {
        return null;
    }

    // TODO do it via markers
    public default void info(String msg) {
        LogUtils.info(getName() + ": " + msg);
    }

    public default void debug(String msg) {
        LogUtils.debug(getName() + ": " + msg);
    }

    public default void trace(String msg) {
        LogUtils.trace(getName() + ": " + msg);
    }

    public default void error(String msg) {
        LogUtils.error(getName() + ": " + msg);
    }

    public default void error(String msg, Throwable t) {
        LogUtils.error(getName() + ": " + msg, t);
    }

    default void fillPlatformInfoFromUa(OpenRtbRequest req) {
        Device dev = req.getDevice();
        if (dev == null) {
            return;
        }
        String ua = dev.getUa();
        if (ua == null) {
            return;
        }

        Lot49Ext lot49Ext = req.getLot49Ext();
        final UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();


        final ReadableUserAgent agent = Utils.getBrowserInfoFromUa(ua);
        if (agent == null) {
            return;
        }
        final String browserName = agent.getName();
        if (browserName != null && lot49Ext.getBrowserName() == null) {
            lot49Ext.setBrowserName(browserName.toLowerCase());
        }

        String browserFamilyStr = null;
        final UserAgentFamily browserFamily = agent.getFamily();
        if (browserFamily != null) {
            browserFamilyStr = browserFamily.getName();
            if (browserFamilyStr != null) {
                lot49Ext.setBrowserFamily(browserFamilyStr.toLowerCase());
            }
        }

        if (dev.getOs() == null) {
            OperatingSystem os = agent.getOperatingSystem();
            if (os != null) {
                dev.setOs(os.getFamilyName());
                VersionNumber vn = os.getVersionNumber();
                List<String> groups = vn.getGroups();
                if (groups != null && groups.size() > 0) {
                    dev.setOsv(StringUtils.join(groups, "."));
                }
            }
        }

        if (dev.getDevicetype() == null) {
            ReadableDeviceCategory rdc = agent.getDeviceCategory();
            if (rdc != null) {
                Category cat = rdc.getCategory();
                switch (cat) {
                    case GAME_CONSOLE:
                        dev.setDeviceType(RtbConstants.DEVICE_TYPE_STB);
                        break;
                    case PDA:
                        dev.setDeviceType(RtbConstants.DEVICE_TYPE_TABLET);
                        break;
                    case PERSONAL_COMPUTER:
                        dev.setDeviceType(RtbConstants.DEVICE_TYPE_PC);
                        break;
                    case SMART_TV:
                        dev.setDeviceType(RtbConstants.DEVICE_TYPE_CONNECTED_TV);
                        break;
                    case SMARTPHONE:
                        dev.setDeviceType(RtbConstants.DEVICE_TYPE_PHONE);
                        break;
                    case TABLET:
                        dev.setDeviceType(RtbConstants.DEVICE_TYPE_MOBILE_TABLET);
                        break;

                    case WEARABLE_COMPUTER:
                    case OTHER:
                    case UNKNOWN:
                    default:
                        break;
                }
            }
        }

    }

    /**
     * Get a {@link ResponseBuilder} proper for the opt-out response for this exchange. Default is
     * to send a {@link Status#NO_CONTENT 204}.
     */
    public default ResponseBuilder getOptoutBuilder(OpenRtbRequest req) {
        return Response.status(Status.NO_CONTENT).header("x-lot49-optout",
                        Bidder.getInstance().getAdCache().getStatus());
    }

    public default boolean isMacrosInNurl() {
        return false;
    }

    default ResponseBuilder setHeaders(OpenRtbResponse bResp, ResponseBuilder builder) {
        return builder;
    }

    /**
     * How long, in seconds, to wait, before declaring a loss.
     */
    public default long getWinTimeout() {
        return Bidder.getInstance().getConfig().getWinTimeoutSeconds();
    }

    /**
     * Return sample winning price suitable for decoding with {@link #parse(String, long)}
     */
    public default String getSampleWinningPrice() {
        return getWinningPriceMacro();
    }

    default String getPartnerInitiatedSyncUrl(String myUserId) {
        return null;
    }

    /**
     * What to return.
     */
    enum BidChoiceAlgorithm {
        RANDOM, MAX, LRU, ALL
    };

    /**
     * Defaults to {@link BidChoiceAlgorithm#MAX}.
     */
    default BidChoiceAlgorithm getBidChoiceAlgorithm() {
        return BidChoiceAlgorithm.MAX;
    }

    default boolean isAggregator() {
        return false;
    }

    String getResponseMediaType();

    String getName();

    /**
     * Whether {@link Bid#getNurl() NUrl is required} (two-step response). Since most exchanges for
     * now accept single-step response (returning {@link Bid#getAdm() the entire markup at once}, we
     * default the implementation to <tt>false</tt>.
     * 
     * @see <a href= "http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-3.pdf">
     *      OpenRTB 2.3</a>, section 4.3
     */
    default boolean isNurlRequired() {
        return false;
    }

    /**
     * Whether the true win is known by the {@link #getWinningPriceMacro() winning price}, gotten
     * either during the call of NUrl or out of band. For example, {@link BidSwitchAdapter OpenX}
     * does the latter, and so a true Win should be registered in
     * {@link StatsSvc#resultsOpenX(javax.ws.rs.core.UriInfo, String, com.enremmeta.rtb.proto.openx.AuctionResultMessage, String, javax.servlet.http.HttpServletRequest, String, javax.ws.rs.core.HttpHeaders)}
     * 
     * 
     * @see StatsSvc#nurl(javax.ws.rs.core.UriInfo, String, String, String, String, String, String,
     *      String, String, String, String, long, String, String, String, String, String, String,
     *      javax.servlet.http.HttpServletRequest, String, String, String, String, String)
     * 
     * @see LogUtils#logWin(String, String, String, String, String, String, String, double, long,
     *      long, long, String, String, String, String, String,
     *      javax.servlet.http.HttpServletRequest, String, Long, String, java.net.URI, boolean,
     *      String, String, String, String, String, String, boolean, boolean, String)
     * 
     * @see StatsSvc#impression(javax.ws.rs.core.UriInfo, String, String, String, String, String,
     *      String, String, String, String, long, String, int, String, String, String, String,
     *      String, String, String, javax.servlet.http.HttpServletRequest, String,
     *      javax.ws.rs.core.HttpHeaders, String, String)
     */
    default boolean trueWinOnNurlOrImpression() {
        return true;
    }

    /**
     * What is the default timeout, in milliseconds (default, because timeout can also be provided
     * dynamically as {@link OpenRtbRequest#getTmax()} on each request, which takes precedence).
     */
    default long getDefaultTimeout() {
        return 200;
    }

    /**
     * Winning price macro.
     * 
     * @see #parse(String, long)
     */
    String getWinningPriceMacro();

    /**
     * Whether the mapping of {@link User#getBuyeruid() our user ID} to {@link User#getId() exchange
     * ID} is stored by us.
     */
    boolean localUserMapping();

    OpenRtbRequest convertRequest(RequestType req) throws Throwable;

    ResponseType convertResponse(OpenRtbRequest req, OpenRtbResponse resp) throws Throwable;

    /**
     * @see #getWinningPriceMacro()
     */
    ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable;

    String getClickMacro();

    String getClickEncMacro();

    default String getSeat(Ad ad) {
        return "OpenDSPSeat";
    }

    /**
     * Post-process a bid. TODO should really be part of convertResponse and that should be passed
     * request.
     * 
     * @see Tag#getBid(OpenRtbRequest, Impression)
     */
    default Bid massageBid(final OpenRtbRequest req, final Impression imp, final Tag tag,
                    final Bid bid) {
        return bid;
    }

    /**
     * Check any exchange-specific targeting. For an example, see
     * {@link BidSwitchAdapter#checkExchangeTargeting(OpenRtbRequest, Impression, ExchangeTargeting)}
     * 
     * 
     * @return <tt>null</tt> if everything is OK, otherwise a <tt>String</tt> with an error message.
     */
    default String checkExchangeTargeting(OpenRtbRequest req, Impression imp,
                    ExchangeTargeting targeting) {
        return null;
    }

    /**
     * For calls from {@link Ad#validate()}, for example. Same semantics. Default implementation is
     * to return <tt>null</tt>.
     */
    default List<String> validateAd(Ad ad) {
        return null;
    }

}
