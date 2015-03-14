package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.nio.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.nio.handlers.ResultHandler;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.UserAttributes;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapOfUserAttributesSpec_updateImpressionsHistoryAsync {
    private DynamoDBService dynamoDBServiceMock;
    private AmazonDynamoDBClient amazonDynamoDBClientMock;
    private DynamoDBDaoMapOfUserAttributes dynamoDBDaoMapOfUserAttributesSpy;
    
    private Ad ad;
    private String uid = "UserID";

    @Before
    public void setUp() throws Exception {
        amazonDynamoDBClientMock = Mockito.mock(AmazonDynamoDBClient.class);
        
        dynamoDBServiceMock = Mockito.mock(DynamoDBService.class);
        Mockito.when(dynamoDBServiceMock.getClient()).thenReturn(amazonDynamoDBClientMock);
        
        DynamoDBDaoMapOfUserAttributes dynamoDBDaoMapOfUserAttributes = new DynamoDBDaoMapOfUserAttributes(dynamoDBServiceMock);
        dynamoDBDaoMapOfUserAttributesSpy = Mockito.spy(dynamoDBDaoMapOfUserAttributes);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void negativeFlow_doesNotCallPutAsyncIfGetItemAsyncHasErrors() {
        Mockito.doAnswer((InvocationOnMock invocation) -> {
            ResultHandler<GetItemResult> asyncHandler = (ResultHandler<GetItemResult>) invocation.getArgumentAt(1, AsyncHandler.class);
            asyncHandler.onError(new RuntimeException("getItemAsync() error")); /// on error
            return null;
        }).when(amazonDynamoDBClientMock).getItem(any(GetItemRequest.class), any());
        
        PowerMockito.mockStatic(LogUtils.class);
        
        dynamoDBDaoMapOfUserAttributesSpy.updateImpressionsHistoryAsync(ad, uid); /// act
        
        Mockito.verify(dynamoDBDaoMapOfUserAttributesSpy, never()).putAsync(any(String.class), any(UserAttributes.class));
        
        PowerMockito.verifyStatic();
        LogUtils.debug("Error updating Impression history for " + uid);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void positiveFlow_callsPutAsyncIfGetItemAsyncIsSuccessful() {
        Mockito.doAnswer((InvocationOnMock invocation) -> {
            ResultHandler<GetItemResult> asyncHandler = (ResultHandler<GetItemResult>) invocation.getArgumentAt(1, AsyncHandler.class);
            GetItemRequest getItemRequest = invocation.getArgumentAt(0, GetItemRequest.class);
            GetItemResult getItemResult = Mockito.mock(GetItemResult.class);
            asyncHandler.onSuccess(getItemResult); /// on success
            return Mockito.mock(Future.class);
        }).when(amazonDynamoDBClientMock).getItem(any(GetItemRequest.class), any());
        
        ad = Mockito.mock(Ad.class);
        Mockito.when(ad.isCampaignFrequencyCap()).thenReturn(true);
        Mockito.when(ad.isStrategyFrequencyCap()).thenReturn(true);
        Mockito.when(ad.getId()).thenReturn("AdId");
        Mockito.when(ad.getCampaignId()).thenReturn("CampaignId");
        
        dynamoDBDaoMapOfUserAttributesSpy.updateImpressionsHistoryAsync(ad, uid); /// act
        
        Mockito.verify(dynamoDBDaoMapOfUserAttributesSpy).putAsync(eq(uid), isA(UserAttributes.class));
    }
}
