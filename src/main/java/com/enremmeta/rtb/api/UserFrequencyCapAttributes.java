package com.enremmeta.rtb.api;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.enremmeta.util.BidderCalendar;

/**
 * Bid and Impression histories for Frequency Cap calculations. In getCount functions the history
 * older than FC time frame is being cleaned. In updateImpression functions the history of Bids
 * earlier the impression is being cleaned.
 * 
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */

public class UserFrequencyCapAttributes {
    private Map<String, Set<String>> bidsHistory;
    private Map<String, Set<String>> impressionsHistory;
    private boolean changed;

    public static final String CAMPAIGN_PREFIX = "c_";
    public static final String TARGETING_STRATEGY_PREFIX = "ts_";

    public UserFrequencyCapAttributes(Map<String, Set<String>> bidsHistory,
                    Map<String, Set<String>> impressionsHistory) {
        this.bidsHistory = bidsHistory;
        this.impressionsHistory = impressionsHistory;
        this.changed = false;
    }

    public Map<String, Set<String>> getBidsHistory() {
        return bidsHistory;
    }

    public void setBidsHistory(Map<String, Set<String>> bidsHistory) {
        this.bidsHistory = bidsHistory;
    }

    public Map<String, Set<String>> getImpressionsHistory() {
        return impressionsHistory;
    }

    public void setImpressionsHistory(Map<String, Set<String>> impressionsHistory) {
        this.impressionsHistory = impressionsHistory;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    private void cleanupHistory(Set<String> history, long timecut) {
        for (Iterator<String> i = history.iterator(); i.hasNext();) {
            long ts = Long.valueOf(i.next()).longValue();
            if (ts < timecut) {
                i.remove();
                changed = true;
            }
        }
    }

    public int getCampaignBidsCount(Ad ad, int hours) {
        long timestamp = BidderCalendar.getInstance().currentTimeMillis();
        Set<String> bidsHistoryForCampaign = bidsHistory.get(CAMPAIGN_PREFIX + ad.getCampaignId());
        if (bidsHistoryForCampaign == null) {
            return 0;
        }
        long timeThen = timestamp - hours * 3600000;
        cleanupHistory(bidsHistoryForCampaign, timeThen);
        if (bidsHistoryForCampaign.size() == 0) {
            bidsHistory.remove(CAMPAIGN_PREFIX + ad.getCampaignId());
        }
        return bidsHistoryForCampaign.size();
    }

    public int getTargetingStrategyBidsCount(Ad ad, int hours) {
        long timestamp = BidderCalendar.getInstance().currentTimeMillis();
        Set<String> bidsHistoryForTS = bidsHistory.get(TARGETING_STRATEGY_PREFIX + ad.getId());
        if (bidsHistoryForTS == null) {
            return 0;
        }
        long timeThen = timestamp - hours * 3600000;
        cleanupHistory(bidsHistoryForTS, timeThen);
        if (bidsHistoryForTS.size() == 0) {
            bidsHistory.remove(TARGETING_STRATEGY_PREFIX + ad.getId());
        }
        return bidsHistoryForTS.size();
    }

    public void updateBidsHistoryForCampaign(Ad ad) {
        long timestamp = BidderCalendar.getInstance().currentTimeMillis();
        Set<String> bidsHistoryForCampaign = bidsHistory.get(CAMPAIGN_PREFIX + ad.getCampaignId());
        if (bidsHistoryForCampaign == null) {
            bidsHistoryForCampaign = new HashSet<String>();
            bidsHistory.put(CAMPAIGN_PREFIX + ad.getCampaignId(), bidsHistoryForCampaign);
        }
        bidsHistoryForCampaign.add(String.valueOf(timestamp));
        changed = true;
    }

    public void updateBidsHistoryForTargetingStrategy(Ad ad) {
        long timestamp = BidderCalendar.getInstance().currentTimeMillis();
        Set<String> bidsHistoryForTS = bidsHistory.get(TARGETING_STRATEGY_PREFIX + ad.getId());
        if (bidsHistoryForTS == null) {
            bidsHistoryForTS = new HashSet<String>();
            bidsHistory.put(TARGETING_STRATEGY_PREFIX + ad.getId(), bidsHistoryForTS);
        }
        bidsHistoryForTS.add(String.valueOf(timestamp));
        changed = true;
    }

    public int getCampaignImpressionsCount(Ad ad, int hours) {
        long timestamp = BidderCalendar.getInstance().currentTimeMillis();
        Set<String> impressionsHistoryForCampaign =
                        impressionsHistory.get(CAMPAIGN_PREFIX + ad.getCampaignId());
        if (impressionsHistoryForCampaign == null) {
            return 0;
        }
        long timeThen = timestamp - hours * 3600000;
        cleanupHistory(impressionsHistoryForCampaign, timeThen);
        if (impressionsHistoryForCampaign.size() == 0) {
            impressionsHistory.remove(CAMPAIGN_PREFIX + ad.getCampaignId());
        }
        return impressionsHistoryForCampaign.size();
    }

    public int getTargetingStrategyImpressionsCount(Ad ad, int hours) {
        long timestamp = BidderCalendar.getInstance().currentTimeMillis();
        Set<String> impressionsHistoryForTS =
                        impressionsHistory.get(TARGETING_STRATEGY_PREFIX + ad.getId());
        if (impressionsHistoryForTS == null) {
            return 0;
        }
        long timeThen = timestamp - hours * 3600000;
        cleanupHistory(impressionsHistoryForTS, timeThen);
        if (impressionsHistoryForTS.size() == 0) {
            impressionsHistory.remove(TARGETING_STRATEGY_PREFIX + ad.getId());
        }
        return impressionsHistoryForTS.size();
    }

    public void updateImpressionsHistoryForCampaign(Ad ad) {
        long timestamp = BidderCalendar.getInstance().currentTimeMillis();
        Set<String> impressionsHistoryForCampaign =
                        impressionsHistory.get(CAMPAIGN_PREFIX + ad.getCampaignId());
        if (impressionsHistoryForCampaign == null) {
            impressionsHistoryForCampaign = new HashSet<String>();
            impressionsHistory.put(CAMPAIGN_PREFIX + ad.getCampaignId(),
                            impressionsHistoryForCampaign);
        }
        impressionsHistoryForCampaign.add(String.valueOf(timestamp));
        Set<String> bidsHistoryForCampaign = bidsHistory.get(CAMPAIGN_PREFIX + ad.getCampaignId());
        if (bidsHistoryForCampaign != null) {
            cleanupHistory(bidsHistoryForCampaign, timestamp);
            if (bidsHistoryForCampaign.size() == 0) {
                bidsHistory.remove(CAMPAIGN_PREFIX + ad.getCampaignId());
            }
        }
        changed = true;
    }

    public void updateImpressionsHistoryForTargetingStrategy(Ad ad) {
        long timestamp = BidderCalendar.getInstance().currentTimeMillis();
        Set<String> impressionsHistoryForTS =
                        impressionsHistory.get(TARGETING_STRATEGY_PREFIX + ad.getId());
        if (impressionsHistoryForTS == null) {
            impressionsHistoryForTS = new HashSet<String>();
            impressionsHistory.put(TARGETING_STRATEGY_PREFIX + ad.getId(), impressionsHistoryForTS);
        }
        impressionsHistoryForTS.add(String.valueOf(timestamp));
        Set<String> bidsHistoryForTS = bidsHistory.get(TARGETING_STRATEGY_PREFIX + ad.getId());
        if (bidsHistoryForTS != null) {
            cleanupHistory(bidsHistoryForTS, timestamp);
            if (bidsHistoryForTS.size() == 0) {
                bidsHistory.remove(TARGETING_STRATEGY_PREFIX + ad.getId());
            }
        }
        changed = true;
    }
}
