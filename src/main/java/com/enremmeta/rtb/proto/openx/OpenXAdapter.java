package com.enremmeta.rtb.proto.openx;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.openx.market.ssrtb.crypter.SsRtbCrypter;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.ExchangeTargeting;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49CustomData;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.spotxchange.ExchangeAdapterImpl;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class OpenXAdapter extends ExchangeAdapterImpl<OpenRtbRequest, OpenRtbResponse> {

    //
    // protected void onBidRequest(final AsyncResponse response, OpenRtbRequest
    // req) {
    // onBidRequestDelegate(this, response, req);
    // }

    private static final SsRtbCrypter OPENX_CRYPTER = new SsRtbCrypter();

    @Override
    public long getWinTimeout() {
        if (this.config.getWinTimeout() > 0) {
            return config.getWinTimeout();
        }
        return super.getWinTimeout();
    }

    /**
     * We put RANDOM here but only because we don't want the default "ALL" - so that we properly key
     * the BidInFlightInfo object. OpenX in any case allows only one.
     */
    @Override
    public BidChoiceAlgorithm getBidChoiceAlgorithm() {
        return BidChoiceAlgorithm.LRU;
    }

    private final OpenXConfig config;

    public OpenXAdapter() throws Lot49Exception {
        super();
        config = Bidder.getInstance().getConfig().getExchanges().getOpenx();
        if (config == null) {
            throw new Lot49Exception("Expected openx config section.");
        }
        if (config.getEncryptionKey() == null || config.getEncryptionKey().length() == 0
                        || config.getIntegrityKey() == null
                        || config.getIntegrityKey().length() == 0) {
            throw new Lot49Exception(
                            "Either encryption or integrity key missing from OpenX configuration.");
        }
    }

    @Override
    public final long getDefaultTimeout() {
        return 125;
    }

    @Override
    public final String getSampleWinningPrice() {
        return "AAABTTTmgPv2qfEU-fGXIdM7_o-YLPbBl-KU8A";
    }

    @Override
    public final String getWinningPriceMacro() {
        return "{winning_price}";
    }

    @Override
    public final boolean localUserMapping() {
        return false;
    }

    @Override
    public final OpenRtbRequest convertRequest(final OpenRtbRequest req) throws Throwable {

        final Lot49Ext lot49Ext = req.getLot49Ext();

        final User user = req.getUser();
        final String openXId = user.getBuyeruid();
        // OpenX user id is sent in buyer UID field
        if (user.getId() != null && user.getId().length() > 0) {
            lot49Ext.getComments().add("OpenX:User.ID=" + user.getId());
        }
        user.setId(openXId);
        user.setBuyeruid(null);

        String customData = user.getCustomdata();
        if (customData != null && customData.length() > 0) {
            Lot49CustomData lot49CustomData = null;
            lot49Ext.getComments().add("OpenX:User.CustomData=" + customData);
            try {

                if (customData.indexOf("\"udat=\"\"") > -1) {
                    customData = customData.replace("\"udat=\"\"", "\"udat\":{}");
                } else if (customData.indexOf("\"udat\"=<>") > -1) {
                    customData = customData.replace("udat\"=<>", "udat\":{}");
                    customData = customData.replace("<", "\"").replace(">", "\"");

                } else if (customData.indexOf("udat\"=<{\"oi\":") > -1) {

                    customData = customData.replace("udat\"=<{\"oi\":", "udat\":{\"oi\":");
                    customData = customData.replace("<", "\"").replace(">", "\"").replace("}\"",
                                    "}");
                } else if (customData.indexOf("udat=\"{\"oi\":\"\"}") > -1) {

                    customData = customData.replace("udat=\"{\"oi\":\"\"}\"", "udat\":{}");

                } else if (customData.indexOf("\"udat\":\"{\"oi\":") > -1) {
                    customData = customData.replace("\"{", "{").replace("}\"", "}");

                } else if (customData.indexOf("\"udat\"=<{\"oi\":\"\"}Domain=.opendsp.com>") > -1) {
                    customData = customData.replace("\"udat\"=<{\"oi\":\"\"}Domain=.opendsp.com>",
                                    "udat:{}");
                    customData = customData.replace("<", "\"").replace(">", "\"");
                } else if (customData.indexOf("\"udat\":{\"oi\":\"\"}Domain=.opendsp.com\"") > -1) {
                    customData = customData.replace("\"udat\":{\"oi\":\"\"}Domain=.opendsp.com\"",
                                    "udat:{}");
                    customData = customData.replace("<", "\"").replace(">", "\"");
                } else if (customData.indexOf("\"udat\":,") > -1) {

                    customData = customData.replace("\"udat\":,", "\"udat\": {},");

                }
                lot49CustomData = Utils.MAPPER.readValue(customData, Lot49CustomData.class);

            } catch (JsonMappingException | JsonParseException e0) {

                lot49CustomData = new Lot49CustomData();
                // Sometimes this is a query string...
                final Map<String, String> customMap = Utils.parseQueryString(customData);
                final String testBid = customMap.get("testbid");

                if (testBid != null && testBid.equals("1")) {
                    req.getLot49Ext().setTest(true);
                }
                // Hack for badly formatted JSON that we started out with.
                // Remove this after a few months - say, remove this
                // if it's after 05/01/2015).
                final int udatIdx = customData.indexOf("{", customData.indexOf("udat") + 4);
                if (udatIdx > 0) {
                    final String udat = customData.substring(udatIdx);
                    final String udat2 = udat.substring(0, udat.indexOf("}") + 1);
                    try {
                        Map udatMap = Utils.MAPPER.readValue(udat2, Map.class);
                        lot49CustomData.setUdat(udatMap);
                    } catch (Exception e) {
                        LogUtils.debug(e);
                    }
                }
            }
            if (lot49CustomData != null) {
                final String uidEnc = lot49CustomData.getUid();
                if (uidEnc != null) {
                    String uid = null;
                    try {
                        uid = Utils.cookieToLogModUid(uidEnc);
                    } catch (Exception e) {
                        uid = e.getClass().getName() + "(" + uidEnc + ")";
                    }
                    user.setBuyeruid(uid);
                }
                req.getLot49Ext().setLot49CustomData(lot49CustomData);
            }
        }
        final Map ext = req.getExt();
        if (ext != null) {
            Object isTest = ext.get("is_test");
            if (isTest != null) {
                req.getLot49Ext().setTest(1 == (int) isTest);
            }
        }

        final Device device = req.getDevice();
        if (device != null) {
            final Geo deviceGeo = device.getGeo();
            if (deviceGeo == null) {
                device.setGeo(new Geo());
            } else {
                final Map devExt = deviceGeo.getExt();
                if (devExt != null) {
                    final String state = (String) devExt.get("state");
                    if (state != null) {
                        deviceGeo.setRegion(state);
                    }
                }
            }
        } else {
            req.setDevice(new Device());
        }

        final List<Impression> imps1 = req.getImp();
        final List<Impression> imps2 = new ArrayList<Impression>();
        req.setImp(imps2);
        if (imps1.size() > 1) {
            LogUtils.error("Expecting only one impression in OpenX bid per http://docs.openx.com/ad_exchange_adv/#openrtb_impression.html");
        }

        final Impression imp1 = imps1.get(0);
        final Banner banner = imp1.getBanner();
        final Video video = imp1.getVideo();
        final Map ext1 = banner != null ? banner.getExt() : (video != null ? video.getExt() : null);
        final Impression imp2 = imp1.clone();
        final Map ext2 = banner != null ? imp2.getBanner().getExt()
                        : (video != null ? imp2.getVideo().getExt() : null);
        if (ext1 != null) {
            final List<Map> matchingAdIdsReceived = new ArrayList<Map>();
            matchingAdIdsReceived.addAll((List<Map>) ext1.get("matching_ad_id"));
            final List<MatchingAdId> matchingAdIds = new ArrayList<MatchingAdId>();
            imps2.add(imp2);
            for (final Map mAdIdObj : matchingAdIdsReceived) {
                // TODO check if they differ... they shouldn't...
                MatchingAdId mAdId = new MatchingAdId(mAdIdObj);
                final int height = mAdId.getAdHeight();
                if (height != 0) {
                    imp2.getBanner().setH(height);
                }
                final int width = mAdId.getAdWidth();
                if (width != 0) {
                    imp2.getBanner().setW(width);
                }
                matchingAdIds.add(mAdId);
            }
            if (ext2 != null)
                ext2.put("matching_ad_id", matchingAdIds);
        }

        req.getLot49Ext().setAdapter(this);
        return req;
    }

    public String getName() {
        return Lot49Constants.EXCHANGE_OPENX;
    }

    @Override
    public OpenRtbResponse convertResponse(final OpenRtbRequest req, final OpenRtbResponse o)
                    throws Exception {
        for (final SeatBid seatBid : o.getSeatbid()) {
            for (final Bid bid : seatBid.getBid()) {

                final List<String> adomains = bid.getAdomain();
                final int size = adomains.size();

                switch (Utils.RANDOM.nextInt() % 2) {
                    // Add URLs
                    case 0:
                        for (int i = 0; i < size; i++) {
                            String adomain = adomains.get(i);
                            adomains.add("http://" + adomain);
                        }
                        break;
                    case 1:
                        // Replace TLDs with URLs
                        for (int i = 0; i < size; i++) {
                            String adomain = adomains.get(i);
                            adomains.set(i, "http://" + adomain);
                        }
                        break;
                    default:
                        // Do nothing
                        break;
                }

            }
        }
        return o;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) throws Throwable {

        final SecretKeySpec encryption = getKeySpec(config.getEncryptionKey());
        final SecretKeySpec integrity = getKeySpec(config.getIntegrityKey());

        long wpMicro = OPENX_CRYPTER.decodeDecrypt(winningPriceString, encryption, integrity);
        double wpDouble = Utils.microToCpm(wpMicro);
        ParsedPriceInfo retval = new ParsedPriceInfo(wpDouble, wpMicro, bidMicros);
        return retval;

    }

    private final SecretKeySpec getKeySpec(final String keyStr)
                    throws UnsupportedEncodingException, DecoderException {
        byte[] keyBytes = null;
        if (keyStr.length() == 44) {
            keyBytes = Base64.decodeBase64(keyStr.getBytes("US-ASCII"));
        } else if (keyStr.length() == 64) {
            keyBytes = Hex.decodeHex(keyStr.toCharArray());
        }
        return new SecretKeySpec(keyBytes, "HmacSHA1");
    }

    @Override
    public String getClickMacro() {
        return "{clickurl}";
    }

    @Override
    public String getClickEncMacro() {
        return "{clickurl_enc}";
    }

    @Override
    public boolean trueWinOnNurlOrImpression() {
        return false;
    }

    /**
     * @see MatchingAdId
     */
    @Override
    public String checkExchangeTargeting(OpenRtbRequest req, Impression imp,
                    ExchangeTargeting targeting) {
        final List<MatchingAdId> required =
                        targeting.getOpenxTargeting().getRequiredMatchingAdIds();
        final Banner banner = imp.getBanner();
        final Video video = imp.getVideo();

        final Map ext = banner != null ? banner.getExt() : (video != null ? video.getExt() : null);
        final List<MatchingAdId> received =
                        ext == null ? null : (List<MatchingAdId>) ext.get("matching_ad_id");
        if (received == null) {
            return null;
        }

        for (final MatchingAdId m1 : received) {
            for (final MatchingAdId m2 : required) {
                if (m1.equals(m2)) {
                    // This mutation happening in convertRequest() is not really
                    // cool, but
                    // o well
                    ext.put("matching_ad_id_found", m1);
                    return null;
                }
            }
        }
        return "No match for any of required matching_ad_ids: " + required
                        + " in received matching_ad_ids: " + received;
    }

    @Override
    public Bid massageBid(final OpenRtbRequest req, final Impression imp, final Tag tag,
                    final Bid bid) {

        final Map ext = imp.getBanner() != null ? imp.getBanner().getExt()
                        : (imp.getVideo() != null ? imp.getVideo().getExt() : null);
        final OpenXBidExt bidExt = new OpenXBidExt();
        if (ext != null) {
            final MatchingAdId mAdId = (MatchingAdId) ext.get("matching_ad_id_found");

            if (mAdId != null) {

                bidExt.setMatching_ad_id(mAdId);
            } else {
                if (tag.getExchangeTargeting() == null) {
                    final List<MatchingAdId> received =
                                    (List<MatchingAdId>) ext.get("matching_ad_id");
                    if (received.size() > 0) {
                        bidExt.setMatching_ad_id(received.get(0));
                    }
                } else {
                    // Weird!!!
                    LogUtils.debug("Expected matching_ad_id_found in " + ext);
                }

            }
        }

        return bid;

    }

    public static final String RESPONSE_MEDIA_TYPE = MediaType.APPLICATION_JSON;

    @Override
    public String getResponseMediaType() {
        return RESPONSE_MEDIA_TYPE;
    }
}
