package com.enremmeta.rtb;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.MarkupType;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.api.proto.openrtb.App;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Content;
import com.enremmeta.rtb.api.proto.openrtb.Deal;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.PMP;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49CustomData;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtGeo;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtRemote;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.RtbConstants;
import com.enremmeta.rtb.jersey.StatsSvc;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.brx.BrxAdapter;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoReceived;
import com.enremmeta.rtb.spi.providers.integral.IntegralService;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindFacade;
import com.enremmeta.rtb.spi.providers.skyhook.SkyhookInfoReceived;
import com.enremmeta.rtb.spi.providers.skyhook.SkyhookProvider;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.DelimLayout;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

import net.sf.uadetector.OperatingSystem;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentFamily;

/**
 * <p>
 * Centralized place for logging calls (as <tt>static</tt> methods). Everything is logged at
 * {@link Level#INFO} level unless otherwise specified.
 * </p>
 * <p>
 * CONTRACT NOTE: This class is ONLY concerned with logging. The rationale is: we are only concerned
 * about things that are to be analyzed later by an external actor. We are journalists/chroniclers,
 * not participants in the action.
 * </p>
 * We will only hold to the following conventions:
 * <ul>
 * <li>Columns are never removed from logging.</li>
 * </ul>
 * <p>
 * The following things are true unless specified otherwise:
 * <ul>
 * <li>"Date" is always date/time more or less at the time of logging (which may be slightly later
 * than actual event but not importantly so) formatted as {@link #LOG_DATE_FORMAT}.</li>
 * <li>Unix time stamp is always, well, Unix time stamp more or less (because it's an asynchronous
 * process) at the time of logging.</li>
 * <li>Both of the above are in UTC.</li>
 * <li>In structured logs, fields are {@link DelimLayout tab-separated}.</li>
 * <li>{@link Bid#getCid() Campaign ID} corresponds to {@link Ad#getId() ad ID}.</li>
 * <li>{@link Bid#getCrid() Creative ID} corresponds to {@link Tag#getId() tag ID}.</li>
 * </ul>
 * <p>
 * </p>
 * This is
 * <a href="https://gettingreal.37signals.com/ch04_Make_Opinionated_Software.php" >opinionated</a>
 * in a sense that:
 * <ul>
 * <li>Any <tt>null</tt> or empty field is logged as {@link DelimLayout#getNullChar() specified in
 * DelimLayout}).</li>
 * <li>We only recognize the following logs in the current implementation:
 * <ul>
 * <li>Structured (string TSV format suitable for loading into a DB; whenever lists of values are
 * encountered in a field, they are written as CSV):
 * <ul>
 * <li>{@link #LOG_ACCESS} - just the access URLs. See</li>
 * <li>{@link #LOG_BID} - bids made (
 * {@link #logBid(OpenRtbRequest, long, String, String, String, Bid, int, int, ExchangeAdapter, String)}
 * </li>
 * <li>{@link #LOG_DEBUG_REQ} debug companion to {@link #LOG_REQUEST}</li>
 * <li>{@link #LOG_ABTEST_ASSIGNMENT} - status assignment for A/B-test made for Campaign
 * {@link #logExperimentCampaign(OpenRtbRequest, String, Ad, UserExperimentAttributes)} or for
 * Targeting Strategy
 * {@link #logExperimentTargetingStrategy(OpenRtbRequest, String, Ad, UserExperimentAttributes)}
 * </li>
 * <li>{@link #LOG_REQUEST} - all bid requests ( {@link #logRequest(OpenRtbRequest, boolean, int)})
 * </li>
 * <li>{@link #LOG_SESSION} - same as {@link #LOG_REQUEST}, but only for requests on which we bid.
 * </li>
 * <li>{@link #LOG_URLS URLs} (see {@link #logRequest(OpenRtbRequest, boolean, int)}).</li>
 * <li>{@link #LOG_WIN} - wins (this is responsible for knowing the winning price). This may or may
 * not be fired at the same time as
 * {@link #logImpression(String, String, String, String, String, String, long, String, String, String, String, String, HttpServletRequest, String, int, String, URI, String, String, String, String, String, MultivaluedMap, boolean, String, String, String, boolean, String)}
 * in cases where an exchange requires using {@link Bid#getNurl() NUrl} mechanism (
 * {@link BrxAdapter BRX} is one such example). See
 * {@link #logWin(String, String, String, String, String, String, String, double, long, long, long, String, String, String, String, String, HttpServletRequest, String, Long, String, URI, boolean, String, String, String, String, String, String, boolean, boolean, String)}
 * </li>
 * <li>{@link #LOG_PSEUDOWIN} - pseudo-wins -- same as above but where <tt>authoritativeWin</tt> is
 * <tt>false</tt>
 * <li>{@link #LOG_IMPRESSION} - impressions. See
 * {@link #logImpression(String, String, String, String, String, String, long, String, String, String, String, String, HttpServletRequest, String, int, String, URI, String, String, String, String, String, MultivaluedMap, boolean, String, String, String, boolean, String)}
 * </li>
 * <li>{@link #LOG_CLICK} - clicks. See
 * {@link #logClick(String, String, String, String, String, String, String, String, String, String, HttpServletRequest, String, String, URI, String, String, String, String)}
 * </li>
 * <li>{@link #LOG_LOST}- lost bids. See
 * {@link #logLost(Long, String, String, String, String, Long, Long, String, String, String, String, HttpServletRequest, String, String, String, String)}
 * </li>
 * </ul>
 * </li>
 * <li>Semi-structured:
 * <ul>
 * <li>{@link #LOG_RESPONSE} - entire bid response (
 * {@link #logResponse(OpenRtbRequest, OpenRtbResponse, Object)})</li>
 * 
 * <li>{@link #LOG_RAW_REQUEST} - raw request as JSON. (
 * {@link #logRawRequest(OpenRtbRequest, Object, UriInfo)})</li>
 * </ul>
 * </li>
 * <li>Unstructured:
 * <ul>
 * <li>{@link #LOG_MAIN} - free-form logging for troubleshooting. See:
 * <ul>
 * <li>{@link #trace(Object)}</li>
 * <li>{@link #debug(Object)}</li>
 * <li>{@link #info(Object)}</li>
 * <li>{@link #error(Object)}</li>
 * <li>{@link #error(Object, Throwable)}</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @see DelimLayout
 * 
 * @see <a href="https://github.com/debedb/microput/">Microput</a>
 * 
 * @author
 *         <p>
 *         <a href="mailto:grisha@alum.mit.edu">Gregory Golberg</a>
 *         </p>
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014-2015. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *         </p>
 */
public class LogUtils {

    static {
        PluginManager.addPackage(DelimLayout.class.getPackage().getName());

    }

    public static final Marker MARKER_BUDGET = MarkerManager.getMarker("BUDGET");

    /**
     * Marker for {@link BidCandidateManager}.
     */
    public static final Marker MARKER_BCM = MarkerManager.getMarker("BCM");

    public static final String LOG_JETTY = "jetty";

    public static final Logger jettyLogger = LogManager.getLogger(LOG_JETTY);

    private static final int CURRENT_LOG_VERSION_NUM = 1;

    public static void init(Object o) {
        info("Initializing: " + o);
        System.out.println(BidderCalendar.getInstance().currentDate() + ": Initializing: " + o);
    }

    /**
     * @see #logBid(OpenRtbRequest, long, String, String, String, Bid, int, int, ExchangeAdapter,
     *      String)
     */
    public final static String LOG_BID = "bid";

    private static final Logger bidLogger = LogManager.getLogger(LOG_BID);

    /**
     * For command-line utilitiies
     * 
     * @param klass
     *            the class of logger
     * @return the logger
     */
    public static final Logger getLogger(Class klass) {
        return LogManager.getLogger(klass);
    }

    /**
     * @see #logExperimentCampaign(OpenRtbRequest, String, Ad, UserExperimentAttributes)
     * @see #logExperimentTargetingStrategy(OpenRtbRequest, String, Ad, UserExperimentAttributes)
     */
    public final static String LOG_ABTEST_ASSIGNMENT = "abtestassignment";

    private static final Logger abTestAssignmentLogger =
                    LogManager.getLogger(LOG_ABTEST_ASSIGNMENT);

    /**
     * @see #logClick(String, String, String, String, String, String, String, String, String,
     *      String, HttpServletRequest, String, String, URI, String, String, String, String)
     */
    public final static String LOG_CLICK = "click";

    private static final Logger clickLogger = LogManager.getLogger(LOG_CLICK);

    /**
     * The log date format - human-readable date as "yyyy-MM-dd hh:mm:ss".
     */
    public static final SimpleDateFormat LOG_DATE_FORMAT =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Name for {@link Logger} that logs data about impressions.
     * 
     * @see #logImpression(String, String, String, String, String, String, long, String, String,
     *      String, String, String, HttpServletRequest, String, int, String, URI, String, String,
     *      String, String, String, MultivaluedMap, boolean, String, String, String, boolean,
     *      String)
     */
    public final static String LOG_IMPRESSION = "impression";

    private static final Logger impressionLogger = LogManager.getLogger(LOG_IMPRESSION);

    /**
     * @see #logWin(String, String, String, String, String, String, String, double, long, long,
     *      long, String, String, String, String, String, HttpServletRequest, String, Long, String,
     *      URI, boolean, String, String, String, String, String, String, boolean, boolean, String)
     */
    public final static String LOG_WIN = "win";

    private static final Logger winLogger = LogManager.getLogger(LOG_WIN);

    /**
     * @see #logWin(String, String, String, String, String, String, String, double, long, long,
     *      long, String, String, String, String, String, HttpServletRequest, String, Long, String,
     *      URI, boolean, String, String, String, String, String, String, boolean, boolean, String)
     */
    public final static String LOG_PSEUDOWIN = "pseudowin";

    private static final Logger pseudowinLogger = LogManager.getLogger(LOG_PSEUDOWIN);

    /**
     * @see #logLost(Long, String, String, String, String, Long, Long, String, String, String,
     *      String, HttpServletRequest, String, String, String, String)
     */
    public final static String LOG_LOST = "lost";

    private static final Logger lostLogger = LogManager.getLogger(LOG_LOST);

    /**
     * This is the "main" logger.
     */
    public final static String LOG_MAIN = "main";

    private static final Logger mainLogger = LogManager.getLogger(LOG_MAIN);
    /**
     * This is the "access" logger. Named like this after the NGINX/Apache pattern.
     */
    public final static String LOG_ACCESS = "access";

    private static final Logger accessLogger = LogManager.getLogger(LOG_ACCESS);

    /**
     * @see #logRequest(OpenRtbRequest, boolean, int)
     */
    public final static String LOG_REQUEST = "request";

    /**
     * @see #LOG_REQUEST
     */
    private static final Logger requestLogger = LogManager.getLogger(LOG_REQUEST);
    /**
     * Fields from {@link #LOG_REQUEST} that are too long to keep there. At DEBUG level only.
     * 
     * @see #logRequest(OpenRtbRequest, boolean, int)
     */
    public final static String LOG_DEBUG_REQ = "debugreq";

    /**
     * @see #LOG_DEBUG_REQ
     */
    private static final Logger debugReqLogger = LogManager.getLogger(LOG_DEBUG_REQ);

    public final static String LOG_REDIR = "redur";

    private static final Logger redirLogger = LogManager.getLogger(LOG_REDIR);

    public final static String LOG_DECISION = "decision";

    private static final Logger decisionLogger = LogManager.getLogger(LOG_DECISION);

    public final static String LOG_TAG_DECISION = "tagdecision";

    private static final Logger tagDecisionLogger = LogManager.getLogger(LOG_TAG_DECISION);

    /**
     * Full response, at {@link Level#DEBUG}.
     * 
     * @see #logResponse(OpenRtbRequest, OpenRtbResponse, Object)
     */
    public final static String LOG_RESPONSE = "response";

    private static final Logger responseLogger = LogManager.getLogger(LOG_RESPONSE);

    /**
     * Raw requests, as JSON. See {@link #logRawRequest(OpenRtbRequest, Object, UriInfo)}. Only
     * available when log level is {@link Level#DEBUG}.
     */
    public final static String LOG_RAW_REQUEST = "rawrequest";

    /**
     * @see #logRawRequest(OpenRtbRequest, Object, UriInfo)
     */
    private static final Logger rawRequestLogger = LogManager.getLogger(LOG_RAW_REQUEST);

    public final static String LOG_PROUST = "proust";

    private static final Logger proustLogger = LogManager.getLogger(LOG_PROUST);

    /**
     * @see #LOG_REQUEST
     * 
     * @see #logRequest(OpenRtbRequest, boolean, int)
     */
    public final static String LOG_SESSION = "session";

    private static final Logger sessionLogger = LogManager.getLogger(LOG_SESSION);

    /**
     *  
     */
    public final static String LOG_URLS = "urls";

    private static final Logger urlsLogger = LogManager.getLogger(LOG_URLS);

    private static String wrapMsg(Object msg) {
        return getRegion() + " " + getNodeId() + " " + msg;
    }

    /**
     * Debug message.
     */
    public static void debug(Object msg) {
        mainLogger.debug(wrapMsg(msg));
    }

    public static void trace(Object msg) {
        if (mainLogger != null) {
            mainLogger.trace(wrapMsg(msg));
        }
    }

    public static void trace(Object msg, Throwable t) {
        mainLogger.trace(wrapMsg(msg), t);
    }

    public static void debug(Marker m, Object msg) {
        mainLogger.debug(m, wrapMsg(msg));
    }

    public static void trace(Marker m, Object msg) {
        mainLogger.trace(m, wrapMsg(msg));
    }

    /**
     * Debug message.
     */
    public static void debug(Object msg, Throwable t) {
        mainLogger.debug(wrapMsg(msg), t);
    }

    public static void error(Object msg) {
        if (msg instanceof Throwable) {
            error(wrapMsg("Error"), (Throwable) msg);
        } else {
            mainLogger.error(wrapMsg(msg));
        }
    }

    public static void fatal(Object msg) {

        if (msg instanceof Throwable) {
            mainLogger.fatal("Error", (Throwable) msg);
        } else {
            mainLogger.fatal(wrapMsg(msg));
        }

    }

    public static void logRedir(String uriCalled, String uriRedir, int statusCode) {
        redirLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        uriCalled,
                        // 4
                        "M1",
                        // 5
                        uriRedir,
                        // 6
                        "M2",
                        // 7
                        statusCode);

    }

    public static void logAdLoadingError(long runNumber, Object o, String comment) {
        decisionLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        runNumber,
                        // 4
                        o == null ? null : o.toString(),
                        // 5
                        null,
                        // 6
                        Lot49Constants.DECISION_LOADING_ERROR,
                        // 7
                        "M1",
                        // 8
                        null,
                        // 9
                        null,
                        // 10
                        comment,
                        // 11
                        "M2",
                        // 12
                        getNodeId(),
                        // 13
                        getRegion());
    }

    /**
     * Logs the following fields:
     * <ol>
     * <li>{@link Date#Date() Current date} (assume UTC)</li>
     * <li>{@link System#currentTimeMillis() Current timestamp} (assume UTC)</li>
     * <li>run number of {@link AdCache}</li>
     * <li>{@link Ad#getId() Ad ID}</li>
     * <li>Exchange</li>
     * <li>Decision type (e.g., {@link Lot49Constants#DECISION_BROWSER})</li>
     * <li>"M1" as literal</li>
     * <li>Count</li>
     * <li>Total</li>
     * <li>Comment (clarifying "decision type")</li>
     * <li>M2 as literal</li>
     * <li>{@link Orchestrator#getNodeId() node ID}</li>
     * <li>Region</li>
     * <li>Phase of decision (e.g., "02" from {@link Lot49Constants#DECISION_BROWSER})</li>
     * <li>Step of decision (e.g., "11" from {@link Lot49Constants#DECISION_BROWSER})</li>
     * </ol>
     */
    public static void logDecision(long runNumber, Ad ad, String xch, String type, Long count,
                    Long total, String comment) {
        decisionLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        runNumber,
                        // 4
                        ad.getId(),
                        // 5
                        xch,
                        // 6
                        type,
                        // 7
                        "M1",
                        // 8
                        count,
                        // 9
                        total,
                        // 10
                        comment,
                        // 11
                        "M2",
                        // 12
                        getNodeId(),
                        // 13
                        getRegion(),
                        // 14
                        getPhaseOfDecision(type),
                        // 15
                        getStepOfDecision(type));

    }

    /**
     * Similar to {@link #logDecision(long, Ad, String, String, Long, Long, String)}. Logs the
     * following: *
     * <ol>
     * <li>{@link Date#Date() Current date} (assume UTC)</li>
     * <li>{@link System#currentTimeMillis() Current timestamp} (assume UTC)</li>
     * <li>run number of {@link AdCache}</li>
     * <li>{@link Ad#getId() Ad ID}</li>
     * <li>{@link Tag#getId() Tag ID}</li>
     * <li>Exchange</li>
     * <li>Decision type (e.g., {@link Lot49Constants#TAG_DECISION_API})</li>
     * <li>Count of decisions of this type</li>
     * <li>Total impressions considered</li>
     * <li>{@link Orchestrator#getNodeId() node ID}</li>
     * <li>Region</li>
     * </ol>
     */
    public static void logTagDecision(long runNumber, Tag tag, String xch, String type, Long count,
                    Long total) {

        tagDecisionLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        runNumber,
                        // 4
                        tag.getAd().getId(),
                        // 5
                        tag.getId(),
                        // 6
                        xch,
                        // 7
                        type,
                        // 8
                        count,
                        // 9
                        total,
                        // 10
                        getNodeId(),
                        // 11
                        getRegion());

    }

    public static void error(Object msg, Throwable t) {
        mainLogger.error(msg, t);
    }

    /**
     * Formats current date with {@link #LOG_DATE_FORMAT}.
     */
    public static final String getDate() {
        Date date = BidderCalendar.getInstance().currentDate();
        return LOG_DATE_FORMAT.format(date);
    }

    public static void info(Object msg) {
        mainLogger.info(wrapMsg(msg));
    }

    public static void info(Object msg, Throwable t) {
        mainLogger.info(wrapMsg(msg), t);
    }

    public static void info(Marker m, Object msg) {
        mainLogger.info(m, wrapMsg(msg));
    }

    public static final Marker MARKER_WINLOSS = MarkerManager.getMarker("WINLOSS");

    public static void logAccess(UriInfo uriInfo) {
        accessLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 4
                        uriInfo == null ? null : uriInfo.getRequestUri(),
                        // 5
                        getNodeId());
    }

    private final static String getNodeId() {
        ServiceRunner inst = Bidder.getInstance();
        if (inst == null) {
            return null;
        }
        Orchestrator orch = inst.getOrchestrator();
        if (orch == null) {
            return null;
        }
        return orch.getNodeId();
    }

    private final static String getRegion() {
        ServiceRunner inst = Bidder.getInstance();
        if (inst == null) {
            return null;
        }
        Orchestrator orch = inst.getOrchestrator();
        if (orch == null) {
            return null;
        }
        return orch.getRegion();
    }

    /**
     * Log a bid. Will log the following information, in order, to {@link #LOG_BID}.
     * <ol>
     * <li>{@link Date#Date() Current date} (assume UTC)</li>
     * <li>{@link System#currentTimeMillis() Current time stamp}</li>
     * <li>Time spent to get this bid
     * <li>{@link OpenRtbRequest#getId() bid request ID}</li>
     * <li>Our {@link User#getBuyeruid() User ID}</li>
     * <li>{@link Bid#getId() bid ID}</li>
     * <li>{@link Bid#getImpid() impression ID}</li>
     * <li>{@link Bid#getCid() campaign ID} - we define this to be {@link Ad#getId() targeting ID}
     * </li>
     * <li>{@link Bid#getCrid() creative ID} - we define this to be {@link Tag#getId() tag ID}</li>
     * <li>Bid, as $ CPM</li>
     * <li>Bid, as micro-$ per impression.</li>
     * <li>M1 literal</li>
     * <li>M2 literal</li>
     * <li>Count of bid candidates considered by {@link BidCandidateManager}.</li>
     * <li>Count of bids returned for this bid request.</li>
     * <li>{@link ExchangeAdapter#getName() Exchange}</li>
     * <li>{@link ExchangeAdapter#getBidChoiceAlgorithm() bid choice algo}</li>
     * <li>SSP</li>
     * <li>"M3" literal</li>
     * <li>How many bids {@link Ad#getRemainingBidsToMake() left to make in this period}</li>
     * <li>How many bids {@link Ad#getOriginalBidsToMake() we were originally supposed to make in
     * this period}</li>
     * <li>{@link Orchestrator#getNodeId() node ID}</li>
     * <li>{@link #seqBid} sequential number of bid per node (see above)</li>
     * <li>Mod UID</li>
     * <li>{@link Ad#getAdVersion()} version of the Ad</li>
     * <li>{@link Tag#getTagVersion} version of the Tag</li>
     * <li>Outcome - one of {@link Lot49Constants#BID_OUTCOME_SUBMITTED}, @link
     * {@link Lot49Constants#BID_OUTCOME_INTERNAL_AUCTION_LOSS}, @link
     * {@link Lot49Constants#BID_OUTCOME_CONTROL}.
     * </ol>
     * 
     * @param userId
     *            {@link User#getBuyeruid()}
     * @see Utils#microToCpm(double)
     * @see Utils#cpmToMicro(double)
     */
    public static void logBid(final OpenRtbRequest req, final long timeSpent,
                    final String bidRequestId, final String userId, String modUid, final Bid bid,
                    final int candCount, final int bidCount, final ExchangeAdapter adapter,
                    String outcome) {
        double doubleBid = bid.getPrice();
        long microBid = Utils.cpmToMicro(doubleBid);
        final String exchange = adapter == null ? "" : adapter.getName();
        final String ssp = adapter == null ? ""
                        : (adapter.isAggregator() ? req.getLot49Ext().getSsp() : exchange);
        final AdCache adCache = Bidder.getInstance().getAdCache();
        final Ad ad = (bid == null || adCache == null) ? null : adCache.getAd(bid.getCid());
        final Tag tag;
        if (ad != null && bid.getCrid() != null) {
            tag = ad.findTagById(bid.getCrid());
        } else {
            tag = null;
        }

        String domain = null;
        String url = null;
        String reqRef = null;
        Site site = req.getSite();
        if (site != null) {
            domain = site.getDomain();
            url = site.getPage();
            reqRef = site.getRef();
        }

        bidLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        timeSpent,
                        // 4
                        bidRequestId,
                        // 5
                        userId,
                        // 6
                        bid == null ? null : bid.getId(),
                        // 7
                        bid == null ? null : bid.getImpid(),
                        // 8
                        bid == null ? null : bid.getCid(),
                        // 9
                        bid == null ? null : bid.getCrid(),
                        // 10
                        doubleBid,
                        // 11
                        microBid,
                        // 12
                        "M1",
                        // 13
                        "M2",
                        // 14
                        candCount,
                        // 15
                        bidCount,
                        // 16
                        adapter == null ? "" : adapter.getName(),
                        // 17
                        adapter == null ? "" : adapter.getBidChoiceAlgorithm(),
                        // 18
                        ssp,
                        // 19
                        "M3",
                        // 20
                        ad == null ? null : ad.getRemainingBidsToMake(),
                        // 21
                        ad == null ? null : ad.getOriginalBidsToMake(),
                        // 22
                        getNodeId(),
                        // 23
                        seqBid.incrementAndGet(),
                        // 24
                        modUid,
                        // 25
                        ad == null ? null : ad.getAdVersion(),
                        // 26
                        tag == null ? null : tag.getTagVersion(),
                        // 27
                        outcome,
                        // 28
                        domain,
                        // 29
                        url,
                        // 30
                        reqRef,
                        // 31
                        "M4");
    }

    /**
     * Log an A/B-test assignment on Campaign. Will log the following information, in order, to
     * {@link #LOG_ABTEST_ASSIGNMENT}.
     * <ol>
     * <li>{@link Date#Date() Current date} (assume UTC)</li>
     * <li>{@link System#currentTimeMillis() Current time stamp}</li>
     * <li>{@link OpenRtbRequest#getId() bid request ID}</li>
     * <li>Our {@link User#getBuyeruid() User ID}</li>
     * <li>Cookie as decoded by {@link Utils#cookieToLogModUid(String)}</li>
     * <li>C literal - experiment assignment on Campaign</li>
     * <li>{@link Ad#getCampaignId() Campaign id}</li>
     * <li>Campaign A/B-test status ("T", "C" or "N")</li>
     * <li>Version of A/B-test.</li>
     * <li>Full User A/B-test status</li>
     * <li>User segments as a comma-separated list each encrypted with an advertiser's key.</li>
     * <li>{@link Ad#getCampaignId() Campaign id}</li>
     * <li>({@link Orchestrator#getRegion() region} )</li>
     * </ol>
     * 
     * @param req
     *            Request {@link OpenRtbRequest}
     * @param modUid
     *            Cookie as decoded by {@link Utils#cookieToLogModUid(String)} of assigned user
     * @param ad
     *            Targeting Strategy of a Campaign under Experiment{@link Ad}
     * @param exptAttributes
     *            Experiment Attributes for user {@link UserExperimentAttributes}
     * 
     * @see #logExperimentTargetingStrategy(OpenRtbRequest, String, Ad, UserExperimentAttributes)
     */
    public static void logExperimentCampaign(final OpenRtbRequest req, final String modUid,
                    final Ad ad, final UserExperimentAttributes exptAttributes) {
        String expDataStr = exptAttributes.getExperimentData() == null ? null
                        : exptAttributes.getExperimentData().toString();

        abTestAssignmentLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        req.getId(),
                        // 4
                        req.getUser().getBuyeruid(),
                        // 5
                        modUid,
                        // 6
                        "C",
                        // 7
                        ad.getCampaignId(),
                        // 8
                        exptAttributes.getStatusForCampaign(ad),
                        // 9
                        exptAttributes.getExperimentVersion(ad),
                        // 10
                        expDataStr,
                        // 11
                        req.getLot49Ext().getLot49ExtRemote().getUserSegments(),
                        // 12
                        ad.getCampaignId(),
                        // 13
                        getRegion());
    }

    /**
     * Log an A/B-test assignment on Targeting Strategy. Will log the following information, in
     * order, to {@link #LOG_ABTEST_ASSIGNMENT}.
     * <ol>
     * <li>{@link Date#Date() Current date} (assume UTC)</li>
     * <li>{@link System#currentTimeMillis() Current time stamp}</li>
     * <li>{@link OpenRtbRequest#getId() bid request ID}</li>
     * <li>Our {@link User#getBuyeruid() User ID}</li>
     * <li>Cookie as decoded by {@link Utils#cookieToLogModUid(String)}</li>
     * <li>TS literal - experiment assignment on Targeting Strategy</li>
     * <li>{@link Ad#getId() Targeting strategy ID}</li>
     * <li>Targeting strategy A/B-test status ("C", "T" or "N").</li>
     * <li>Version of A/B-test.</li>
     * <li>Full User A/B-test status</li>
     * <li>User segments as a comma-separated list each encrypted with an advertiser's key.</li>
     * <li>({@link Orchestrator#getRegion() region} )</li>
     * </ol>
     * 
     * @param req
     *            Request {@link OpenRtbRequest}
     * @param modUid
     *            Cookie as decoded by {@link Utils#cookieToLogModUid(String)} of assigned user
     * @param ad
     *            Targeting Strategy under Experiment{@link Ad}
     * @param exptAttributes
     *            Experiment Attributes for user {@link UserExperimentAttributes}
     */
    public static void logExperimentTargetingStrategy(final OpenRtbRequest req, final String modUid,
                    final Ad ad, final UserExperimentAttributes exptAttributes) {

        abTestAssignmentLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        req.getId(),
                        // 4
                        req.getUser().getBuyeruid(),
                        // 5
                        modUid,
                        // 6
                        "TS",
                        // 7
                        ad.getId(),
                        // 8
                        exptAttributes.getStatusForTargetingStrategy(ad),
                        // 9
                        exptAttributes.getExperimentVersion(ad),
                        // 10
                        exptAttributes.getExperimentData(),
                        // 11
                        req.getLot49Ext().getLot49ExtRemote().getUserSegments(),
                        // 12
                        ad.getCampaignId(),
                        // 13
                        getRegion());
    }

    /**
     * Log a click. Logs the following information, in order:
     * <ol>
     * <li>Date, human-readable</li>
     * <li>Unix timestamp</li>
     * <li>cookie The value of the cookie that serves as the user ID. The cookie name is taken from
     * {@link Lot49Config#getUserIdCookie() USER_ID_COOKIE configuration parameter}.</li>
     * <li>Exchange</li>
     * <li>{@link Bid#getId() Bid ID}</li>
     * <li>{@link Bid#getImpid() Impression ID}</li>
     * <li>{@link Bid#getCid() RTB's Campaign ID} - same as {@link Ad#getId()}</li>
     * <li>{@link Bid#getCrid()} RTB's Creative ID) - same as {@link Tag#getId()}</li>
     * <li>Redirect URL</li>
     * <li>"M1" literal</li>
     * <li>Referer</li>
     * <li>"M2" literal</li>
     * <li>User agent</li>
     * <li>"M3" literal</li>
     * <li>X-Forwarded-For header (could be user's, if NGINX is in front, but if there's ELB in
     * front of NGINX, it'll be ELB's)</li>
     * 
     * <li>{@link HttpServletRequest#getRemoteHost()}</li>
     * <li>{@link HttpServletRequest#getRemotePort()}</li>
     * <li>{@link HttpServletRequest#getRemoteAddr()}</li>
     * <li>X-Real-IP header (in case of Load-Balancer - NGINX - Lot49 setup, X-forwarded-for address
     * would be the balancer's, IP would be NGINX's, and X-Real-IP would be remote user's)</li>
     * <li>{@link Geo#getCity()}</li>
     * <li>{@link Geo#getRegion()}</li>
     * <li>{@link Geo#getZip()}</li>
     * <li>{@link Geo#getMetro()}</li>
     * <li>{@link Geo#getCountry()}</li>
     * <li>ModUID (HEX) format cookie as decoded by {@link Utils#cookieToLogModUid(String)}</li>
     * <li>URI of this request</li>
     * <li>"M4" literal</li>
     * <li>{@link Bid#getId() Bid request ID}</li>
     * <li>SSP</li>
     * <li>All our cookies</li>
     * <li>"M5" literal</li>
     * <li>{@link Orchestrator#getNodeId() Node ID} that originated the bid resulting in this click
     * </li>
     * <li>{@link Orchestrator#getNodeId() Node ID} of this server</li>
     * <li>{@link Ad#getAdVersion()} version of the Ad</li>
     * <li>{@link Tag#getTagVersion} version of the Tag</li>
     * </ol>
     */
    public static void logClick(String xch, String cookie, String bId, String iId, String cId,
                    String crId, String redir, String ref, String ua, String xff,
                    HttpServletRequest servletRequest, String xrip, String modUid, URI uri,
                    String brId, String ssp, String cookies, String originatingNodeId) {
        final ServiceRunner bidder = Bidder.getInstance();
        final MaxMindFacade mm = bidder.getMaxMind();
        final Geo geo = mm == null ? new Geo() : mm.getGeo(xrip);
        final AdCache adCache = bidder.getAdCache();
        final Ad ad = (cId == null || adCache == null) ? null : adCache.getAd(cId);
        final Tag tag;
        if (ad != null && crId != null) {
            tag = ad.findTagById(crId);
        } else {
            tag = null;
        }

        clickLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        cookie,
                        // 4
                        xch,
                        // 5
                        bId,
                        // 6
                        iId,
                        // 7
                        cId,
                        // 8
                        crId,
                        // 9
                        redir,
                        // 10
                        "M1",
                        // 11
                        ref,
                        // 12
                        "M2",
                        // 13
                        ua,
                        // 14
                        "M3",
                        // 15
                        xff,
                        // 16
                        servletRequest == null ? "" : servletRequest.getRemoteHost(),
                        // 17
                        servletRequest == null ? "" : servletRequest.getRemotePort(),
                        // 18
                        servletRequest == null ? "" : servletRequest.getRemoteAddr(),
                        // 19
                        xrip,
                        // 20
                        geo == null ? null : geo.getCity(),
                        // 21
                        geo == null ? null : geo.getRegion(),
                        // 22
                        geo == null ? null : geo.getZip(),
                        // 23
                        geo == null ? null : geo.getMetro(),
                        // 24
                        geo == null ? null : geo.getCountry(),
                        // 25
                        modUid,
                        // 26
                        uri,
                        // 27
                        "M4",
                        // 28
                        brId,
                        // 29
                        ssp,
                        // 30
                        cookies,
                        // 31
                        "M5",
                        // 32
                        originatingNodeId,
                        // 33
                        getNodeId(),
                        // 34
                        ad == null ? null : ad.getAdVersion(),
                        // 35
                        tag == null ? null : tag.getTagVersion());

    }

    private final static AtomicLong seqImp = new AtomicLong(0);
    private final static AtomicLong seqWin = new AtomicLong(0);
    private final static AtomicLong seqPseudoWin = new AtomicLong(0);
    private final static AtomicLong seqLoss = new AtomicLong(0);
    private final static AtomicLong seqBid = new AtomicLong(0);

    /**
     * Log an impression. Log the following, in order, to {@link #LOG_IMPRESSION}:
     * 
     * <ol>
     * <li>{@link Date#Date() Current date} (assume UTC)</li>
     * <li>{@link System#currentTimeMillis() Current timestamp} (assume UTC)</li>
     * 
     * <li>httpCookie The value of the cookie set on the user as a result of this impression. This
     * may or may not be the same cookie as was received (see <tt>origHttpCookie</tt> below)</li>
     * <li>bid Id (see {@link Bid#getId()})</li>
     * <li>impression Id (see {@link Bid#getImpid()})</li>
     * <li>campaign Id (see {@link Bid#getCid()})</li>
     * <li>creative Id (see {@link Bid#getCrid()})</li>
     * <li>null (pricing information was formerly in this and next 3 columns, now for that see
     * {@link #logWin(String, String, String, String, String, String, String, double, long, long, long, String, String, String, String, String, HttpServletRequest, String, Long, String, URI, boolean, String, String, String, String, String, String, boolean, boolean, String)}
     * </li>
     * <li>null</li>
     * <li>null</li>
     * <li>null</li>
     * <li>bidCreatedTimestamp Timestamp (in millis, UTC) of bid creation</li>
     * <li>Redirect URL</li>
     * <li>Literal "M1" (always "M1" - marker)
     * <li>
     * <li>Referer</li>
     * <li>Literal "M2" (always "M2" - marker)
     * <li>
     * <li>User-agent</li>
     * <li>Literal "M3" (always "M3" - marker)
     * <li>
     * <li>custom data that we may have decided to pass through the impression back to us (see
     * {@link Tag#getCustomImpPassThruData()}) (and a marker - literal "M4" after).</li>
     * <li>Exchange</li>
     * <li>X-Forwarded-For header (could be user's, if NGINX is in front, but if there's ELB in
     * front of NGINX, it'll be ELB's)</li>
     * <li>{@link HttpServletRequest#getRemoteHost()}</li>
     * <li>{@link HttpServletRequest#getRemotePort()}</li>
     * <li>{@link HttpServletRequest#getRemoteAddr()} -- which is user's, unless we have something
     * in front like NGINX or ELB, in which case it's kind of useless, it's theirs</li>
     * <li>X-Real-IP header (in case of Load-Balancer - NGINX - Lot49 setup, X-forwarded-for address
     * would be the balancer's, IP would be NGINX's, and X-Real-IP would be remote user's), followed
     * by, as {@link MaxMindFacade looked up} from it</li>
     * 
     * <li>{@link Geo#getCity()}</li>
     * <li>{@link Geo#getRegion()}</li>
     * <li>{@link Geo#getZip()}</li>
     * <li>{@link Geo#getMetro()}</li>
     * <li>{@link Geo#getCountry()}</li>
     * <li>nurl (1 or 0)</li>
     * <li>modUid cookie as decoded by {@link Utils#cookieToLogModUid(String)}</li>
     * <li>uri that was requested, followed by M5</li>
     * <li>Bid request ID</li>
     * <li>SSP</li>
     * <li>forceCookieReset</li>
     * <li>forceCookieResync</li>
     * <li>All cookies</li>
     * <li>"M6" literal</li>
     * <li>All headers at debug level or higher</li>
     * <li>"M7" literal</li>
     * <li>whether a cookie logged above was created here (if false, it was received)</li>
     * <li>exchangeCookie cookie passed from exchange in request that resulted in this impression
     * </li>
     * <li>Original HTTP cookie we saw in this request (could be different than <tt>httpCookie</tt>)
     * </li>
     * <li>free-form text describing logic of <tt>createCookie</tt>, followed by "M8" literal</li>
     * 
     * <li>whether we consider this impression suspicious</li>
     * <li>Node which originated the bid that resulted in this event</li>
     * 
     * <li>{@link Orchestrator#getNodeId() node ID of self}</li>
     * <li>sequential number ({@link #seqImp})</li>
     * <li>({@link Orchestrator#getRegion() region} )</li>
     * <li>Browser name as determined from <tt>ua</tt></li>
     * <li>Browser family as determined from <tt>ua</tt></li>
     * <li>OS as determined from <tt>ua</tt></li>
     * <li>{@link Ad#getAdVersion()} version of the Ad</li>
     * <li>{@link Tag#getTagVersion} version of the Tag</li>
     * </ol>
     * 
     * @see StatsSvc#impression(UriInfo, String, String, String, String, String, String, String,
     *      String, String, long, String, int, String, String, String, String, String, String,
     *      String, HttpServletRequest, String, javax.ws.rs.core.HttpHeaders, String, String)
     * @see StatsSvc#nurl(UriInfo, String, String, String, String, String, String, String, String,
     *      String, String, long, String, String, String, String, String, String,
     *      HttpServletRequest, String, String, String, String, String)
     * @see #logWin(String, String, String, String, String, String, String, double, long, long,
     *      long, String, String, String, String, String, HttpServletRequest, String, Long, String,
     *      URI, boolean, String, String, String, String, String, String, boolean, boolean, String)
     * 
     */
    public static void logImpression(String httpCookie, String xch, String bId, String iId,
                    String cId, String crId, long bidCreatedTimestamp, String redir, String ref,
                    String ua, String custom, String xff, HttpServletRequest servletRequest,
                    String xrip, int nurl, String modUid, URI uri, String brId, String ssp,
                    String forceCookieReset, String forceCookieResync, String cookies,
                    MultivaluedMap<String, String> headers, boolean createCookie,
                    String exchangeCookie, String origHttpCookie, String cookieComment,
                    boolean suspicious, String originatingNodeId, BidInFlightInfo bif) {
        final ServiceRunner bidder = Bidder.getInstance();
        final MaxMindFacade mm = bidder.getMaxMind();
        final Geo geo = mm == null ? new Geo() : mm.getGeo(xrip);
        final ReadableUserAgent agent = Utils.getBrowserInfoFromUa(ua);

        final String browserName = agent == null ? null : agent.getName();
        final UserAgentFamily browserFamily = agent == null ? null : agent.getFamily();
        final String browserFamilyStr = browserFamily == null ? null : browserFamily.getName();
        final OperatingSystem os = agent == null ? null : agent.getOperatingSystem();
        final String osStr = os == null ? null
                        : (os.getFamilyName() + (os.getVersionNumber() == null ? null
                                        : " " + os.getVersionNumber()));
        final AdCache adCache = bidder.getAdCache();
        final Ad ad = (cId == null || adCache == null) ? null : adCache.getAd(cId);
        final Tag tag;
        if (ad != null && crId != null) {
            tag = ad.findTagById(crId);
        } else {
            tag = null;
        }

        Map<String, String> logValues = null;
        if (bif == null) {
            logValues = new HashMap<String, String>();
        } else {
            logValues = bif.getLogValues();
        }
        impressionLogger.info("",

                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        httpCookie,
                        // 4
                        bId,
                        // 5
                        iId,
                        // 6
                        cId,
                        // 7
                        crId,
                        // 8
                        null,
                        // 9
                        null,
                        // 10
                        null,
                        // 11
                        null,
                        // 12
                        bidCreatedTimestamp,
                        // 13
                        redir,
                        // 14
                        "M1",
                        // 15
                        ref,
                        // 16
                        "M2",
                        // 17
                        ua,
                        // 18
                        "M3",
                        // 19
                        custom,
                        // 20
                        "M4",
                        // 21
                        xch,
                        // 22
                        xff,
                        // 23
                        servletRequest == null ? "" : servletRequest.getRemoteHost(),
                        // 24
                        servletRequest == null ? "" : servletRequest.getRemotePort(),
                        // 25
                        servletRequest == null ? "" : servletRequest.getRemoteAddr(),
                        // 26
                        xrip,
                        // 27
                        geo == null ? null : geo.getCity(),
                        // 28
                        geo == null ? null : geo.getRegion(),
                        // 29
                        geo == null ? null : geo.getZip(),
                        // 30
                        geo == null ? null : geo.getMetro(),
                        // 31
                        geo == null ? null : geo.getCountry(),
                        // 32
                        nurl,
                        // 33
                        modUid,
                        // 34
                        uri,
                        // 35
                        "M5",
                        // 36
                        brId,
                        // 37
                        ssp,
                        // 38
                        forceCookieReset,
                        // 39
                        forceCookieResync,
                        // 40
                        cookies,
                        // 41
                        "M6",
                        // 42
                        logAt(impressionLogger.getLevel(), Level.DEBUG, headers),
                        // 43
                        "M7",
                        // 44
                        createCookie,
                        // 45
                        exchangeCookie,
                        // 46
                        origHttpCookie,
                        // 47
                        cookieComment,
                        // 48,
                        "M8",
                        // 49
                        suspicious,
                        // 50
                        originatingNodeId,
                        // 51
                        getNodeId(),
                        // 52
                        seqImp == null ? null : seqImp.incrementAndGet(),
                        // 53
                        getRegion(),
                        // 54
                        browserName,
                        // 55
                        browserFamilyStr,
                        // 56
                        osStr,
                        // 57
                        ad == null ? null : ad.getAdVersion(),
                        // 58
                        tag == null ? null : tag.getTagVersion(),
                        // 59
                        logValues.get("domain"),
                        // 60
                        logValues.get("url"),
                        // 61
                        "M9");

    }

    /**
     * Logs requests to
     * {@link StatsSvc#proust(UriInfo, String, String, String, String, String, String, String, String, String, String, String, String, String, String, HttpServletRequest, String, String, String)
     * Proust}.
     */
    public static void logProust(String cookie, String newCookie, String xch, String bId,
                    String iId, String cId, String crId, String ref, String ua, String xff,
                    HttpServletRequest servletRequest, String xrip, String modUid, URI uri,
                    String brId, String ssp, String fcr, String phase, String redir, String cookies,
                    String urlVersion, String originatingNodeId) {

        Geo geo = Bidder.getInstance().getMaxMind().getGeo(xrip);

        proustLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        cookie,
                        // 4
                        modUid,
                        // 5
                        newCookie,
                        // 6
                        brId,
                        // 7
                        bId,
                        // 8
                        iId,
                        // 9
                        cId,
                        // 10
                        crId,
                        // 11
                        xch,
                        // 12,
                        ssp,
                        // 13
                        fcr,
                        // 14
                        phase,
                        // 15
                        uri,
                        // 16
                        "M1",
                        // 17
                        ref,
                        // 18
                        "M2",
                        // 19
                        ua,
                        // 20
                        "M3",
                        // 21
                        xff,
                        // 22
                        servletRequest == null ? "" : servletRequest.getRemoteAddr(),
                        // 23
                        xrip,
                        // 24
                        geo == null ? null : geo.getCity(),
                        // 25
                        geo == null ? null : geo.getRegion(),
                        // 26
                        geo == null ? null : geo.getZip(),
                        // 27
                        geo == null ? null : geo.getMetro(),
                        // 28
                        geo == null ? null : geo.getCountry(),
                        // 29
                        redir,
                        // 30
                        "M4",
                        // 31
                        cookies,
                        // 32
                        "M5",
                        // 33
                        urlVersion,
                        // 34
                        originatingNodeId,
                        // 35
                        getNodeId());

    }

    /**
     * Similar to
     * {@link #logImpression(String, String, String, String, String, String, long, String, String, String, String, String, HttpServletRequest, String, int, String, URI, String, String, String, String, String, MultivaluedMap, boolean, String, String, String, boolean, String)}
     * except that:
     * 
     * <ol>
     * <li>The four columns after <tt>crId</tt> are not <tt>null</tt> but carry pricing information,
     * as follows:
     * <ol>
     * <li>Winning price as sent by the exchange (prior to it being decoded by the method specific
     * to the exchange)</li>
     * 
     * <li>Decoded winning price as CPM</li>
     * 
     * <li>Decoded winning price as micro$</li>
     * 
     * <li>Bid price, as micro$</li>
     * </ol>
     * </li>
     * <li>Instead of <tt>nurl</tt> column, here we have <tt>nurlId</tt> column.</li>
     * <li>All columns up to 35 ("M5") are otherwise the same. After that, as follows.</li>
     * <li>
     * <ol start="36">
     * <li>Whether this Win is authoritative (depends who wrote it and on the setting of
     * {@link ExchangeAdapter#trueWinOnNurlOrImpression()}.</li>
     * <li>{@link OpenRtbRequest#getId() Bid Request ID}</li>
     * <li>SSP</li>
     * <li>Reason if the information for the bid underlying this win is not found</li>
     * <li>Click-through macro value (only makes sense if {@link ExchangeAdapter#isMacrosInNurl()}).
     * </li>
     * <li>Literal "M6" as marker.</li>
     * <li>Encoded click-through macro value (only makes sense if
     * {@link ExchangeAdapter#isMacrosInNurl()}).</li>
     * <li>Literal "M7" as marker.</li>
     * <li>All cookies</li>
     * <li>Literal "M8" as marker.</li>
     * <li>Whether the cookie was created right now</li>
     * <li>Whether win is suspicious</li>
     * <li>Node which originated the bid that resulted in this event</li>
     * <li>{@link Orchestrator#getNodeId() node ID of self}</li>
     * <li>{@link #seqWin next win sequence number}</li>
     * <li>{@link Orchestrator#getRegion() region}</li>
     * <li>{@link Ad#getAdVersion()} version of the Ad</li>
     * <li>{@link Tag#getTagVersion} version of the Tag</li>
     * </ol>
     * </li>
     * </ol>
     * 
     * @see LostAuctionTask#cancelLostAuctionTask(String, BidInFlightInfo)
     * @see StatsSvc#nurl(UriInfo, String, String, String, String, String, String, String, String,
     *      String, String, long, String, String, String, String, String, String,
     *      HttpServletRequest, String, String, String, String, String)
     * @see StatsSvc#impression(UriInfo, String, String, String, String, String, String, String,
     *      String, String, long, String, int, String, String, String, String, String, String,
     *      String, HttpServletRequest, String, javax.ws.rs.core.HttpHeaders, String, String)
     */
    public static void logWin(String cookie, String xch, String bId, String iId, String cId,
                    String crId, String sentWinningPrice, double winningCpm, long microWinningPrice,
                    long microBidPrice, long bidCreatedTimestamp, String redir, String ref,
                    String ua, String custom, String xff, HttpServletRequest servletRequest,
                    String xrip, Long nurlId, String modUid, URI uri, boolean authoritativeWin,
                    String brId, String ssp, String notFoundReason, String clickThroughMacroValue,
                    String clickThroughEncodedMacroValue, String cookies, boolean createCookie,
                    boolean suspiciousWin, String originatingNodeId) {
        final ServiceRunner bidder = Bidder.getInstance();
        final MaxMindFacade mm = bidder.getMaxMind();
        final Geo geo = mm == null ? new Geo() : mm.getGeo(xrip);
        final AdCache adCache = bidder.getAdCache();
        final Ad ad = (cId == null || adCache == null) ? null : adCache.getAd(cId);
        final Tag tag;
        if (ad != null && crId != null) {
            tag = ad.findTagById(crId);
        } else {
            tag = null;
        }

        final Logger logger = authoritativeWin ? winLogger : pseudowinLogger;
        logger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        cookie,
                        // 4
                        bId,
                        // 5
                        iId,
                        // 6
                        cId,
                        // 7
                        crId,
                        // 8
                        sentWinningPrice,
                        // 9
                        winningCpm,
                        // 10
                        microWinningPrice,
                        // 11
                        microBidPrice,
                        // 12
                        bidCreatedTimestamp,
                        // 13
                        redir,
                        // 14
                        "M1",
                        // 15
                        ref,
                        // 16
                        "M2",
                        // 17
                        ua,
                        // 18
                        "M3",
                        // 19
                        custom,
                        // 20
                        "M4",
                        // 21
                        xch,
                        // 22
                        xff,
                        // 23
                        servletRequest == null ? "" : servletRequest.getRemoteHost(),
                        // 24
                        servletRequest == null ? "" : servletRequest.getRemotePort(),
                        // 25
                        servletRequest == null ? "" : servletRequest.getRemoteAddr(),
                        // 26
                        xrip,
                        // 27
                        geo == null ? null : geo.getCity(),
                        // 28
                        geo == null ? null : geo.getRegion(),
                        // 29
                        geo == null ? null : geo.getZip(),
                        // 30
                        geo == null ? null : geo.getMetro(),
                        // 31
                        geo == null ? null : geo.getCountry(),
                        // 32
                        nurlId,
                        // 33
                        modUid,
                        // 34
                        uri,
                        // 35
                        "M5",
                        // 36
                        authoritativeWin ? 1 : 0,
                        // 37
                        brId,
                        // 38
                        ssp,
                        // 39
                        notFoundReason,
                        // 40
                        clickThroughMacroValue,
                        // 41,
                        "M6",
                        // 42
                        clickThroughEncodedMacroValue,
                        // 43
                        "M7",
                        // 44
                        cookies,
                        // 45
                        "M8",
                        // 46
                        createCookie,
                        // 47
                        suspiciousWin,
                        // 48
                        originatingNodeId,
                        // 49
                        getNodeId(),
                        // 50
                        seqWin == null ? null : seqWin.incrementAndGet(),
                        // 51
                        getRegion(),
                        // 52
                        ad == null ? null : ad.getAdVersion(),
                        // 53
                        tag == null ? null : tag.getTagVersion());
    }

    /**
     * Log a lost bid. We consider a bid lost when no impression came from it within
     * {@link Lot49Config#getWinTimeoutSeconds() winTimeout} time, or when we explicitly were told
     * that the bid was lost. The following information is logged to {@link LogUtils#LOG_LOST}:
     * 
     * <ol>
     * <li>{@link Date#Date() current date} (assume UTC)</li>
     * <li>{@link System#currentTimeMillis() Current timestamp} (assume UTC)
     * <li><tt>bidSentOn</tt> parameter - timestamp when the bid was sent to the exchange</li>
     * <li>Parameter <tt>bId</tt> - bid Id (see {@link Bid#getId()})</li>
     * <li>Parameter <tt>iId</tt> - impression Id (see {@link Bid#getImpid()})</li>
     * <li>Parameter <tt>cId</tt> - campaign Id (see {@link Bid#getCid()})</li>
     * <li>Parameter <tt>crId</tt> - creative Id (see {@link Bid#getCrid()})</li>
     * <li>Parameter <tt>microBid</tt> - the bid price in micro-dollars</li>
     * <li>Parameter <tt>winningBid</tt> - bid which won (not always available). In micro$</li>
     * <li>Parameter <tt>reason</tt> - reason for losing bid (not always available)</li>
     * <li>Parameter <tt>exchange</tt> - exchange</li>
     * <li>Parameter <tt>msg</tt> - original message from the exchange</li>
     * <li>"M1" literal</li>
     * <li>Parameter <tt>xff</tt> - X-Forwarded-For header (could be user's, if NGINX is in front,
     * but if there's ELB in front of NGINX, it'll be ELB's).</li>
     * <li>{@link HttpServletRequest#getRemoteHost()} from parameter <tt>servletRequest</tt>, which
     * is user's, unless we have something in front like NGINX or ELB, in which case it's kind of
     * useless, it's theirs.</li>
     * <li>{@link HttpServletRequest#getRemotePort()} ditto</li>
     * <li>{@link HttpServletRequest#getRemoteAddr()}, ditto</li>
     * <li>Parameter <tt>xrip</tt> - X-Real-IP header (in case of Load-Balancer - NGINX - Lot49
     * setup, X-forwarded-for address would be the balancer's, IP would be NGINX's, and X-Real-IP
     * would be remote user's)</li>
     * <li>{@link Geo#getCity() city} based on {@link MaxMindFacade MaxMind lookup} from
     * <tt>xrip</tt></li>
     * <li>{@link Geo#getRegion() region}, ditto</li>
     * <li>{@link Geo#getZip() zip}, ditto</li>
     * <li>{@link Geo#getMetro() metro}, ditto</li>
     * <li>{@link Geo#getCountry() country}, ditto</li>
     * <li>Parameter <tt>ssp</tt> - SSP</li>
     * <li>Parameter <tt>brId</tt> - {@link OpenRtbRequest#getId() bid request ID}</li>
     * <li>Parameter <tt>originatingNodeId</tt> Node which originated the bid that resulted in this
     * event</li>
     * 
     * <li>{@link Orchestrator#getNodeId() node ID}</li>
     * <li>{@link Orchestrator#getRegion() region of the node}</li>
     * 
     * </ol>
     */
    public static void logLost(final Long bidSentOn, final String bId, final String iId,
                    final String cId, final String crId, final Long microBid, final Long winningBid,
                    final String reason, final String exchange, final String msg, String xff,
                    HttpServletRequest servletRequest, String xrip, String ssp, String brId,
                    String originatingNodeId) {
        final ServiceRunner bidder = Bidder.getInstance();
        final MaxMindFacade mm = bidder.getMaxMind();
        final Geo geo = mm == null ? new Geo() : mm.getGeo(xrip);

        lostLogger.info("",
                        // 1
                        getDate(),
                        // 2
                        BidderCalendar.getInstance().currentTimeMillis(),
                        // 3
                        bidSentOn,
                        // 4
                        bId,
                        // 5
                        iId,
                        // 6
                        cId,
                        // 7
                        crId,
                        // 8
                        microBid,
                        // 9
                        winningBid,
                        // 10
                        reason,
                        // 11
                        exchange,
                        // 12
                        msg,
                        // 13
                        "M1",
                        // 14
                        xff,
                        // 15
                        servletRequest == null ? "" : servletRequest.getRemoteHost(),

                        // 16
                        servletRequest == null ? "" : servletRequest.getRemotePort(),
                        // 17
                        servletRequest == null ? "" : servletRequest.getRemoteAddr(),
                        // 18
                        xrip,
                        // 19
                        geo == null ? null : geo.getCity(),
                        // 20
                        geo == null ? null : geo.getRegion(),
                        // 22
                        geo == null ? null : geo.getZip(),
                        // 22
                        geo == null ? null : geo.getMetro(),
                        // 23
                        geo == null ? null : geo.getCountry(),
                        // 24
                        ssp,
                        // 25
                        brId,
                        // 26
                        originatingNodeId,
                        // 27
                        getNodeId(),
                        // 28
                        getRegion());

    }

    /**
     * Logs the entire bid response to {@link #LOG_RESPONSE}. Logged at {@link Level#DEBUG} debug
     * level.
     */
    public static void logResponse(OpenRtbRequest req, OpenRtbResponse br, Object response) {
        if (responseLogger.getLevel().compareTo(Level.DEBUG) < 0) {
            return;
        }
        final Lot49Ext lot49Ext = req.getLot49Ext();
        final ExchangeAdapter adapter = lot49Ext.getAdapter();
        String exchange = adapter.getName();
        String ssp = lot49Ext.getSsp();

        StringBuilder msg = new StringBuilder();
        msg.append("\n================================================================================\n");
        msg.append(exchange).append("\t").append(ssp).append("\t").append(br.getId()).append("\n");
        try {
            msg.append(Utils.MAPPER.writeValueAsString(br));
        } catch (JsonProcessingException e) {
            msg.append("Could not serialize ").append(br).append(": ").append(e.getMessage())
                            .append(": ").append(e.getLocation());
        }

        for (SeatBid sb : br.getSeatbid()) {
            for (Bid b : sb.getBid()) {
                msg.append("\n--------------------------------------------------------------------------------\n");
                msg.append("Bid ID: ").append(b.getId()).append("\n");
                msg.append("SSL Required: ").append(lot49Ext.isSsl()).append("\n");
                msg.append("Ad markup:\n").append(b.getAdm()).append("\n");
                msg.append("NUrl: ").append(b.getNurl()).append("\n");
                msg.append("Tag object under NUrl:\n")
                                .append(lot49Ext.getBidIdToTagObject().get(b.getId())).append("\n");

                msg.append("Tag text under NUrl:\n")
                                .append(lot49Ext.getBidIdToTagText().get(b.getId())).append("\n");
                msg.append("\n--------------------------------------------------------------------------------\n");
            }
        }
        msg.append("\n---------------------------- EXCHANGE SPECIFIC ---------------------------------\n");
        if (response instanceof Message) {
            JsonFormat jf = new JsonFormat();
            msg.append(jf.printToString((Message) response));
        } else {
            msg.append(response);
        }
        msg.append("\n--------------------------------------------------------------------------------\n");
        msg.append("\n================================================================================\n");
        responseLogger.debug(msg.toString());
    }

    public static void logResponse(String exchange, String ssp, String bidRequestId, String bidId,
                    String tag) {
        if (responseLogger.getLevel().compareTo(Level.DEBUG) < 0) {
            return;
        }
        StringBuilder msg = new StringBuilder();
        msg.append("\n================================================================================\n");
        msg.append(exchange).append("\t").append(ssp).append("\t").append(bidRequestId)
                        .append("\n");

        msg.append("\n--------------------------------------------------------------------------------\n");
        msg.append("Bid ID: ").append(bidId).append("\n");
        msg.append(tag);
        msg.append("\n--------------------------------------------------------------------------------\n");

        msg.append("\n---------------------------- EXCHANGE SPECIFIC ---------------------------------\n");
        msg.append("N/A");
        msg.append("\n--------------------------------------------------------------------------------\n");
        msg.append("\n================================================================================\n");
        responseLogger.debug(msg.toString());
    }

    /**
     * Logs raw request, as JSON. Logged at "debug" level.
     * 
     * This logs the record with the following columns:
     * 
     * <ol>
     * <li>Human-readable timestamp</li>
     * <li>Unix timestamp</li>
     * <li>URL of this request</li>
     * <li>{@link OpenRtbRequest#getId() bid request ID}</li>
     * <li>"converted" as literal -- because this record is for a request already converted into
     * {@link OpenRtbRequest}, our internal format</li>
     * <li>Parameter <tt>req</tt> ({@link OpenRtbRequest}) converted to JSON</li>
     * </ol>
     * Also, if <tt>origReq</tt> parameter is not <tt>null</tt>, the new record is written, similar
     * to the above except:
     * <ol>
     * <li>"raw" is written instead of "converted"</li>
     * <li>Instead of <tt>req</tt>, <tt>origReq</tt> in a JSON format is written</li>
     * </ol>
     * 
     * @see LogUtils#LOG_RAW_REQUEST
     */
    public static final void logRawRequest(OpenRtbRequest req, Object origReq, UriInfo uriInfo) {
        // Ensure we only log once...
        if (req == null) {
            return;
        }
        if (!req.getLot49Ext().isRawRequestLogged()) {
            String jsonStr = "";
            try {
                jsonStr = Utils.MAPPER.writeValueAsString(req);
            } catch (Exception e) {
                jsonStr = e.toString();
            }
            rawRequestLogger.debug("", getDate(), BidderCalendar.getInstance().currentTimeMillis(),
                            uriInfo.getRequestUri(), req.getId(), "converted", jsonStr);

            if (origReq != null) {
                if (origReq instanceof Message) {
                    JsonFormat jf = new JsonFormat();
                    rawRequestLogger.debug("", getDate(),
                                    BidderCalendar.getInstance().currentTimeMillis(),
                                    uriInfo.getRequestUri(), req.getId(), "raw",
                                    jf.printToString((Message) origReq));

                } else if (origReq instanceof OpenRtbRequest) {
                    jsonStr = "";
                    try {
                        jsonStr = Utils.MAPPER.writeValueAsString((OpenRtbRequest) origReq);
                    } catch (Exception e) {
                        jsonStr = e.toString();
                    }
                    rawRequestLogger.debug("", getDate(),
                                    BidderCalendar.getInstance().currentTimeMillis(),
                                    uriInfo.getRequestUri(), req.getId(), "raw", jsonStr);
                } else {
                    rawRequestLogger.debug("", getDate(),
                                    BidderCalendar.getInstance().currentTimeMillis(),
                                    uriInfo.getRequestUri(), req.getId(), "raw", origReq);
                }
            }
            req.getLot49Ext().setRawRequestLogged(true);
        }
    }

    /**
     * <p>
     * Log every {@link Impression} request (could be multiple per actual {@link OpenRtbRequest bid
     * request}). Note that not all fields are logged at all levels. Check the documentation per
     * field. When not otherwise specified, everything here applies to INFO level.
     * </p>
     * This actually writes more than one log:
     * <ul>
     * <li>In case of <tt>didBid</tt> being false, entries to three logs are written:
     * <ol>
     * <li>{@link #LOG_REQUEST request log} which has hashed URLs (per standard business agreements
     * with publishers and exchanges that this information cannot be shared with third parties)</li>
     * <li>{@link #LOG_URLS URL log where actual URLs and their hashes are stored} which can be
     * referred to hashed URLs from the above one (per standard business agreements with publishers
     * and exchanges that this information cannot be shared with third parties)</li>
     * <li>{@link #LOG_DEBUG_REQ debug request log} which has extra information useful for
     * debugging.</li>
     * </ol>
     * </li>
     * <li>If <tt>didBid</tt> is true (this happens when we decide to bid, and happens after the
     * above, but much less frequently), a copy of the entry written earlier to {@link #LOG_REQUEST}
     * is now written to {@link #LOG_SESSION}. The other files are not written at this point.</li>
     * </ul>
     * <h3>Request log</h3>
     * <p>
     * The following fields are logged, in this order (remember that any <tt>null</tt> or empty
     * field is logged as {@link DelimLayout#getNullChar()}).
     * <ol>
     * <li>{@link Date#Date() Current date in human-readable format}. We are always assuming this to
     * be in UTC.</li>
     * <li>{@link System#currentTimeMillis() Unix epoch time}. We are always assuming this to be in
     * UTC.</li>
     * <li><tt>M0</tt>, as a literal.</li>
     * <li>Exchange (e.g., {@link Lot49Constants#EXCHANGE_OPENX openx}).</li>
     * <li>{@link OpenRtbRequest#getId() Bid request ID}</li>
     * <li>{@link Impression#getId() Impression ID}</li>
     * <li>{@link User#getBuyeruid() Our cookie}, after corrections. See below for original version.
     * </li>
     * <li>{@link User#getId() Exchange's user ID}</li>
     * <li>{@link User#getCustomdata() Custom data} we synced with Exchange on user</li>
     * <li>{@link Lot49ExtRemote#getUserSegments() User segments}, encrypted with
     * {@link Lot49Config#getLogKey()}, or plain text if that is null or empty.</li>
     * <li>{@link Geo#getCity() city} based on {@link Device#getGeo()}</li>
     * <li>{@link Geo#getRegion() region/state/province} based on {@link Device#getGeo()}</li>
     * <li>{@link Geo#getZip() zip/postal code} based on {@link Device#getGeo()}</li>
     * <li>{@link Geo#getLat() latitude} based on {@link Device#getGeo()}</li>
     * <li>{@link Geo#getLon() longitude} based on {@link Device#getGeo()}</li>
     * <li>{@link Geo#getMetro() metro code} based on {@link Device#getGeo()}</li>
     * 
     * <li>{@link Geo#getCity() city} based on {@link User#getGeo() user}</li>
     * <li>{@link Geo#getRegion() region/state/province} based on {@link User#getGeo() user}</li>
     * <li>{@link Geo#getZip() zip/postal code} based on {@link User#getGeo() user}</li>
     * <li>{@link Geo#getLat() latitude} based on {@link User#getGeo() user}</li>
     * <li>{@link Geo#getLon() longitude} based on {@link User#getGeo() user}</li>
     * <li>{@link Geo#getMetro() metro code} based on {@link User#getGeo() user}</li>
     * 
     * <li>{@link Geo#getCity() city} based on our own {@link Lot49Ext#getGeo() lookup}</li>
     * <li>{@link Geo#getRegion() region/state/province} based on our own {@link Lot49Ext#getGeo()
     * lookup}</li>
     * <li>{@link Geo#getZip() zip/postal code} based on our own {@link Lot49Ext#getGeo() lookup}
     * </li>
     * <li>{@link Geo#getLat() latitude} based on our own {@link Lot49Ext#getGeo() lookup}</li>
     * <li>{@link Geo#getLon() longitude} based on our own {@link Lot49Ext#getGeo() lookup}</li>
     * <li>{@link Geo#getMetro() metro code} based on our own {@link Lot49Ext#getGeo() lookup}</li>
     * 
     * <li>{@link User#getGender() Gender} (per Exchange's opinion)</li>
     * <li>{@link Device#getIp() IP address} of the user</li>
     * <li>{@link Device#getUa() User-agent} of the user's browser</li>
     * <li>M1 literal (marker)</li>
     * <li>{@link Device#getMake() Make} of user's device</li>
     * <li>{@link Device#getModel() Model} of user's device</li>
     * <li>{@link Device#getOs() OS} of user's device</li>
     * <li>{@link Device#getOsv() OS version} of user's device</li>
     * <li>{@link Device#getLanguage() Language} of a user's device</li>
     * <li>MD5 hash of the domain {@link Site#getDomain() Domain}</li>
     * <li>{@link Site#getCat() Categories} for site</li>
     * <li>{@link Site#getKeywords() Keywords} for site</li>
     * <li>{@link Site#getName() Name}</li>
     * <li>MD5-hashed {@link Site#getPage() Page URL}</li>
     * <li>{@link Site#getPagecat() Page category}</li>
     * <li>{@link Content#getCat() Content categories}</li>
     * <li>{@link Content#getKeywords() Content keywords}</li>
     * <li>{@link Content#getLanguage() Content language}</li>
     * <li>Type ("video" or "banner")</li>
     * <li>Size (width x height)</li>
     * <li>Mimes ({@link Banner#getMimes()} or {@link Video#getMimes()})</li>
     * <li>{@link Video#getApi()} or {@link Banner#getApi()}
     * <li>{@link Video#getLinearity() linearity}</li>
     * <li>{@link Video#getProtocols() protocols}</li>
     * <li>{@link Video#getMinduration() min duration}</li>
     * <li>{@link Video#getMaxduration() max duration}</li>
     * <li>{@link Lot49Ext#getxForwardedFor() X-Forwarded-For header value}</li>
     * <li>{@link Geo#getCountry() Country} from {@link Device}</li>
     * <li>{@link Geo#getCountry() Country} from {@link User}</li>
     * <li>{@link Geo#getCountry() Country} looked up by us</li>
     * <li>{@link Site#getSectioncat() Section} categories</li>
     * <li>{@link Lot49ExtGeo#getTz() timezone}</li>
     * <li>{@link Lot49ExtGeo#getDom() user's domain}</li>
     * <li>{@link Lot49ExtGeo#getIsp() user's ISP}</li>
     * <li>{@link Lot49ExtGeo#getOrg() organization}</li>
     * <li>{@link Lot49ExtGeo#getConn() connection type}</li>
     * <li>{@link Lot49Ext#isTest() test?}</li>
     * <li>Literal "X1"</li>
     * <li>Literal marker "M2".</li>
     * <li>{@link Lot49CustomData#getUdat() User-data stored at Exchange and passed to us.}</li>
     * <li>{@link Lot49CustomData#getUid()}</li>
     * <li>@param candidates -- how many candidates in {@link BidCandidateManager}, that is, how
     * many ads we are considering eligible for this request, pending data request.</li>
     * <li>Literal "M3".</li>
     * <li>{@link Lot49Ext#getRealIpHeader()}</li>
     * <li>{@link Lot49Ext#getRemoteHost()}</li>
     * <li>{@link Lot49Ext#getRemotePort()}</li>
     * <li>{@link Lot49Ext#getRemoteAddr()}</li>
     * <li>{@link OpenRtbRequest#getExt() ext} object as is</li>
     * <li>Literal "M4".</li>
     * <li>Device type (see
     * <a href= "http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf" >section 6.16
     * of OpenRTB spec</a>.)</li>
     * <li>{@link Lot49ExtGeo#getUserTs() User's Unix timestamp}</li>
     * <li>{@link Lot49ExtGeo#getUserDow() User's day of week}</li>
     * <li>{@link Lot49ExtGeo#getBidderDow() Bidder's day of week}</li>
     * <li>{@link Lot49ExtGeo#getUserHour() User's hour}</li>
     * <li>{@link Lot49ExtGeo#getBidderHour() Bidder's hour}</li>
     * <li>{@link Lot49ExtGeo#getUserTsStr() User's timestamp as string}</li>
     * <li>Literal "M5"</li>
     * <li>SSP (actual exchange if the request is from an {@link ExchangeAdapter#isAggregator()
     * aggregator}, or same as exchange).</li>
     * <li>forceCookieReset</li>
     * <li>forceCookieResync</li>
     * <li>receivedBuyerUid</li>
     * <li>Literal "X2"</li>
     * <li>Literal "M6"</li>
     * <li>Integral Ad Science's data</li>
     * <li>{@link ExchangeAdapter#getBidChoiceAlgorithm() bid choice algo}</li>
     * 
     * <li>{@link Lot49Ext#getBrowserName() browser name}</li>
     * <li>{@link Lot49Ext#getBrowserFamily() browser family}</li>
     * <li>{@link Lot49Ext#getProviderInfo() third party provider information}</li>
     * 
     * <li>{@link SkyhookInfoReceived#getCats() skyhook categories}</li>
     * 
     * <li>"M7" as a literal</li>
     * <li>{@link Lot49Ext#isSsl() whether SSL was required}</li>
     * <li>{@link Lot49Ext#getExcludedMarkups() excluded markups}, as a list (see {@link MarkupType}
     * ).</li>
     * <li>{@link Impression#getBidfloor() Bid floor}</li>
     * <li><a href="http://www.lexa.ru/programs/mod-uid-eng.html">mod_uid encoding</a> of
     * {@link User#getBuyeruid() cookie.}</li>
     * <li>"M8" as a literal</li>
     * <li>{@link Lot49ExtGeo#getRegions() other region names}</li>
     * <li>{@link PMP Private deals info}</li>
     * <li>{@link Orchestrator#getNodeId() node ID}</li>
     * <li>{@link Orchestrator#getRegion() region of the node}</li>
     * <li>User segments as a comma-separated list each encrypted with an advertiser's key.</li>
     * <li>"M9" as a literal</li>
     * <li>{@link App#getName() App name}</li>
     * <li>{@link App#getBunble() App bundle ID}</li>
     * </ol>
     * <h3>URL log</h3> {@link #LOG_URLS} is written with the following fields:
     * <ol>
     * <li>{@link Date#Date() Current date in human-readable format}. We are always assuming this to
     * be in UTC.</li>
     * <li>{@link System#currentTimeMillis() Unix epoch time}. We are always assuming this to be in
     * UTC.</li>
     * <li>Server ID</li>
     * <li>Exchange</li>
     * <li>Bid request ID</li>
     * <li>Impression ID</li>
     * <li>URL</li>
     * <li>MD5 hash of the URL</li>
     * <li>SSP (actual exchange if the request is from an {@link ExchangeAdapter#isAggregator()
     * aggregator}, or same as exchange).</li>
     * <li>{@link User#getBuyeruid() Our cookie}</li>
     * <li>{@link User#getId() Exchange's user ID}</li>
     * <li>{@link User#getCustomdata() Custom data} we synced with Exchange on user</li>
     * <li><a href="http://www.lexa.ru/programs/mod-uid-eng.html">mod_uid encoding</a> of
     * {@link User#getBuyeruid() cookie.}</li>
     * <li>Domain</li>
     * <li>MD5 hash of the Domain</li>
     * </ol>
     * <h3>Debug request log</h3> {@link #LOG_DEBUG_REQ} is written with following fields:
     * <ol>
     * <li>{@link Date#Date() Current date in human-readable format}. We are always assuming this to
     * be in UTC.</li>
     * <li>{@link System#currentTimeMillis() Unix epoch time}. We are always assuming this to be in
     * UTC.</li>
     * <li>{@link OpenRtbRequest#getId() Bid request ID}</li>
     * <li>{@link Impression#getId() Impression ID}</li>
     * <li>{@link Lot49Ext#getOptoutReasons() optout reasons}</li>
     * <li>Literal "M1"</li>
     * <li>{@link Lot49Ext#getComments() comments}</li>
     * <li>Literal "M2"</li>
     * </ol>
     */

    public static void logRequest(final OpenRtbRequest req, final boolean didBid,
                    final int candidates) {
        Device device = req.getDevice();
        if (device == null) {
            device = new Device();
        }
        Geo deviceGeo = device.getGeo();
        if (deviceGeo == null) {
            deviceGeo = new Geo();
        }
        User user = req.getUser();
        if (user == null) {
            user = new User();
        }
        Geo userGeo = user.getGeo();

        if (userGeo == null) {
            userGeo = new Geo();
        }
        Site site = req.getSite();
        if (site == null) {
            site = new Site();
        }
        Content content = site.getContent();
        if (content == null) {
            content = new Content();
        }

        App app = req.getApp();
        if (app == null) {
            app = new App();
        }

        final Lot49Ext lot49Ext = req.getLot49Ext();

        final ExchangeAdapter adapter = lot49Ext.getAdapter();
        final String exchange = adapter == null ? "" : adapter.getName();
        String ssp = adapter == null ? "" : (adapter.isAggregator() ? lot49Ext.getSsp() : exchange);

        final Lot49ExtRemote lot49ExtRemote = lot49Ext.getLot49ExtRemote();

        Geo lot49Geo = lot49Ext.getGeo();
        if (lot49Geo == null) {
            lot49Geo = new Geo();
        }
        Lot49ExtGeo lot49ExtGeo = lot49Geo.getExt() == null ? null
                        : (Lot49ExtGeo) lot49Geo.getExt().get(Lot49ExtGeo.GEO_EXT_KEY);
        if (lot49ExtGeo == null) {
            lot49ExtGeo = new Lot49ExtGeo();
        }

        Lot49CustomData lot49CustomData = lot49Ext.getLot49CustomData();
        if (lot49CustomData == null) {
            lot49CustomData = new Lot49CustomData();
        }

        final SkyhookInfoReceived skyInfo = (SkyhookInfoReceived) lot49Ext.getProviderInfo()
                        .get(SkyhookProvider.SKYHOOK_PROVIDER_NAME);

        final Set<Integer> skyCats = skyInfo == null ? null : skyInfo.getCats();

        final IntegralInfoReceived integralInfo = (IntegralInfoReceived) lot49Ext.getProviderInfo()
                        .get(IntegralService.INTEGRAL_PROVIDER_NAME);
        String integralData =
                        "INTEGRAL_TARGETING["
                                        + (integralInfo == null ? "]"
                                                        : integralInfo.getTargetingStatus()
                                                                        + "]response=")
                                        + (integralInfo == null ? ""
                                                        : integralInfo.getResponseJson());

        for (Impression imp : req.getImp()) {
            String type = null;
            String size = null;
            List<String> mimes = null;
            List<String> api = null;
            String linearity = null;
            List<String> protocols = new ArrayList<String>();
            int minDuration = 0;
            int maxDuration = 0;
            Banner b = imp.getBanner();

            Video v = imp.getVideo();
            if (b != null) {
                type = "banner";
                size = b.getW() + "x" + b.getH();
                mimes = b.getMimes();
                if (b.getApi() != null) {
                    api = Utils.apisToString(b.getApi());
                }
            } else if (v != null) {
                type = "video";
                size = v.getW() + "x" + v.getH();
                mimes = v.getMimes();
                if (v.getApi() != null) {
                    api = Utils.apisToString(v.getApi());
                }
                linearity = v.getLinearity() == RtbConstants.LINEARITY_LINEAR ? "linear"
                                : "nonlinear";
                List<Integer> protos = v.getProtocols();
                if (protos == null) {
                    protos = new ArrayList<Integer>();
                }
                if (v.getProtocol() != 0) {
                    protos.add(v.getProtocol());
                }
                protocols = Utils.protosToString(protos);
                minDuration = v.getMinduration();
                maxDuration = v.getMaxduration();
            }
            final Map ext = b != null ? b.getExt() : (v != null ? v.getExt() : null);
            final String xff = req.getLot49Ext().getxForwardedFor();

            MessageDigest md = null;
            final String domain = site.getDomain();
            final String url = site.getPage();

            if (url != null || domain != null) {
                try {
                    md = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException nsae) {
                    LogUtils.error("Error getting MD5 digest ", nsae);
                    return;
                }
            }

            byte urlBytes[] = null;
            if (url != null) {
                try {
                    urlBytes = url.getBytes("UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    // Should never happen
                    LogUtils.error("Error getting bytes from " + url, uee);

                }
            }
            final byte[] urlHash = (urlBytes == null) ? null : md.digest(urlBytes);

            byte domainBytes[] = null;
            if (domain != null) {
                try {
                    domainBytes = domain.getBytes("UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    // Should never happen
                    LogUtils.error("Error getting bytes from " + domain, uee);
                }
            }
            final byte[] domainHash = (domainBytes == null) ? null : md.digest(domainBytes);


            final Level logLevel = requestLogger.getLevel();

            StringBuilder dealsStr = new StringBuilder();
            PMP pmp = imp.getPmp();
            if (pmp != null) {
                List<Deal> deals = pmp.getDeals();
                if (deals != null && deals.size() > 0) {
                    dealsStr.append("{privateAuction:").append(pmp.getPrivate_auction())
                                    .append(", deals:");
                    StringBuilder dd = new StringBuilder();
                    for (Deal deal : deals) {
                        if (dd.length() > 0) {
                            dd.append(", ");
                        }
                        dd.append("{ id: ").append(deal.getId()).append(",");
                        dd.append("auctionType").append(deal.getAt()).append(",");
                        dd.append("floor: ").append(deal.getBidfloor()).append(",");
                        dd.append("cur: ").append(deal.getBidfloorcur()).append(",");
                        dd.append("wadomain: ").append(deal.getWadomain()).append(",");
                        dd.append("wseat: ").append(deal.getWseat()).append(",");
                        dd.append("ext: ").append(deal.getExt()).append(" }");

                    }
                    dealsStr.append(dd.toString());
                    dealsStr.append("]}");
                }
            }

            UserSegments userSegments = lot49ExtRemote.getUserSegments();

            final Object[] logParams = new Object[] {
                            // 1
                            getDate(),
                            // 2
                            BidderCalendar.getInstance().currentTimeMillis(),
                            // 3
                            "M0",
                            // 4
                            exchange,
                            // 5
                            req.getId(),
                            // 6
                            imp.getId(),
                            // 7
                            user.getBuyeruid(),
                            // 8
                            user.getId(),
                            // 9
                            user.getCustomdata(),
                            // 10
                            new InternalSegmentEncoder(userSegments).getEncoded(),
                            // 11
                            deviceGeo.getCity(),
                            // 12
                            deviceGeo.getRegion(),
                            // 13
                            deviceGeo.getZip(),
                            // 14
                            deviceGeo.getLat(),
                            // 15
                            deviceGeo.getLon(),
                            // 16
                            deviceGeo.getMetro(),
                            // 17
                            userGeo.getCity(),
                            // 18
                            userGeo.getRegion(),
                            // 19
                            userGeo.getZip(),
                            // 20
                            userGeo.getLat(),
                            // 21
                            userGeo.getLon(),
                            // 22
                            userGeo.getMetro(),
                            // 23
                            lot49Geo.getCity(),
                            // 24
                            lot49Geo.getRegion(),
                            // 25
                            lot49Geo.getZip(),
                            // 26
                            lot49Geo.getLat(),
                            // 27
                            lot49Geo.getLon(),
                            // 28
                            lot49Geo.getMetro(),
                            // 29
                            user.getGender(),
                            // 30
                            device.getIp(),
                            // 31
                            device.getUa(),
                            // 32
                            "M1",
                            // 33
                            device.getMake(),
                            // 34
                            device.getModel(),
                            // 35
                            device.getOs(),
                            // 36
                            device.getOsv(),
                            // 37
                            device.getLanguage(),
                            // 38
                            didBid ? domain : domainHash,
                            // 39
                            site.getCat(),
                            // 40
                            site.getKeywords(),
                            // 41
                            site.getName(),
                            // 42
                            didBid ? url : urlHash,
                            // 43
                            site.getPagecat(),
                            // 44
                            content.getCat(),
                            // 45
                            content.getKeywords(),
                            // 46
                            content.getLanguage(),
                            // 47
                            type,
                            // 48
                            size,
                            // 49
                            mimes,
                            // 50
                            api,
                            // 51
                            linearity,
                            // 52
                            protocols,
                            // 53
                            minDuration,
                            // 54
                            maxDuration,
                            // 55
                            xff,
                            // 56
                            deviceGeo.getCountry(),
                            // 57
                            userGeo.getCountry(),
                            // 58
                            lot49Geo.getCountry(),
                            // 59
                            site.getSectioncat(),
                            // 60
                            lot49ExtGeo.getTz(),
                            // 61
                            lot49ExtGeo.getDom(),
                            // 62
                            lot49ExtGeo.getIsp(),
                            // 63
                            lot49ExtGeo.getOrg(),
                            // 64
                            lot49ExtGeo.getConn(),
                            // 65
                            lot49Ext.isTest(),
                            // 66
                            "X1",
                            // 67
                            "M2",
                            // 68
                            lot49CustomData.getUdat(),
                            // 69
                            lot49CustomData.getUid(),
                            // 70
                            candidates,
                            // 71
                            "M3",
                            // 72
                            lot49Ext.getRealIpHeader(),
                            // 73
                            lot49Ext.getRemoteHost(),
                            // 74
                            lot49Ext.getRemotePort(),
                            // 75
                            lot49Ext.getRemoteAddr(),
                            // 76
                            ext,
                            // 77
                            "M4",
                            // 78
                            device.getDevicetype(),
                            // 79
                            lot49ExtGeo.getUserTs(),
                            // 80
                            lot49ExtGeo.getUserDow(),
                            // 81
                            lot49ExtGeo.getBidderDow(),
                            // 82
                            lot49ExtGeo.getUserHour(),
                            // 83
                            lot49ExtGeo.getBidderHour(),
                            // 84
                            lot49ExtGeo.getUserTsStr(),
                            // 85
                            "M5",
                            // 86
                            ssp,
                            // 87
                            lot49Ext.isForceCookieReset(),
                            // 88
                            lot49Ext.isForceCookieResync(),
                            // 89
                            lot49Ext.getReceivedBuyerUid(),
                            // 90
                            "X2",
                            // 91
                            "M6",
                            // 92
                            integralData,
                            // 93
                            adapter == null ? "" : adapter.getBidChoiceAlgorithm(),
                            // 94
                            lot49Ext.getBrowserFamily(),
                            // 95
                            lot49Ext.getBrowserName(),
                            // 96
                            lot49Ext.getProviderInfo(),
                            // 97
                            skyCats,
                            // 98
                            "M7",
                            // 99
                            lot49Ext.isSsl(),
                            // 100
                            lot49Ext.getExcludedMarkups(),
                            // 101
                            imp == null ? "0" : imp.getBidfloor(),
                            // 102
                            lot49Ext.getModUid(),
                            // 103
                            "M8",
                            // 104
                            lot49ExtGeo.getRegions(),
                            // 105
                            dealsStr.toString(),
                            // 106
                            getNodeId(),
                            // 107
                            getRegion(),
                            // 108
                            new ExternalSegmentEncoder(userSegments).getEncoded(),
                            // 109
                            "M9",
                            // 110
                            app.getName(),
                            // 111
                            app.getBundle()};

            if (didBid) {
                sessionLogger.info("", logParams);
            } else {
                requestLogger.info("", logParams);
            }
            urlsLogger.info("",
                            // 1
                            getDate(),
                            // 2
                            BidderCalendar.getInstance().currentTimeMillis(),
                            // 3
                            ServiceRunner.getInstance().getOrchestrator().getNodeId(),
                            // 4
                            exchange,
                            // 5
                            req.getId(),
                            // 6
                            imp.getId(),
                            // 7
                            url,
                            // 8
                            urlHash,
                            // 9
                            ssp,
                            // 10
                            user.getBuyeruid(),
                            // 11
                            user.getId(),
                            // 12
                            user.getCustomdata(),
                            // 13
                            lot49Ext.getModUid(),
                            // 14
                            domain,
                            // 15,
                            domainHash);

            debugReqLogger.debug("",
                            // 1
                            getDate(),
                            // 2
                            BidderCalendar.getInstance().currentTimeMillis(),
                            // 3
                            req.getId(),
                            // 4
                            imp.getId(),
                            // 5
                            lot49Ext.getOptoutReasons().toString(),
                            // 6
                            "M1",
                            // 7
                            lot49Ext.getComments().toString(),
                            // 8
                            "M2");

        }

    }


    /**
     * Log at this level or a finer one.
     */
    private static final Object logAt(Level curLevel, Level expectedLevel, Object msg) {
        if (curLevel.isLessSpecificThan(expectedLevel) || curLevel.equals(expectedLevel)) {
            return msg;
        } else {
            return (curLevel + "_LEVEL_NOT_ENOUGH");
        }
    }

    public static void warn(Object msg) {
        mainLogger.warn(msg);
    }

    public static void warn(Object msg, Throwable t) {
        mainLogger.warn(msg, t);
    }

    public static String getPhaseOfDecision(String decisionReason) {
        String phase = "";
        if (decisionReason != null && !decisionReason.trim().isEmpty()
                        && decisionReason.trim().length() > 1) {
            phase = decisionReason.substring(0, 2);
        }
        return phase;
    }

    public static String getStepOfDecision(String decisionReason) {
        String step = "";
        if (decisionReason != null && !decisionReason.trim().isEmpty()
                        && decisionReason.trim().length() > 4) {
            step = decisionReason.substring(3, 5);
        }
        return step;
    }

}
