package com.enremmeta.rtb.dao.impl.dynamodb;

import static com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBDaoMapOfUserAttributes.ATTRIBUTES_BIDS_FIELD;
import static com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBDaoMapOfUserAttributes.ATTRIBUTES_EXPERIMENT_FIELD;
import static com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBDaoMapOfUserAttributes.ATTRIBUTES_IMPRESSIONS_FIELD;
import static com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBDaoMapOfUserAttributes.ATTRIBUTES_TIMESTAMP_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.nio.AmazonDynamoDBClient;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.UserFrequencyCapAttributes;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamoDBDaoMapOfUserAttributes.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapOfUserAttributesSpec_putAsync {
    private DynamoDBService dynamoDBServiceMock;
    private AmazonDynamoDBClient amazonDynamoDBClientMock;
    private DynamoDBDaoMapOfUserAttributes dynamoDBDaoMapOfUserAttributes;
    
    private String uid = "UserID";
    private String key = "Key";
    private String value = "Value";

    @Before
    public void setUp() throws Exception {
        amazonDynamoDBClientMock = Mockito.mock(AmazonDynamoDBClient.class);
        
        dynamoDBServiceMock = Mockito.mock(DynamoDBService.class);
        Mockito.when(dynamoDBServiceMock.getClient()).thenReturn(amazonDynamoDBClientMock);
        
        dynamoDBDaoMapOfUserAttributes = new DynamoDBDaoMapOfUserAttributes(dynamoDBServiceMock);
    }

    @Test
    public void negativeFlow_doesNotPutItemIfUserAttributesIsNull() {
        UserAttributes userAttributes = null;
        
        dynamoDBDaoMapOfUserAttributes.putAsync(uid, userAttributes); /// act
        
        Mockito.verify(amazonDynamoDBClientMock, never()).putItem(any());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void positiveFlow_putsItemIfUserAttributesContainesChanhedData() throws Exception {
        String tableName = "TableName";
        String keyField = "KeyField";
        Mockito.when(dynamoDBServiceMock.getTableName()).thenReturn(tableName);
        Mockito.when(dynamoDBServiceMock.getKeyField()).thenReturn(keyField);

        PutItemRequest putItemRequestMock = Mockito.mock(PutItemRequest.class);
        Mockito.when(putItemRequestMock.withTableName(tableName)).thenReturn(putItemRequestMock);
        Mockito.when(putItemRequestMock.withItem(any())).thenReturn(putItemRequestMock);
        
        PowerMockito.whenNew(PutItemRequest.class).withNoArguments().thenReturn(putItemRequestMock);
        
        UserAttributes userAttributes = createAndFillUserAttributes();
        
        dynamoDBDaoMapOfUserAttributes.putAsync(uid, userAttributes); /// act
        
        Mockito.verify(putItemRequestMock).withTableName(tableName);
        
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(putItemRequestMock).withItem(mapCaptor.capture());
        
        Map<String, AttributeValue> itemValues = mapCaptor.getValue();
        assertThat(itemValues.get(ATTRIBUTES_TIMESTAMP_FIELD), not(equalTo(null)));
        assertThat(InternalUtils.toSimpleMapValue(itemValues.get(ATTRIBUTES_EXPERIMENT_FIELD).getM()).get(key), equalTo(value));
        assertThat(((Set)InternalUtils.toSimpleMapValue(itemValues.get(ATTRIBUTES_BIDS_FIELD).getM()).get(key)).contains(value), is(true));
        assertThat(((Set)InternalUtils.toSimpleMapValue(itemValues.get(ATTRIBUTES_IMPRESSIONS_FIELD).getM()).get(key)).contains(value), is(true));
        assertThat(itemValues.get(keyField).getS(), equalTo(uid));

        Mockito.verify(amazonDynamoDBClientMock).putItem(any());
    }

    private UserAttributes createAndFillUserAttributes() {
        Set<String> hashSet = new HashSet<String>();
        hashSet.add(value);
        
        Map<String, String> experimentMap = new HashMap<String, String>();
        experimentMap.put(key, value);

        Map<String, Set<String>> bidsHistory = new HashMap<String, Set<String>>();
        bidsHistory.put(key, hashSet);

        Map<String, Set<String>> impressionsHistory = new HashMap<String, Set<String>>();
        impressionsHistory.put(key, hashSet);
        
        UserExperimentAttributes experimentAttributes = new UserExperimentAttributes(experimentMap);
        UserFrequencyCapAttributes frequencyCapAttributes = new UserFrequencyCapAttributes(bidsHistory, impressionsHistory);
        frequencyCapAttributes.setChanged(true);
        
        return new UserAttributes(experimentAttributes, frequencyCapAttributes);
    }
}
