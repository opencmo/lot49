package com.enremmeta.rtb.jersey;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralAllResponse;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralScoresDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ManifestUtils;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Here the services related to various operations-related things are gathered, and never really
 * die.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. This code is licensed under
 *         <a href="http://www.gnu.org/licenses/agpl-3.0.html">Affero GPL 3.0</a>
 *
 */
@Provider
@Path("/")
public class OpsSvc implements JerseySvc, Lot49Constants {

    @GET
    @Path("/db2/client/integralClientId/all.json")
    @Produces(MediaType.APPLICATION_JSON)
    /*
     * { "action": "passed", "clu": 1000, "country": "US", "dma": "501", "iab1": ["iab_news",
     * "iab_travel"], "iab2": [], "lang": "en", "risk": "LowRisk", "scores": { "v_s1": 1,
     * "visibility": 1000, "iv3": 45, "iv2": 45, "rsa": 1000, "vio0328": 1000, "hat": 1000, "sam":
     * 1000, "viv2": 45, "vio": 875, "arf": 1000, "pol": 1000, "v_w300": 1, "v_r6_5": 1, "webmail":
     * 1000, "ivp_160x600": 95, "v_c": 0, "ivp_728x90": 25, "off1220": 1000, "ivl_160x600": 95,
     * "trq": 750, "jdl0426": 1000, "ivl_300x250": 25, "v_ap": 1, "dlm": 1000, "zibm": 250,
     * "jof0427": 1000, "ivt": 25000, "ugf": 1000, "ivp": 45, "ugb": 1000, "v_h250": 1, "jha0420":
     * 1000, "ugc": 1000, "ugd": 1000, "ivl_728x90": 5, "lang": 9, "ivu": 35, "ugm": 1000, "par":
     * 1000, "iab_news": 250, "drg": 1000, "adt": 1000, "top": 55000, "pac": 3, "alc": 1000,
     * "ivp_300x250": 45, "iviab": 55, "niv": 45, "ugt": 1000, "ugs": 1000, "iviab_728x90": 35,
     * "pro": 1000, "off": 1000, "zult": 1000, "iviab_160x600": 95, "ivl": 15, "iab_travel": 250,
     * "iviab_300x250": 55 }, "si": "www.cnn.com", "state": "NY", "traq": 750, "ttl":
     * "2016-06-30T13:47-0400", "uem": { "ivt": 25000, "ivp": 45, "niv": 45, "ivl": 15, "ivu": 35,
     * "iviab": 55, "top": 55000 } }
     */
    public Response integral(final @QueryParam("adsafe_url") String url) {
        IntegralAllResponse resp = new IntegralAllResponse();
        Random rand = new Random(System.currentTimeMillis());

        resp.setAction(rand.nextBoolean() ? "passed" : "failed");
        resp.setClu(rand.nextInt(1000));
        resp.setCountry("US");
        resp.setDma("501");
        resp.setIab1(new ArrayList<String>() {
            {
                add("iab_news");
                add("iab_business");
            }
        });
        resp.setIab2(new ArrayList<String>() {
            {
                add("iab_t2_businessnews");
                add("iab_t2_stocks");
            }
        });
        resp.setLang("en");
        String risk = "";
        switch (rand.nextInt(4)) {
            case 0:
                risk = "LowRisk";
                break;
            case 1:
                risk = "ModerateRisk";
                break;
            case 2:
                risk = "HighRisk";
                break;
            default:
                risk = "VeryHighRisk";
        }
        resp.setRisk(risk);

        IntegralScoresDto scores = new IntegralScoresDto();
        resp.setScores(scores);
        scores.setV_s1(rand.nextInt(10));
        // Visibility score determines how much information about the page Integral can deduce from
        // the
        // URL contained in the bid request.
        scores.setVisibility(rand.nextInt(1000));
        // ???
        scores.setIv3(rand.nextInt(100));
        // ???
        scores.setIv2(rand.nextInt(100));
        // ???
        scores.setRsa(rand.nextInt(100));
        // ???
        scores.setVio1029(rand.nextInt(1000));
        // Hate
        scores.setHat(rand.nextInt(1000));
        // Suspicious Activity/Fraud
        scores.setSam(rand.nextInt(1000));
        // ???
        scores.setViv2(rand.nextInt(100));
        // ???
        scores.setArf(rand.nextInt(1000));
        // Violence
        scores.setVio(rand.nextInt(1000));
        // ???
        scores.setPol(rand.nextInt(1000));
        // ???
        scores.setV_w300(rand.nextInt(10));
        // ???
        scores.setV_r6_5(rand.nextInt(10));
        // ???
        scores.setWebmail(rand.nextInt(1000));
        scores.setIvp_160x600(rand.nextInt(100));
        scores.setV_c(rand.nextInt(10));
        scores.setIvp_728x90(rand.nextInt(100));
        scores.setOff1220(rand.nextInt(100));
        scores.setIvl_160x600(rand.nextInt(100));
        scores.setTrq(rand.nextInt(1000));
        scores.setJdl0426(rand.nextInt(1000));
        scores.setIvl_300x250(rand.nextInt(100));
        scores.setV_ap(rand.nextInt(10));

        // Illegal downloads
        scores.setDlm(rand.nextInt(1000));
        scores.setZibm(rand.nextInt(1000));
        scores.setJof0427(rand.nextInt(1000));
        scores.setIvt(rand.nextInt(100000));
        scores.setUgf(rand.nextInt(1000));
        scores.setIvp(rand.nextInt(100));
        scores.setUgb(rand.nextInt(1000));
        scores.setV_h250(rand.nextInt(10));
        scores.setJha0420(rand.nextInt(1000));
        scores.setUgc(rand.nextInt(1000));
        scores.setUgd(rand.nextInt(1000));
        scores.setIvl_728x90(rand.nextInt(10));
        scores.setLang(9);
        scores.setIvu(rand.nextInt(100));
        scores.setUgm(rand.nextInt(1000));
        scores.setPar(rand.nextInt(1000));
        scores.setIab_news(rand.nextInt(1000));

        // Drugs
        scores.setDrg(rand.nextInt(1000));
        // Adult
        scores.setAdt(rand.nextInt(1000));
        scores.setTop(rand.nextInt(100000));
        scores.setPac(rand.nextInt(10));

        // Alcohol
        scores.setAlc(rand.nextInt(1000));
        scores.setIvp_300x250(rand.nextInt(100));
        scores.setIviab(rand.nextInt(100));
        scores.setNiv(rand.nextInt(100));
        scores.setUgt(rand.nextInt(1000));
        scores.setUgs(rand.nextInt(1000));
        scores.setIvp_728x90(rand.nextInt(100));
        scores.setPro(rand.nextInt(1000));
        // Offensive
        scores.setOff(rand.nextInt(1000));

        scores.setZult(rand.nextInt(1000));
        scores.setIviab_160x600(rand.nextInt(100));
        scores.setIvl(rand.nextInt(100));
        scores.setIab_travel(rand.nextInt(100));
        scores.setIviab_300x250(rand.nextInt(100));
        scores.setIviab_728x90(rand.nextInt(100));
        scores.setIab_business(rand.nextInt(100));
        scores.setIab_religion(rand.nextInt(100));


        try {
            resp.setSi(new URL(url).getHost());
        } catch (MalformedURLException e) {
            return Response.status(Status.BAD_REQUEST).entity("Bad URL: " + url).build();
        }
        resp.setState("NY");
        // TRAQ (True Advertising Quality) Scores
        resp.setTraq(rand.nextInt(1000));
        // TODO - right now doesn't work because we use
        // Jackson here which serializes differently than ISO 8601 expected
        // by gson.
        // resp.setTtl(new DateTime().plusDays(1).toDate());

        ViewabilityDto uem = new ViewabilityDto();
        resp.setUem(uem);

        // Takes the minimum value of all the iviab
        // placement probabilities
        uem.setIviab(rand.nextInt(100));
        // scores.setIab_religion(iab_religion);
        // The probability of an ad being in view when
        // the user opens the page.
        uem.setIvl(rand.nextInt(100));
        // The probability of an ad being in view for
        // more than 5 seconds. A higher score indicates
        // a higher probability that the ad will be
        // viewable by the user.
        uem.setIvp(rand.nextInt(100));
        // The average time an advertisement is in view
        // on a page.
        uem.setIvt(rand.nextInt(100));
        // Probability that at least 50% of the ad will be
        // viewable when the user leaves the page.
        uem.setIvu(rand.nextInt(100));
        // The probability that an ad will not be viewed
        // by a user. A higher score indicates a higher
        // probability that the ad will NOT be viewed by
        // the user.
        uem.setNiv(rand.nextInt(100));
        // Average time a user spends on a page.
        uem.setTop(rand.nextInt(100));
        return Response.ok().entity(resp).build();
    }


    /**
     * Health check URL
     * 
     * Generates a simple JSON message with info:
     * <ul>
     * <li>Current timestamp</li>
     * <li>Current DateTime string</li>
     * <li>Version of Bidder</li>
     * <li>Name of branch (from Git)</li>
     * <li>Commit (from Git)</li>
     * <li>Build time</li>
     * </ul>
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response version() {

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode result = factory.objectNode();
        result.put("Current timestamp", BidderCalendar.getInstance().currentTimeMillis());
        result.put("Current DateTime string", BidderCalendar.getInstance().currentDateString());

        if (!ManifestUtils.getVersion().isEmpty()) {
            result.put("Version", ManifestUtils.getVersion());
        }

        if (!ManifestUtils.getBuildBranch().isEmpty()) {
            result.put(ManifestUtils.BUILD_BRANCH_KEY, ManifestUtils.getBuildBranch());
        }

        if (!ManifestUtils.getBuildRevision().isEmpty()) {
            result.put(ManifestUtils.BUILD_REVISION_KEY, ManifestUtils.getBuildRevision());
        }

        if (!ManifestUtils.getBuildTime().isEmpty()) {
            result.put(ManifestUtils.BUILD_TIME_KEY, ManifestUtils.getBuildTime());
        }

        return Response.status(Response.Status.OK).entity(result).type(MediaType.APPLICATION_JSON)
                        .build();

    }

    @GET
    @Path("favicon.ico")
    public Object favicon() {
        String favicon = Bidder.getInstance().getConfig().getFavicon();
        if (favicon == null) {
            return Response.noContent().build();
        } else {
            favicon = favicon.trim();
            if (favicon.length() == 0) {
                return Response.noContent().build();
            } else {
                try {
                    return Response.seeOther(new URI(favicon)).build();
                } catch (URISyntaxException e) {
                    LogUtils.error(e);
                    return Response.serverError().build();
                }
            }
        }
    }

    @GET
    @Path(Lot49Constants.REL_PATH_DEBUG_NURL + "/{nurlId}")
    public Object debugNurl(@PathParam("nurlId") String nurlId) throws Lot49Exception {
        final AdCache adCache = Bidder.getInstance().getAdCache();
        DaoShortLivedMap<CacheableWebResponse> cwrMap = adCache.getCwrMap();
        CacheableWebResponse cwr = cwrMap.get(KVKeysValues.DEBUG_NURL_PREFIX + nurlId);
        return cwr.getResponse();
    }

    @GET
    @Path("loaderio-{id:[a-z0-9]+}")
    @Produces(MediaType.TEXT_PLAIN)
    public String loaderio(@PathParam("id") String id) {
        String retval = "loaderio-" + id;
        return retval;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("crossdomain.xml")
    public String crossdomain() {
        LogUtils.info("A crossdomain.xml request - should be handled by frontend");
        final String retval = "<?xml version=\"1.0\"?>\n"
                        + "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">\n"
                        + "<cross-domain-policy>\n"
                        + "<site-control permitted-cross-domain-policies=\"all\"/>\n"
                        + "<allow-access-from domain=\"*\" secure=\"false\"/>\n"
                        + "<allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>"
                        + "</cross-domain-policy>";
        return retval;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("robots.txt")
    public String robots() {
        LogUtils.info("A crossdomain.xml request - should be handled by frontend");
        // Should totally return
        // A 302 to
        // https://www.youtube.com/watch?v=S_oMD6-6q5Y
        return "User-agent: *\r\nDisallow: /";
    }
}
