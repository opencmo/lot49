package com.enremmeta.rtb.jersey;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Interval;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.LostAuctionTask;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.ClientConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.Macros;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapter.BidChoiceAlgorithm;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.rtb.proto.openx.AuctionResult;
import com.enremmeta.rtb.proto.openx.AuctionResultMessage;
import com.enremmeta.rtb.proto.openx.OpenXAdapter;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.google.protos.adx.NetworkBid;

/**
 * Services to react to things that happen after a successful bid. Called <tt>StatsSvc</tt> because
 * this is where logging of most of important statistics happens.
 * 
 * @author <a href="mailto:grisha@alum.mit.edu">Gregory Golberg</a>
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
@Path(Lot49Constants.ROOT_PATH_STATS)
public class StatsSvc implements JerseySvc {

    private static final Map<String, Set<String>> wins = new HashMap<String, Set<String>>();
    private static final Map<String, Set<String>> imps = new HashMap<String, Set<String>>();

    private static final int maxCookieAge = Bidder.getInstance().getConfig().getMaxCookieAge();

    @GET
    @Path(Lot49Constants.DEFAULT_CLICK_PATH_RELATIVE)
    public final Object click(final @Context UriInfo uriInfo, final @QueryParam("xch") String xch,
                    final @QueryParam("ssp") String ssp, final @QueryParam("cid") String cId,
                    final @QueryParam("crid") String crId, final @QueryParam("bid") String bId,
                    final @QueryParam("brid") String brId, final @QueryParam("iid") String iId,
                    final @QueryParam("r") String redir,
                    final @HeaderParam("Cookie") String cookies,
                    final @HeaderParam("referer") String ref,
                    final @HeaderParam("user-agent") String ua,
                    final @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam("nodeId") String nodeId) {
        LogUtils.info("Entering click: " + uriInfo.getRequestUri());
        LogUtils.logAccess(uriInfo);
        final String cookie = getMyCookie(cookies);
        String modUid = null;
        if (cookie != null) {
            try {
                modUid = Utils.cookieToLogModUid(cookie);
            } catch (Exception e) {
                modUid = "ERROR_DECODING_COOKIE(" + cookie + ", " + e.getClass().getName();
                StackTraceElement stes[] = e.getStackTrace();
                modUid += ", " + stes[0].getFileName() + ":" + stes[0].getLineNumber();

            }
        }

        LogUtils.logClick(xch, cookie, bId, iId, cId, crId, redir, ref, ua, xff, servletRequest,
                        xrip, modUid, uriInfo.getRequestUri(), brId, ssp, cookies, nodeId);

        final String cookieValue = String.valueOf(BidderCalendar.getInstance().currentTimeMillis());
        final Cookie rstCookie = new Cookie(KVKeysValues.COOKIE_TS_CLICK + cId, cookieValue, "/",
                        getCookieDomain(cId), 2);
        final NewCookie newRstCookie = new NewCookie(rstCookie, cookieValue, maxCookieAge, false);
        if (redir != null) {
            try {
                return Response.seeOther(new URI(redir)).cacheControl(NO_CACHE).cookie(newRstCookie)
                                .build();
            } catch (URISyntaxException e) {
                LogUtils.error("Bad redirect " + redir, e);
                return Response.status(Status.BAD_REQUEST).cacheControl(NO_CACHE)
                                .cookie(newRstCookie).build();
            }
        }
        return Response.ok(PIXEL_BYTES, "image/gif").cacheControl(NO_CACHE).build();
    }

    private final String getCookieDomain(final String adId) {
        final ServiceRunner bidder = Bidder.getInstance();
        final AdCache adCache = bidder.getAdCache();
        final Ad ad = adCache.getAd(adId);
        final Lot49Config bidderConfig = bidder.getConfig();
        final String defaultCookieDomain = bidderConfig.getCookieDomain();
        if (ad == null) {
            return defaultCookieDomain;
        }
        final String clientId = ad.getClientId();
        if (clientId == null) {
            return defaultCookieDomain;
        }

        final Map<String, ClientConfig> clientConfig = bidderConfig.getClients();
        if (clientConfig == null) {
            return defaultCookieDomain;
        }

        final ClientConfig curClientConfig = clientConfig.get(clientId);
        if (curClientConfig == null) {
            return defaultCookieDomain;
        }
        return curClientConfig.getCookieDomain() == null ? defaultCookieDomain
                        : curClientConfig.getCookieDomain();
    }

    public static final CacheControl NO_CACHE = new CacheControl();

    static {
        NO_CACHE.setNoCache(true);
    }

    /**
     * Cookie-related handler. Most important parameter is <tt>phase</tt>, others are mostly for
     * logging.
     * 
     * @param phase
     *            at the moment, one of the following:
     *            <ul>
     *            <li>rest - RESET - creates a new cookie (see {@link Utils#createModUidCookie()})
     *            and sends it as a response with a redirect to self with a <tt>sync</tt> phase</li>
     *            <li>sync - SYNCHRONIZE - sends a redirect to the
     *            {@link ExchangeAdapter#getPartnerInitiatedSyncUrl(String) partner-initiated sync
     *            URI} for the given exchange, if available. Otherwise, just returns a pixel.</li>
     *            </ul>
     * 
     * @see ExchangeAdapter#getPartnerInitiatedSyncUrl(String)
     * 
     * @see Utils#createModUidCookie()
     * 
     * @see Utils#cookieToLogModUid(String)
     * 
     * @see Utils#logToCookieModUid(String)
     */
    @GET
    @Path(Lot49Constants.DEFAULT_PROUST_PATH_RELATIVE)
    public final Object proust(final @Context UriInfo uriInfo, final @QueryParam("xch") String xch,
                    final @QueryParam("ssp") String ssp, final @QueryParam("cid") String cId,
                    final @QueryParam("crid") String crId, final @QueryParam("bid") String bId,
                    final @QueryParam("iid") String iId, final @QueryParam("brid") String brId,
                    final @QueryParam("fcr") String fcr, final @QueryParam("phase") String phase,
                    final @HeaderParam("referer") String ref,
                    final @HeaderParam("Cookie") String cookies,
                    final @HeaderParam("user-agent") String ua,
                    final @QueryParam("custom") String custom,
                    final @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam(Lot49Constants.LOT49_VERSION_KEY) String lot49Version,
                    final @QueryParam("nodeId") String nodeId) {
        LogUtils.info("Entering proust: " + uriInfo.getRequestUri());
        LogUtils.logAccess(uriInfo);
        String cookie = getMyCookie(cookies);
        String modUid = null;
        if (cookie != null) {
            try {
                modUid = Utils.cookieToLogModUid(cookie);
            } catch (Exception e) {
                modUid = e.getClass().getName();
            }
        }

        boolean fcrp = fcr != null && (fcr.equalsIgnoreCase("yes") || fcr.equalsIgnoreCase("true")
                        || fcr.equalsIgnoreCase("on") || fcr.equals("1"));
        ResponseBuilder respBuilder = null;
        String newCookieValue = null;
        String redir = null;

        if (phase == null) {
            // Why are we even here?
            respBuilder = Response.status(Status.BAD_REQUEST).entity("Empty phase");
        } else {

            switch (phase.toLowerCase()) {
                // rest phase: means "reset" a cookie. Create a new cookie.
                case "rest":

                    StringBuilder redirBuilder = new StringBuilder();
                    redirBuilder.append(StringUtils.replace(uriInfo.getBaseUri().toString(),
                                    "http:", "https:", 1));
                    redirBuilder.append(uriInfo.getPath());
                    redirBuilder.append("?");
                    redirBuilder.append(Lot49Constants.LOT49_VERSION_KEY).append("=")
                                    .append(Lot49Constants.LOT49_VERSION_VALUE).append("&base=")
                                    .append(URLEncoder.encode(uriInfo.getBaseUri().toString()))
                                    .append("&");
                    redirBuilder.append("xch=").append(xch).append("&ssp=").append(ssp)
                                    .append("&cid=").append(cId).append("&crid=").append(crId)
                                    .append("&bid=").append(bId).append("&iid=").append(iId)
                                    .append("&brid=").append(URLEncoder.encode(brId))
                                    .append("&fcr=").append(fcr).append("&custom=").append(custom)
                                    .append("&phase=sync");
                    redir = redirBuilder.toString();

                    URI redirUri;
                    try {
                        redirUri = new URI(redir);
                        respBuilder = Response.seeOther(redirUri);
                    } catch (URISyntaxException e) {
                        LogUtils.error("Error: " + redir, e);
                        respBuilder = Response.serverError();
                    }

                    if (fcrp) {
                        // non-urlSafe cookie for setting to user
                        newCookieValue = Utils.createModUidCookie(false);
                        final Cookie rstCookie = new Cookie(USER_ID_COOKIE, newCookieValue, "/",
                                        getCookieDomain(cId), 2);
                        final NewCookie newRstCookie = new NewCookie(rstCookie, newCookieValue,
                                        maxCookieAge, false);
                        respBuilder = respBuilder.cookie(newRstCookie);
                    }
                    break;
                case "sync":
                    if (cookie == null) {
                        // This really means the browser refuses cookies...
                        // TODO What do we want to do about it?
                        LogUtils.debug("PROUST GOT EMPTY COOKIE from IP " + xrip + " (" + xch
                                        + ")");
                        respBuilder = Response.ok(PIXEL_BYTES, "image/gif").cacheControl(NO_CACHE);
                    } else {
                        final ExchangeAdapter adapter =
                                        ExchangeAdapterFactory.getExchangeAdapter(xch);
                        // urlSafe cookie for syncing with exchange
                        cookie = Utils.logToCookieModUid(modUid, true);
                        redir = adapter.getPartnerInitiatedSyncUrl(cookie);
                        LogUtils.debug("Partner-initiated sync url for " + cookie + " for " + xch
                                        + " is " + redir);
                        if (redir != null) {
                            try {
                                redirUri = new URI(redir);
                                respBuilder = Response.seeOther(redirUri);
                            } catch (URISyntaxException e) {
                                LogUtils.error("Error: " + redir, e);
                                respBuilder = Response.serverError();
                            }
                        } else {
                            respBuilder = Response.ok(PIXEL_BYTES, "image/gif")
                                            .cacheControl(NO_CACHE);
                        }
                    }
                    break;
                default:
                    respBuilder = Response.status(Status.BAD_REQUEST).entity("Bad phase: " + phase);
            }
        }

        LogUtils.logProust(cookie, newCookieValue, xch, bId, iId, cId, crId, ref, ua, xff,
                        servletRequest, xrip, modUid, uriInfo.getRequestUri(), brId, ssp, fcr,
                        phase, redir, cookies, lot49Version, nodeId);
        return respBuilder.cacheControl(NO_CACHE).build();

    }

    @GET
    @Path(Lot49Constants.DEFAULT_TEST_PATH_RELATIVE)
    public final Object test(final @Context UriInfo uriInfo,
                    final @HeaderParam("Cookie") String cookies,
                    final @HeaderParam("user-agent") String ua, final @QueryParam("cId") String cId,
                    final @Context HttpHeaders hh) {
        String msg = "In /test";
        MultivaluedMap<String, String> m = hh.getRequestHeaders();
        msg += "\n\tHeaders: " + m;
        String cookie = getMyCookie(cookies);
        msg += "\n\tMy cookie: " + cookie;
        ResponseBuilder respBuilder = Response.ok(PIXEL_BYTES, "image/gif").cacheControl(NO_CACHE);
        if (cookie == null) {
            msg += "\n\tNo cookie found, will set new one";
            NewCookie newCookie = null;
            String cookieDomain = getCookieDomain(cId);
            msg += "\n\tCookie domain: " + cookieDomain;
            String newCookieValue = Utils.createModUidCookie();
            msg += "\n\tCookie value: " + newCookieValue;
            final Cookie rC = new Cookie(USER_ID_COOKIE, newCookieValue, "/", cookieDomain, 2);
            newCookie = new NewCookie(rC, newCookieValue, maxCookieAge, false);
            LogUtils.debug("Setting new cookie " + newCookie + " because did not find ours in "
                            + cookies);
            respBuilder = respBuilder.cookie(newCookie);
        }
        LogUtils.info(Lot49Constants.DEFAULT_TEST_PATH_RELATIVE + ": " + msg);
        return respBuilder.build();
    }

    @GET
    @Path(Lot49Constants.DEFAULT_REDIR_PATH_RELATIVE)
    public final Object redir(final @Context UriInfo uriInfo, final @QueryParam("r") String redir) {

        ResponseBuilder respBuilder;
        if (redir != null) {
            try {
                respBuilder = Response.seeOther(new URI(redir));

            } catch (URISyntaxException e) {
                LogUtils.error("Bad redirect " + redir, e);
                respBuilder = Response.status(Status.BAD_REQUEST);

            }
        } else {
            LogUtils.error("Empty redirect.");
            respBuilder = Response.status(Status.BAD_REQUEST);
        }
        Response resp = respBuilder.cacheControl(NO_CACHE).build();
        LogUtils.logRedir(uriInfo.getRequestUri().toString(), redir, resp.getStatus());
        return resp;
    }

    /**
     * Will assume:
     * <ul>
     * <li>using of in-line ({@link Bid#getAdm() ad markup}) mechanism for now, as opposed to
     * {@link Bid#getNurl()}</li>
     * <li><tt>adid</tt> and <tt>crid</tt> is {@link Tag#getId()}.
     * <li><tt>cid</tt> is {@link Ad#getId()}.
     * </ul>
     * 
     * @see <a href= "http://www.iab.net/media/file/OpenRTB-API-Specification-Version-2-1.pdf">
     *      OpenRTB 2.1 spec</a>, item 4.5
     */
    @GET
    @Path(Lot49Constants.DEFAULT_IMPRESSION_PATH_RELATIVE)
    public final Object impression(final @Context UriInfo uriInfo,

                    final @QueryParam("xch") String exchange, final @QueryParam("ssp") String ssp,
                    final @QueryParam("wp") String wp, final @QueryParam("cid") String cId,
                    final @QueryParam("crid") String crId, final @QueryParam("bid") String bId,
                    final @QueryParam("iid") String iId, final @QueryParam("brid") String brId,
                    final @QueryParam("bp") String bp,
                    final @QueryParam("ts") long bidCreatedTimestamp, @QueryParam("r") String redir,
                    final @QueryParam("nurl") int nurl,
                    final @QueryParam("fcr") String forceCookieReset,
                    final @QueryParam("fcrx") String forceCookieResync,
                    final @HeaderParam("referer") String ref,
                    final @HeaderParam("Cookie") String cookies,
                    final @HeaderParam("user-agent") String ua,
                    final @QueryParam("custom") String custom,
                    final @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip, final @Context HttpHeaders hh,
                    final @QueryParam("buid") String buyerUid,
                    final @QueryParam("nodeId") String nodeId) {
        long t0 = BidderCalendar.getInstance().currentTimeMillis();
        LogUtils.info("Entering impression: " + (uriInfo == null ? null : uriInfo.getRequestUri()));
        LogUtils.logAccess(uriInfo);
        try {
            Set<String> x = Utils.incrMapSet(imps, brId, uriInfo.getRequestUri().toString() + " @ "
                            + BidderCalendar.getInstance().currentTimeMillis());

            String what = "imps";
            if (nurl == 0) {
                what = "imps/wins";
            }
            if (x.size() > 1) {
                LogUtils.warn("impression(): Multiple " + what + " for " + brId + ": " + x);
            }

            final ExchangeAdapter adapter = ExchangeAdapterFactory.getExchangeAdapter(exchange);
            ParsedPriceInfo priceInfo =
                            nurl == 0 ? parsePriceInformation(adapter, bp, wp, bId, cId, crId)
                                            : new ParsedPriceInfo();

            if (priceInfo.getStatus() != null) {
                return Response.status(priceInfo.getStatus()).build();
            }

            boolean forceCookieResyncBool = Utils.isTrue(forceCookieResync);

            String cookie = getMyCookie(cookies);
            if (cookie == null) {
                LogUtils.debug("No cookie for " + brId + ", here are headers: "
                                + hh.getRequestHeaders() + " and cookies " + hh.getCookies());
            }
            String cookieModUid = null;
            try {
                cookieModUid = Utils.cookieToLogModUid(cookie);
            } catch (Exception e) {
                cookieModUid = null;
            }
            String buyerUidModUid = null;
            try {
                buyerUidModUid = Utils.cookieToLogModUid(buyerUid);
            } catch (Exception e) {
                buyerUidModUid = null;
            }
            final String origCookie = cookie;
            String cookieComment = null;
            boolean setCookie = cookieModUid == null;
            if (setCookie) {
                if (buyerUidModUid != null && buyerUidModUid.length() > 0) {
                    cookie = Utils.logToCookieModUid(buyerUidModUid, false);
                    cookieComment = "setCookie=true because HTTP request cookie (null) != Exchange's urlSafe cookie ("
                                    + buyerUid + "), setting to non-urlSafe " + cookie;
                    LogUtils.debug(cookieComment);
                } else {
                    cookie = Utils.createModUidCookie(false);
                    cookieComment = "setCookie=true because both HTTP request cookie and Exchange cookie were null, this is a new user, generated new non-urlSafe: "
                                    + cookie;
                    LogUtils.debug(cookieComment);
                    LogUtils.debug("forceCookieResync=true because creating new cookie.");
                    forceCookieResyncBool = true;
                }
            } else {
                if (buyerUidModUid == null || buyerUidModUid.length() == 0) {
                    LogUtils.debug("forceCookieResync=true because buyerUid was null");
                    forceCookieResyncBool = true;
                } else {
                    if (!cookieModUid.equals(buyerUidModUid)) {
                        setCookie = true;
                        cookie = Utils.logToCookieModUid(buyerUidModUid, false);
                        cookieComment = "setCookie=true because ModUid " + cookieModUid
                                        + " for HTTP cookie " + origCookie
                                        + " != Exchange's ModUid " + cookieModUid
                                        + " for urlSafe cookie, " + buyerUid
                                        + ", setting to non-urlSafe" + cookie;

                        LogUtils.debug(cookieComment);

                    }
                }
            }
            String modUid = buyerUidModUid;

            boolean forceCookieResetBool = Utils.isTrue(forceCookieReset);
            if (redir == null) {
                if (forceCookieResetBool || forceCookieResyncBool) {
                    StringBuilder redirBuilder = new StringBuilder();
                    redirBuilder.append(StringUtils.replace(uriInfo.getBaseUri().toString(),
                                    "http:", "https:", 1))
                                    .append(Lot49Constants.DEFAULT_PROUST_PATH_ABSOLUTE
                                                    .substring(1));
                    String phase = forceCookieResetBool ? "rest" : "sync";
                    redirBuilder.append("?");
                    redirBuilder.append(Lot49Constants.LOT49_VERSION_KEY).append("=")
                                    .append(Lot49Constants.LOT49_VERSION_VALUE).append("&base=")
                                    .append(URLEncoder.encode(uriInfo.getBaseUri().toString()))
                                    .append("&");
                    redirBuilder.append("xch=").append(exchange).append("&ssp=").append(ssp)
                                    .append("&cid=").append(cId).append("&crid=").append(crId)
                                    .append("&bid=").append(bId).append("&iid=").append(iId)
                                    .append("&brid=").append(URLEncoder.encode(brId))
                                    .append("&fcr=1&custom=").append(custom).append("&phase=")
                                    .append(phase);

                    redir = redirBuilder.toString();
                }
            }
            long t1 = BidderCalendar.getInstance().currentTimeMillis();
            LogUtils.debug("Time to get to just before getBidInFlightInfoMap()" + (t1 - t0));
            DaoShortLivedMap<BidInFlightInfo> map =
                            Bidder.getInstance().getAdCache().getBidInFlightInfoMap();
            long t2 = BidderCalendar.getInstance().currentTimeMillis();
            LogUtils.debug("Time to do getBidInFlightInfoMap()" + (t2 - t1));

            String taskKey = adapter.getBidChoiceAlgorithm() == BidChoiceAlgorithm.ALL
                            ? (KVKeysValues.BID_PREFIX + bId)
                            : (KVKeysValues.BID_REQUEST_PREFIX + brId);
            BidInFlightInfo bif = map.get(taskKey);
            final long winPriceMicros = priceInfo.getWpMicro();
            boolean suspicious = false;
            if (!adapter.isNurlRequired()) {
                // Win and impression coincide, if so...
                if (winPriceMicros == 0) {
                    // Zero win price
                    suspicious = true;
                } else if (bif != null && bif.getWinPrice() != null) {
                    // There is information about a prior win in the
                    // BidInFlightInfo - that
                    // means we already got this win.
                    suspicious = true;
                    LogUtils.warn(brId + ": seems like a duplicate impression (BidInFlightInfo: "
                                    + bif + "): " + x);
                }
            }
            LogUtils.logImpression(cookie, exchange, bId, iId, cId, crId, bidCreatedTimestamp,
                            redir, ref, ua, custom, xff, servletRequest, xrip, nurl, modUid,
                            uriInfo.getRequestUri(), brId, ssp, forceCookieReset, forceCookieResync,
                            cookies, hh == null ? null : hh.getRequestHeaders(), setCookie,
                            buyerUid, origCookie, cookieComment, suspicious, nodeId, bif);

            final AdCache adCache = Bidder.getInstance().getAdCache();
            final Ad ad = adCache.getAd(cId);
            ServiceRunner.getInstance().getUserAttributesCacheService()
                            .updateImpressionsHistoryAsync(ad, modUid);

            String notFoundReason = null;
            if (nurl == 0) {
                final boolean trueWin = adapter.trueWinOnNurlOrImpression();

                if (trueWin) {

                    bif = handleWinLossError(uriInfo.getRequestUri().toString(), adapter, bId, brId,
                                    cId, null, winPriceMicros);
                }
                // If a pseudowin, we'll log
                // But if true, we need a few more things to check...
                boolean suspiciousWin = false;
                if (bif != null && bif.getWinPrice() != null) {
                    LogUtils.warn(brId + ": seems like a duplicate win (BidInFlightInfo: " + bif
                                    + ")");
                    suspiciousWin = true;
                }
                LogUtils.logWin(cookie, exchange, bId, iId, cId, crId, wp,
                                priceInfo == null ? null : priceInfo.getWpDouble(),
                                priceInfo == null ? null : priceInfo.getWpMicro(),
                                priceInfo == null ? null : priceInfo.getBpLong(),
                                bidCreatedTimestamp, redir, ref, ua, custom, xff, servletRequest,
                                xrip, null, modUid, uriInfo.getRequestUri(), trueWin, brId, ssp,
                                notFoundReason, null, null, cookies, setCookie, suspiciousWin,
                                nodeId);
            }

            final String impCookieValue =
                            String.valueOf(BidderCalendar.getInstance().currentTimeMillis());
            final Cookie impCookie = new Cookie(KVKeysValues.COOKIE_TS_IMPRESSION + cId,
                            impCookieValue, "/", getCookieDomain(cId), 2);
            final NewCookie newImpCookie =
                            new NewCookie(impCookie, impCookieValue, maxCookieAge, false);

            ResponseBuilder respBuilder = Response.ok(PIXEL_BYTES, "image/gif");

            NewCookie newCookie = null;

            if (setCookie) {
                final Cookie rC = new Cookie(USER_ID_COOKIE, cookie, "/", getCookieDomain(cId), 2);
                newCookie = new NewCookie(rC, cookie, maxCookieAge, false);

            }

            if (redir != null) {
                try {
                    respBuilder = Response.seeOther(new URI(redir));

                } catch (URISyntaxException e) {
                    LogUtils.error("Bad redirect " + redir, e);
                    respBuilder = Response.status(Status.BAD_REQUEST);

                }
            }
            if (setCookie) {
                respBuilder = respBuilder.cookie(newCookie);
            }
            return respBuilder.cacheControl(NO_CACHE).cookie(newImpCookie).build();
        } catch (Throwable t) {
            LogUtils.error("Error parsing " + (uriInfo == null ? null : uriInfo.getRequestUri()),
                            t);
            return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(NO_CACHE).build();

        }
    }

    public static final String PIXEL_B64 =
                    "R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==";
    public static final byte[] PIXEL_BYTES = Base64.getDecoder().decode(PIXEL_B64.getBytes());

    @GET
    @Produces("image/gif")
    @Path(Lot49Constants.DEFAULT_PIXEL_PATH_RELATIVE)
    public final byte[] pixel() {
        return PIXEL_BYTES;
    }

    public static final BidInFlightInfo handleWinLossError(String url, ExchangeAdapter adapter,
                    String bId, String brId, String adId, String reason, Long winPrice)
                    throws Lot49Exception {
        String taskKey = adapter.getBidChoiceAlgorithm() == BidChoiceAlgorithm.ALL
                        ? (KVKeysValues.BID_PREFIX + bId)
                        : (KVKeysValues.BID_REQUEST_PREFIX + brId);

        BidInFlightInfo info = null;
        String prefix = "handleWinLossError at " + url + " (RequestID: " + brId + ", Reason: "
                        + reason + ", WinPrice: " + winPrice + "): ";
        if (reason == null && (winPrice == null || winPrice < 0)) {
            throw new Lot49Exception(
                            prefix + "Cannot have win price " + winPrice + " and reason " + reason);

        }
        StringBuilder infoMsg = new StringBuilder();
        StringBuilder warnMsg = new StringBuilder();

        if (reason == null) {
            if (winPrice == 0) {
                LogUtils.debug(prefix + "Zero win price, looks like a test, bye.");
                return null;
            }
            infoMsg.append("\n\tWIN for ").append(winPrice);
            info = LostAuctionTask.cancelLostAuctionTask(taskKey, new BidInFlightInfo(winPrice));
            Ad ad = null;
            if (adId == null) {
                if (info == null || info.getAdId() == null) {
                    warnMsg.append("\n\tAd ID not passed in and not found in bid info.");
                } else {
                    ad = Bidder.getInstance().getAdCache().getAd(info.getAdId());
                }
            } else {
                ad = Bidder.getInstance().getAdCache().getAd(adId);
            }
            if (ad == null) {
                warnMsg.append("\n\tAd ").append(info.getAdId()).append(" not found.");
            } else {
                ad.incrWins();
                ad.incrSpendAmount(winPrice);
                infoMsg.append("\n\t").append(ad.getId()).append(" has ").append(ad.getWins())
                                .append(" wins and spend of ").append(ad.getSpendAmount());
            }
            if (info == null) {
                warnMsg.append("\n\tNo info found under ").append(taskKey);
            } else if (info.getAdId() == null) {
                warnMsg.append("\n\tNo ad ID found in ").append(info);
            } else if (!adId.equals(info.getAdId())) {
                warnMsg.append("\n\tReceived event for ").append(adId).append(", found info for ")
                                .append(info.getAdId());
            }
        } else {
            infoMsg.append("\n\t").append(reason);
            info = LostAuctionTask.cancelLostAuctionTask(taskKey, new BidInFlightInfo(reason));
        }

        if (info == null) {
            warnMsg.append("\n\tIn-flight information for key ").append(taskKey)
                            .append(" not found, refund  may be lost.");
        } else {
            if (!brId.equals(info.getRequestId())) {
                warnMsg.append("\n\tIn-flight information for key ").append(taskKey)
                                .append(" contains bid request ID ").append(info.getRequestId())
                                .append(", expected ").append(brId).append(", refund may be lost.");
            }
            if (bId != null && !bId.equals(info.getBidId())) {
                warnMsg.append("\n\tIn-flight information for key ").append(taskKey)
                                .append(" contains bid ID ").append(info.getBidId())
                                .append(", expected " + bId + ", refund may be lost.");
            }
        }
        if (infoMsg.length() > 0) {
            // Substring because we don't want to include first newline.
            LogUtils.info(prefix + infoMsg.substring(1));
        }
        if (warnMsg.length() > 0) {
            LogUtils.warn(prefix + warnMsg.substring(1));
        }
        return info;
    }

    @GET
    @Path(Lot49Constants.DEFAULT_NURL_PATH_RELATIVE)
    public final Object nurl(final @Context UriInfo uriInfo,

                    final @QueryParam("xch") String exchange, final @QueryParam("ssp") String ssp,
                    final @DefaultValue(MediaType.TEXT_HTML) @QueryParam("ctype") String contentType,
                    final @QueryParam("wp") String wp, final @QueryParam("cid") String cId,
                    final @QueryParam("crid") String crId, final @QueryParam("bid") String bId,
                    final @QueryParam("iid") String iId, final @QueryParam("brid") String brId,
                    final @QueryParam("bp") String bp,
                    final @QueryParam("ts") long bidCreatedTimestamp,
                    final @QueryParam("nurlId") String nurlId,
                    final @HeaderParam("referer") String ref,
                    final @HeaderParam("Cookie") String cookies,
                    final @HeaderParam("user-agent") String ua,
                    final @QueryParam("custom") String custom,
                    final @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip,
                    final @QueryParam(Lot49Constants.QUERY_STRING_EXCHANGE_CLICK_THROUGH_MACRO) String ct,
                    final @QueryParam(Lot49Constants.QUERY_STRING_EXCHANGE_CLICK_THROUGH_ENCODED_MACRO) String cte,
                    final @QueryParam(Lot49Constants.QUERY_STRING_LOT49_CLICK_THROUGH_MACRO) String myct,
                    final @QueryParam("nodeId") String nodeId,
                    final @QueryParam("nt") int nurlType) {
        LogUtils.info("Entering Nurl: " + (uriInfo == null ? null : uriInfo.getRequestUri()));
        LogUtils.logAccess(uriInfo);
        try {
            final ExchangeAdapter adapter = ExchangeAdapterFactory.getExchangeAdapter(exchange);
            final boolean trueWin = adapter.trueWinOnNurlOrImpression();
            final ServiceRunner runner = Bidder.getInstance();
            ParsedPriceInfo priceInfo = null;

            if (nurlType == Lot49Constants.NURL_STANDART
                            || nurlType == Lot49Constants.NURL_ONLY_WIN_NOTIFICATION) {
                Set<String> x = Utils.incrMapSet(wins, brId, uriInfo.getRequestUri().toString()
                                + " @ " + System.currentTimeMillis());
                if (x.size() > 1) {
                    LogUtils.warn("nurl(): Multiple wins for " + brId + ": " + x);
                }

                priceInfo = parsePriceInformation(adapter, bp, wp, bId, cId, crId);

                Response resp = null;
                if (priceInfo.getStatus() != null) {
                    LogUtils.error("Could not parse price from " + wp + " from "
                                    + uriInfo.getRequestUri());
                    LogUtils.logResponse(exchange, ssp, brId, bId, "CANNOT PARSE PRICE");
                    return Response.status(priceInfo.getStatus()).cacheControl(NO_CACHE).build();
                }


                if (trueWin) {
                    final long bidPrice = priceInfo.getBpLong();
                    final long winPrice = priceInfo.getWpMicro();
                    handleWinLossError(uriInfo.getRequestUri().toString(), adapter, bId, brId, cId,
                                    null, winPrice);
                    final AdCache adCache = Bidder.getInstance().getAdCache();
                    final Ad ad = adCache.getAd(cId);
                    if (ad == null) {
                        LogUtils.error("Unknown Ad ID in " + uriInfo.getRequestUri() + ": " + cId);
                    }
                }
            }
            final String cookieValue =
                            String.valueOf(BidderCalendar.getInstance().currentTimeMillis());
            final Cookie rstCookie = new Cookie(KVKeysValues.COOKIE_TS_IMPRESSION + cId,
                            cookieValue, "/", getCookieDomain(cId), 2);
            final NewCookie newRstCookie =
                            new NewCookie(rstCookie, cookieValue, maxCookieAge, false);

            String tag = "";

            final AdCache adCache = runner.getAdCache();
            DaoShortLivedMap<String> nurlMap = adCache.getNurlMap();

            final String tagKey = KVKeysValues.NURL_PREFIX + nurlId;
            if (Bidder.getInstance().getConfig().isRemoveTagOnFirstNurlRequest()
                            && nurlType == Lot49Constants.NURL_STANDART) {
                LogUtils.debug("nurl(): Removing tag at " + tagKey);
                tag = nurlMap.remove(tagKey);
            } else {
                LogUtils.debug("nurl(): Getting tag at " + tagKey);
                tag = nurlMap.get(tagKey);
            }

            String cookie = getMyCookie(cookies);
            boolean createCookie = cookie == null;
            if (createCookie) {
                cookie = Utils.createModUidCookie();
            }
            String modUid = null;
            if (cookie != null) {
                try {
                    modUid = Utils.cookieToLogModUid(cookie);
                } catch (Exception e) {
                    modUid = e.getClass().getName();
                }
            }

            String notFoundReason = "";
            // Need to init the notFoundReason string to log
            // then we can return an empty response.
            if (tag == null) {
                if (notFoundReason != null) {
                    notFoundReason = "";
                } else {
                    notFoundReason += "; ";
                }
                notFoundReason += "Tag not found at URL " + uriInfo.getRequestUri() + " under "
                                + KVKeysValues.NURL_PREFIX + nurlId;
                long tsNow = BidderCalendar.getInstance().currentTimeMillis();

                notFoundReason += "; bid created "
                                + Utils.DEFAULT_PERIOD_FORMATTER.print(
                                                new Interval(bidCreatedTimestamp, tsNow).toPeriod())
                                + " ago";

                try {
                    int lastUnderscore = nurlId.lastIndexOf("_");
                    String tsStr = nurlId.substring(lastUnderscore + 1);
                    long ts = Long.parseLong(tsStr);
                    notFoundReason += "; NUrl created " + Utils.DEFAULT_PERIOD_FORMATTER
                                    .print(new Interval(ts, tsNow).toPeriod()) + " ago";
                } catch (Throwable t) {
                    // Ignore temporarily
                }
                LogUtils.logResponse(exchange, ssp, brId, bId, notFoundReason);
            }
            if (nurlType == Lot49Constants.NURL_STANDART
                            || nurlType == Lot49Constants.NURL_ONLY_WIN_NOTIFICATION) {
                LogUtils.logWin(cookie, exchange, bId, iId, cId, crId, wp,
                                priceInfo == null ? null : priceInfo.getWpDouble(),
                                priceInfo == null ? null : priceInfo.getWpMicro(),
                                priceInfo == null ? null : priceInfo.getBpLong(),
                                bidCreatedTimestamp, null, ref, ua, custom, xff, servletRequest,
                                xrip, null, modUid, uriInfo.getRequestUri(), trueWin, brId, ssp,
                                notFoundReason, ct, cte, cookies, createCookie,
                                // TODO
                                false, nodeId);
                if (nurlType == Lot49Constants.NURL_ONLY_WIN_NOTIFICATION) {
                    return Response.ok().build();
                }

            }

            NewCookie newCookie = null;
            if (createCookie) {
                String newCookieValue = Utils.createModUidCookie();
                final Cookie rC = new Cookie(USER_ID_COOKIE, newCookieValue, "/",
                                getCookieDomain(cId), 2);
                newCookie = new NewCookie(rC, newCookieValue, maxCookieAge, false);

            }

            if (tag == null) {
                ResponseBuilder rb = Response.status(Status.NOT_FOUND).cacheControl(NO_CACHE)
                                .cookie(newRstCookie);
                if (newCookie != null) {
                    LogUtils.debug("Setting new cookie " + newCookie
                                    + " because did not find ours in " + cookies);
                    rb = rb.cookie(newCookie);
                }
                return rb.build();
            }

            // Tag supposed to be returned

            if (adapter.isMacrosInNurl() && nurlType == Lot49Constants.NURL_STANDART) {
                boolean noMacrosPassed = (ct == null || ct.length() == 0
                                || ct.equals(adapter.getClickMacro()))
                                && (cte == null || cte.length() == 0
                                                || cte.equals(adapter.getClickEncMacro()));
                if (noMacrosPassed) {
                    LogUtils.warn("Expected " + adapter.getClickMacro() + " or "
                                    + adapter.getClickEncMacro()
                                    + " to be passed, but got respectively " + ct + " and " + cte
                                    + " from " + exchange + "/" + ssp);
                }

                // Pass 3. Set up click chain.
                if (noMacrosPassed) {
                    tag = tag.replace(Macros.MACRO_LOT49_CLICK_CHAIN_ENC,
                                    Macros.MACRO_LOT49_CLICK_ENC);
                } else {
                    tag = tag.replace(Macros.MACRO_LOT49_CLICK_CHAIN_ENC,
                                    Macros.MACRO_LOT49_EXCHANGE_CLICK_ENC
                                                    + Macros.MACRO_LOT49_CLICK_ENC_ENC);
                }
            }
            final String clickUrl = myct;
            final String clickUrlEnc = URLEncoder.encode(myct);
            final String clickUrlEncEnc = URLEncoder.encode(clickUrlEnc);

            // Pass 4.ClickEncEnc
            tag = tag.replace(Macros.MACRO_LOT49_CLICK_ENC_ENC, clickUrlEncEnc);

            // Pass 5. ClickEnc
            tag = tag.replace(Macros.MACRO_LOT49_CLICK_ENC, clickUrlEnc);

            // Pass 6. Click
            tag = tag.replace(Macros.MACRO_LOT49_CLICK, clickUrl);

            // Pass 7. ExchangeClickEnc
            // Do this last!
            if (cte != null) {
                tag = tag.replace(Macros.MACRO_LOT49_EXCHANGE_CLICK_ENC, cte);
            }

            // Pass 8. Exchange Click.
            if (ct != null) {
                tag = tag.replace(Macros.MACRO_LOT49_EXCHANGE_CLICK, ct);
            }

            LogUtils.logResponse(exchange, ssp, brId, bId, tag);
            ResponseBuilder rb = Response.ok(tag, contentType).cacheControl(NO_CACHE)
                            .cookie(newRstCookie);
            if (newCookie != null) {
                rb = rb.cookie(newCookie);
                LogUtils.debug("Setting new cookie " + newCookie + " because did not find ours in "
                                + cookies);
            }
            return rb.build();

        } catch (Throwable t) {
            LogUtils.error("Error parsing " + (uriInfo == null ? null : uriInfo.getRequestUri()),
                            t);
            return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(NO_CACHE).build();

        }

    }

    public final ParsedPriceInfo parsePriceInformation(ExchangeAdapter adapter, String bp,
                    String wp, String bId, String cId, String crId) {

        ParsedPriceInfo retval = new ParsedPriceInfo();

        long bpLong = 0;
        try {
            bpLong = Long.valueOf(bp);

        } catch (Throwable e) {
            this.cause = e;
            LogUtils.error("Error parsing bid price from request: " + bp, e);
        }
        try {
            retval = adapter.parse(wp, bpLong);
        } catch (Throwable e) {
            this.cause = e;
            LogUtils.error("Error parsing winning price from " + wp, e);
            retval.setStatus(Status.BAD_REQUEST);
            return retval;
        }
        return retval;
    }

    public Throwable getCause() {
        return cause;
    }

    private Throwable cause;

    @SuppressWarnings("unused")
    @POST
    @Path(Lot49Constants.AUCTION_RESULTS_PATH_ADX)
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public final Object resultAdX(@Suspended final AsyncResponse response, @Context UriInfo uriInfo,
                    NetworkBid.BidRequest.BidResponseFeedback fb,
                    @HeaderParam("x-forwarded-for") final String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip) {
        try {
            AdXAdapter adapter = new AdXAdapter();
            String brid = AdXAdapter.getStringFromByteSting(fb.getRequestId());
            int cridx = fb.getCreativeIndex();
            int code = fb.getCreativeStatusCode();
            long winPriceMicros = fb.getCpmMicros();
            DaoShortLivedMap<BidInFlightInfo> bidMap =
                            Bidder.getInstance().getAdCache().getBidInFlightInfoMap();
            switch (code) {
                // Win
                case 1:
                    // No-op
                    return Response.status(Status.OK).cacheControl(NO_CACHE).build();

                default:
                    LogUtils.error("Unknown feedback code: " + code);
                    return Response.status(Status.BAD_REQUEST).cacheControl(NO_CACHE).build();
            }
        } catch (Throwable t) {
            LogUtils.error("Error in resultAdX", t);
            return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(NO_CACHE).build();
        }
    }

    /**
     * Handle OpenX auction result message. Usually this is for a loss, so we
     * {@link LogUtils#logLost(Long, String, String, String, String, Long, Long, String, String, String, String, HttpServletRequest, String, String, String, String)
     * log the loss}
     */
    @SuppressWarnings("unused")
    @POST
    @Path(Lot49Constants.AUCTION_RESULTS_PATH_OPENX)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Object resultsOpenX(final @Context UriInfo uriInfo,
                    @HeaderParam("Cookie") String cookies,
                    final AuctionResultMessage auctionResults,
                    final @HeaderParam("x-forwarded-for") String xff,
                    final @Context HttpServletRequest servletRequest,
                    final @HeaderParam("x-real-ip") String xrip, @Context HttpHeaders hh) {
        try {
            // throw new UnsupportedOperationException("Check with GG");
            String auctionResultsStr = null;

            auctionResultsStr = Utils.MAPPER.writeValueAsString(auctionResults);

            final String exchange = Lot49Constants.EXCHANGE_OPENX;
            final String ssp = exchange;
            final String brId = auctionResults.getAuctionId();
            // Cancel regardless of any other message.
            BidInFlightInfo bidInfo = null;
            OpenXAdapter adapter;

            adapter = new OpenXAdapter();

            for (final AuctionResult aRes : auctionResults.getResults()) {

                long winningBidMicros = -1;
                String reason = null;
                final int status = aRes.getStatus();
                if (status == AuctionResult.STATUS_WIN) {
                    // TODO
                    final String clearingPriceMicrosStr = aRes.getClearingPriceMicros();
                    long wpMicros = 0;
                    try {
                        wpMicros = Long.parseLong(clearingPriceMicrosStr) / 1000;
                    } catch (NumberFormatException npe) {
                        LogUtils.error("Error parsing " + clearingPriceMicrosStr
                                        + " as a clearing price in micro$; full request: "
                                        + auctionResultsStr);
                        return Response.status(Status.BAD_REQUEST).cacheControl(NO_CACHE).build();
                    }
                    bidInfo = handleWinLossError(uriInfo.getRequestUri().toString(), adapter, null,
                                    brId, null, null, wpMicros);

                    if (bidInfo == null) {
                        LogUtils.debug(LogUtils.MARKER_WINLOSS, "Authoritative win for " + brId
                                        + " but no bidInfo (" + bidInfo.getReason() + ")"
                                        + ", should be taking away " + wpMicros
                                        + " (assuming the cancellation will refund original price)");
                        // TODO
                        LogUtils.logWin(
                                        // Cookie -- will be null here
                                        null, exchange, null, null, null, null,
                                        clearingPriceMicrosStr, Utils.microToCpm(wpMicros),
                                        wpMicros,
                                        // TODO Bid price micros
                                        -1,
                                        // TODO Bid created timestamp
                                        -1,
                                        // Redir -- will be null here
                                        null,
                                        // Referer -- will be null here
                                        null,
                                        // TODO User-agent
                                        null,
                                        // Custom -- will be null here
                                        null, xff, servletRequest, xrip,
                                        // nurlId -- always 0 here
                                        0l,
                                        // decoded cookie -- always 0 here
                                        null, uriInfo.getRequestUri(), true, null, ssp,
                                        bidInfo.getReason(), null, null, cookies, false, false,
                                        bidInfo == null ? null : bidInfo.getInstanceId());
                    } else {
                        final long bidPriceMicros = bidInfo.getBidPriceMicros();
                        final long toRefund = bidPriceMicros - wpMicros;
                        final String bidId = bidInfo.getBidId();
                        LogUtils.debug(LogUtils.MARKER_WINLOSS,
                                        "Authoritative win for " + bidInfo.getCampaignId()
                                                        + ", refunding " + bidPriceMicros + "-"
                                                        + wpMicros + "=" + toRefund);
                        final AdCache adCache = Bidder.getInstance().getAdCache();

                        final String campaignId = bidInfo.getCampaignId();
                        final Ad ad = adCache.getAd(campaignId);
                        if (ad == null) {
                            LogUtils.error("Unknown Ad ID: " + campaignId);
                        }

                        LogUtils.logWin(
                                        // Cookie -- will be null here
                                        null, exchange, bidId,
                                        bidInfo == null ? null : bidInfo.getImpressionId(),
                                        bidInfo == null ? null : bidInfo.getCampaignId(),
                                        bidInfo == null ? null : bidInfo.getCreativeId(),
                                        clearingPriceMicrosStr, Utils.microToCpm(wpMicros),
                                        wpMicros,
                                        // TODO Bid price micros
                                        bidInfo.getBidPriceMicros(),
                                        // TODO Bid created timestamp
                                        bidInfo.getBidCreatedOnTimestamp(),
                                        // Redir -- will be null here
                                        null,
                                        // Referer -- will be null here
                                        null,
                                        // TODO User-agent
                                        null,
                                        // Custom -- will be null here
                                        null, xff, servletRequest, xrip,
                                        // nurlId -- always 0 here
                                        0l,
                                        // decoded cookie -- always 0 here
                                        null, uriInfo.getRequestUri(), true, brId, ssp, null, null,
                                        null, cookies, false, false,
                                        bidInfo == null ? null : bidInfo.getInstanceId());
                    }

                } else if (status == AuctionResult.STATUS_LOSS
                                || status == AuctionResult.STATUS_ERROR) {
                    final boolean loss = status == AuctionResult.STATUS_LOSS;
                    handleWinLossError(uriInfo.getRequestUri().toString(), adapter, null, brId,
                                    null,
                                    loss ? LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_LOSS
                                                    : LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_ERROR,
                                    null);
                    final String lostTaskKey = KVKeysValues.BID_REQUEST_PREFIX + brId;

                    if (loss) {
                        final String lossReason = aRes.getLossReason();
                        if (lossReason.equalsIgnoreCase(AuctionResult.LOSS_REASON_PRICE)) {
                            winningBidMicros = Long.parseLong(aRes.getWinningBidMicros());

                        }
                        reason = lossReason;
                    } else {
                        reason = aRes.getErrorReason();
                    }

                    LogUtils.logLost(bidInfo == null ? null : bidInfo.getBidCreatedOnTimestamp(),
                                    bidInfo == null ? null : bidInfo.getBidId(),
                                    bidInfo == null ? null : bidInfo.getImpressionId(),
                                    bidInfo == null ? null : bidInfo.getCampaignId(),
                                    bidInfo == null ? null : bidInfo.getCreativeId(),
                                    bidInfo == null ? null : bidInfo.getBidPriceMicros(),
                                    winningBidMicros, reason, exchange, auctionResultsStr, xff,
                                    servletRequest, xrip, ssp, brId,
                                    bidInfo == null ? null : bidInfo.getInstanceId());
                } else {
                    // Unknown status
                    LogUtils.error("Unknown status " + aRes.getStatus() + " in "
                                    + auctionResultsStr);
                }
            }
        } catch (Throwable t) {
            LogUtils.error("Error", t);
        }
        return Response.status(Status.NO_CONTENT).cacheControl(NO_CACHE).build();
    }
}
