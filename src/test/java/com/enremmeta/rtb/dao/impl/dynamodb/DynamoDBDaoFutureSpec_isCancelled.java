package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.enremmeta.rtb.caches.KnownFuture;

@Ignore("DynamoDBDaoFuture was removed")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoFutureSpec_isCancelled {
    private DynamoDBService dynamoDBService = new DynamoDBService();
    //private DynamoDBDaoFuture<String> dynamoDBDaoFuture;
    
    @Test
    public void returnsExpectedValue() {
        boolean expectedResult = true;
        
        @SuppressWarnings("unchecked")
        Future<GetItemResult> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.isCancelled()).thenReturn(expectedResult);
        //dynamoDBDaoFuture = new DynamoDBDaoFuture<String>(dynamoDBService, futureMock, "Key");
        
        //boolean result = dynamoDBDaoFuture.isCancelled();
        
        Mockito.verify(futureMock).isCancelled();
        //assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void returnsFalseForKnownFuture() {
        Future<GetItemResult> knownFuture = new KnownFuture<GetItemResult>(null);
        //dynamoDBDaoFuture = new DynamoDBDaoFuture<String>(dynamoDBService, knownFuture, "Key");
        
        //boolean result = dynamoDBDaoFuture.isCancelled();
        
        //assertThat(result, equalTo(false));
    }
}
