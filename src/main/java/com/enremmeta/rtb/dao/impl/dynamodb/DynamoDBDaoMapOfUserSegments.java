package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;
import com.enremmeta.util.BidderCalendar;

/**
 * DAO for access to {@link UserSegments} in DynamoDB
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 * 
 */

public class DynamoDBDaoMapOfUserSegments implements DaoMapOfUserSegments {

    public static final String ATTRIBUTES_SEGMENTS_FIELD = "data";

    private final DynamoDBService svc;

    public static UserSegments getResultFromResponse(GetItemResult res, long startTime) {
        Map<String, AttributeValue> attributes = res.getItem();

        Map<String, Map<String, String>> segmentsMap;
        if (attributes != null && attributes.get(ATTRIBUTES_SEGMENTS_FIELD) != null) {
            segmentsMap = InternalUtils
                            .toSimpleMapValue(attributes.get(ATTRIBUTES_SEGMENTS_FIELD).getM());
        } else {
            segmentsMap = new HashMap<String, Map<String, String>>();
        }
        long endTime = BidderCalendar.getInstance().currentTimeMillis();
        LogUtils.debug("Got UserSegments from DynamoDB in " + (endTime - startTime) + " ms.");
        return new UserSegments(segmentsMap);

    }

    public DynamoDBDaoMapOfUserSegments(DynamoDBService svc) {
        super();
        this.svc = svc;
    }

    @Override
    public Future<UserSegments> getAsync(String uid) {
        GetItemRequest getItemRequest = new GetItemRequest().withTableName(svc.getTableName())
                        .addKeyEntry(svc.getKeyField(), new AttributeValue(uid));
        Future<GetItemResult> res = svc.getClient().getItem(getItemRequest);
        return new DynamoDBUserSegmentsFuture(res);
    }

}
