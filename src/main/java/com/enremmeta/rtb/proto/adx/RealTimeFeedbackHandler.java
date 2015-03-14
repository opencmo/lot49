package com.enremmeta.rtb.proto.adx;

import java.util.List;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.LostAuctionTask;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.jersey.StatsSvc;
import com.google.protos.adx.NetworkBid;


public class RealTimeFeedbackHandler implements Runnable {

    public RealTimeFeedbackHandler(AdXAdapter adapter, NetworkBid.BidRequest req) {
        super();
        this.req = req;
        this.adapter = adapter;

    }

    private final AdXAdapter adapter;

    private final NetworkBid.BidRequest req;

    @Override
    public void run() {
        try {
            List<NetworkBid.BidRequest.BidResponseFeedback> list = req.getBidResponseFeedbackList();
            LogUtils.trace("AdX: " + list.size() + " feedbacks");
            for (NetworkBid.BidRequest.BidResponseFeedback brf : list) {
                String brId = AdXAdapter.getStringFromByteSting(brf.getRequestId());
                LogUtils.trace("Got real-time feedback for " + brId);
                int cridx = brf.getCreativeIndex();
                int code = brf.getCreativeStatusCode();
                long winPriceMicros = brf.getCpmMicros();
                BidInFlightInfo bif = null;
                if (code != 1) {
                    if (code == 79) {
                        bif = StatsSvc.handleWinLossError("BidResponseFeedback", adapter, null,
                                        brId, null,
                                        LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_LOSS,
                                        null);
                    } else {
                        bif = StatsSvc.handleWinLossError("BidResponseFeedback", adapter, null,
                                        brId, null,
                                        LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_ERROR,
                                        null);
                    }
                    LogUtils.logLost(bif == null ? null : bif.getBidCreatedOnTimestamp(),
                                    bif == null ? null : bif.getBidId(),
                                    bif == null ? null : bif.getImpressionId(),
                                    bif == null ? null : bif.getCampaignId(),
                                    bif == null ? null : bif.getCreativeId(),
                                    bif == null ? null : bif.getBidPriceMicros(),
                                    winPriceMicros / 1000,
                                    code == 79 ? LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_LOSS
                                                    : LostAuctionTask.CANCELLATION_REASON_AUTHORITATIVE_ERROR,
                                    adapter.getName(),
                                    code == 79 ? "outbid"
                                                    : Lot49Constants.EXCHANGE_ADX + "_" + code,
                                    null, null, null, adapter.getName(), brId,
                                    bif == null ? null : bif.getInstanceId());

                }
            }
        } catch (Throwable t) {
            LogUtils.error("Error in parsing RealTimeFeedback", t);
        }

    }

}
