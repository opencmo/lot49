package com.enremmeta.rtb.proto.brx;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.amazonaws.util.StringInputStream;
import com.enremmeta.rtb.api.proto.openrtb.Content;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.Video;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.RtbConstants;
import com.enremmeta.rtb.proto.ParsedPriceInfo;
import com.enremmeta.rtb.proto.brx.BrxRtb095.Api;
import com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest;
import com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Ext;
import com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Imp;
import com.enremmeta.rtb.proto.brx.BrxRtb095.BidResponse;
import com.enremmeta.rtb.proto.brx.BrxRtb095.ContentCategory;
import com.enremmeta.rtb.proto.brx.BrxRtb095.Linearity;
import com.enremmeta.rtb.proto.brx.BrxRtb095.Mimes;
import com.enremmeta.rtb.proto.spotxchange.ExchangeAdapterImpl;
import com.enremmeta.util.Utils;
import com.googlecode.protobuf.format.JsonFormat;

public class BrxAdapter extends ExchangeAdapterImpl<BrxRtb095.BidRequest, BrxRtb095.BidResponse> {

    public BrxAdapter() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public final long getDefaultTimeout() {
        return 90;
    }

    @Override
    public final String getWinningPriceMacro() {
        return "##BRX_CLEARING_PRICE##";
    }

    @Override
    public final boolean localUserMapping() {
        return true;
    }

    @Override
    public ParsedPriceInfo parse(String winningPriceString, long bidMicros) {
        // The following steps detail how to calculate the clearing price
        // given the ratio replaced in the VAST tag using the clearing price
        // macro. The example below assumes a max bid of $10 and a clearing
        // price ratio
        // of 0000000000000000667333:
        // 1. Take the integer value of the clearing price ratio returned.
        // E.g., 0000000000000000667333 --> 667333
        // 2. Dividetheresultingintegerby1,000,000 667333/1000000 -->
        // 0.667333
        // 3. Multipletheresultwiththemaxbidandroundto2digitsto calculate
        // the clearing price:
        // ROUND(0.667333 * $10) --> $6.67

        final double wpDouble = Integer.valueOf(winningPriceString) / 1000000.
                        * Utils.microToCpm(bidMicros);
        final long wpMicro = Utils.cpmToMicro(wpDouble);
        final ParsedPriceInfo retval = new ParsedPriceInfo(wpDouble, wpMicro, bidMicros);
        return retval;
    }

    @Override
    public String getName() {
        return Lot49Constants.EXCHANGE_BRX;
    }

    public boolean isNurlRequired() {
        return true;
    }

    @Override
    public OpenRtbRequest convertRequest(BidRequest req1) {
        final OpenRtbRequest req2 = new OpenRtbRequest();
        Lot49Ext lot49Ext = req2.getLot49Ext();

        req2.setId(req1.getId());
        final Ext ext1 = req1.getExt();

        if (ext1.getIsTest()) {
            req2.getLot49Ext().setTest(true);
        }

        // 1. Impression
        final Imp imp1 = req1.getImp();
        final Impression imp2 = new Impression();
        req2.setImp(new ArrayList<Impression>(1));
        imp2.setId(imp1.getId());
        req2.getImp().add(imp2);

        // 1.1 Video
        final Video vid2 = new Video();
        imp2.setVideo(vid2);
        final com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Video vid1 = imp1.getVideo();

        // 1.1.1 API
        final List<Integer> apiList2 = new ArrayList<Integer>(vid1.getApiCount());
        vid2.setApi(apiList2);
        for (final Api api1 : vid1.getApiList()) {
            apiList2.add(api1.getNumber());
        }

        // 1.1.2 Dimensions
        vid2.setW(vid1.getW());
        vid2.setH(vid1.getH());

        // 1.1.3 Linearity
        vid2.setLinearity(vid1.getLinearity() == Linearity.LINEAR ? RtbConstants.LINEARITY_LINEAR
                        : RtbConstants.LINEARITY_NON_LINEAR);

        // 1.1.4 Duration & bitrate
        vid2.setMinduration(vid1.getMinduration());
        vid2.setMaxduration(vid1.getMaxduration());
        vid2.setMaxbitrate(vid1.getMaxbitrate());

        // 1.1.5 Mimes
        List<String> mimes2 = new ArrayList<String>(vid1.getMimesCount());
        vid2.setMimes(mimes2);
        for (Mimes mime : vid1.getMimesList()) {
            mimes2.add(Utils.brxMimeToString(mime));
        }

        // 1.1.6 Pos
        vid2.setPos(vid1.getPos().getNumber());

        // 1.1.7 Start delay
        vid2.setStartdelay(vid1.getStartdelay());

        // 1.2 Site
        final Site site2 = new Site();
        req2.setSite(site2);
        final com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Site site1 = req1.getSite();

        // 1.2.1 Page
        site2.setPage(site1.getPage());

        // 1.2.2 Cat
        List<String> cat2 = new ArrayList<String>();
        site2.setCat(cat2);
        for (ContentCategory cat1 : site1.getCatList()) {
            cat2.add("BRXIAB_" + cat1.getNumber());
        }

        // 1.2.3 Section cat
        List<String> sectionCat2 = new ArrayList<String>();
        site2.setSectioncat(sectionCat2);
        for (ContentCategory sectionCat1 : site1.getSectioncatList()) {
            sectionCat2.add("BRXIAB_" + sectionCat1.getNumber());
        }

        // 1.2.4 Page cat
        List<String> pageCat2 = new ArrayList<String>();
        site2.setPagecat(pageCat2);
        for (ContentCategory pageCat1 : site1.getPagecatList()) {
            pageCat2.add("BRXIAB_" + pageCat1.getNumber());
        }

        // 1.2.5 Content
        Content content2 = new Content();
        site2.setContent(content2);
        com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Content content1 = site1.getContent();

        // 1.2.5.1 Content cat
        List<String> contentCat2 = new ArrayList<String>();
        content2.setCat(contentCat2);
        for (ContentCategory contentCat1 : content1.getCatList()) {
            contentCat2.add("BRXIAB_" + contentCat1.getNumber());
        }

        // 1.2.5.2 Content rating
        content2.setContentrating(content1.getContentrating());

        // 1.2.5.3 Content language
        content2.setLanguage(content1.getLanguage());

        // 1.2.5.4 Embeddable
        content2.setEmbeddable(content1.getEmbeddable().getNumber());

        // 1.2.6 Referer
        site2.setRef(site1.getRef());

        // 1.2.7 Domain
        site2.setDomain(site1.getDomain());

        // 1.2.8 Keywords
        site2.setKeywords(site1.getKeywords());

        // 1.3 APP - TODO

        // 1.4 Device
        Device dev2 = new Device();
        req2.setDevice(dev2);
        com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.Device dev1 = req1.getDevice();

        dev2.setCarrier(dev1.getCarrier());
        dev2.setConnectiontype(dev1.getConnectiontype().getNumber());
        dev2.setDeviceType(dev1.getDevicetype().getNumber());
        dev2.setDidmd5(dev1.getDidmd5());
        dev2.setDidsha1(dev2.getDidsha1());
        dev2.setDnt(dev1.getDnt().getNumber());
        dev2.setDpidmd5(dev1.getDpidmd5());
        dev2.setIp(dev1.getIp());
        dev2.setIpv6(dev1.getIpv6());
        dev2.setLanguage(dev1.getLanguage());
        dev2.setMake(dev1.getMake());
        dev2.setModel(dev1.getModel());
        dev2.setOs(dev1.getOs());
        dev2.setOsv(dev1.getOsv());
        dev2.setUa(dev1.getUa());
        // BRX does not set Device geo...

        // 1.5 User
        User user2 = new User();
        req2.setUser(user2);
        com.enremmeta.rtb.proto.brx.BrxRtb095.BidRequest.User user1 = req1.getUser();
        user2.setId(user1.getId());

        req2.getLot49Ext().setAdapter(this);
        return req2;
    }

    @Override
    public BrxRtb095.BidResponse convertResponse(OpenRtbRequest req, OpenRtbResponse resp)
                    throws Exception {
        // TODO this may be slow... object manipulation may be way faster than
        // generating JSON...
        BidResponse.Builder builder = BrxRtb095.BidResponse.newBuilder();
        String jsonStr = Utils.MAPPER.writeValueAsString(resp);
        JsonFormat jf = new JsonFormat();
        jf.merge(new StringInputStream(jsonStr), builder);
        BrxRtb095.BidResponse retval = builder.build();
        return retval;
    }

    @Override
    public String getClickMacro() {
        return "";
    }

    @Override
    public String getClickEncMacro() {
        return "";
    }

    public static final String RESPONSE_MEDIA_TYPE = MediaType.APPLICATION_OCTET_STREAM;

    @Override
    public String getResponseMediaType() {
        return RESPONSE_MEDIA_TYPE;
    }
}
