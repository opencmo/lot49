package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBServiceSpec_getDaoMapOfUserAttributes {
    // should return not null DaoMapOfUserAttributes object
    
    private DynamoDBService dynamoDBService;
    
    @Before
    public void setUp() throws Exception {
        dynamoDBService = new DynamoDBService();
    }

    @Test
    public void positiveFlow_returnsNotNullObject () {
        DaoMapOfUserAttributes daoMapOfUserAttributes = dynamoDBService.getDaoMapOfUserAttributes();
        
        assertThat(daoMapOfUserAttributes, not(equalTo(null)));
        assertThat(daoMapOfUserAttributes instanceof DynamoDBDaoMapOfUserAttributes, is(true));
    }
}
