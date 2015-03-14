package com.enremmeta.rtb.dao.impl.hazelcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.TagDescription;
import com.enremmeta.rtb.AwsOrchestrator;
import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.HazelcastServiceConfig;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.instance.HazelcastInstanceImpl;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class, Hazelcast.class, AwsOrchestrator.class, Utils.class, AmazonEC2Client.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*", "javax.net.ssl.*"})
public class HazelcastServiceSpec {
    private ServiceRunner serviceRunnerMock;
    private Orchestrator orch;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        orch = new LocalOrchestrator(new OrchestratorConfig(){{
            setDeployType(LocalOrchestrator.DEPLOY_TYPE);
        }});
        
        Mockito.when(serviceRunnerMock.getOrchestrator()).thenReturn(orch);
    }

    @Test
    public void positiveFlow_constructor_sholudSetIdGenerator() throws Lot49Exception {

        HazelcastServiceConfig hcsConfig = new HazelcastServiceConfig();
        
        IdGenerator idGenMock = Mockito.mock(IdGenerator.class);
        Mockito.when(idGenMock.toString()).thenReturn("TEST_ID_GEN");
        
        HazelcastInstance hcInstanceMock = Mockito.mock(HazelcastInstanceImpl.class);
        Mockito.when(hcInstanceMock.getIdGenerator(anyString())).thenReturn(idGenMock);
        
        PowerMockito.mockStatic(Hazelcast.class);
        PowerMockito.doReturn(hcInstanceMock).when(Hazelcast.class);
        Hazelcast.newHazelcastInstance(any());
        
        
        HazelcastService hcs = new HazelcastService(serviceRunnerMock, hcsConfig);
        
        assertEquals("TEST_ID_GEN", Whitebox.getInternalState(hcs, "idGenerator").toString());
    }
    
    @Test
    public void positiveFlow_constructorNoInit_sholudNotSetIdGenerator2() throws Lot49Exception {

        HazelcastServiceConfig hcsConfig = new HazelcastServiceConfig();
        
        IdGenerator idGenMock = Mockito.mock(IdGenerator.class);
        Mockito.when(idGenMock.toString()).thenReturn("TEST_ID_GEN");
        
        HazelcastInstance hcInstanceMock = Mockito.mock(HazelcastInstanceImpl.class);
        Mockito.when(hcInstanceMock.getIdGenerator(anyString())).thenReturn(idGenMock);
        
        PowerMockito.mockStatic(Hazelcast.class);
        PowerMockito.doReturn(hcInstanceMock).when(Hazelcast.class);
        Hazelcast.newHazelcastInstance(any());
        
        
        HazelcastService hcs = new HazelcastService(serviceRunnerMock, hcsConfig, true);
        
        assertNull(Whitebox.getInternalState(hcs, "idGenerator"));
    }
    
    @Test
    public void negativeFlow_constructor_AwsOrchestrator_ENV_EC2_ENDPOINTShouldBeSet() throws Lot49Exception, IOException {

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.doReturn("TEST_NODE_ID").when(Utils.class);
        Utils.readUrl(anyString());
        
        try{
            orch = new AwsOrchestrator(new OrchestratorConfig(){{
                setDeployType(AwsOrchestrator.DEPLOY_TYPE);
            }});
            fail("should throw exception");
        }catch(Lot49Exception e){
            assertEquals("Expected environment variable LOT49_EC2_ENDPOINT to be set.", e.getMessage());
        }
        
        //TODO: move to separate spec for AwsOrchestrator
    }

    @Test
    public void negativeFlow_constructor_AwsOrchestrator_ENV_ELB_ENDPOINTShouldBeSet() throws Lot49Exception, IOException {

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.doReturn("TEST_NODE_ID").when(Utils.class);
        Utils.readUrl(anyString());
        
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv())
                        .thenReturn(new HashMap<String, String>(){{
                            put(KVKeysValues.ENV_EC2_ENDPOINT, "TEST_ENV_EC2_ENDPOINT");
                        }});
       
        try{
            orch = new AwsOrchestrator(new OrchestratorConfig(){{
                setDeployType(AwsOrchestrator.DEPLOY_TYPE);
            }});
            fail("should throw exception");
        }catch(Lot49Exception e){
            assertEquals("Expected environment variable LOT49_ELB_ENDPOINT to be set.", e.getMessage());
        }
        
        //TODO: move to separate spec for AwsOrchestrator
        
        HazelcastServiceConfig hcsConfig = new HazelcastServiceConfig();
        
        IdGenerator idGenMock = Mockito.mock(IdGenerator.class);
        Mockito.when(idGenMock.toString()).thenReturn("TEST_ID_GEN");
        
        HazelcastInstance hcInstanceMock = Mockito.mock(HazelcastInstanceImpl.class);
        Mockito.when(hcInstanceMock.getIdGenerator(anyString())).thenReturn(idGenMock);
        
        PowerMockito.mockStatic(Hazelcast.class);
        PowerMockito.doReturn(hcInstanceMock).when(Hazelcast.class);
        Hazelcast.newHazelcastInstance(any());
        
        HazelcastService hcs = new HazelcastService(serviceRunnerMock, hcsConfig);

        
        
    }
    
    @Test
    public void negativeFlow_constructor_awsRegionRequired() throws Exception {

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.doReturn("TEST_NODE_ID").when(Utils.class);
        Utils.readUrl(anyString());
        
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv())
                        .thenReturn(new HashMap<String, String>(){{
                            put(KVKeysValues.ENV_EC2_ENDPOINT, "TEST_ENV_EC2_ENDPOINT");
                            put(KVKeysValues.ENV_ELB_ENDPOINT, "TEST_ENV_ELB_ENDPOINT");
                        }});
        
        DescribeTagsResult dtrMock = Mockito.mock(DescribeTagsResult.class);
        Mockito.when(dtrMock.getTags()).thenReturn(new LinkedList<TagDescription>(){{
            add(new TagDescription(){{
                setKey("TEST_KEY");
                setValue("TEST_ELB");
            }});
        }});
        AmazonEC2Client aec2ClientMock = Mockito.mock(AmazonEC2Client.class);
        Mockito.when(aec2ClientMock.describeTags(any())).thenReturn(dtrMock);
        
        PowerMockito.whenNew(AmazonEC2Client.class).withAnyArguments()
            .thenReturn(aec2ClientMock);
       
        orch = new AwsOrchestrator(new OrchestratorConfig(){{
            setDeployType(AwsOrchestrator.DEPLOY_TYPE);
            setTagName("TEST_KEY");
            setLot49ElbName("TEST_ELB");
        }});

        Mockito.when(serviceRunnerMock.getOrchestrator()).thenReturn(orch);
        
        HazelcastServiceConfig hcsConfig = new HazelcastServiceConfig();
        
        IdGenerator idGenMock = Mockito.mock(IdGenerator.class);
        Mockito.when(idGenMock.toString()).thenReturn("TEST_ID_GEN");
        
        HazelcastInstance hcInstanceMock = Mockito.mock(HazelcastInstanceImpl.class);
        Mockito.when(hcInstanceMock.getIdGenerator(anyString())).thenReturn(idGenMock);
        
        PowerMockito.mockStatic(Hazelcast.class);
        PowerMockito.doReturn(hcInstanceMock).when(Hazelcast.class);
        Hazelcast.newHazelcastInstance(any());
        
        HazelcastService hcs = null;
        
        try{
            hcs = new HazelcastService(serviceRunnerMock, hcsConfig);
        }catch(Lot49Exception e){
            assertEquals("Hazelcast configuration: awsRegion required.", e.getMessage());
        }

    }
    
    @Test
    public void positiveFlow_constructor_AWSOchestratorCase() throws Exception {

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.doReturn("TEST_NODE_ID").when(Utils.class);
        Utils.readUrl(anyString());
        
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv())
                        .thenReturn(new HashMap<String, String>(){{
                            put(KVKeysValues.ENV_EC2_ENDPOINT, "TEST_ENV_EC2_ENDPOINT");
                            put(KVKeysValues.ENV_ELB_ENDPOINT, "TEST_ENV_ELB_ENDPOINT");
                        }});
        
        DescribeTagsResult dtrMock = Mockito.mock(DescribeTagsResult.class);
        Mockito.when(dtrMock.getTags()).thenReturn(new LinkedList<TagDescription>(){{
            add(new TagDescription(){{
                setKey("TEST_KEY");
                setValue("TEST_ELB");
            }});
        }});
        AmazonEC2Client aec2ClientMock = Mockito.mock(AmazonEC2Client.class);
        Mockito.when(aec2ClientMock.describeTags(any())).thenReturn(dtrMock);
        
        PowerMockito.whenNew(AmazonEC2Client.class).withAnyArguments()
            .thenReturn(aec2ClientMock);
       
        orch = new AwsOrchestrator(new OrchestratorConfig(){{
            setDeployType(AwsOrchestrator.DEPLOY_TYPE);
            setTagName("TEST_KEY");
            setLot49ElbName("TEST_ELB");
        }});

        Mockito.when(serviceRunnerMock.getOrchestrator()).thenReturn(orch);
        
        HazelcastServiceConfig hcsConfig = new HazelcastServiceConfig();
        hcsConfig.setAwsRegion("TEST_AWS_REGION");
        hcsConfig.setAwsSecretKey("TEST_AWS_SECRET_KEY");
        hcsConfig.setAwsAccessKey("TEST_AWS_ACCESS_KEY");
        
        IdGenerator idGenMock = Mockito.mock(IdGenerator.class);
        Mockito.when(idGenMock.toString()).thenReturn("TEST_ID_GEN");
        
        HazelcastInstance hcInstanceMock = Mockito.mock(HazelcastInstanceImpl.class);
        Mockito.when(hcInstanceMock.getIdGenerator(anyString())).thenReturn(idGenMock);
        
        PowerMockito.mockStatic(Hazelcast.class);
        PowerMockito.doReturn(hcInstanceMock).when(Hazelcast.class);
        Hazelcast.newHazelcastInstance(any());
        
        HazelcastService hcs = null;
        
        hcs = new HazelcastService(serviceRunnerMock, hcsConfig);

        assertEquals("TEST_ID_GEN", Whitebox.getInternalState(hcs, "idGenerator").toString());
    }
}
