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

@Ignore("Method DynamoDBService.getDaoMap(Class) was removed")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBServiceSpec_getDaoMap_Class {
    // should return not null DaoMap object
    
    private DynamoDBService dynamoDBService;
    
    @Before
    public void setUp() throws Exception {
        dynamoDBService = new DynamoDBService();
    }

    @Test
    public void positiveFlow_createsNewDaoMapIfItDoesNotExist() {
        Map<Class<?>, DaoMap<? extends Object>> mapOfMaps = Whitebox.getInternalState(dynamoDBService, "mapOfMaps");
        assertThat(mapOfMaps.get(String.class), equalTo(null));
        
//        DaoMap<String> daoMap = dynamoDBService.getDaoMap(String.class);
        
//        assertThat(daoMap, not(equalTo(null)));
//        assertThat(daoMap instanceof DynamoDBDaoMap, is(true));
//        
//        assertThat(mapOfMaps.get(String.class), equalTo(daoMap));
    }

    @Test
    public void positiveFlow_returnsExistingDaoMapIfItExists() {
        Map<Class<?>, DaoMap<? extends Object>> mapOfMaps = Whitebox.getInternalState(dynamoDBService, "mapOfMaps");
        assertThat(mapOfMaps.get(String.class), equalTo(null));
        
//        DynamoDBDaoMap<String> dynamoDBDaoMap = new DynamoDBDaoMap<String>(dynamoDBService);
//        mapOfMaps.put(String.class, dynamoDBDaoMap);
        
//        DaoMap<String> daoMap = dynamoDBService.getDaoMap(String.class);
        
//        assertThat(daoMap, equalTo(dynamoDBDaoMap));
        
//        assertThat(mapOfMaps.get(String.class), equalTo(daoMap));
    }

    @Test
    public void positiveFlow_createsDifferentDaoMapsForDifferentTypes() {
        Map<Class<?>, DaoMap<? extends Object>> mapOfMaps = Whitebox.getInternalState(dynamoDBService, "mapOfMaps");
        assertThat(mapOfMaps.size(), is(0));
        
//        DaoMap<String> daoMapString = dynamoDBService.getDaoMap(String.class);
//        DaoMap<Integer> daoMapInteger = dynamoDBService.getDaoMap(Integer.class);
        
//        assertThat(daoMapString, not(equalTo(null)));
//        assertThat(daoMapInteger, not(equalTo(null)));
//        assertThat(daoMapString, not(equalTo(daoMapInteger)));
        
//        assertThat(mapOfMaps.get(String.class), equalTo(daoMapString));
//        assertThat(mapOfMaps.get(Integer.class), equalTo(daoMapInteger));
        assertThat(mapOfMaps.size(), is(2));
    }
}
