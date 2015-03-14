package com.enremmeta.rtb.dao.impl.aerospike;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.async.AsyncClient;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.WritePolicy;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.UserFrequencyCapAttributes;
import com.enremmeta.rtb.config.AerospikeDBServiceConfig;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AerospikeDaoMapOfUserAttributes.class, AsyncClient.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AerospikeDaoMapOfUserAttributesSpec {

    @Test
    public void testPutZeroBins() {
        
        AsyncClient ac = PowerMockito.mock(AsyncClient.class);
        Mockito.doNothing().when(ac).put(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyVararg());
        
        AerospikeDaoMapOfUserAttributes aDMUAttribs = new AerospikeDaoMapOfUserAttributes(new AerospikeDBService(){{
          setConfig(new AerospikeDBServiceConfig(){{
              setNamespace("TEST_NAMESPACE");
          }});
          setClient(ac);
        }});
        UserAttributes ua = Mockito.mock(UserAttributes.class);
        UserExperimentAttributes uea = Mockito.mock(UserExperimentAttributes.class);
        Mockito.when(uea.isChanged()).thenReturn(true);
        Mockito.when(ua.getUserExperimentData()).thenReturn(uea);
        Mockito.when(ua.getUserFrequencyCap()).thenReturn(Mockito.mock(UserFrequencyCapAttributes.class));
        aDMUAttribs.putAsync("TEST_UID", ua);
        
        ArgumentCaptor<Bin> varArgs = ArgumentCaptor.forClass(Bin.class);
        Mockito.verify(ac, Mockito.times(1)).put(Mockito.any(WritePolicy.class), Mockito.any(WriteListener.class), Mockito.any(), varArgs.capture());
        
        assertEquals(0L, varArgs.getAllValues().size());
    }
    
    @Test
    public void testPut_ATTRIBUTES_EXPERIMENT_BIN_Bin() {
        
        AsyncClient ac = PowerMockito.mock(AsyncClient.class);
        Mockito.doNothing().when(ac).put(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyVararg());
        
        AerospikeDaoMapOfUserAttributes aDMUAttribs = new AerospikeDaoMapOfUserAttributes(new AerospikeDBService(){{
          setConfig(new AerospikeDBServiceConfig(){{
              setNamespace("TEST_NAMESPACE");
          }});
          setClient(ac);
        }});
        UserAttributes ua = Mockito.mock(UserAttributes.class);
        UserExperimentAttributes uea = Mockito.mock(UserExperimentAttributes.class);
        Mockito.when(uea.getExperimentData()).thenReturn(new HashMap<String, String>(){{
          put("TEST_KEY", "TEST_VALUE");  
        }});
        
        Mockito.when(uea.isChanged()).thenReturn(true);
        Mockito.when(ua.getUserExperimentData()).thenReturn(uea);
        Mockito.when(ua.getUserFrequencyCap()).thenReturn(Mockito.mock(UserFrequencyCapAttributes.class));
        aDMUAttribs.putAsync("TEST_UID", ua);
        
        ArgumentCaptor<Bin> varArgs = ArgumentCaptor.forClass(Bin.class);
        Mockito.verify(ac, Mockito.times(1)).put(Mockito.any(WritePolicy.class), Mockito.any(WriteListener.class), Mockito.any(), varArgs.capture());
        
        assertEquals(1L, varArgs.getAllValues().size());
        assertEquals("{TEST_KEY=TEST_VALUE}", varArgs.getAllValues().get(0).value.toString());
    }
    
    @Test
    public void testPut_ATTRIBUTES_BIDS_BIN_Bin() {
        
        AsyncClient ac = PowerMockito.mock(AsyncClient.class);
        Mockito.doNothing().when(ac).put(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyVararg());
        
        AerospikeDaoMapOfUserAttributes aDMUAttribs = new AerospikeDaoMapOfUserAttributes(new AerospikeDBService(){{
          setConfig(new AerospikeDBServiceConfig(){{
              setNamespace("TEST_NAMESPACE");
          }});
          setClient(ac);
        }});
        UserAttributes ua = Mockito.mock(UserAttributes.class);
        UserExperimentAttributes uea = Mockito.mock(UserExperimentAttributes.class);
        
        UserFrequencyCapAttributes ufca = Mockito.mock(UserFrequencyCapAttributes.class);
        Mockito.when(ufca.getBidsHistory()).thenReturn(new HashMap<String, Set<String>>(){{
          put("TEST_KEY", new HashSet<String>(){{
              add("TEST_VALUE");
          }});  
        }});
        
        Mockito.when(uea.isChanged()).thenReturn(true);
        Mockito.when(ua.getUserExperimentData()).thenReturn(uea);
        Mockito.when(ua.getUserFrequencyCap()).thenReturn(ufca);
        aDMUAttribs.putAsync("TEST_UID", ua);
        
        ArgumentCaptor<Bin> varArgs = ArgumentCaptor.forClass(Bin.class);
        Mockito.verify(ac, Mockito.times(1)).put(Mockito.any(WritePolicy.class), Mockito.any(WriteListener.class), Mockito.any(), varArgs.capture());
        
        assertEquals(1L, varArgs.getAllValues().size());
        assertEquals("{TEST_KEY=[TEST_VALUE]}", varArgs.getAllValues().get(0).value.toString());
        
    }
    
    @Test
    public void testPut_ATTRIBUTES_IMPRESSIONS_BIN_Bin() {
        
        AsyncClient ac = PowerMockito.mock(AsyncClient.class);
        Mockito.doNothing().when(ac).put(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyVararg());
        
        AerospikeDaoMapOfUserAttributes aDMUAttribs = new AerospikeDaoMapOfUserAttributes(new AerospikeDBService(){{
          setConfig(new AerospikeDBServiceConfig(){{
              setNamespace("TEST_NAMESPACE");
          }});
          setClient(ac);
        }});
        UserAttributes ua = Mockito.mock(UserAttributes.class);
        UserExperimentAttributes uea = Mockito.mock(UserExperimentAttributes.class);
        
        UserFrequencyCapAttributes ufca = Mockito.mock(UserFrequencyCapAttributes.class);
        Mockito.when(ufca.getImpressionsHistory()).thenReturn(new HashMap<String, Set<String>>(){{
          put("TEST_KEY", new HashSet<String>(){{
              add("TEST_VALUE");
          }});  
        }});
        
        Mockito.when(uea.isChanged()).thenReturn(true);
        Mockito.when(ua.getUserExperimentData()).thenReturn(uea);
        Mockito.when(ua.getUserFrequencyCap()).thenReturn(ufca);
        aDMUAttribs.putAsync("TEST_UID", ua);
        
        ArgumentCaptor<Bin> varArgs = ArgumentCaptor.forClass(Bin.class);
        Mockito.verify(ac, Mockito.times(1)).put(Mockito.any(WritePolicy.class), Mockito.any(WriteListener.class), Mockito.any(), varArgs.capture());
        
        assertEquals(1L, varArgs.getAllValues().size());
        assertEquals("{TEST_KEY=[TEST_VALUE]}", varArgs.getAllValues().get(0).value.toString());
    }

    @Test
    public void recordToUserAttributes_ATTRIBUTES_EXPERIMENT_BIN() {
        
        Record record = new Record(new HashMap<String, Object>(){{
            put("experiment", new HashMap<String, String>(){{
                put("TEST_KEY", "TEST_VALUE");
            }});
        }}, 0, 0);
        
        assertEquals("{TEST_KEY=TEST_VALUE}", AerospikeDaoMapOfUserAttributes.recordToUserAttributes(record).getUserExperimentData().getExperimentData().toString());
        
    
    }
    
    @Test
    public void recordToUserAttributes_ATTRIBUTES_BIDS_BIN() {
        
        Record record = new Record(new HashMap<String, Object>(){{
            put("bids", new HashMap<String, Set<String>>(){{
                put("TEST_KEY", new HashSet<String>(){{
                    add("TEST_VALUE");
                }});  
              }});
        }}, 0, 0);
        
        assertEquals("{TEST_KEY=[TEST_VALUE]}", AerospikeDaoMapOfUserAttributes.recordToUserAttributes(record).getUserFrequencyCap().getBidsHistory().toString());
        
    }
    
    @Test
    public void recordToUserAttributes_ATTRIBUTES_IMPRESSIONS_BIN() {
        
        Record record = new Record(new HashMap<String, Object>(){{
            put("impressions", new HashMap<String, Set<String>>(){{
                put("TEST_KEY", new HashSet<String>(){{
                    add("TEST_VALUE");
                }});  
              }});
        }}, 0, 0);
        
        assertEquals("{TEST_KEY=[TEST_VALUE]}", AerospikeDaoMapOfUserAttributes.recordToUserAttributes(record).getUserFrequencyCap().getImpressionsHistory().toString());
        
    }
    
    @Test
    public void getAsync() {
        

        AsyncClient ac = PowerMockito.mock(AsyncClient.class);
        Mockito.doNothing().when(ac).put(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyVararg());
        
        AerospikeDaoMapOfUserAttributes aDMUAttribs = new AerospikeDaoMapOfUserAttributes(new AerospikeDBService(){{
          setConfig(new AerospikeDBServiceConfig(){{
              setNamespace("TEST_NAMESPACE");
          }});
          setClient(ac);
        }});
        
        assertEquals("com.enremmeta.rtb.dao.impl.aerospike.AerospikeUserAttributesFuture", aDMUAttribs.getAsync("TEST_UID").getClass().getCanonicalName());
        
    }
    
    @Test
    public void updateImpressionsHistoryAsync() throws Lot49Exception {
        

        AsyncClient ac = PowerMockito.mock(AsyncClient.class);
        Mockito.doNothing().when(ac).get(Mockito.any(), Mockito.any(), Mockito.any(Key.class));
        
        AerospikeDaoMapOfUserAttributes aDMUAttribs = new AerospikeDaoMapOfUserAttributes(new AerospikeDBService(){{
          setConfig(new AerospikeDBServiceConfig(){{
              setNamespace("TEST_NAMESPACE");
          }});
          setClient(ac);
        }});
        
        aDMUAttribs.updateImpressionsHistoryAsync(Mockito.mock(Ad.class), "TEST_UID");
        
        Mockito.verify(ac, Mockito.times(1)).get(Mockito.any(), Mockito.any(), Mockito.any(Key.class));
        
    }
}
