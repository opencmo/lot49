package com.enremmeta.rtb;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49SubscriptionData;

/**
 * Represents an {@link Ad} during the process of a bid request and keeps track of which aspects of
 * this <tt>Ad</tt> pass the bid request and internal restrictions, and therefore will be allowed to
 * be returned to the exchange with the bid, and which will not.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public final class BidCandidate {

    private long bidPrice = -1;

    public long getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(long bidPrice) {
        this.bidPrice = bidPrice;
    }

    private Lot49Ext lot49Ext;

    public BidCandidate(final BidCandidateManager bcMgr, final Ad ad, final Bid bid) {
        super();
        this.bcMgr = bcMgr;
        this.ad = ad;
        this.bid = bid;
        this.userSegmentsCompleted = !ad.needUserInfo();
        this.experimentInfoCompleted = !ad.needExperimentInfo();
        this.fcInfoCompleted = !ad.needFrequencyCap();
        this.lot49Ext = bcMgr.getBidRequest().getLot49Ext();
        this.integralCompleted = !(ad.needIntegralInfo() && (lot49Ext.getSubscriptionData() == null
                        || lot49Ext.getSubscriptionData().isAllowedService(ad.getClientId(),
                                        Lot49SubscriptionData.Lot49SubscriptionServiceName.INTEGRAL)));
    }

    public Bid getBid() {
        return this.bid;
    }

    private final Ad ad;

    public Ad getAd() {
        return ad;
    }

    public BidCandidateManager getBcMgr() {
        return bcMgr;
    }

    private final BidCandidateManager bcMgr;

    private final Bid bid;

    private boolean doneAndPassed = false;
    private boolean doneAndFailed = false;

    private boolean integralCompleted;
    private boolean userSegmentsCompleted;
    private boolean experimentInfoCompleted;
    private boolean fcInfoCompleted;

    private boolean segmentsPassed = true;
    private boolean integralPassed = true;
    private boolean experimentPassed = true;
    private boolean fcPassed = true;

    public boolean isDone() {
        return doneAndPassed || doneAndFailed;
    }

    public boolean isDoneAndPassed() {
        return doneAndPassed;
    }

    public boolean isDoneAndFailed() {
        return doneAndFailed;
    }

    /**
     * @return <tt>true</tt> if <b>all</b> checks passed.
     */
    public final boolean passed() {
        if (doneAndPassed) {
            return true;
        }
        if (doneAndFailed) {
            return false;
        }
        final boolean need2 = ad.needCanBid2();
        if (!need2) {
            doneAndPassed = true;
            doneAndFailed = false;
            return true;
        }

        if (!userSegmentsCompleted) {
            segmentsPassed = false;
            UserSegments userSegments = bcMgr.getUserSegments();
            if (userSegments == null) {
                if (bcMgr.getUserSegmentsFuture().isDone()) {
                    try {
                        userSegments = bcMgr.getUserSegmentsFuture().get();
                    } catch (Exception e) {
                        LogUtils.debug("Error getting User segments data for "
                                        + lot49Ext.getModUid() + ", " + e);
                        doneAndFailed = true;
                        doneAndPassed = false;
                        return false;
                    }
                    if (userSegments == null) {
                        userSegments = new UserSegments();
                    }
                    bcMgr.setUserSegments(userSegments);
                }
            }
            if (userSegments != null) {
                userSegmentsCompleted = true;
                segmentsPassed = ad.checkSegments(bcMgr.getBidRequest(), userSegments);
                if (!segmentsPassed) {
                    doneAndFailed = true;
                    doneAndPassed = false;
                    return false;
                }
            }
        }

        if (!integralCompleted) {
            integralPassed = false;
            if (bcMgr.getIntegralInfoReceived().isCompleted()) {
                integralCompleted = true;
                integralPassed = ad.checkIntegralTargeting(bcMgr.getBidRequest(),
                                bcMgr.getBidRequest().getSite(), bcMgr.getIntegralInfoReceived());
                if (!integralPassed) {
                    doneAndFailed = true;
                    doneAndPassed = false;
                    return false;
                }
            }
        }

        if (!experimentInfoCompleted) {
            // The next line is for local debugging ONLY
            // DO NOT FORGET TO REMOVE IN PRODUCTION
            // TimeUnit.MILLISECONDS.sleep(400);
            UserAttributes userAttributes = bcMgr.getUserAttributes();
            if (userAttributes == null) {
                experimentPassed = false;
                if (bcMgr.getUserAttributesFuture().isDone()) {
                    try {
                        userAttributes = bcMgr.getUserAttributesFuture().get();
                    } catch (Exception e) {
                        LogUtils.debug("Error getting Experiment data for " + lot49Ext.getModUid()
                                        + ", " + e);
                        doneAndFailed = true;
                        doneAndPassed = false;
                        return false;
                    }
                    bcMgr.setUserAttributes(userAttributes);
                    experimentInfoCompleted = true;
                    experimentPassed = true;
                }
            } else {
                experimentInfoCompleted = true;
                experimentPassed = true;
            }
        }

        if (!fcInfoCompleted) {
            fcPassed = false;
            UserAttributes userAttributes = bcMgr.getUserAttributes();
            if (userAttributes == null) {
                if (bcMgr.getUserAttributesFuture().isDone()) {
                    try {
                        userAttributes = bcMgr.getUserAttributesFuture().get();
                    } catch (Exception e) {
                        LogUtils.debug("Error getting Frequency Cap data for "
                                        + lot49Ext.getModUid() + ", " + e);
                        doneAndFailed = true;
                        doneAndPassed = false;
                        return false;
                    }
                    bcMgr.setUserAttributes(userAttributes);
                }
            }
            if (userAttributes != null) {
                fcInfoCompleted = true;
                fcPassed = ad.checkFrequencyCap(bcMgr.getBidRequest(),
                                userAttributes.getUserFrequencyCap());
                if (!fcPassed) {
                    doneAndFailed = true;
                    doneAndPassed = false;
                    return false;
                }
            }
        }

        if (segmentsPassed && integralPassed && experimentPassed && fcPassed) {
            doneAndFailed = false;
            doneAndPassed = true;
            return true;
        }

        return false;
    }

    /**
     * @return <tt>true</tt> if <b>any</b> checks passed.
     */
    public final boolean failed() {
        if (doneAndPassed) {
            return false;
        }
        if (doneAndFailed) {
            return true;
        }
        if (!ad.needCanBid2()) {
            doneAndPassed = true;
            doneAndFailed = false;
            return false;
        }

        if (!userSegmentsCompleted) {
            segmentsPassed = false;
            UserSegments userSegments = bcMgr.getUserSegments();
            if (userSegments == null) {
                if (bcMgr.getUserSegmentsFuture().isDone()) {
                    try {
                        userSegments = bcMgr.getUserSegmentsFuture().get();
                    } catch (Exception e) {
                        LogUtils.debug("Error getting User segments data for "
                                        + lot49Ext.getModUid() + ", " + e);
                        doneAndFailed = true;
                        doneAndPassed = false;
                        return true;
                    }
                    if (userSegments == null) {
                        userSegments = new UserSegments();
                    }
                    bcMgr.setUserSegments(userSegments);
                }
            }
            if (userSegments != null) {
                userSegmentsCompleted = true;
                segmentsPassed = ad.checkSegments(bcMgr.getBidRequest(), userSegments);
                if (!segmentsPassed) {
                    doneAndFailed = true;
                    doneAndPassed = false;
                    return true;
                }
            }
        }

        if (!integralCompleted) {
            integralPassed = false;
            if (bcMgr.getIntegralInfoReceived().isCompleted()) {
                integralCompleted = true;
                integralPassed = ad.checkIntegralTargeting(bcMgr.getBidRequest(),
                                bcMgr.getBidRequest().getSite(), bcMgr.getIntegralInfoReceived());
                if (!integralPassed) {
                    doneAndFailed = true;
                    doneAndPassed = false;
                    return true;
                }
            }
        }

        if (!experimentInfoCompleted) {
            UserAttributes userAttributes = bcMgr.getUserAttributes();
            if (userAttributes == null) {
                experimentPassed = false;
                if (bcMgr.getUserAttributesFuture().isDone()) {
                    try {
                        userAttributes = bcMgr.getUserAttributesFuture().get();
                    } catch (Exception e) {
                        LogUtils.debug("Error getting Experiment data for " + lot49Ext.getModUid()
                                        + ", " + e);
                        doneAndFailed = true;
                        doneAndPassed = false;
                        return true;
                    }
                    bcMgr.setUserAttributes(userAttributes);
                    experimentInfoCompleted = true;
                    experimentPassed = true;
                }
            } else {
                experimentInfoCompleted = true;
                experimentPassed = true;
            }
        }

        if (!fcInfoCompleted) {
            fcPassed = false;
            UserAttributes userAttributes = bcMgr.getUserAttributes();
            if (userAttributes == null) {
                if (bcMgr.getUserAttributesFuture().isDone()) {
                    try {
                        userAttributes = bcMgr.getUserAttributesFuture().get();
                    } catch (Exception e) {
                        LogUtils.debug("Error getting Frequency Cap data for "
                                        + lot49Ext.getModUid() + ", " + e);
                        doneAndFailed = true;
                        doneAndPassed = false;
                        return true;
                    }
                    bcMgr.setUserAttributes(userAttributes);
                }
            }
            if (userAttributes != null) {
                fcInfoCompleted = true;
                fcPassed = ad.checkFrequencyCap(bcMgr.getBidRequest(),
                                userAttributes.getUserFrequencyCap());
                if (!fcPassed) {
                    doneAndFailed = true;
                    doneAndPassed = false;
                    return true;
                }
            }
        }
        if (segmentsPassed && integralPassed && experimentPassed && fcPassed) {
            doneAndFailed = false;
            doneAndPassed = true;
        }

        return false;
    }

    private String getStringStatus(boolean completed, boolean status) {
        if (!completed) {
            return "NOT DONE";
        } else if (status) {
            return "PASSED";
        } else {
            return "FAILED";
        }
    }

    @Override
    public String toString() {
        return "BidCandidate<Ad: " + ad + "; bid: " + bid + "; userSegments: "
                        + getStringStatus(userSegmentsCompleted, segmentsPassed) + "; Integral: "
                        + getStringStatus(integralCompleted, integralPassed) + "; FC: "
                        + getStringStatus(fcInfoCompleted, fcPassed) + "; Experiment: "
                        + (experimentInfoCompleted ? "DONE" : "NOT DONE") + ">";
    }

    public boolean isIntegralCompleted() {
        return integralCompleted;
    }

    public void setIntegralCompleted(boolean integralCompleted) {
        this.integralCompleted = integralCompleted;
    }

    public boolean isUserInfoCompleted() {
        return userSegmentsCompleted;
    }

    public void setUserInfoCompleted(boolean userSegmentsCompleted) {
        this.userSegmentsCompleted = userSegmentsCompleted;
    }

    public boolean isExperimentInfoCompleted() {
        return experimentInfoCompleted;
    }

    public void setExperimentInfoCompleted(boolean experimentInfoCompleted) {
        this.experimentInfoCompleted = experimentInfoCompleted;
    }

    public boolean isFcInfoCompleted() {
        return fcInfoCompleted;
    }

    public void setFcInfoCompleted(boolean fcInfoCompleted) {
        this.fcInfoCompleted = fcInfoCompleted;
    }

}
