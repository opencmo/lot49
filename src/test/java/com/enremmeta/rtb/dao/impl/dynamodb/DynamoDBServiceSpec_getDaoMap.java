package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.dao.DaoMap;

@Ignore("Method DynamoDBService.getDaoMap() was removed")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBServiceSpec_getDaoMap {
    // should return the same DaoMap object for different types - is it correct ?
    
    private DynamoDBService dynamoDBService;
    
    @Before
    public void setUp() throws Exception {
        dynamoDBService = new DynamoDBService();
    }

    @Test
    public void positiveFlow_returnsTheSameDaoMapForDifferentTypes() {
        Map<Class<?>, DaoMap<? extends Object>> mapOfMaps = Whitebox.getInternalState(dynamoDBService, "mapOfMaps");
        assertThat(mapOfMaps.get(Object.class), equalTo(null));
        assertThat(mapOfMaps.size(), is(0));
        
//        DaoMap<String> daoMapString = dynamoDBService.<String>getDaoMap();
//        DaoMap<Integer> daoMapInteger = dynamoDBService.<Integer>getDaoMap();
        
//        assertThat(daoMapString, not(equalTo(null)));
//        assertThat(daoMapString instanceof DynamoDBDaoMap, is(true));
//        assertThat(daoMapString, equalTo(daoMapInteger));
//        
//        assertThat(mapOfMaps.get(Object.class), equalTo(daoMapString));
        assertThat(mapOfMaps.size(), is(1));
    }
}
