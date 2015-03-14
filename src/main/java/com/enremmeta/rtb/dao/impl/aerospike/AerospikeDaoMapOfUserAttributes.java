package com.enremmeta.rtb.dao.impl.aerospike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.listener.RecordListener;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.UserFrequencyCapAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;

/**
 * Dao for access to {@link UserAttributes} in Aerospike
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class AerospikeDaoMapOfUserAttributes implements DaoMapOfUserAttributes {

    private final AerospikeDBService svc;

    public static final String ATTRIBUTES_EXPERIMENT_BIN = "experiment";
    public static final String ATTRIBUTES_BIDS_BIN = "bids";
    public static final String ATTRIBUTES_IMPRESSIONS_BIN = "impressions";

    public static UserAttributes recordToUserAttributes(Record record) {
        Map<String, String> experimentMap;
        if (record != null && record.getValue(ATTRIBUTES_EXPERIMENT_BIN) != null) {
            experimentMap = (Map<String, String>) record.getValue(ATTRIBUTES_EXPERIMENT_BIN);
        } else {
            experimentMap = new HashMap<String, String>();
        }
        UserExperimentAttributes experimentAttributes = new UserExperimentAttributes(experimentMap);

        Map<String, Set<String>> bidsHistory;
        if (record != null && record.getValue(ATTRIBUTES_BIDS_BIN) != null) {
            bidsHistory = (Map<String, Set<String>>) record.getValue(ATTRIBUTES_BIDS_BIN);
        } else {
            bidsHistory = new HashMap<String, Set<String>>();
        }

        Map<String, Set<String>> impressionsHistory;
        if (record != null && record.getValue(ATTRIBUTES_IMPRESSIONS_BIN) != null) {
            impressionsHistory =
                            (Map<String, Set<String>>) record.getValue(ATTRIBUTES_IMPRESSIONS_BIN);
        } else {
            impressionsHistory = new HashMap<String, Set<String>>();
        }
        UserFrequencyCapAttributes frequencyCapAttributes =
                        new UserFrequencyCapAttributes(bidsHistory, impressionsHistory);
        return new UserAttributes(experimentAttributes, frequencyCapAttributes);
    }

    public AerospikeDaoMapOfUserAttributes(AerospikeDBService svc) {
        super();
        this.svc = svc;
    }

    @Override
    public Future<UserAttributes> getAsync(String uid) {
        AerospikeUserAttributesFuture future = new AerospikeUserAttributesFuture();
        svc.getClient().get(null, new AerospikeUserAttributesReadHandler(future),
                        new Key(svc.getConfig().getNamespace(), svc.getConfig().getSet(), uid));
        return future;
    }

    @Override
    public void putAsync(String uid, UserAttributes userAttributes) {
        if (userAttributes != null && (userAttributes.getUserExperimentData().isChanged()
                        || userAttributes.getUserFrequencyCap().isChanged())) {
            ArrayList<Bin> record = new ArrayList<Bin>();


            Map<String, String> experimentData =
                            userAttributes.getUserExperimentData().getExperimentData();
            if (experimentData != null && experimentData.size() > 0) {
                record.add(new Bin(ATTRIBUTES_EXPERIMENT_BIN, experimentData));
            }

            Map<String, Set<String>> bidsHistory =
                            userAttributes.getUserFrequencyCap().getBidsHistory();
            if (bidsHistory != null && bidsHistory.size() > 0) {
                record.add(new Bin(ATTRIBUTES_BIDS_BIN, bidsHistory));
            }

            Map<String, Set<String>> impressionsHistory =
                            userAttributes.getUserFrequencyCap().getImpressionsHistory();
            if (impressionsHistory != null && impressionsHistory.size() > 0) {
                record.add(new Bin(ATTRIBUTES_IMPRESSIONS_BIN, impressionsHistory));
            }

            svc.getClient().put(null, new AerospikeUserAttributesWriteHandler(),
                            new Key(svc.getConfig().getNamespace(), svc.getConfig().getSet(), uid),
                            record.toArray(new Bin[0]));
        }

    }

    @Override
    public void updateImpressionsHistoryAsync(Ad ad, String uid) {
        // TODO Shall be done with UDF
        svc.getClient().get(null, new RecordListener() {
            public void onSuccess(Key key, Record record) {
                UserAttributes userAttributes = recordToUserAttributes(record);

                UserFrequencyCapAttributes frequencyCapAttributes =
                                userAttributes.getUserFrequencyCap();
                if (ad.isCampaignFrequencyCap()) {
                    frequencyCapAttributes.updateImpressionsHistoryForCampaign(ad);
                }
                if (ad.isStrategyFrequencyCap()) {
                    frequencyCapAttributes.updateImpressionsHistoryForTargetingStrategy(ad);
                }
                putAsync(uid, userAttributes);

            }

            public void onFailure(AerospikeException exception) {
                LogUtils.debug("Error updating Impression history for " + uid);
            }
        }, new Key(svc.getConfig().getNamespace(), svc.getConfig().getSet(), uid));
    }

}
