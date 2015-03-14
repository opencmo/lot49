package com.enremmeta.rtb.proto.adaptv;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.Lot49RuntimeException;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.spotxchange.ExchangeAdapterImpl;
import com.enremmeta.util.Utils;

/**
 * Converter for {@link Lot49Constants#EXCHANGE_ADAPTV}.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class AdaptvAdapter extends ExchangeAdapterImpl<OpenRtbRequest, OpenRtbResponse> {

    @Override
    public void parseUserId(String userId, OpenRtbRequest req) {
        if (userId == null) {
            return;
        }
        userId = BAD_ADAPTV_PATTERN_1_PATTERN.matcher(userId).replaceAll("");
        super.parseUserId(userId, req);
    }

    @Override
    public String getSampleWinningPrice() {
        return "250000";
    }

    private final String buyerId;

    private final int defaultMaxDuration;

    private final boolean assumeSwfIfVpaid;

    public static final String BAD_ADAPTV_PATTERN_1_STR = "%(25)+(3D)?";

    public static final Pattern BAD_ADAPTV_PATTERN_1_PATTERN =
                    Pattern.compile(BAD_ADAPTV_PATTERN_1_STR);

    public AdaptvAdapter() {
        super();
        final Lot49Config conf = Bidder.getInstance().getConfig();
        this.config = conf.getExchanges() == null ? new AdaptvConfig()
                        : (conf.getExchanges().getAdaptv() == null ? new AdaptvConfig()
                                        : conf.getExchanges().getAdaptv());
        this.buyerId = config.getBuyerId();
        if (this.buyerId == null) {
            throw new Lot49RuntimeException("buyerId is null");
        }
        this.defaultMaxDuration = config.getDefaultMaxDuration();
        this.assumeSwfIfVpaid = config.isAssumeSwfIfVpaid();

    }

    private final AdaptvConfig config;

    /**
     * AdapTV expects a VAST in rsponse.
     */
    @Override
    public OpenRtbResponse convertResponse(OpenRtbRequest req, OpenRtbResponse resp)
                    throws Exception {
        return resp;
    }

    @Override
    public final String getWinningPriceMacro() {
        return "${AUCTION_PRICE}";
    }

    @Override
    public boolean localUserMapping() {
        return true;
    }

    private static String tmpCookieFixup(String s, int depth) {
        if (depth > 10) {
            return s;
        }
        int start = 0;
        if (s.length() >= Utils.MOD_UID_COOKIE_LENGTH_MIN
                        && s.length() <= Utils.MOD_UID_COOKIE_LENGTH_MAX) {
            return s;
        }

        while (true) {
            final int percIdx = s.indexOf("%", start);
            if (percIdx < 0) {
                break;
            }
            if (percIdx < s.length() - 2) {
                String maybeEncoded = s.substring(percIdx + 1, percIdx + 3);
                if (percIdx > 22 && maybeEncoded.equals("25")) {
                    start = percIdx + 3;
                    continue;
                }
                if (Character.digit(maybeEncoded.charAt(0), 16) > -1
                                && Character.digit(maybeEncoded.charAt(1), 16) > -1) {
                    s = s.replace("%" + maybeEncoded, String.valueOf(
                                    Character.toChars(Integer.parseInt(maybeEncoded, 16))));
                    continue;
                } else {
                    start = percIdx + 3;
                    continue;
                }
            } else {
                break;
            }
        }
        final int slen = s.length();
        if (slen >= 25 && s.substring(22, 25).equals("%25")) {
            // These are not magic numbers, these are mod UID cookie version
            // lengths.
            // look those up
            s = s.substring(0, 22) + "==";
        } else if (slen > Utils.MOD_UID_COOKIE_LENGTH_MAX) {
            final int per25Idx0 = s.indexOf("%25");
            if (per25Idx0 < 0) {
                return s;
            }
            final int per25Idx1 = per25Idx0 + 3;
            String goodS = s.substring(0, per25Idx0);
            boolean inCorruptedSubstring = false;
            if (per25Idx1 < Utils.MOD_UID_COOKIE_LENGTH_MIN) {
                int i = per25Idx1;
                for (; i < slen - 1; i += 2) {
                    if (!s.substring(i, i + 2).equals("25")) {
                        break;
                    }
                }
                final String rest = s.substring(i);
                final String maybeEncoded = rest.substring(0, 2);
                final String rest2 = rest.substring(2);
                if (!maybeEncoded.equals("25") && (Character.digit(maybeEncoded.charAt(0), 16) > -1
                                && Character.digit(maybeEncoded.charAt(1), 16) > -1)) {
                    goodS += String.valueOf(Character.toChars(Integer.parseInt(maybeEncoded, 16)));
                } else {
                    goodS += maybeEncoded;
                }
                goodS += rest2;
                s = tmpCookieFixup(goodS, depth + 1);
            }

        }
        return s;
    }

    @Override
    public OpenRtbRequest convertRequest(OpenRtbRequest req) {
        Site site = req.getSite();
        if (site != null) {
            String page = site.getPage();
            if (page != null && page.trim().length() > 0) {
                try {
                    if (page.startsWith("//")) {
                        page = "http:" + page;
                    } else if (!page.startsWith("http://") && !page.startsWith("https://")) {
                        page = "http://" + page;
                    }

                    String domain = new URL(page).getHost();
                    if (domain.startsWith("www.")) {
                        domain = domain.substring(4);
                    }
                    site.setDomain(domain);

                } catch (MalformedURLException e) {
                    if (!Utils.validateDomain(page)) {
                        trace("Malformed URL " + page + " in request");
                    }
                }
            }
        }

        final User user = req.getUser();
        if (user != null) {
            parseUserId(user.getBuyeruid(), req);
        }
        req.getLot49Ext().setAdapter(this);
        return req;
    }

    @Override
    public String getName() {
        return Lot49Constants.EXCHANGE_ADAPTV;
    }

    @Override
    public String getPartnerInitiatedSyncUrl(String myUserId) {
        return "http://sync.adaptv.advertising.com/sync?type=gif&key=" + this.buyerId + "&uid="
                        + myUserId;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) {
        final Double winningPriceCpm = Double.parseDouble(winningPriceString);

        final long wpMicros = Utils.cpmToMicro(winningPriceCpm);
        ParsedPriceInfo ppi = new ParsedPriceInfo(winningPriceCpm, wpMicros, bidMicros);
        return ppi;
    }

    @Override
    public long getDefaultTimeout() {
        return 100;
    }

    @Override
    public String getClickMacro() {
        return "";
    }

    @Override
    public String getClickEncMacro() {
        return "";
    }

    public static final String RESPONSE_MEDIA_TYPE = MediaType.APPLICATION_JSON;

    @Override
    public String getResponseMediaType() {
        return RESPONSE_MEDIA_TYPE;
    }
}
