package com.enremmeta.rtb.dao.impl.dynamodb;

import static com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBDaoMapOfUserAttributes.ATTRIBUTES_BIDS_FIELD;
import static com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBDaoMapOfUserAttributes.ATTRIBUTES_EXPERIMENT_FIELD;
import static com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBDaoMapOfUserAttributes.ATTRIBUTES_IMPRESSIONS_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.enremmeta.rtb.api.UserAttributes;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapOfUserAttributesSpec_getResultFromResponse {
    private GetItemResult getItemResultMock;
    private Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();

    @Before
    public void setUp() throws Exception {
        getItemResultMock = Mockito.mock(GetItemResult.class);
        Mockito.when(getItemResultMock.getItem()).thenReturn(attributes);
    }

    @Test
    public void positiveFlow_returnsEmptyMapsIfParameterDoesNotContainAttributes() {
        UserAttributes result = DynamoDBDaoMapOfUserAttributes.getResultFromResponse(getItemResultMock, 0); /// act
        
        Map<String, String> experimentData = result.getUserExperimentData().getExperimentData();
        Map<String, Set<String>> bidsHistory = result.getUserFrequencyCap().getBidsHistory();
        Map<String, Set<String>> impressionsHistory = result.getUserFrequencyCap().getImpressionsHistory();
        
        assertThat(experimentData.size(), equalTo(0));
        assertThat(bidsHistory.size(), equalTo(0));
        assertThat(impressionsHistory.size(), equalTo(0));
    }

    @Test
    public void positiveFlow_returnsExpectedResultsIfParameterContainsAttributes() {
        String key = "Key";
        String value = "Value";
        
        Set<String> hashSet = new HashSet<String>();
        hashSet.add(value);
        
        Map<String, String> expectedExperimentData = new HashMap<String, String>();
        expectedExperimentData.put(key, value);
        attributes.put(ATTRIBUTES_EXPERIMENT_FIELD, InternalUtils.toAttributeValue(expectedExperimentData));

        Map<String, Set<String>> expectedBidsHistory = new HashMap<String, Set<String>>();
        expectedBidsHistory.put(key, hashSet);
        attributes.put(ATTRIBUTES_BIDS_FIELD, InternalUtils.toAttributeValue(expectedBidsHistory));

        Map<String, Set<String>> expectedImpressionsHistory = new HashMap<String, Set<String>>();
        expectedImpressionsHistory.put(key, hashSet);
        attributes.put(ATTRIBUTES_IMPRESSIONS_FIELD, InternalUtils.toAttributeValue(expectedImpressionsHistory));
        
        UserAttributes result = DynamoDBDaoMapOfUserAttributes.getResultFromResponse(getItemResultMock, 0); /// act
        
        Map<String, String> experimentData = result.getUserExperimentData().getExperimentData();
        Map<String, Set<String>> bidsHistory = result.getUserFrequencyCap().getBidsHistory();
        Map<String, Set<String>> impressionsHistory = result.getUserFrequencyCap().getImpressionsHistory();
        
        assertThat(experimentData.size(), equalTo(1));
        assertThat(bidsHistory.size(), equalTo(1));
        assertThat(impressionsHistory.size(), equalTo(1));
        
        assertThat(experimentData.get(key), equalTo(value));
        assertThat(bidsHistory.get(key).contains(value), is(true));
        assertThat(impressionsHistory.get(key).contains(value), is(true));
    }
}
