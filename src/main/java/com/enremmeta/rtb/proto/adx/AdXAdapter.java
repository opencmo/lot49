package com.enremmeta.rtb.proto.adx;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.Dimension;
import com.enremmeta.rtb.api.FixedDimension;
import com.enremmeta.rtb.api.MarkupType;
import com.enremmeta.rtb.api.RangeDimension;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
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
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.RtbConstants;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.Utils;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.protobuf.ByteString;
import com.google.protos.adx.NetworkBid;

/**
 * Google (AdX) connector.
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class AdXAdapter implements ExchangeAdapter<NetworkBid.BidRequest, NetworkBid.BidResponse> {
    private final AdXConfig config;

    private final long timeout;

    private static Map<Integer, AdxGeo> geo = null;

    /**
     * The only one purpose of this constructors is to run test AdxTest
     * 
     * @param test
     * @throws Lot49Exception
     */
    public AdXAdapter(boolean test) throws Lot49Exception {
        super();

        config = null;
        timeout = 0;
        keys = null;

    }

    public AdXAdapter() throws Lot49Exception {
        super();

        config = Bidder.getInstance().getConfig().getExchanges().getAdx();
        if (config == null) {
            throw new Lot49Exception("Expected adx config section.");
        }
        if (config.getEncryptionKey() == null || config.getEncryptionKey().length == 0
                        || config.getIntegrityKey() == null
                        || config.getIntegrityKey().length == 0) {
            throw new Lot49Exception(
                            "Either encryption or integrity key missing from OpenX configuration.");
        }
        SecretKey encKey = new SecretKeySpec(config.getEncryptionKey(), "HmacSHA1");
        SecretKey intKey = new SecretKeySpec(config.getIntegrityKey(), "HmacSHA1");
        try {
            keys = new DoubleClickCrypto.Keys(encKey, intKey);
        } catch (InvalidKeyException e) {
            throw new Lot49Exception(e);
        }
        if (geo == null) {
            geo = new HashMap<Integer, AdxGeo>();
            String geoFile = config.getGeoTable();
            if (geoFile == null) {
                error("Geo table not specified, will not lookup geo.");
            } else {

                final long startTime = BidderCalendar.getInstance().currentTimeMillis();
                Bidder.getInstance().getExecutor().execute(new Runnable() {
                    public void run() {
                        info("Initializing geo from  " + geoFile);
                        try {
                            CSVParser p = CSVParser.parse(new File(geoFile),
                                            Charset.forName("UTF8"), CSVFormat.EXCEL);

                            List<CSVRecord> records = p.getRecords();

                            for (CSVRecord r : records) {
                                int id = Integer.parseInt(r.get(0));
                                String name = r.get(1);
                                String canonicalName = r.get(2);
                                // TODO
                                String dummy1 = r.get(3);
                                String dummy2 = r.get(4);
                                String countryCode = r.get(5);
                                AdxGeo entry = new AdxGeo();
                                entry.setCriteriaId(id);
                                entry.setCountryCode(countryCode.toLowerCase());
                                entry.setCanonicalName(canonicalName);
                                entry.setName(name);
                                geo.put(id, entry);
                            }

                            long elapsed = BidderCalendar.getInstance().currentTimeMillis()
                                            - startTime;


                            info("Read " + records.size() + " geo entries in "
                                            + Math.round(elapsed / 1000) + " seconds.");
                        } catch (IOException e) {
                            error("Error initializing AdX Geo: ", e);
                        }
                    }
                });
            }
        }

        timeout = config.getTimeout() > 0 ? config.getTimeout() : AdXConfig.DEFAULT_TIMEOUT;

        try {
            ParsedPriceInfo i1 = parse("VeLGngAMLzwKQnkIAAlJ4yaZbw-zHzK0z2ErZA", 1);
            i1 = parse("VeNUfwAMhogKQpfsAAXpunUCOKtFRcQlAqUZpA", 1);
            i1 = parse("VeNZLgAGdSgKQrqLAAlCxm9UmaYNavSxIIJoAQ", 1);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private final DoubleClickCrypto.Keys keys;

    @Override
    public long getDefaultTimeout() {
        return timeout;
    }

    @Override
    public String getWinningPriceMacro() {
        return "%%WINNING_PRICE%%";
    }

    @Override
    public boolean localUserMapping() {
        return false;
    }

    public static final String getStringFromByteSting(ByteString bs) {
        return Base64.encodeBase64String(bs.toByteArray());
    }

    @Override
    public String getPartnerInitiatedSyncUrl(String myUserId) {
        return "https://cm.g.doubleclick.net/pixel?google_nid=" + config.getNid()
                        + "&google_cm&google_hm=" + myUserId + "&lot49=lot49";
    }

    @Override
    public OpenRtbRequest convertRequest(final NetworkBid.BidRequest req) {

        final OpenRtbRequest req2 = new OpenRtbRequest();

        final Lot49Ext lot49Ext = req2.getLot49Ext();
        lot49Ext.setRawRequestAlreadyLogged(true);
        if (req.getBidResponseFeedbackCount() > 0) {
            Bidder.getInstance().getExecutor().submit(new RealTimeFeedbackHandler(this, req));
        }

        lot49Ext.setAdapter(this);

        // ID
        final ByteString bs = req.getId();
        final String reqId = getStringFromByteSting(bs);
        req2.setId(reqId);

        // IP
        final Device dev2 = new Device();
        req2.setDevice(dev2);
        String ipStr = "";

        for (final byte b : req.getIp().toByteArray()) {
            if (ipStr.length() > 0) {
                ipStr += ".";
            }
            // Unsigned, Carl!
            final int i = Byte.toUnsignedInt(b);
            ipStr += i;
        }
        // Google cuts off the last octet, let's add "1" after that
        if (!ipStr.isEmpty()) {
            ipStr += ".1";
        }
        dev2.setIp(ipStr);

        // Geo
        if (geo != null) {
            AdxGeo geoEntry = geo.get(req.getGeoCriteriaId());
            if (geoEntry != null) {
                Geo reqGeo = new Geo();
                dev2.setGeo(reqGeo);
                reqGeo.setCountry(geoEntry.getCountryCode());
            }
        }

        // UA
        String ua = req.getUserAgent();
        dev2.setUa(ua);

        fillPlatformInfoFromUa(req2);

        NetworkBid.BidRequest.Device mob = req.getDevice();
        if (mob != null) {
            dev2.setMake(mob.getPlatform());
            NetworkBid.BidRequest.Device.OsVersion dov = mob.getOsVersion();
            if (dov != null) {
                dev2.setOsv(String.valueOf(dov.getMajor()));
            }
        }

        // URL
        final Site site = new Site();
        req2.setSite(site);
        String url = req.getUrl();

        final String origUrl = url;
        site.setPage(url);
        if (url != null && url.trim().length() > 0) {
            try {
                if (url.startsWith("//")) {
                    url = "http:" + url;
                } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }

                String domain = new URL(url).getHost();
                if (domain.startsWith("www.")) {
                    domain = domain.substring(4);
                }
                site.setDomain(domain);

            } catch (MalformedURLException e) {
                if (!Utils.validateDomain(url)) {
                    trace("Error: strange domain in request: " + origUrl
                                    + ", tried to deal with as " + url);
                }

                site.setDomain(url);
                // Ignore for now...
            }
        }
        // Language
        if (req.getDetectedLanguageCount() > 0) {
            dev2.setLanguage(req.getDetectedLanguage(0));
        }
        // Vertical
        final List<String> pageCats = new ArrayList<String>();
        site.setPagecat(pageCats);
        for (final NetworkBid.BidRequest.Vertical v : req.getDetectedVerticalList()) {
            pageCats.add("GOOGLE_VERTICAL_" + v.getId());
        }

        // Impressions
        final List<Impression> imps = new ArrayList<Impression>(req.getAdslotCount());

        req2.setImp(imps);
        com.enremmeta.rtb.api.proto.openrtb.Video video = null;
        for (final NetworkBid.BidRequest.AdSlot adSlot : req.getAdslotList()) {
            // TODO add matching ad data
            // TODO check log level

            final Impression imp = new Impression();

            imps.add(imp);

            // ID
            imp.setId("" + adSlot.getId());

            final NetworkBid.BidRequest.Video video0 = req.getVideo();

            if (video0 == null || video0.getMaxAdDuration() == 0) {
                // Banner
                final Banner banner = new Banner();
                imp.setBanner(banner);
                if (adSlot.getWidthCount() > 0) {
                    // TODO predicate this on log level!
                    lot49Ext.getComments().add("Widths: " + adSlot.getWidthList());
                    banner.setW(adSlot.getWidth(0));
                }
                if (adSlot.getHeightCount() > 0) {
                    // TODO predicate this on log level!
                    lot49Ext.getComments().add("Heights: " + adSlot.getHeightList());
                    banner.setH(adSlot.getHeight(0));
                }

            } else {
                // Video
                video = new com.enremmeta.rtb.api.proto.openrtb.Video();
                video.setLinearity(RtbConstants.LINEARITY_LINEAR);
                // video0.getis
                if (adSlot.getWidthCount() > 0) {
                    // TODO predicate this on log level!
                    lot49Ext.getComments().add("Widths: " + adSlot.getWidthList());
                    video.setW(adSlot.getWidth(0));
                }
                if (adSlot.getHeightCount() > 0) {
                    // TODO predicate this on log level!
                    lot49Ext.getComments().add("Heights: " + adSlot.getHeightList());
                    video.setH(adSlot.getHeight(0));
                }
                video.setMinduration(video0.getMinAdDuration() / 1000);
                video.setMaxduration(video0.getMaxAdDuration() / 1000);
                final List<NetworkBid.BidRequest.Video.VideoFormat> formats =
                                video0.getAllowedVideoFormatsList();
                final List<String> mimes = new ArrayList<String>();
                video.setMimes(mimes);

                for (NetworkBid.BidRequest.Video.VideoFormat format : formats) {
                    switch (format) {
                        case VIDEO_FLV:
                            mimes.add(Lot49Constants.MEDIA_TYPE_VIDEO_FLV);
                            break;
                        case VIDEO_MP4:
                            mimes.add(Lot49Constants.MEDIA_TYPE_VIDEO_MP4);
                            break;
                    }
                }

                // We are adding everything by default and then excluding stuff.
                mimes.add(Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH);
                mimes.add(Lot49Constants.MEDIA_TYPE_APPLICATION_JAVASCRIPT);

                List<Integer> apis = new ArrayList<Integer>();
                video.setApi(apis);
                apis.add(RtbConstants.API_VPAID_1);
                apis.add(RtbConstants.API_VPAID_2);

                List<Integer> protos = new ArrayList<Integer>();
                video.setProtocols(protos);
                protos.add(RtbConstants.VIDEO_PROTOCOL_VAST_1);
                protos.add(RtbConstants.VIDEO_PROTOCOL_VAST_2);
                protos.add(RtbConstants.VIDEO_PROTOCOL_VAST_3);
                protos.add(RtbConstants.VIDEO_PROTOCOL_VAST_WRAPPER_1);
                protos.add(RtbConstants.VIDEO_PROTOCOL_VAST_WRAPPER_2);
                protos.add(RtbConstants.VIDEO_PROTOCOL_VAST_WRAPPER_3);
                imp.setVideo(video);
            }
            if (adSlot.getExcludedAttributeCount() > 0) {

                HashSet<MarkupType> excludedMarkups = new HashSet<MarkupType>();
                lot49Ext.setExcludedMarkups(excludedMarkups);
                for (int exclAttr : adSlot.getExcludedAttributeList()) {
                    switch (exclAttr) {
                        case AdXConstants.EXCLUDED_COOKIETARGETING_ISCOOKIETARGETED:
                            // What to do
                            break;
                        case AdXConstants.EXCLUDED_CREATIVETYPE_HTML:
                            excludedMarkups.add(MarkupType.OWN_HTML);
                            excludedMarkups.add(MarkupType.THIRD_PARTY_HTML);
                            excludedMarkups.add(MarkupType.THIRD_PARTY_HTML5);
                            break;
                        case AdXConstants.EXCLUDED_CREATIVETYPE_VASTVIDEO:
                            excludedMarkups.add(MarkupType.VAST_PLAIN_FLASH_ONLY);
                            excludedMarkups.add(MarkupType.VAST_PLAIN_MULTIPLE);
                            excludedMarkups.add(MarkupType.VAST_WRAPPER_PLAIN_FLASH_ONLY);
                            excludedMarkups.add(MarkupType.VAST_WRAPPER_PLAIN_MULTIPLE);
                            if (video != null) {
                                video.getMimes().remove(Lot49Constants.MEDIA_TYPE_VIDEO_FLV);
                                video.getMimes().remove(Lot49Constants.MEDIA_TYPE_VIDEO_MP4);
                            }
                            break;
                        case AdXConstants.EXCLUDED_INSTREAMVASTVIDEOTYPE_VPAID_FLASH:
                            if (video != null) {
                                video.getMimes().remove(
                                                Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH);

                            }
                            // excludedMarkups.add(MarkupType.VAST_VPAID);
                            // excludedMarkups.add(MarkupType.VAST_WRAPPER_VPAID);

                            break;
                        case AdXConstants.EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYNONSSL:
                            // Special case here
                            lot49Ext.setSsl(true);
                            break;
                        case AdXConstants.EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYFLASH:
                            excludedMarkups.add(MarkupType.OWN_FLASH);
                            excludedMarkups.add(MarkupType.THIRD_PARTY_FLASH);
                            break;
                        case AdXConstants.EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYHTML5:
                            excludedMarkups.add(MarkupType.OWN_HTML5);
                            excludedMarkups.add(MarkupType.THIRD_PARTY_HTML5);
                            break;
                    }
                }
            }

            // Matching ad data
            final Map map = new HashMap();
            imp.setExt(map);
            final AdXTargeting adxTargeting = new AdXTargeting();

            map.put("matching_ad_data", adxTargeting);
            final List<NetworkBid.BidRequest.AdSlot.MatchingAdData> madList =
                            adSlot.getMatchingAdDataList();
            final PMP pmp = new PMP();
            imp.setPmp(pmp);
            final List<Deal> deals = new ArrayList<Deal>();
            pmp.setDeals(deals);
            if (madList != null) {
                for (NetworkBid.BidRequest.AdSlot.MatchingAdData mad : madList) {
                    adxTargeting.getAdGroupIds().addAll(mad.getBillingIdList());
                    final long floor = mad.getMinimumCpmMicros() / 1000;
                    float flfl = (float) Utils.microToCpm(floor);
                    if (imp.getBidfloor() == null || imp.getBidfloor() == 0) {
                        imp.setBidfloor(flfl);
                    } else if (imp.getBidfloor() > flfl) {
                        imp.setBidfloor(flfl);
                    }

                    List<NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal> ddList =
                                    mad.getDirectDealList();
                    if (ddList != null && ddList.size() > 0) {
                        for (NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal dd : ddList) {
                            long dealId = dd.getDirectDealId();
                            Deal newDeal = new Deal();
                            deals.add(newDeal);

                            newDeal.setBidfloorcur("USD");
                            long fixedCpmMicros = dd.getFixedCpmMicros();
                            double fixedCpm = Utils.microToCpm(fixedCpmMicros / 1000);
                            newDeal.setBidfloor((float) fixedCpm);

                            NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal.DealType dealType =
                                            dd.getDealType();
                            newDeal.setId(String.valueOf(dealId));
                            switch (dealType.getNumber()) {
                                case NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal.DealType.UNKNOWN_DEAL_TYPE_VALUE:
                                    pmp.setPrivate_auction(0);
                                    newDeal.setAt(RtbConstants.AUCTION_TYPE_SECOND_PRICE_PLUS);
                                    break;
                                case NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal.DealType.PREFERRED_DEAL_VALUE:
                                    pmp.setPrivate_auction(1);
                                    newDeal.setAt(RtbConstants.AUCTION_TYPE_FIXED_PRICE);
                                    break;
                                case NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal.DealType.PRIVATE_AUCTION_VALUE:
                                    // https://support.google.com/adxbuyer/answer/3081011?hl=en
                                    pmp.setPrivate_auction(0);
                                    newDeal.setAt(RtbConstants.AUCTION_TYPE_SECOND_PRICE_PLUS);
                                    break;
                            }
                        }
                    }
                }
            }

        }

        // isTest
        lot49Ext.setTest(req.getIsTest());

        // google_user_id
        final User user2 = new User();
        req2.setUser(user2);
        user2.setId(req.getGoogleUserId());

        // hosted_match_data
        final ByteString bsUser = req.getHostedMatchData();

        String userId = bsUser.size() == 0 ? null : getStringFromByteSting(bsUser);
        parseUserId(userId, req2);
        return req2;
    }

    /**
     * Default behavior is not OK here per
     * <a href="https://developers.google.com/ad-exchange/rtb/response-guide"> Build the
     * Response</a> document.
     */
    @Override
    public ResponseBuilder getOptoutBuilder(OpenRtbRequest req) {
        NetworkBid.BidResponse.Builder respBuilder = NetworkBid.BidResponse.newBuilder();
        respBuilder.setProcessingTimeMs((int) req.getLot49Ext().getProcessingTime());
        NetworkBid.BidResponse result = respBuilder.build();
        return Response.ok(result);
    }

    public static final String ADX_SPECIFIC_INSTRUCTION_VENDOR_TYPE = "vendor_type";

    @Override
    public Map<String, Object> makeExchangeSpecificInstructionsMap() {
        return new HashMap() {
            {
                put(ADX_SPECIFIC_INSTRUCTION_VENDOR_TYPE, new HashSet<Integer>());
            }
        };
    }

    @Override
    public NetworkBid.BidResponse convertResponse(OpenRtbRequest req, OpenRtbResponse resp)
                    throws Throwable {
        NetworkBid.BidResponse.Builder respBuilder = NetworkBid.BidResponse.newBuilder();
        NetworkBid.BidResponse.Ad.Builder adBuilder = respBuilder.addAdBuilder();
        final Lot49Ext ext = req.getLot49Ext();
        // adBuilder.setHeight(value);
        // adBuilder.setWidth(value)


        NetworkBid.BidResponse.Ad.AdSlot.Builder adSlotBuilder = adBuilder.addAdslotBuilder();
        respBuilder.setProcessingTimeMs((int) req.getLot49Ext().getProcessingTime());
        Impression imp = req.getImp().get(0);
        Banner b = imp.getBanner();
        com.enremmeta.rtb.api.proto.openrtb.Video v = req.getImp().get(0).getVideo();

        for (final SeatBid sb : resp.getSeatbid()) {
            for (final Bid bid : sb.getBid()) {
                final List<String> adomains = bid.getAdomain();
                if (adomains == null || adomains.size() == 0) {
                    error("Empty adomains in bid for ad " + bid.getCid());
                    continue;
                }

                final Tag tag = ext.getBidIdToTagObject().get(bid.getId());
                final String markup = bid.getAdm();
                if (markup.indexOf("http://") > -1 && req.getLot49Ext().isSsl()
                                && tag.isSslCapable()) {

                    error("SSL ERROR: markup for " + tag.getId() + " has http:// but SSL required! "
                                    + markup);


                }
                if (b != null) {
                    adBuilder = adBuilder.setHtmlSnippet(markup);
                } else if (v != null) {
                    adBuilder.setVideoUrl(markup);
                }
                final com.enremmeta.rtb.api.Ad ad =
                                ext.getBidRequestIdToAdObject().get(req.getId());
                Object vendorTypesObj = ad.getExchangeSpecificInstructions()
                                .get(ADX_SPECIFIC_INSTRUCTION_VENDOR_TYPE);

                if (vendorTypesObj != null) {
                    Collection<Integer> vendorTypes = (Collection<Integer>) vendorTypesObj;
                    if (vendorTypes != null && vendorTypes.size() > 0) {
                        int vendorTypeIdx = 0;
                        for (int vendorType : vendorTypes) {
                            adBuilder = adBuilder.addVendorType(vendorType);
                        }
                    }
                }
                String proto;
                String creativeId = bid.getCid() + "_" + ad.getAdVersion() + "_" + bid.getCrid()
                                + "_" + tag.getTagVersion();
                if (ext.isSsl() && tag.isSslCapable()) {
                    proto = "https";
                    creativeId += "_ssl";
                    adBuilder = adBuilder.addAttribute(
                                    AdXConstants.DECLARABLE_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYSSL);

                } else {
                    proto = "http";
                    creativeId += "_nossl";
                }
                creativeId += "_" + Lot49Constants.LOT49_VERSION_VALUE;
                adBuilder.setBuyerCreativeId(creativeId);

                adBuilder.addClickThroughUrl("http://" + adomains.get(0));

                final Dimension dim = tag.getDimension();
                if (dim instanceof FixedDimension) {
                    FixedDimension fd = (FixedDimension) dim;
                    adBuilder = adBuilder.setWidth(fd.getWidth()).setHeight(fd.getHeight());
                } else if (dim instanceof RangeDimension) {
                    if (b != null) {
                        adBuilder = adBuilder.setWidth(b.getW()).setHeight(b.getH());
                    } else if (v != null) {
                        adBuilder = adBuilder.setWidth(v.getW()).setHeight(v.getH());
                    }
                }
                Map impExt = imp.getExt();
                if (impExt != null) {
                    /*
                     * AdXTargeting targeting = (AdXTargeting) impExt.get( "matching_ad_data"); if
                     * (targeting != null) {
                     * 
                     * List<Long> adGroupIds = targeting.getAdGroupIds(); if (adGroupIds != null &&
                     * adGroupIds.size() > 0) { Long videoId =
                     * config.getAdGroupIdMap().get("video"); Long displayId =
                     * config.getAdGroupIdMap().get("display"); if (tag.isVideo() &&
                     * adGroupIds.contains(videoId)) { adSlotBuilder =
                     * adSlotBuilder.setBillingId(videoId); } else if
                     * (adGroupIds.contains(displayId)) { adSlotBuilder =
                     * adSlotBuilder.setBillingId(displayId); } } }
                     */

                    AdXTargeting targeting = (AdXTargeting) impExt.get("matching_ad_data");
                    if (targeting != null) {
                        List<Long> adGroupIds = targeting.getAdGroupIds();
                        if (adGroupIds != null && adGroupIds.size() > 0) {
                            adSlotBuilder = adSlotBuilder.setBillingId(adGroupIds.get(0));
                        }
                    }



                }
                // Else won't happen, this is done in validation.
                final int maxCpmMicros = Math.round(bid.getPrice() * 1000000);

                adSlotBuilder = adSlotBuilder.setId(Integer.parseInt(bid.getImpid()))
                                .setMaxCpmMicros(maxCpmMicros);

                final String dealId = bid.getDealid();
                if (dealId != null) {
                    try {
                        adSlotBuilder = adSlotBuilder.setDealId(Long.parseLong(dealId));

                    } catch (NumberFormatException nfe) {
                        LogUtils.error("Expected numeric deal ID, got " + dealId, nfe);
                    }
                }

                trace("CPM Micros: " + maxCpmMicros);
                final MarkupType mt = tag.getMarkupType();
                switch (mt) {
                    case THIRD_PARTY_HTML5:
                    case OWN_HTML5:
                        adBuilder = adBuilder.addAttribute(
                                        AdXConstants.DECLARABLE_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYHTML5);
                    case OWN_HTML:
                    case THIRD_PARTY_HTML:
                        adBuilder = adBuilder.addAttribute(
                                        AdXConstants.DECLARABLE_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYNONFLASH);


                        break;

                    case VAST_PLAIN_FLASH_ONLY:
                    case VAST_PLAIN_MULTIPLE:
                    case VAST_WRAPPER_PLAIN_FLASH_ONLY:
                    case VAST_WRAPPER_PLAIN_MULTIPLE:
                        break;
                    case OWN_FLASH:
                    case THIRD_PARTY_FLASH:

                        adBuilder = adBuilder.addAttribute(
                                        AdXConstants.DECLARABLE_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYFLASH);

                        break;
                    case VAST_VPAID:
                    case VAST_WRAPPER_VPAID:
                        String mimeType = tag.getMime();
                        if (mimeType != null && mimeType.equalsIgnoreCase(
                                        Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH)) {
                            adBuilder = adBuilder.addAttribute(
                                            AdXConstants.DECLARABLE_INSTREAMVASTVIDEOTYPE_VPAID_FLASH);
                        }
                        break;
                }

            }
        }

        NetworkBid.BidResponse result = respBuilder.build();

        return result;
    }

    /**
     * @see <a href="https://developers.google.com/ad-exchange/rtb/response-guide/decrypt-price">
     *      https://developers.google.com/ad-exchange/rtb/response-guide/decrypt-price</a>
     * 
     * @see DoubleClickCrypto
     */
    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {
        final DoubleClickCrypto.Price dccp = new DoubleClickCrypto.Price(keys);
        final long wpMicros = dccp.decodePriceMicros(winningPriceString);
        final ParsedPriceInfo priceInfo =
                        new ParsedPriceInfo(Utils.microToCpm(wpMicros), wpMicros, bidMicros);
        return priceInfo;
    }

    @Override
    public String getName() {
        return Lot49Constants.EXCHANGE_ADX;
    }

    @Override
    public String getClickMacro() {
        return "%%CLICK_URL_UNESC%%";
    }

    @Override
    public String getClickEncMacro() {
        return "%%CLICK_URL_ESC%%";
    }

    public static final String RESPONSE_MEDIA_TYPE = MediaType.APPLICATION_OCTET_STREAM;

    @Override
    public String getResponseMediaType() {
        return RESPONSE_MEDIA_TYPE;
    }

    @Override
    public List<String> validateAd(com.enremmeta.rtb.api.Ad ad) {
        final List<String> retval = new ArrayList<String>();
        final String prefix = "Ad " + ad.getId() + " not suited for " + getName() + ": ";
        final List<String> adomain = ad.getAdomain();
        if (adomain == null || adomain.size() == 0) {
            retval.add(prefix + "adomain must be present.");
        }
        for (Tag t : ad.getTags()) {
            Dimension dim = t.getDimension();
            if (t.isBanner() && !(dim instanceof FixedDimension)) {
                retval.add(prefix + "banner tag " + t.getId() + " has dimension of type "
                                + dim.getClass()
                                + ", only FixedDimension allowed for AdX banners.");
            }
        }
        return retval;
    }

    // TODO add to test
    @Override
    public String getSampleWinningPrice() {
        // 100 VZulpgAJgdoKaZHQAAKycGhZkDddRtAsmawfqg
        // 1100 VZulpgAJggAKaZHQAAKycCaZLJMeBeU3h-Rzfw
        // 2100 VZulpgAJggMKaZHQAAKycIrWQmC-f0RZfBgHdQ
        // 3100 VZulpgAJggUKaZHQAAKycEtZMLfoG1eQGuvshg
        // 4100 VZulpgAJggcKaZHQAAKycIcwx5xECynmR1CxZw
        // 5100 VZulpgAJggoKaZHQAAKycMlzM7cjhPPQ89cOEg
        // 6100 VZulpgAJggwKaZHQAAKycIx88BJo_AoaPPBN0g
        // 7100 VZulpgAJgg4KaZHQAAKycBzZ35KO42mz_XKvTg
        // 8100 VZulpgAJghEKaZHQAAKycMyxW9Z6xm7-b7TeVA
        // 9100 VZulpgAJghMKaZHQAAKycMi_j-ClQ782PRnYZA
        // 10100 VZulpgAJghYKaZHQAAKycLa6DTFooEBmpg0_6Q
        return "VZulpgAJgdoKaZHQAAKycGhZkDddRtAsmawfqg";
    }
}
