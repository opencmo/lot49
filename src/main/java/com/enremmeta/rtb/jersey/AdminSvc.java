package com.enremmeta.rtb.jersey;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.TargetingGeo;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.enremmeta.rtb.config.AdminConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.dao.impl.hazelcast.HazelcastService;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.proto.adx.AdXTargeting;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;

/**
 * Administration utilities.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *
 */
@Provider
@Path(Lot49Constants.ROOT_PATH_ADMIN)
public class AdminSvc implements JerseySvc, Lot49Constants {

    private static final Map<String, Object> map = new HashMap<String, Object>();

    public static final String DEBUG_RESPONSE_TYPE_FULL_JSON =
                    "Full bid response, as JSON MIME Type";
    public static final String DEBUG_RESPONSE_TYPE_FULL_TEXT =
                    "Full bid response, as plain text, JSON-formatted";
    public static final String DEBUG_RESPONSE_TYPE_FULL_BINARY =
                    "Full bid response, as exchange-specific media type";
    public static final String DEBUG_RESPONSE_TYPE_TAG_TEXT = "Tag, as text/plain";
    public static final String DEBUG_RESPONSE_TYPE_TAG_BINARY = "Tag, as appropriate media type";
    public static final String DEBUG_RESPONSE_TYPE_TAG_CONTAINER =
                    "Tag, in the appropriate container";
    public static final String DEBUG_RESPONSE_TYPE_TAG_CACHED =
                    "Link to a cached tag, for later invocation";

    private static final String[] DEBUG_RESPONSE_TYPES =
                    new String[] {DEBUG_RESPONSE_TYPE_FULL_JSON, DEBUG_RESPONSE_TYPE_FULL_TEXT,
                                    DEBUG_RESPONSE_TYPE_FULL_BINARY, DEBUG_RESPONSE_TYPE_TAG_TEXT,
                                    DEBUG_RESPONSE_TYPE_TAG_BINARY, DEBUG_RESPONSE_TYPE_TAG_CACHED,
                                    DEBUG_RESPONSE_TYPE_TAG_CONTAINER};

    public static final String DEBUG_ACTION_TYPE_DISPLAY = "Display";

    // public static final String DEBUG_ACTION_TYPE_CACHE =
    // "Cache and return link";

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("login")
    public void login(@Suspended final AsyncResponse response) {
        String form = "<form method=POST action=\"" + Lot49Constants.ROOT_PATH_ADMIN + "/auth\">";
        form += "Username: <input name=username><br>";
        form += "Password: <input name=password><br>";
        form += "<input type=submit><br>";
        form += "</form>";
        response.resume(form);
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("auth")
    public void auth(@Suspended final AsyncResponse response,
                    @FormParam("username") String username,
                    @FormParam("password") String password) {
        Lot49Config config = Bidder.getInstance().getConfig();
        AdminConfig adminConfig = config.getAdmin();
        String configUsername = adminConfig.getUsername();
        if (configUsername != null && configUsername.length() > 0) {
            if (!username.equals(adminConfig.getUsername())
                            || !password.equals(adminConfig.getPassword())) {
                response.resume(Response.status(Response.Status.FORBIDDEN).build());
                return;
            }
        }

        try {
            final String url = Bidder.getInstance().getConfig().getBaseUrl()
                            + Lot49Constants.ROOT_PATH_ADMIN + "/";
            final ResponseBuilder rb = Response.seeOther(new URI(url));
            final String cookieValue = JerseySvc.AUTH_COOKIE_DU_JOUR;
            // TODO make configurable
            final int cookieTtl = 60 * 60 * 24;
            rb.cookie(new NewCookie("auth", cookieValue, "/",
                            Bidder.getInstance().getConfig().getCookieDomain(), "", cookieTtl,
                            false));
            response.resume(rb.build());
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * For ad ops, listing of available {@link Ad}s.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/")
    public void index(@Suspended final AsyncResponse response, @CookieParam("auth") String auth) {

        if (!auth(response, auth)) {
            return;
        }

        String resp = "<h1>Admin</h1>\n";
        resp += "<i>Please be advised that this is intended mostly for QA.</i>\n";
        resp += "<h2>Utilities</h2>\n";
        resp += "<ul.>\n";
        resp += "<li><a href=\"" + Lot49Constants.ROOT_PATH_ADMIN
                        + "/refreshAdCache\">Refresh ad cache</a></li>\n";
        resp += "<li><a href=\"" + Lot49Constants.ROOT_PATH_ADMIN
                        + "/hazelcast\">Hazelcast info</a></li>\n";
        resp += "</ul>";
        resp += "<h2>Valid ads</h2>\n";
        resp += "<ul>\n";
        AdCache adCache = Bidder.getInstance().getAdCache();
        for (Ad ad : adCache.getAll()) {
            resp += htmlAdTitle(ad, true);
        }
        resp += "</ul>\n";

        resp += "<h2>Ineligible ads</h2>\n";

        List<Ad> zeroBudgetAds = adCache.getZeroBudgetAds();
        if (zeroBudgetAds.size() > 0) {
            resp += "<h3>Ads with 0 budget</h3>\n";
            resp += "<table border=1>\n";
            for (Ad ad : zeroBudgetAds) {
                resp += "<tr><td>" + htmlAdTitle(ad, true) + "</td>\n";
                resp += "<td>\n";
                resp += "<FORM action=\"" + Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + ad.getId()
                                + "/setBudget\" METHOD=\"GET\">\n";
                resp += "Amount in micro$: <INPUT NAME=\"amount\" VALUE=\"1000000000\">\n";
                resp += "<INPUT TYPE=\"submit\" VALUE=\"Set\">\n";
                resp += "</FORM>";
                resp += "</td></tr>\n";
            }
            resp += "</table>";
        }

        Map<Ad, String> invalidAds = adCache.getInvalidAds();
        if (invalidAds.size() > 0) {
            resp += "<h3>Invalid ads</h3>\n";
            resp += "<ul>\n";
            for (Ad ad : invalidAds.keySet()) {
                resp += htmlAdTitle(ad, false);
                resp += "<p><blockquote><pre>" + invalidAds.get(ad) + "</pre></blockquote></p>";
                resp += "</li>";
            }
            resp += "</ul>";
        }

        Map<String, String> uncompilableAds = adCache.getUncompilableAds();
        if (uncompilableAds.size() > 0) {
            resp += "<h3>Uncompilable ad scripts</h3>";
            resp += "<ul>";
            for (String f : uncompilableAds.keySet()) {
                String error = uncompilableAds.get(f);
                resp += "<li>" + f + ": " + error + "</li>\n";
            }
            resp += "</ul>";
        }
        response.resume(resp);
    }

    private String htmlAdTitle(Ad ad, boolean closeLi) {
        String retval = "<li><a href=\"" + Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + ad.getId()
                        + "\">" + ad.getId() + " " + ad.getName()
                        + (ad.getDesc() == null ? "" : " (" + ad.getDesc() + ")") + "</a>";
        if (closeLi) {
            retval += "</li>\n";
        }
        return retval;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("refreshAdCache")
    public String refreshAdCache(@Suspended final AsyncResponse response,
                    @CookieParam("auth") String auth) {

        if (!auth(response, auth)) {
            return "";
        }

        AdCache cache = Bidder.getInstance().getAdCache();
        String msg = "<h1>Ad cache refresh</h1>\n<pre>\n";

        Boolean currentSetting = cache.getConfig().isValidateBudgetBeforeLoadingAd();
        cache.getConfig().setValidateBudgetBeforeLoadingAd(false);

        msg += cache.doRun(false);

        cache.getConfig().setValidateBudgetBeforeLoadingAd(currentSetting);

        msg += "</pre>\n";
        msg += "<hr/>\n";
        msg += "<a href=\"" + Lot49Constants.ROOT_PATH_ADMIN + "\">Back</a>";
        return msg;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("ad/{adId}/setBudget")
    public void setBudget(@Suspended final AsyncResponse response, @PathParam("adId") String adId,
                    @QueryParam("amount") long amount, @CookieParam("auth") String auth)
                    throws Lot49Exception {

        if (!auth(response, auth)) {
            return;
        }

        AdCache cache = Bidder.getInstance().getAdCache();
        String retval = cache.setBudget(adId, amount);
        String resp = "Set budget micro$" + amount + " for " + adId + ": " + retval;
        resp += "\n<br>";
        resp += "<a href=\"" + Lot49Constants.ROOT_PATH_ADMIN
                        + "/refreshAdCache\">Refresh ad cache</a>";
        response.resume(Response.ok(resp).build());
    }

    /**
     * For ad ops, listing of available {@link Tag}s in the chosen {@link Ad}.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("ad/{adId}")
    public void invAd(@Suspended final AsyncResponse response, @PathParam("adId") String adId,
                    @CookieParam("auth") String auth) {

        if (!auth(response, auth)) {
            return;
        }

        String resp = "";

        Ad foundAd = findAd(adId);

        resp += "Bid CPM: " + foundAd.getBidPriceCpm(null) + "<br>\n";
        resp += "Bid micro$: " + foundAd.getBidPrice(null) + "<br>\n";

        resp += "Probability: " + foundAd.getBidProbability() + "<br>\n";
        resp += "Segments: " + foundAd.getParsedTargetingSegments() + "<br>\n";
        List<TargetingGeo> geos = foundAd.getGeos();
        if (geos != null) {
            resp += "Geos: ";
            resp += "<ul>";
            for (TargetingGeo geo : geos) {
                resp += "<li>" + geo.toString() + "</li>";
            }
            resp += "</ul>";
        }

        resp += "<ul>";
        for (Tag tg : foundAd.getTags()) {
            String desc = "";
            if (tg.getDesc() != null) {
                desc = " (" + tg.getDesc() + ")";
            }
            resp += "<li><a href=\"" + Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + adId + "/tag/"
                            + tg.getId() + "\">" + tg.getId() + " " + tg.getName() + desc
                            + "</a></li>";
        }
        resp += "</ul>";
        response.resume(resp);
    }

    private boolean auth(AsyncResponse response, String authCookie) throws WebApplicationException {
        String host = Bidder.getInstance().getConfig().getHost();
        // Localhost is ok...
        if (!host.equals("localhost") && (authCookie == null
                        || !authCookie.equals(JerseySvc.AUTH_COOKIE_DU_JOUR))) {
            try {
                response.resume(Response
                                .seeOther(new URI(Bidder.getInstance().getConfig().getBaseUrl()
                                                + Lot49Constants.ROOT_PATH_ADMIN + "/login"))
                                .build());
                return false;

            } catch (URISyntaxException e) {
                throw new WebApplicationException(e);
            }
        }
        return true;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("ad/{adId}/tag/{tagId}")
    public void invTag(@Suspended final AsyncResponse response, @PathParam("adId") String adId,
                    @PathParam("tagId") String tagId, @CookieParam("auth") String auth) {

        if (!auth(response, auth)) {
            return;
        }

        String resp = "";

        Ad ad = findAd(adId);
        Tag tag = findTag(ad, tagId);

        List<ExchangeAdapter> adapters = ExchangeAdapterFactory.getAllExchangeAdapters();
        List<String> exchanges = ExchangeAdapterFactory.getAllExchangeAdapterNames();

        resp += "<!DOCTYPE html>\n";
        resp += "<HTML>\n";
        resp += "<HEAD>\n";
        resp += "<TITLE>Tag " + tag.getId() + "</TITLE>\n";

        resp += "<BODY>\n";

        resp += "<h1>Tag " + tag.getId() + " " + tag.getName() + "</h1>\n";
        if (tag.getDesc() != null) {
            resp += "<h2>" + tag.getDesc() + "</h2>\n";
        }
        resp += "<ul>\n";
        resp += "<li> Name: " + tag.getName() + "</li>\n";
        resp += "<li> Description: " + tag.getDesc() + "</li>\n";
        resp += "</ul>\n";

        resp += "<h2>Conditions</h2>\n";
        resp += "<ul>\n";

        boolean banner = tag.isBanner();
        resp += "<li> Type: " + (banner ? "Banner" : "Video") + "</li>\n";
        resp += "<li> Dimension: " + tag.getDimension() + "</li>\n";
        resp += "<li> Mime: " + tag.getMime() + "</li>\n";
        if (!banner) {
            resp += "<li> API: " + Utils.apiToString(tag.getApi()) + "</li>\n";
            resp += "<li> Protocols: " + Utils.protoToString(tag.getProtocol()) + "</li>\n";
            resp += "<li> Duration: " + tag.getDuration() + "</li>\n";
        }
        resp += "</ul>\n";
        resp += "<h2>Debug</h2>\n";

        resp += "<FORM action=\"" + Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + adId + "/tag/" + tagId
                        + "/debug1\" METHOD=\"GET\">\n";

        resp += "<br/>\n";

        resp += "Exchange:\n<SELECT id=\"exchangeSelector\" name=\"exchange\">\n";

        boolean first = true;
        for (String exchange : exchanges) {
            resp += "<OPTION value=\"" + exchange + "\"" + (first ? " SELECTED" : "") + ">"
                            + exchange + "</OPTION>\n";
            first = false;
        }
        resp += "</SELECT>\n";

        resp += "<INPUT TYPE=submit VALUE=Debug>";
        resp += "</FORM>\n<HR>\n";

        resp += "</BODY>\n</HTML>";
        response.resume(resp);
    }

    @GET
    @Path("ad/{adId}/tag/{tagId}/debug1")
    public void debug1(@PathParam("adId") String adId, @PathParam("tagId") String tagId,
                    @QueryParam("exchange") String exchange,
                    final @Context HttpServletRequest servletRequest,
                    @Suspended final AsyncResponse response, @CookieParam("auth") String auth) {

        if (!auth(response, auth)) {
            return;
        }

        try {
            String resp = "";
            resp += "Ad: " + adId + "<br>\n";
            resp += "Tag: " + tagId + "<br>\n";
            resp += "<hr>";

            Ad ad = findAd(adId);
            Tag tag = findTag(ad, tagId);

            if (tag == null) {
                throw new WebApplicationException(Status.NOT_FOUND);
            }

            ExchangeAdapter adapter = ExchangeAdapterFactory.getExchangeAdapter(exchange);

            String debug2Url = Lot49Constants.ROOT_PATH_ADMIN + "/ad/" + adId + "/tag/" + tagId
                            + "/debug2";
            resp += "<FORM  action=\"" + debug2Url + "\" METHOD=\"GET\">\n";
            resp += "<ul>";
            resp += "<li>Exchange: <INPUT READONLY NAME=\"exchange\" VALUE=\"" + exchange
                            + "\"></li>\n";
            boolean noMacros = true;
            String clickEncMacro = adapter.getClickEncMacro();

            String clickSimUrl = Bidder.getInstance().getConfig().getAdmin()
                            .getExchangeClickSimulatorUrl();
            if (clickEncMacro != null && !"".equals(clickEncMacro)) {
                noMacros = false;
                resp += "<LI>";
                resp += "<INPUT  TYPE=\"hidden\" NAME=\"" + exchange
                                + "_clickenc_macro_key\" VALUE=\"" + clickEncMacro + "\">\n";

                resp += "Replace " + clickEncMacro + " with " + "<INPUT SIZE=80 name=\"" + exchange
                                + "_clickenc_macro_val\" VALUE=\"" + (clickSimUrl == null
                                                ? clickEncMacro : URLEncoder.encode(clickSimUrl))
                                + "\"/>\n";
                resp += "</LI>\n";
            }

            String clickMacro = adapter.getClickMacro();
            if (clickMacro != null && !"".equals(clickMacro)) {
                resp += "<LI>";
                noMacros = false;
                resp += "Replace " + clickMacro + " with " + "<INPUT SIZE=80  name=\"" + exchange
                                + "_click_macro_val\" VALUE=\""
                                + (clickSimUrl == null ? clickMacro : clickSimUrl) + "\"/>";
                resp += "<BR>\n";
                resp += "<INPUT TYPE=\"hidden\" name=\"" + exchange + "_click_macro_key\" VALUE=\""
                                + clickMacro + "\"/>\n";
                resp += "</LI>\n";
            }

            String wpMacro = adapter.getWinningPriceMacro();
            if (wpMacro != null && !"".equals(wpMacro)) {
                resp += "<LI>";
                noMacros = false;
                String sampleWinPrice = adapter.getSampleWinningPrice();
                resp += "Replace " + wpMacro + " with " + "<INPUT SIZE=80  name=\"" + exchange
                                + "_wp_macro_val\" VALUE=\"" + sampleWinPrice + "\"/>\n";
                resp += "<blockquote>Suggested values (copy and paste into above):<ul>";
                resp += "<li> Leave as is: <INPUT name=dummy VALUE=\"" + wpMacro
                                + "\" READONLY></li>";
                resp += "<li> Sample value for this exchange: <INPUT name=dummy VALUE=\""
                                + sampleWinPrice + "\" READONLY></li>";
                resp += "</ul></blockquote>";
                resp += "\n";
                resp += "<INPUT TYPE=\"hidden\" name=\"" + exchange + "_wp_macro_key\" VALUE=\""
                                + wpMacro + "\"/>\n";
                resp += "</LI>\n";
            }

            if (noMacros) {
                try {
                    response.resume(Response.seeOther(new URI(debug2Url)));
                } catch (URISyntaxException e) {
                    throw new WebApplicationException(e);
                }
            }
            resp += "<LI>";
            resp += "Response type:\n<SELECT id=\"responseTypeSelector\" name=\"responseType\">\n";
            for (String s : DEBUG_RESPONSE_TYPES) {
                resp += "<OPTION VALUE=\"" + s + "\">" + s + "</OPTION>\n";
            }

            resp += "</SELECT>\n<br/>\n";
            resp += "</LI>\n";
            resp += "</UL>";
            resp += "<INPUT NAME=\"actionType\" VALUE=\"" + DEBUG_ACTION_TYPE_DISPLAY
                            + "\" TYPE=\"submit\"/>\n";

            resp += "</FORM>\n";
            resp += "<BR>\n";
            response.resume(Response.ok().entity(resp).build());
        } catch (Throwable t) {
            LogUtils.error("Error", t);
            response.resume(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
    }

    private Ad findAd(String adId) throws WebApplicationException {
        final AdCache adCache = Bidder.getInstance().getAdCache();
        Ad foundAd = null;
        for (Ad ad : adCache.getAll()) {
            if (ad.getId().equals(adId)) {
                foundAd = ad;
                break;
            }
        }
        if (foundAd == null) {
            for (Ad ad : adCache.getZeroBudgetAds()) {
                if (ad.getId().equals(adId)) {
                    foundAd = ad;
                    break;
                }
            }
        }
        if (foundAd == null) {
            throw new WebApplicationException(
                            "No Active Ad found with ID " + adId
                                            + "; maybe you are searching for an invalid ad.",
                            Status.NOT_FOUND);
        }
        return foundAd;
    }

    private Tag findTag(Ad ad, String tagId) throws WebApplicationException {
        Tag foundTag = null;
        for (Tag tag : ad.getTags()) {
            if (tag.getId().equals(tagId)) {
                foundTag = tag;
                break;
            }
        }
        if (foundTag == null) {
            throw new WebApplicationException("No Tag found with ID " + tagId, Status.NOT_FOUND);
        }
        return foundTag;
    }

    /**
     * <p>
     * A debug URL that, when hit, can show the tag and/or bid response as would be returned. The
     * URL is of the form:
     * </p>
     * <tt>/admin/ad/{adId}/tag/{tagId}?exchange={exchange}&amp;responseType={responseType}&amp;strict={strict}</tt>
     * <p>
     * The meaning of the parameters is as follows:
     * </p>
     * 
     * @param adId
     *            {@link Ad#getId() Ad ID}
     * @param tagId
     *            {@link Tag#getId() Tag ID}
     * @param exchange
     *            Exchange (e.g. {@link Lot49Constants#EXCHANGE_ADAPTV}).
     * @param responseType
     *            can take on the values as follows:
     *            <table border="1">
     *            <caption></caption>
     *            <tr>
     *            <th>responseType</th>
     *            <th>Behavior</th>
     *            </tr>
     * 
     *            <tr>
     *            <td>{@link #DEBUG_RESPONSE_TYPE_FULL_JSON}</td>
     * 
     *            <td>Full bid response is returned with {@link MediaType#APPLICATION_JSON
     *            application/json} content type.</td>
     *            </tr>
     * 
     *            <tr>
     *            <td>{@link #DEBUG_RESPONSE_TYPE_FULL_BINARY}</td>
     *            <td>Full bid response is returned with the content type
     *            {@link ExchangeAdapter#getResponseMediaType() the exchange expects in a response}
     *            -- this could be a binary type such as {@link MediaType#APPLICATION_OCTET_STREAM
     *            application/x-octet-stream}.</td>
     *            </tr>
     * 
     * 
     *            <tr>
     *            <td>{@link #DEBUG_RESPONSE_TYPE_FULL_TEXT}</td>
     * 
     *            <td>Full bid response is returned with {@link MediaType#TEXT_PLAIN text/plain}
     *            content type.</td>
     *            </tr>
     * 
     *            <tr>
     *            <td>{@link #DEBUG_RESPONSE_TYPE_TAG_TEXT}</td>
     *            <td>Just the ad markup (the "tag") is returned, with {@link MediaType#TEXT_PLAIN
     *            text/plain} content type.</td>
     *            </tr>
     * 
     *            <tr>
     *            <td>{@link #DEBUG_RESPONSE_TYPE_TAG_BINARY}</td>
     *            <td>Just the ad markup (the "tag") is returned, with the
     *            {@link MediaType#TEXT_HTML text/html} in case of a {@link Impression#getBanner() a
     *            banner}, and {@link MediaType#APPLICATION_XML application/xml} in case of a
     *            {@link Impression#getVideo() video} ad. Also,
     *            <tt>X-Lot49-Creative-Content-Type</tt> custom header type will be returned with
     *            the value of {@link Tag#getMime() creative's MIME type}.</td>
     *            </tr>
     * 
     *            <tr>
     *            <td>{@link #DEBUG_RESPONSE_TYPE_TAG_CONTAINER}</td>
     *            <td>The ad markup (the "tag") is returned within the container -- an IFRAME in
     *            case of a {@link Impression#getBanner() a banner}, and <A href=
     *            "https://developers.google.com/interactive-media-ads/docs/vastinspector_dual" >
     *            Google VAST Inspector</a> in case of a {@link Impression#getVideo() video} ad.
     *            </td>
     *            </tr>
     * 
     *            <tr>
     *            <td>{@link #DEBUG_RESPONSE_TYPE_TAG_CACHED}</td>
     *            <td>A link is returned which, when followed, will yield the result as from a
     *            {@link #DEBUG_RESPONSE_TYPE_TAG_BINARY}. The link will not be under the Admin URL
     *            (see {@link OpsSvc}), that is, it is suitable for sending to third parties. The
     *            link will survive for as long as the {@link HazelcastService cache} survives.</td>
     *            </tr>
     *            </table>
     * 
     * @return
     */
    @GET
    @Path("ad/{adId}/tag/{tagId}/debug2")
    public Object debug2(@PathParam("adId") String adId, @PathParam("tagId") String tagId,
                    @QueryParam("exchange") String exchange,
                    @QueryParam("responseType") String responseType,
                    @QueryParam("actionType") String actionType,
                    final @Context HttpServletRequest servletRequest,
                    @Suspended final AsyncResponse response, @CookieParam("auth") String auth) {

        if (!auth(response, auth)) {
            return "";
        }

        if (responseType == null) {
            // Be forgiving
            responseType = DEBUG_RESPONSE_TYPE_FULL_JSON;
        }

        if (actionType == null) {
            actionType = DEBUG_ACTION_TYPE_DISPLAY;
        }

        ExchangeAdapter adapter = null;
        if (exchange != null && !exchange.trim().equals("")) {
            adapter = ExchangeAdapterFactory.getExchangeAdapter(exchange);
            if (adapter == null) {
                throw new WebApplicationException("Unknown exchange: " + exchange,
                                Status.NOT_FOUND);
            }
        }

        Ad ad = findAd(adId);
        Tag tag = findTag(ad, tagId);

        OpenRtbResponse resp = createBidResponse(adapter, ad, tag);

        Bid bid = resp.getSeatbid().get(0).getBid().get(0);
        String adm = "";
        if (!adapter.isNurlRequired()) {

            adm = bid.getAdm();

            String[] macroParamPrefixes = new String[] {exchange + "_clickenc_macro_",
                            exchange + "_click_macro_", exchange + "_wp_macro_"};

            for (String macroParamPrefix : macroParamPrefixes) {
                String val = servletRequest.getParameter(macroParamPrefix + "val");
                if (val == null) {
                    continue;
                }
                val = val.trim();

                String key = servletRequest.getParameter(macroParamPrefix + "key");
                while (true) {
                    String newAdm = adm.replace(key, val);
                    if (newAdm.equals(adm)) {
                        break;
                    }
                    adm = newAdm;
                }

            }

            resp.getSeatbid().get(0).getBid().get(0).setAdm(adm);

        }
        ResponseBuilder rb = null;
        Object o = null;
        try {

            OpenRtbRequest request = new OpenRtbRequest();
            List<Impression> impressions = new ArrayList<>();
            Impression imp1 = new Impression();
            Map<String, Object> ext = new HashMap<>();
            List<Long> adGroupIds = new ArrayList<Long>(1);
            adGroupIds.add(23639780849L);
            adGroupIds.add(26269227449L);
            AdXTargeting adXTargeting = new AdXTargeting();
            adXTargeting.setAdGroupId(adGroupIds);
            ext.put("matching_ad_data", adXTargeting);

            imp1.setId("12345");

            imp1.setExt(ext);

            impressions.add(imp1);
            request.setImp(impressions);
            request.setId("1234");

            Map<String, Ad> bidRequestIdToAdObject = new HashMap<String, Ad>();
            bidRequestIdToAdObject.put(request.getId(), ad);

            request.getLot49Ext().setBidRequestIdToAdObject(bidRequestIdToAdObject);

            Map<String, Tag> bidIdToTagObject = new HashMap<String, Tag>();
            bidIdToTagObject.put(resp.getSeatbid().get(0).getBid().get(0).getId(),
                            ad.getTags().get(0));
            request.getLot49Ext().setBidIdToTagObject(bidIdToTagObject);

            resp.getSeatbid().get(0).getBid().get(0).setImpid(imp1.getId());
            o = adapter.convertResponse(request, resp);
        } catch (Throwable t) {
            throw new WebApplicationException(t);
        }

        switch (responseType) {
            case DEBUG_RESPONSE_TYPE_FULL_BINARY:

                rb = Response.ok(o, adapter.getResponseMediaType());

                break;
            case DEBUG_RESPONSE_TYPE_FULL_TEXT:
                rb = Response.ok(o.toString(), MediaType.TEXT_PLAIN);

                break;
            case DEBUG_RESPONSE_TYPE_FULL_JSON:
                if (o instanceof String) {
                    rb = Response.ok(o.toString(), MediaType.TEXT_PLAIN);
                } else {
                    try {
                        String json = Utils.MAPPER.writeValueAsString(o);
                        rb = Response.ok(json, MediaType.APPLICATION_JSON);
                    } catch (Throwable t) {
                        throw new WebApplicationException(t);
                    }
                }
                break;

            case DEBUG_RESPONSE_TYPE_TAG_BINARY:
                if (tag.isBanner()) {
                    rb = Response.ok(adm, MediaType.TEXT_HTML)
                                    .header("X-Lot49-Creative-Content-Type", tag.getMime());
                } else {
                    // Mime type for video is the type of underlying creative,
                    // not
                    // the tag - which is always XML (VAST)
                    rb = Response.ok(adm, MediaType.APPLICATION_XML)
                                    .header("X-Lot49-Creative-Content-Type", tag.getMime());
                }
                break;
            case DEBUG_RESPONSE_TYPE_TAG_TEXT:
                rb = Response.ok(adm, MediaType.TEXT_PLAIN);
                break;
            case DEBUG_RESPONSE_TYPE_TAG_CACHED:
                if (tag.isBanner()) {
                    rb = Response.ok(adm, MediaType.TEXT_HTML)
                                    .header("X-Lot49-Creative-Content-Type", tag.getMime());
                } else {
                    // Mime type for video is the type of underlying creative,
                    // not
                    // the tag - which is always XML (VAST)
                    rb = Response.ok(adm, MediaType.APPLICATION_XML)
                                    .header("X-Lot49-Creative-Content-Type", tag.getMime());
                }
                final String nurlIdCache = Utils.getId();
                Bidder.getInstance().getAdCache().getCwrMap().put(
                                KVKeysValues.DEBUG_NURL_PREFIX + nurlIdCache,
                                new CacheableWebResponse(rb));
                map.put(KVKeysValues.DEBUG_NURL_PREFIX + nurlIdCache, new CacheableWebResponse(rb));
                String cacheUrlCache = "/debugNurl/" + nurlIdCache;
                String fullUrl = Bidder.getInstance().getConfig().getBaseUrl() + cacheUrlCache;
                rb = Response.ok("<a href=\"" + fullUrl + "\">" + cacheUrlCache + "</a>",
                                MediaType.TEXT_HTML);
                break;
            case DEBUG_RESPONSE_TYPE_TAG_CONTAINER:
                if (tag.isBanner()) {
                    rb = Response.ok(adm, MediaType.TEXT_HTML)
                                    .header("X-Lot49-Creative-Content-Type", tag.getMime());
                } else {
                    // Mime type for video is the type of underlying creative,
                    // not
                    // the tag - which is always XML (VAST)
                    rb = Response.ok(adm, MediaType.APPLICATION_XML)
                                    .header("X-Lot49-Creative-Content-Type", tag.getMime());
                }
                final String nurlIdCont = Utils.getId();
                Bidder.getInstance().getAdCache().getCwrMap().put(
                                KVKeysValues.DEBUG_NURL_PREFIX + nurlIdCont,
                                new CacheableWebResponse(rb));
                String cacheUrlCont = Bidder.getInstance().getConfig().getBaseUrl() + "/"
                                + Lot49Constants.REL_PATH_DEBUG_NURL + "/" + nurlIdCont;
                if (tag.isBanner()) {
                    rb = Response.ok("<IFRAME SRC=\"" + cacheUrlCont + "\"></IFRAME>",
                                    MediaType.TEXT_HTML);
                } else {
                    String vastInspectorUrl =
                                    "https://developers.google.com/interactive-media-ads/docs/vastinspector_dual?player=flash&tag="
                                                    + cacheUrlCont;
                    try {
                        rb = Response.seeOther(new URI(vastInspectorUrl));
                    } catch (URISyntaxException e) {
                        LogUtils.error("Error creating URI from " + vastInspectorUrl, e);
                        throw new WebApplicationException(e);
                    }
                }

                break;

            default:
                throw new WebApplicationException("Unknown response type: " + responseType,
                                Status.BAD_REQUEST);
        }
        return response.resume(rb.build());
    }

    /**
     * We are very minimally simulating onBidRequestDelegate here.
     * 
     * @see AuctionsSvc#onBidRequestDelegate(JerseySvc, ExchangeAdapter, AsyncResponse,
     *      OpenRtbRequest, String, HttpServletRequest, String)
     */
    private OpenRtbResponse createBidResponse(ExchangeAdapter adapter, Ad ad, Tag tag) {
        OpenRtbRequest req = new OpenRtbRequest();
        String brId = "test_br_" + Utils.getId();
        req.setId(brId);
        List<Impression> imps = new ArrayList<Impression>();
        req.setImp(imps);
        Impression imp = new Impression();
        imps.add(imp);
        imp.setId("test_imp_1");
        if (tag.isBanner()) {
            Banner b = new Banner();
            imp.setBanner(b);
            b.setW(300);
            b.setH(250);
        } else {
            Video v = new Video();
            imp.setVideo(v);
            v.setW(300);
            v.setH(250);
        }

        final Lot49Ext lot49Ext = req.getLot49Ext();
        lot49Ext.setNoBid(true);
        lot49Ext.setAdapter(adapter);

        LogUtils.logRequest(req, true, 1);
        LogUtils.logRequest(req, false, 1);

        final OpenRtbResponse resp = new OpenRtbResponse();
        resp.setId(req.getId());
        resp.setBidid("test_bid_1");
        resp.setCur("USD");
        final SeatBid seatBid = new SeatBid();
        resp.getSeatbid().add(seatBid);
        seatBid.setSeat(adapter.getSeat(ad));

        final List<Bid> bids = new ArrayList<Bid>();
        seatBid.setBid(bids);

        Bid bid = tag.getBid(req, imp);
        if (adapter.isNurlRequired()) {
            String txt = req.getLot49Ext().getBidIdToTagText().get(bid.getId());
            DaoShortLivedMap<String> nurlMap =
                            ServiceRunner.getInstance().getAdCache().getNurlMap();
            final String nurlId =
                            (String) bid.getHiddenAttributes().remove(KVKeysValues.NURL_PREFIX);
            final String nurlKey = KVKeysValues.NURL_PREFIX + bid.getNurl();
            nurlMap.put(nurlKey, txt);

        }
        bids.add(bid);
        LogUtils.logBid(req, 1, brId, "pushkin", "pushkin", bid, 1, 1, null,
                        Lot49Constants.BID_OUTCOME_SUBMITTED);
        return resp;

    }
}
