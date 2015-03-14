package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.nio.AmazonDynamoDBClient;
import com.enremmeta.rtb.api.UserAttributes;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamoDBDaoMapOfUserAttributes.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapOfUserAttributesSpec_getAsync {
    private DynamoDBService dynamoDBServiceMock;
    private AmazonDynamoDBClient amazonDynamoDBClientMock;
    private DynamoDBUserAttributesFuture dynamoDBUserAttributesFutureMock = Mockito.mock(DynamoDBUserAttributesFuture.class);
    private DynamoDBDaoMapOfUserAttributes dynamoDBDaoMapOfUserAttributes;

    @Before
    public void setUp() throws Exception {
        @SuppressWarnings("unchecked")
        Future<GetItemResult> futureMock = Mockito.mock(Future.class);
        
        amazonDynamoDBClientMock = Mockito.mock(AmazonDynamoDBClient.class);
        Mockito.when(amazonDynamoDBClientMock.getItem(any())).thenReturn((Future<GetItemResult>) futureMock);
        
        dynamoDBServiceMock = Mockito.mock(DynamoDBService.class);
        Mockito.when(dynamoDBServiceMock.getClient()).thenReturn(amazonDynamoDBClientMock);
        
        PowerMockito.whenNew(DynamoDBUserAttributesFuture.class).withArguments(futureMock).thenReturn(dynamoDBUserAttributesFutureMock);
        
        dynamoDBDaoMapOfUserAttributes = new DynamoDBDaoMapOfUserAttributes(dynamoDBServiceMock);
    }

    @Test
    public void returnsDynamoDBUserAttributesFuture() {
        Future<UserAttributes> result = dynamoDBDaoMapOfUserAttributes.getAsync("UserID"); /// act
        
        Mockito.verify(amazonDynamoDBClientMock).getItem(any());
        
        assertThat(result, equalTo(dynamoDBUserAttributesFutureMock));
    }
}
