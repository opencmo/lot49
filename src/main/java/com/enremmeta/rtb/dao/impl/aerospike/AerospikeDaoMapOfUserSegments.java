package com.enremmeta.rtb.dao.impl.aerospike;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;

/**
 * Dao for access to {@link UserSegments} in Aerospike
 *
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class AerospikeDaoMapOfUserSegments implements DaoMapOfUserSegments {

    private final AerospikeDBService svc;

    public static final String ATTRIBUTES_SEGMENTS_FIELD = "data";

    public static UserSegments recordToUserSegments(Record record) {
        Map<String, Map<String, String>> segmentsMap;

        if (record != null && record.getValue(ATTRIBUTES_SEGMENTS_FIELD) != null) {
            segmentsMap = (Map<String, Map<String, String>>) record
                            .getValue(ATTRIBUTES_SEGMENTS_FIELD);
        } else {
            segmentsMap = new HashMap<String, Map<String, String>>();
        }
        return new UserSegments(segmentsMap);
    }

    public AerospikeDaoMapOfUserSegments(AerospikeDBService svc) {
        super();
        this.svc = svc;
    }

    @Override
    public Future<UserSegments> getAsync(String uid) {
        AerospikeUserSegmentsFuture future = new AerospikeUserSegmentsFuture();
        svc.getClient().get(null, new AerospikeUserSegmentsReadHandler(future),
                        new Key(svc.getConfig().getNamespace(), svc.getConfig().getSet(), uid));
        return future;
    }

}
