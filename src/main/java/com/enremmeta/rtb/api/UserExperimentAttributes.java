package com.enremmeta.rtb.api;

import java.util.Map;

/**
 * A Map of Experiment Status for Campaigns and Targeting Strategies.
 * 
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */

public class UserExperimentAttributes {
    public static final String TEST = "T";
    public static final String CONTROL = "C";
    public static final String NOT_EXPERIMENT = "N";

    public static final String CAMPAIGN_PREFIX = "c_";
    public static final String TARGETING_STRATEGY_PREFIX = "ts_";
    public static final String CAMPAIGN_EXPERIMENT_VERSION_PREFIX = "cv_";

    private Map<String, String> experimentData;
    private boolean changed;

    public UserExperimentAttributes(Map<String, String> experimentData) {
        this.experimentData = experimentData;
        this.changed = false;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public Map<String, String> getExperimentData() {
        return experimentData;
    }

    public void setExperimentData(Map<String, String> experimentData) {
        this.experimentData = experimentData;
    }

    public void setExperimentVersion(Ad ad, String version) {
        if (!version.equals(experimentData.get(ad.getCampaignId()))) {
            experimentData.put(CAMPAIGN_EXPERIMENT_VERSION_PREFIX + ad.getCampaignId(), version);
            changed = true;
        }
    }

    public String getExperimentVersion(Ad ad) {
        String version = experimentData
                        .get(CAMPAIGN_EXPERIMENT_VERSION_PREFIX + ad.getCampaignId());
        if (version == null) {
            // TODO default experiment version
            version = "2";
        }
        return version;
    }

    public void setStatusForCampaign(Ad ad, String status) {
        if (!status.equals(experimentData.get(ad.getCampaignId()))) {
            experimentData.put(CAMPAIGN_PREFIX + ad.getCampaignId(), status);
            changed = true;
        }
    }

    public void setStatusTestForCampaign(Ad ad) {
        if (!TEST.equals(experimentData.get(ad.getCampaignId()))) {
            experimentData.put(CAMPAIGN_PREFIX + ad.getCampaignId(), TEST);
            changed = true;
        }
    }

    public void setStatusControlForCampaign(Ad ad) {
        if (!CONTROL.equals(experimentData.get(ad.getCampaignId()))) {
            experimentData.put(CAMPAIGN_PREFIX + ad.getCampaignId(), CONTROL);
            changed = true;
        }
    }

    public void setStatusNotExperimentForCampaign(Ad ad) {
        if (!NOT_EXPERIMENT.equals(experimentData.get(ad.getCampaignId()))) {
            experimentData.put(CAMPAIGN_PREFIX + ad.getCampaignId(), NOT_EXPERIMENT);
            changed = true;
        }
    }

    public String getStatusForCampaign(Ad ad) {
        return experimentData.get(CAMPAIGN_PREFIX + ad.getCampaignId());
    }

    public boolean isStatusTestForCampaign(Ad ad) {
        String status = getStatusForCampaign(ad);
        if (status == null) {
            return false;
        } else {
            return TEST.equals(status);
        }
    }

    public boolean isStatusControlForCampaign(Ad ad) {
        String status = getStatusForCampaign(ad);
        if (status == null) {
            return false;
        } else {
            return CONTROL.equals(status);
        }
    }

    public boolean isStatusNotExperimentForCampaign(Ad ad) {
        String status = getStatusForCampaign(ad);
        if (status == null) {
            return false;
        } else {
            return NOT_EXPERIMENT.equals(status);
        }
    }

    public void setStatusForTargetingStrategy(Ad ad, String status) {
        if (!status.equals(experimentData.get(ad.getId()))) {
            experimentData.put(TARGETING_STRATEGY_PREFIX + ad.getId(), status);
            changed = true;
        }
    }

    public void setStatusTestForTargetingStrategy(Ad ad) {
        if (!TEST.equals(experimentData.get(ad.getId()))) {
            experimentData.put(TARGETING_STRATEGY_PREFIX + ad.getId(), TEST);
            changed = true;
        }
    }

    public void setStatusControlForTargetingStrategy(Ad ad) {
        if (!CONTROL.equals(experimentData.get(ad.getId()))) {
            experimentData.put(TARGETING_STRATEGY_PREFIX + ad.getId(), CONTROL);
            changed = true;
        }
    }

    public void setStatusNotExperimentForTargetingStrategy(Ad ad) {
        if (!NOT_EXPERIMENT.equals(experimentData.get(ad.getId()))) {
            experimentData.put(TARGETING_STRATEGY_PREFIX + ad.getId(), NOT_EXPERIMENT);
            changed = true;
        }
    }

    public String getStatusForTargetingStrategy(Ad ad) {
        return experimentData.get(TARGETING_STRATEGY_PREFIX + ad.getId());
    }

    public boolean isStatusTestForTargetingStrategy(Ad ad) {
        String status = getStatusForTargetingStrategy(ad);
        if (status == null) {
            return false;
        } else {
            return TEST.equals(status);
        }
    }

    public boolean isStatusControlForTargetingStrategy(Ad ad) {
        String status = getStatusForTargetingStrategy(ad);
        if (status == null) {
            return false;
        } else {
            return CONTROL.equals(status);
        }
    }

    public boolean isStatusNotExperimentForTargetingStrategy(Ad ad) {
        String status = getStatusForTargetingStrategy(ad);
        if (status == null) {
            return false;
        } else {
            return NOT_EXPERIMENT.equals(status);
        }
    }
}
