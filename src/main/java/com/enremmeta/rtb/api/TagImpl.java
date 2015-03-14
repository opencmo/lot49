package com.enremmeta.rtb.api;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.config.ClientConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.Macros;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.proto.bidswitch.BidswitchVideoExt;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;

/**
 * Implements most of the default functionality, leaving only things specific to each tag.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 * 
 */
public abstract class TagImpl implements Tag, Lot49Constants {
    @Override
    public String getTagVersion() {
        return tagVersion;
    }

    protected String tagVersion = "1";

    @Override
    public SortedMap<String, SortedMap<String, AtomicLong>> getOptoutsByExchange() {
        return optoutsByExchange;
    }

    @Override
    public void incrOptout(OpenRtbRequest req, String name) {

        final String reqId = req.getId();
        final String exchange = req.getLot49Ext().getAdapter().getName();

        String alreadyOptedOut = uniqOptoutReasonMap.putIfAbsent(exchange + "_" + reqId, name);
        if (alreadyOptedOut == null) {
            SortedMap<String, AtomicLong> xchOptout = optoutsByExchange.get(exchange);
            xchOptout.get(name).incrementAndGet();
        } else {
            LogUtils.trace(getId() + ": " + "Not opting out for reason of " + name
                            + " because already for " + alreadyOptedOut + " (" + reqId + ")");
        }
    }

    private ConcurrentMap<String, String> uniqOptoutReasonMap =
                    new ConcurrentHashMap<String, String>();

    public Dimension getDimension() {
        return this.dim;
    }

    public ExchangeTargeting getExchangeTargeting() {
        return exchangeTargeting;
    }

    protected ExchangeTargeting exchangeTargeting;

    protected Dimension dim;

    protected boolean banner = false;

    protected String customImpPassThruData;

    public String getCustomImpPassThruData() {
        return customImpPassThruData;
    }

    protected int protocol;

    @Override
    public int getProtocol() {
        return protocol;
    }

    @Override
    public int getApi() {
        return api;
    }

    protected int api;

    protected String impRedir = null;
    protected String clickRedir = null;

    public String getImpRedir() {
        return impRedir;
    }

    private ServiceRunner bidder = Bidder.getInstance();
    protected String desc;
    protected int duration = 0;
    private String id = "";

    protected boolean linear = false;

    protected String mime = "application/x-shockwave-flash";

    protected List<String> mimes = null;

    public List<String> getMimes() {
        return mimes;
    }

    protected String name;

    private int passedCount;

    private Ad ad;

    protected boolean test = false;

    protected boolean video = false;

    protected boolean vpaid = false;

    public TagImpl(Ad ad) throws Lot49Exception {
        super();
        this.ad = ad;
        String[] classNameElts = getClass().getSimpleName().split("_");
        if (classNameElts.length != 5) {
            throw new Lot49Exception("Incorrect naming of " + getClass().getName()
                            + ", expected Tag__<tagId>_<tagName>_<adId>_<adName>");
        }
        if (!classNameElts[0].equals("Tag")) {
            throw new Lot49Exception("Incorrect naming of " + getClass().getName()
                            + ", expected Tag__<tagId>_<tagName>_<adId>_<adName>");
        }

        if (!this.ad.getId().equals(classNameElts[3])
                        || !this.ad.getName().equals(classNameElts[4])) {
            throw new Lot49Exception("Ad ID (" + classNameElts[3] + ") or name (" + classNameElts[4]
                            + ") specified in name different than in " + this.ad);
        }

        id = classNameElts[1];
        name = classNameElts[2];

        final Lot49Config config = bidder.getConfig();
        if (config == null) {
            statsUrl = null;
        } else {
            final Map<String, ClientConfig> clientConfig = config.getClients();
            if (clientConfig == null) {
                statsUrl = config.getStatsUrl();
            } else {
                final String clientId = getAd().getClientId();
                if (clientId == null) {
                    statsUrl = config.getStatsUrl();
                } else {
                    final ClientConfig curClientConfig = clientConfig.get(clientId);
                    if (curClientConfig == null) {
                        statsUrl = config.getStatsUrl();
                    } else {
                        statsUrl = curClientConfig.getStatsUrl() == null ? config.getStatsUrl()
                                        : curClientConfig.getStatsUrl();
                    }
                }
            }
        }
        LogUtils.trace("Stats URL for " + this + ": " + statsUrl);
        this.secureStatsUrl = StringUtils.replace(statsUrl, "http", "https", 1);
        init();
    }

    private final String statsUrl;
    private final String secureStatsUrl;

    /**
     * Default is true.
     */
    protected boolean sslCapable = true;

    public boolean isSslCapable() {
        return sslCapable;
    }

    @Override
    public String getProto(OpenRtbRequest req) {
        return req.getLot49Ext().isSsl() ? "https" : "http";
    }

    @Override
    public long getImpressionsConsidered(String exchange) {
        return impsConsideredByExchange.get(exchange).get();
    }

    @Override
    public String canBid(OpenRtbRequest req, Impression imp) {
        String xch = req.getLot49Ext().getAdapter().getName();
        impsConsideredByExchange.get(xch).incrementAndGet();
        final Lot49Ext ext = req.getLot49Ext();
        if (ext.isSsl() && !isSslCapable()) {
            incrOptout(req, TAG_DECISION_SSL_REQUIRED);
            return "SSL required in the request - I'm sorry, I can't, don't hate me.";
        }

        final Set<MarkupType> excludedMarkups = ext.getExcludedMarkups();
        if (excludedMarkups != null && excludedMarkups.contains(markupType)) {
            incrOptout(req, TAG_DECISION_REQUEST_DISALLOWS_MARKUP);
            return "Request disallows type: " + this.markupType;
        }

        if (video) {
            Video v = imp.getVideo();
            if (v == null) {
                incrOptout(req, TAG_DECISION_NOT_VIDEO);
                return "Not video";
            }
            boolean linearityOk =
                            (linear && v.getLinearity() == 1) || (!linear && v.getLinearity() == 2);
            if (!linearityOk) {
                incrOptout(req, TAG_DECISION_VIDEO_LINEARITY);
                return "Linearity: " + v.getLinearity();
            }

            if (!dim.check(v.getW(), v.getH())) {
                incrOptout(req, TAG_DECISION_DIMENSIONS);
                return "Dimensions: " + v.getW() + "x" + v.getH() + " (not " + dim + ")";
            }

            boolean mimeOk = false;
            if (v.getMimes() != null) {
                if (mimes == null) {
                    if (v.getMimes().contains(mime)) {
                        mimeOk = true;
                    } else {
                        incrOptout(req, TAG_DECISION_MIME);
                        return "Mime: " + mime + " not in " + v.getMimes();
                    }
                } else {
                    for (String m : mimes) {
                        if (v.getMimes().contains(m)) {
                            mimeOk = true;
                            break;
                        }
                    }
                    if (!mimeOk) {
                        incrOptout(req, TAG_DECISION_MIME);
                        return "None of " + mimes + " in " + v.getMimes();
                    }
                }
            } else {
                incrOptout(req, TAG_DECISION_MIME);
                return "No Video mimes in request";
            }

            if (v.getMaxduration() != 0 && getDuration() > v.getMaxduration()) {
                incrOptout(req, TAG_DECISION_DURATION);
                return "Max duration: " + v.getMaxduration();
            }

            if (v.getMinduration() != 0 && getDuration() < v.getMinduration()) {
                incrOptout(req, TAG_DECISION_DURATION);
                return "Min duration: " + v.getMinduration();
            }

            if (v.getProtocol() != 0 && protocol != v.getProtocol()) {
                incrOptout(req, TAG_DECISION_PROTOCOL);
                return "Protocol: " + v.getProtocol() + "(NOT " + protocol + ")";
            }

            if (v.getProtocols() != null && !v.getProtocols().contains(protocol)) {
                incrOptout(req, TAG_DECISION_PROTOCOL);
                return "Protocols: " + protocol + " not in " + v.getProtocols();
            }

            if (v.getApi() != null && api != 0 && !v.getApi().contains(api)) {
                incrOptout(req, TAG_DECISION_API);
                return "API: " + api + " not in " + v.getApi();
            }

            passedCount++;
            return null;
        } else if (banner) {
            Banner b = imp.getBanner();
            if (b == null) {
                incrOptout(req, TAG_DECISION_NOT_BANNER);
                return "Not banner";
            }

            if (!dim.check(b.getW(), b.getH())) {
                incrOptout(req, TAG_DECISION_DIMENSIONS);
                return "Dimensions: " + b.getW() + "x" + b.getH() + " (not " + dim + ")";
            }

            boolean mimeOk = false;
            if (b.getMimes() != null) {
                if (mimes == null) {
                    if (b.getMimes().contains(mime)) {
                        mimeOk = true;
                    } else {
                        incrOptout(req, TAG_DECISION_MIME);
                        return "Mime: " + mime + " not in " + b.getMimes();
                    }
                } else {
                    for (String m : mimes) {
                        if (b.getMimes().contains(m)) {
                            mimeOk = true;
                            break;
                        }
                    }
                    if (!mimeOk) {
                        incrOptout(req, TAG_DECISION_MIME);
                        return "None of " + mimes + " in " + b.getMimes();
                    }
                }
            }
            passedCount++;
            return null;
        }

        return "Unknown";

    }

    public ServiceRunner getBidder() {
        return bidder;
    }

    /**
     * @see Macros
     * 
     * @see <a href=
     *      "http://www.adopsinsider.com/ad-ops-basics/what-is-a-cache-buster-and-how-does-it-work/">
     *      Cachebuster</a>
     */
    public String getCb() {
        final long val0 = Utils.RANDOM.nextLong();
        final long val1 = val0 < 0 ? -val0 : val0;
        final String retval = "c" + val1 + "b";
        return retval;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getNUrl(OpenRtbRequest req, Bid bid, String nurlId) {
        return getNUrlByType(req, bid, nurlId, Lot49Constants.NURL_STANDART);
    }

    private String getNUrlByType(OpenRtbRequest req, Bid bid, String nurlId, int nurlType) {
        if (bid == null) {
            throw new IllegalStateException("Cannot obtain NUrl if bid has not been constructed.");
        }
        StringBuilder url = getStatsUrl(req);
        String impPath = null;

        // TODO we can have a map for imp paths...
        impPath = Lot49Constants.DEFAULT_NURL_PATH_ABSOLUTE;

        final ExchangeAdapter adapter = req.getLot49Ext().getAdapter();
        final String exchange = adapter.getName();
        final String wpMacro = adapter.getWinningPriceMacro();

        String ct = MediaType.TEXT_HTML;
        switch (markupType) {
            case VAST_PLAIN_FLASH_ONLY:
            case VAST_PLAIN_MULTIPLE:
            case VAST_VPAID:
            case VAST_WRAPPER_PLAIN_FLASH_ONLY:
            case VAST_WRAPPER_PLAIN_MULTIPLE:
            case VAST_WRAPPER_VPAID:
                ct = MediaType.APPLICATION_XML;
        }
        final String ssp = adapter.isAggregator() ? req.getLot49Ext().getSsp() : exchange;

        url.append(impPath);
        url.append("?");
        if (nurlType == Lot49Constants.NURL_STANDART
                        || nurlType == Lot49Constants.NURL_ONLY_WIN_NOTIFICATION) {
            url.append("wp=").append(wpMacro).append("&");
        }
        url.append("xch=").append(exchange).append("&crid=").append(getId()).append("&cid=")
                        .append(getAdId()).append("&iid=").append(bid.getImpid()).append("&bid=")
                        .append(bid.getId()).append("&bp=").append(ad.getBidPrice(req))
                        .append("&cid=").append(getAdId()).append("&crid=").append(getId())
                        .append("&ts=").append(System.currentTimeMillis()).append("&cb=")
                        .append(getCb()).append("&nurlId=").append(nurlId).append("&brid=")
                        .append(URLEncoder.encode(req.getId())).append("&ctype=").append(ct)
                        .append("&ssp=").append(ssp).append("&nodeId=")
                        .append(Bidder.getInstance().getOrchestrator().getNodeId());


        if (nurlType == Lot49Constants.NURL_STANDART && adapter.isMacrosInNurl()) {
            url.append("&").append(Lot49Constants.QUERY_STRING_EXCHANGE_CLICK_THROUGH_MACRO)
                            .append("=").append(adapter.getClickMacro()).append("&")
                            .append(Lot49Constants.QUERY_STRING_EXCHANGE_CLICK_THROUGH_ENCODED_MACRO)
                            .append("=").append(adapter.getClickEncMacro());

        }

        if (nurlType == Lot49Constants.NURL_STANDART || nurlType == Lot49Constants.NURL_ONLY_TAG) {
            final String clickUrl = getClickUrl(req, bid);
            // final String clickUrlEnc = encode(clickUrl);
            // final String clickUrlEncEnc = encode(clickUrlEnc);

            // Pass this downstream

            url.append("&").append(Lot49Constants.QUERY_STRING_LOT49_CLICK_THROUGH_MACRO)
                            .append("=").append(URLEncoder.encode(clickUrl));
        }

        url.append("&nt=").append(nurlType);

        return url.toString();
    }

    /**
     * @see Tag#getImpressionUrl(OpenRtbRequest, Bid, boolean)
     */
    @Override
    public String getImpressionUrl(OpenRtbRequest req, Bid bid, boolean nurl) {
        if (bid == null) {
            throw new IllegalStateException(
                            "Cannot obtain impression URL if bid has not been constructed.");
        }
        StringBuilder url = getStatsUrl(req);
        String impPath = null;
        // TODO we can have a map for imp paths...
        impPath = Lot49Constants.DEFAULT_IMPRESSION_PATH_ABSOLUTE;

        url.append(impPath).append("?");
        // Pass in passed exchange-provided UID
        final User user = req.getUser();
        String buyerUid = user == null ? null : user.getBuyeruid();
        if (buyerUid == null) {
            buyerUid = "";
        }
        url.append("buid=").append(URLEncoder.encode(buyerUid)).append("&");

        final Lot49Ext lot49Ext = req.getLot49Ext();
        final ExchangeAdapter adapter = lot49Ext.getAdapter();
        final String exchange = adapter.getName();

        if (nurl) {
            url.append("nurl=1&");
        } else {
            // TODO replace - you can get this from Lot49Ext - faster than
            // constructing new object.
            final String wpMacro = adapter.getWinningPriceMacro();
            url.append("wp=").append(wpMacro).append("&nurl=0&");
        }
        final String ssp = adapter.isAggregator() ? req.getLot49Ext().getSsp() : exchange;
        String bidPriceStr = ad.isDynamicPricing() ? "0" : String.valueOf(ad.getBidPrice(req));
        url.append("xch=").append(exchange).append("&crid=").append(getId()).append("&cid=")
                        .append(getAdId()).append("&iid=").append(bid.getImpid()).append("&bid=")
                        .append(bid.getId()).append("&brid=").append(URLEncoder.encode(req.getId()))
                        .append("&bp=").append(bidPriceStr).append("&ts=")
                        .append(BidderCalendar.getInstance().currentTimeMillis()).append("&cb=")
                        .append(getCb()).append("&ssp=").append(ssp).append("&nodeId=")
                        .append(Bidder.getInstance().getOrchestrator().getNodeId());

        if (lot49Ext.isForceCookieReset()) {
            url.append("&fcr=1");
        }
        if (lot49Ext.isForceCookieResync()) {
            url.append("&fcrx=1");
        }

        if (impRedir != null) {
            // Should this automatically include macros? Probably not.

            String clickUrl = getClickUrl(req, bid);
            String clickUrlEnc = encode(clickUrl);
            String clickUrlEncEnc = encode(clickUrlEnc);

            StringBuilder impRedir2 = Utils.replace(new StringBuilder(impRedir),
                            Macros.MACRO_LOT49_CLICK_ENC_ENC, clickUrlEncEnc);

            impRedir2 = Utils.replace(impRedir2, Macros.MACRO_LOT49_CLICK_ENC, clickUrlEnc);

            impRedir2 = Utils.replace(impRedir2, Macros.MACRO_LOT49_CLICK, clickUrl);

            url.append("&r=").append(URLEncoder.encode(impRedir2.toString()));
        }

        if (customImpPassThruData != null) {
            url.append("&custom=").append(customImpPassThruData);
        }

        return url.toString();
    }

    @Override
    public String getClickUrl(final OpenRtbRequest req, final Bid bid) {
        if (bid == null) {
            throw new IllegalStateException(
                            "Cannot obtain click URL if bid has not been constructed.");
        }
        StringBuilder url = new StringBuilder(getStatsUrl(req));
        String clickPath = null;

        // TODO we can have a map for imp paths...
        clickPath = Lot49Constants.DEFAULT_CLICK_PATH_ABSOLUTE;
        final ExchangeAdapter adapter = req.getLot49Ext().getAdapter();
        final String exchange = adapter.getName();
        final String ssp = adapter.isAggregator() ? req.getLot49Ext().getSsp() : exchange;
        url.append(clickPath).append("?crid=").append(getId()).append("&cid=").append(getAdId())
                        .append("&iid=").append(bid.getImpid()).append("&bid=").append(bid.getId())
                        .append("&brid=").append(URLEncoder.encode(req.getId())).append("&cb=")
                        .append(getCb()).append("&xch=").append(exchange).append("&ssp=")
                        .append(ssp).append("&nodeId=")
                        .append(Bidder.getInstance().getOrchestrator().getNodeId());

        String redir = getClickRedir(req, bid);
        if (redir != null && redir.length() > 0) {
            if (redir.startsWith("http://") && req.getLot49Ext().isSsl() && isSslCapable()) {
                redir = StringUtils.replace(redir, "http://", "https://", 1);
            }
            redir = encode(redir);
            url.append("&r=").append(redir);
        } else {
            url.append("&r=");
        }

        return url.toString();
    }

    @Override
    public String getClickRedir(final OpenRtbRequest req, final Bid bid) {
        return clickRedir;
    }

    public String getMime() {
        return mime;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<String> validate() {
        final List<String> retval = new ArrayList<String>();

        if ((banner && video) || (!banner && !video)) {
            retval.add("Only one of 'banner' or 'video' must be true");
        }

        if (dim == null) {
            retval.add("Dimension not specified.");
        }

        if (retval.size() == 0) {
            return null;
        }

        return retval;
    }

    private StringBuilder getStatsUrl(final OpenRtbRequest req) {
        StringBuilder url = new StringBuilder(
                        (req.getLot49Ext().isSsl() && sslCapable) ? secureStatsUrl : statsUrl);

        return url;
    }

    private StringBuilder replaceProustMacros(final OpenRtbRequest req, final Impression imp,
                    final Bid bid, StringBuilder tag) {
        StringBuilder url = getStatsUrl(req);

        String proustPath = Lot49Constants.DEFAULT_IMPRESSION_PATH_ABSOLUTE;

        url.append(proustPath);
        final Lot49Ext lot49Ext = req.getLot49Ext();
        final ExchangeAdapter adapter = lot49Ext.getAdapter();
        final String exchange = adapter.getName();
        final String ssp = adapter.isAggregator() ? req.getLot49Ext().getSsp() : exchange;
        url.append(Lot49Constants.LOT49_VERSION_KEY + "=" + Lot49Constants.LOT49_VERSION_VALUE
                        + "&");
        url.append("xch=").append(exchange).append("&crid=").append(getId()).append("&cid=")
                        .append(getAdId()).append("&iid=").append(bid.getImpid()).append("&bid=")
                        .append(bid.getId()).append("&brid=").append(URLEncoder.encode(req.getId()))
                        .append("&cb=").append(getCb()).append("&ssp=").append(ssp)
                        .append("&nodeId=")
                        .append(Bidder.getInstance().getOrchestrator().getNodeId());

        if (lot49Ext.isForceCookieReset()) {
            url.append("&fcr=1");
        } else if (lot49Ext.isForceCookieResync()) {
            url.append("&fcrx=1");
        }

        if (tag.indexOf(Macros.MACRO_LOT49_PROUST_REST) >= 0) {
            tag = Utils.replace(tag, Macros.MACRO_LOT49_PROUST_REST,
                            url.append("&phase=rest").toString());
        } else if (tag.indexOf(Macros.MACRO_LOT49_PROUST_SYNC) >= 0) {
            tag = Utils.replace(tag, Macros.MACRO_LOT49_PROUST_SYNC,
                            url.append("&phase=sync").toString());
        }

        return tag;
    }

    /**
     * @see Tag#getTag(OpenRtbRequest, Impression, Bid, boolean)
     */
    @Override
    public String getTag(final OpenRtbRequest req, final Impression imp, final Bid bid,
                    final boolean nurl) {
        final ExchangeAdapter adapter = req.getLot49Ext().getAdapter();

        String impUrl = getImpressionUrl(req, bid, nurl);
        String impUrlEnc = Utils.encodeUrl(impUrl, adapter);

        final String clickUrl = getClickUrl(req, bid);
        final String clickUrlEnc = encode(clickUrl);
        final String clickUrlEncEnc = encode(clickUrlEnc);
        final String exchClick = adapter.getClickMacro();
        final String exchClickEnc = adapter.getClickEncMacro();

        StringBuilder tag = new StringBuilder(getTagTemplate(req, imp, bid));
        tag.trimToSize();

        // Pass 1. Impression.
        tag = Utils.replace(tag, Macros.MACRO_LOT49_IMPRESSION, impUrl);

        tag = Utils.replace(tag, Macros.MACRO_LOT49_IMPRESSION_ENC, impUrlEnc);

        // Pass 2. Proust
        tag = replaceProustMacros(req, imp, bid, tag);

        // Pass 3. Set up click chain.
        if (exchClick == null || exchClick.length() == 0) {
            tag = Utils.replace(tag, Macros.MACRO_LOT49_CLICK_CHAIN_ENC,
                            Macros.MACRO_LOT49_CLICK_ENC);
        } else {
            tag = Utils.replace(tag, Macros.MACRO_LOT49_CLICK_CHAIN_ENC,
                            Macros.MACRO_LOT49_EXCHANGE_CLICK_ENC
                                            + Macros.MACRO_LOT49_CLICK_ENC_ENC);
        }

        if (!adapter.isMacrosInNurl()) {

            // Pass 4.ClickEncEnc
            tag = Utils.replace(tag, Macros.MACRO_LOT49_CLICK_ENC_ENC, clickUrlEncEnc);

            // Pass 5. ClickEnc
            tag = Utils.replace(tag, Macros.MACRO_LOT49_CLICK_ENC, clickUrlEnc);

            // Pass 6. Click
            tag = Utils.replace(tag, Macros.MACRO_LOT49_CLICK, clickUrl);

            // Pass 7. ExchangeClickEnc
            // Do this last!
            if (exchClickEnc == null) {
                if (tag.indexOf(Macros.MACRO_LOT49_EXCHANGE_CLICK_ENC) > 0) {
                    String simClick = getStatsUrl(req) + Lot49Constants.DEFAULT_REDIR_PATH_ABSOLUTE
                                    + "?r=";
                    String simClickEnc = URLEncoder.encode(simClick);
                    LogUtils.warn(Macros.MACRO_LOT49_EXCHANGE_CLICK_ENC + " exists in tag "
                                    + getId() + " but exchange does not provide it, will use "
                                    + simClickEnc);
                    tag = Utils.replace(tag, Macros.MACRO_LOT49_EXCHANGE_CLICK_ENC, simClickEnc);
                }
            } else {
                tag = Utils.replace(tag, Macros.MACRO_LOT49_EXCHANGE_CLICK_ENC, exchClickEnc);
            }

            // Pass 8. Exchange Click.
            if (exchClick != null) {
                tag = Utils.replace(tag, Macros.MACRO_LOT49_EXCHANGE_CLICK, exchClick);
            }
        }
        // TODO point of optimization -- prepare indices in the tag template
        // where macros exist
        // req.getLot49Ext().get
        req.getLot49Ext().getBidIdToTagObject().put(bid.getId(), this);

        if (req.getLot49Ext().isSsl() && isSslCapable()) {
            tag = Utils.replaceAll(tag, "http:", "https:");
        }

        return tag.toString();
    }

    private SortedMap<String, SortedMap<String, AtomicLong>> optoutsByExchange =
                    new TreeMap<String, SortedMap<String, AtomicLong>>() {
                        {
                            for (final String xch : ExchangeAdapterFactory
                                            .getAllExchangeAdapterNames()) {
                                put(xch, makeSingleExchangeOptoutMap());
                            }

                        }
                    };

    private SortedMap<String, AtomicLong> impsConsideredByExchange =
                    new TreeMap<String, AtomicLong>() {
                        {
                            for (final String xch : ExchangeAdapterFactory
                                            .getAllExchangeAdapterNames()) {
                                put(xch, new AtomicLong(0));
                            }

                        }
                    };

    private static final SortedMap<String, AtomicLong> makeSingleExchangeOptoutMap() {
        final SortedMap<String, AtomicLong> retval = new TreeMap<String, AtomicLong>() {

            {
                put(TAG_DECISION_API, new AtomicLong(0));
                put(TAG_DECISION_DIMENSIONS, new AtomicLong(0));
                put(TAG_DECISION_DURATION, new AtomicLong(0));
                put(TAG_DECISION_MIME, new AtomicLong(0));
                put(TAG_DECISION_NOT_BANNER, new AtomicLong(0));
                put(TAG_DECISION_NOT_VIDEO, new AtomicLong(0));
                put(TAG_DECISION_PROTOCOL, new AtomicLong(0));
                put(TAG_DECISION_REQUEST_DISALLOWS_MARKUP, new AtomicLong(0));
                put(TAG_DECISION_SSL_REQUIRED, new AtomicLong(0));
                put(TAG_DECISION_VIDEO_LINEARITY, new AtomicLong(0));

            }
        };
        return retval;
    }

    public String getAdId() {
        return ad.getId();
    }

    @Override
    public Ad getAd() {
        return ad;
    }

    @Override
    public boolean isBanner() {
        return banner;
    }

    public boolean isTest() {
        return test;
    }

    @Override
    public boolean isVideo() {
        return video;
    }

    @Override
    public boolean isLinear() {
        return linear;
    }

    @Override
    public boolean isVpaid() {
        return vpaid;
    }

    @Override
    public Bid getBid(OpenRtbRequest req, Impression imp) {
        final Bid bid = new Bid();
        bid.setId(ServiceRunner.getInstance().getNextId());
        bid.setImpid(imp.getId());
        List<String> adomain = bid.getAdomain();
        if (adomain == null) {
            adomain = new ArrayList<String>();
            bid.setAdomain(adomain);
        }
        bid.getAdomain().addAll(ad.getAdomain());
        String adId = getAdId();
        bid.setAdid(adId + "_" + id);
        bid.setCid(adId);
        if (getAd().getIurl() != null) {
            bid.setIurl(getAd().getIurl());
        }
        bid.setCrid(id);
        // Do this at the last moment.
        final Lot49Ext lot49Ext = req.getLot49Ext();
        final ExchangeAdapter adapter = lot49Ext.getAdapter();
        final String exchange = adapter.getName();
        ServiceRunner runner = Bidder.getInstance();
        if (adapter.isNurlRequired()) {
            // If NURL is required...
            final String nurlId = ServiceRunner.getInstance().getNextId() + "_"
                            + BidderCalendar.getInstance().currentTimeMillis();
            String tag = getTag(req, imp, bid, true);

            req.getLot49Ext().getBidIdToTagText().put(bid.getId(), tag);
            bid.getHiddenAttributes().put(KVKeysValues.NURL_PREFIX, nurlId);

            if (imp != null && imp.getVideo() != null && exchange.equals("bidswitch")) {
                // Special case for Bidswitch Video

                String nurl = getNUrlByType(req, bid, nurlId,
                                Lot49Constants.NURL_ONLY_WIN_NOTIFICATION);
                bid.setNurl(nurl);
                BidswitchVideoExt ext = new BidswitchVideoExt();
                ext.setDuration(duration);
                ext.setAdvertiser_name(getAd().getAdvertiser());
                ext.setVast_url(getNUrlByType(req, bid, nurlId, Lot49Constants.NURL_ONLY_TAG));
                bid.setExt(ext);
            } else {
                String nurl = getNUrl(req, bid, nurlId);
                bid.setNurl(nurl);
            }
        } else {
            final String tag = getTag(req, imp, bid, false);
            bid.setAdm(tag);
        }

        final Bid bid2 = adapter.massageBid(req, imp, this, bid);
        return bid2;
    }

    protected MarkupType markupType;

    public MarkupType getMarkupType() {
        return markupType;
    }

    public void setBidder(ServiceRunner bidder) {
        this.bidder = bidder;
    }

    @Override
    public String toString() {
        return "Tag id: " + getId() + "; name: " + getName() + "; ad ID: " + getAdId();
    }

}
