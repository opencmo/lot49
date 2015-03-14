package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.enremmeta.rtb.caches.KnownFuture;

@Ignore("DynamoDBDaoFuture was removed")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoFutureSpec_get_long_TimeUnit {
    private DynamoDBService dynamoDBService = new DynamoDBService();
    private GetItemResult getItemResult = new GetItemResult();
    //private DynamoDBDaoFuture<String> dynamoDBDaoFuture;

    @Before
    public void setUp() throws Exception {
        Future<GetItemResult> future = new KnownFuture<GetItemResult>(getItemResult); 
                        
        //dynamoDBDaoFuture = new DynamoDBDaoFuture<String>(dynamoDBService, future, "Key");
    }

    @Test
    public void negativeFlow_returnsNullIfGetItemResultIsEmpty() throws Exception {
        //String result = dynamoDBDaoFuture.get(10, TimeUnit.SECONDS);
        
        //assertThat(result, equalTo(null));
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfGetItemResultContainsIt() throws Exception {
        String expectedResult = "Object attribute value";
        
        //getItemResult.addItemEntry(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue("java.lang.String"));
        //getItemResult.addItemEntry(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue(expectedResult));
        
        //String result = dynamoDBDaoFuture.get(10, TimeUnit.SECONDS);
        
        //assertThat(result, equalTo(expectedResult));
    }
}
