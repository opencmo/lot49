package com.enremmeta.rtb.dao.impl.dynamodb;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@Ignore("DynamoDBDaoMap was removed")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapSpec_putAsync {
    @Test
    public void callsPut() {
        String keyValue = "KeyValue";
        String objectAttributeValue = "ObjectAttributeValue";

        //@SuppressWarnings("unchecked")
        //DynamoDBDaoMap<String> dynamoDBDaoMap = Mockito.mock(DynamoDBDaoMap.class, Mockito.CALLS_REAL_METHODS);
        //Mockito.doNothing().when(dynamoDBDaoMap).put(keyValue, objectAttributeValue);

        //dynamoDBDaoMap.putAsync(keyValue, objectAttributeValue);
        
        //Mockito.verify(dynamoDBDaoMap).put(keyValue, objectAttributeValue);
    }
}
