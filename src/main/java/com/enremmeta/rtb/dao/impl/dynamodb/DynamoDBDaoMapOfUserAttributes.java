/**
 *
 */
package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.nio.handlers.ResultHandler;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.UserFrequencyCapAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.util.BidderCalendar;

/**
 * Dao for access to {@link UserAttributes} in DynamoDB
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 */
public class DynamoDBDaoMapOfUserAttributes implements DaoMapOfUserAttributes {

    public static final String ATTRIBUTES_EXPERIMENT_FIELD = "experiment";
    public static final String ATTRIBUTES_TIMESTAMP_FIELD = "timestamp";
    public static final String ATTRIBUTES_BIDS_FIELD = "bids";
    public static final String ATTRIBUTES_IMPRESSIONS_FIELD = "impressions";

    private final DynamoDBService svc;

    public static UserAttributes getResultFromResponse(GetItemResult res, long startTime) {
        Map<String, AttributeValue> attributes = res.getItem();

        Map<String, String> experimentMap;
        if (attributes != null && attributes.get(ATTRIBUTES_EXPERIMENT_FIELD) != null) {
            experimentMap = InternalUtils
                            .toSimpleMapValue(attributes.get(ATTRIBUTES_EXPERIMENT_FIELD).getM());
        } else {
            experimentMap = new HashMap<String, String>();
        }
        UserExperimentAttributes experimentAttributes = new UserExperimentAttributes(experimentMap);

        Map<String, Set<String>> bidsHistory;
        if (attributes != null && attributes.get(ATTRIBUTES_BIDS_FIELD) != null) {
            bidsHistory = InternalUtils
                            .toSimpleMapValue(attributes.get(ATTRIBUTES_BIDS_FIELD).getM());
        } else {
            bidsHistory = new HashMap<String, Set<String>>();
        }

        Map<String, Set<String>> impressionsHistory;
        if (attributes != null && attributes.get(ATTRIBUTES_IMPRESSIONS_FIELD) != null) {
            impressionsHistory = InternalUtils
                            .toSimpleMapValue(attributes.get(ATTRIBUTES_IMPRESSIONS_FIELD).getM());
        } else {
            impressionsHistory = new HashMap<String, Set<String>>();
        }
        UserFrequencyCapAttributes frequencyCapAttributes =
                        new UserFrequencyCapAttributes(bidsHistory, impressionsHistory);
        long endTime = BidderCalendar.getInstance().currentTimeMillis();
        LogUtils.debug("Got UserAttributes from DynamoDB in " + (endTime - startTime) + " ms.");
        return new UserAttributes(experimentAttributes, frequencyCapAttributes);

    }

    public DynamoDBDaoMapOfUserAttributes(DynamoDBService svc) {
        super();
        this.svc = svc;
    }

    @Override
    public Future<UserAttributes> getAsync(String uid) {
        GetItemRequest getItemRequest = new GetItemRequest().withTableName(svc.getTableName())
                        .addKeyEntry(svc.getKeyField(), new AttributeValue(uid));
        Future<GetItemResult> res = svc.getClient().getItem(getItemRequest);
        return new DynamoDBUserAttributesFuture(res);
    }

    @Override
    public void putAsync(String uid, UserAttributes userAttributes) {
        if (userAttributes != null && (userAttributes.getUserExperimentData().isChanged()
                        || userAttributes.getUserFrequencyCap().isChanged())) {
            Map<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>();
            itemValues.put(ATTRIBUTES_TIMESTAMP_FIELD, new AttributeValue().withS(
                            String.valueOf(BidderCalendar.getInstance().currentTimeMillis())));
            Map<String, String> experimentData =
                            userAttributes.getUserExperimentData().getExperimentData();
            if (experimentData != null && experimentData.size() > 0) {
                itemValues.put(ATTRIBUTES_EXPERIMENT_FIELD,
                                InternalUtils.toAttributeValue(experimentData));
            }

            Map<String, Set<String>> bidsHistory =
                            userAttributes.getUserFrequencyCap().getBidsHistory();
            if (bidsHistory != null && bidsHistory.size() > 0) {
                itemValues.put(ATTRIBUTES_BIDS_FIELD, InternalUtils.toAttributeValue(bidsHistory));
            }

            Map<String, Set<String>> impressionsHistory =
                            userAttributes.getUserFrequencyCap().getImpressionsHistory();
            if (impressionsHistory != null && impressionsHistory.size() > 0) {
                itemValues.put(ATTRIBUTES_IMPRESSIONS_FIELD,
                                InternalUtils.toAttributeValue(impressionsHistory));
            }

            itemValues.put(svc.getKeyField(), new AttributeValue(uid));
            PutItemRequest putItemRequest = new PutItemRequest().withTableName(svc.getTableName())
                            .withItem(itemValues);
            Future<PutItemResult> res = svc.getClient().putItem(putItemRequest);
        }
    }

    @Override
    public void updateImpressionsHistoryAsync(Ad ad, String uid) {
        long startTime = BidderCalendar.getInstance().currentTimeMillis();
        GetItemRequest getItemRequest = new GetItemRequest().withTableName(svc.getTableName())
                        .addKeyEntry(svc.getKeyField(), new AttributeValue(uid));

        svc.getClient().getItem(getItemRequest, new ResultHandler<GetItemResult>() {
            public void onSuccess(GetItemResult res) {

                UserAttributes userAttributes = getResultFromResponse(res, startTime);

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

            public void onError(Exception exception) {
                LogUtils.debug("Error updating Impression history for " + uid);
            }
        });

    }
}
