package com.enremmeta.rtb.spi.providers.skyhook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.SimpleCallback;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class, SkyhookProvider.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class SkyhookProviderSpec_run {
    
    private ServiceRunner serviceRunnerMock;
    private SkyhookProvider skyHook;
    private ScheduledThreadPoolExecutor sExecutor;
    private SimpleCallback innerInitCallback;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        
        sExecutor = Mockito.mock(ScheduledThreadPoolExecutor.class);
        Mockito.when(sExecutor.schedule(any(SkyhookProvider.class), Mockito.anyLong(), any()))
                        .thenReturn(Mockito.mock(ScheduledFuture.class));

        Mockito.when(serviceRunnerMock.getScheduledExecutor()).thenReturn(sExecutor);
        
    }

    @Test
    public void negativeFlow_fullGridFileError() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        
        skyHook = new SkyhookProvider(serviceRunnerMock, new HashMap() {
            {
                put("enabled", true);
            }
        });
        
        Class clazz = Whitebox.getInnerClassType(SkyhookProvider.class, "InitCallback");
        Constructor constructor = Whitebox.getConstructor(clazz, SkyhookProvider.class);
        innerInitCallback = (SimpleCallback) constructor.newInstance(skyHook);
        
        Whitebox.setInternalState(skyHook, "cb", innerInitCallback);

        skyHook.run();
        assertEquals("Could not initialize Skyhook", ((Lot49Exception)Whitebox.getInternalState(innerInitCallback, "t")).getMessage());
        
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void negativeFlow_skyhookFileError() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        
        folder.newFile("fullGrid.txt");
        
        skyHook = new SkyhookProvider(serviceRunnerMock, new HashMap() {
            {
                put("enabled", true);
                put("fullGrid", folder.getRoot().getAbsolutePath() + "/fullGrid.txt");
            }
        });
        
        Class clazz = Whitebox.getInnerClassType(SkyhookProvider.class, "InitCallback");
        Constructor constructor = Whitebox.getConstructor(clazz, SkyhookProvider.class);
        innerInitCallback = (SimpleCallback) constructor.newInstance(skyHook);
        
        Whitebox.setInternalState(skyHook, "cb", innerInitCallback);

        skyHook.run();
        assertEquals("Could not initialize Skyhook", ((Lot49Exception)Whitebox.getInternalState(innerInitCallback, "t")).getMessage());
        
    }
    
    @Test
    public void positiveFlow_emptyconfigFiles() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        
        folder.newFile("fullGrid.txt");
        folder.newFile("skyhook.txt");
        
        skyHook = new SkyhookProvider(serviceRunnerMock, new HashMap() {
            {
                put("enabled", true);
                put("fullGrid", folder.getRoot().getAbsolutePath() + "/fullGrid.txt");
                put("skyhook", folder.getRoot().getAbsolutePath() + "/skyhook.txt");
            }
        });
        
        Class clazz = Whitebox.getInnerClassType(SkyhookProvider.class, "InitCallback");
        Constructor constructor = Whitebox.getConstructor(clazz, SkyhookProvider.class);
        innerInitCallback = (SimpleCallback) constructor.newInstance(skyHook);
        
        Whitebox.setInternalState(skyHook, "cb", innerInitCallback);
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.info(anyString());

        skyHook.run();
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("Skyhook: Memory usage before"));
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("Skyhook: Read 0 entries from fullGrid."));
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("Skyhook: Time spent"));
        assertNull(Whitebox.getInternalState(innerInitCallback, "t"));
        
    }
    
    @Test
    public void positiveFlow_notEmptyconfigFiles() throws Exception{
        
        File fGrid = folder.newFile("fullGrid.txt");
        writeTestString(fGrid);

        File fSH = folder.newFile("skyhook.txt");
        writeTestString(fSH);
        
        skyHook = new SkyhookProvider(serviceRunnerMock, new HashMap() {
            {
                put("enabled", true);
                put("fullGrid", folder.getRoot().getAbsolutePath() + "/fullGrid.txt");
                put("skyhook", folder.getRoot().getAbsolutePath() + "/skyhook.txt");
            }
        });
        
        Class clazz = Whitebox.getInnerClassType(SkyhookProvider.class, "InitCallback");
        Constructor constructor = Whitebox.getConstructor(clazz, SkyhookProvider.class);
        innerInitCallback = (SimpleCallback) constructor.newInstance(skyHook);
        
        Whitebox.setInternalState(skyHook, "cb", innerInitCallback);
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.info(anyString());

        ObjectMapper omMock = PowerMockito.mock(ObjectMapper.class);
        Mockito.when(omMock.readValue(Mockito.anyString(), Mockito.eq(SkyhookEntryBean.class)))
            .thenReturn(new SkyhookEntryBean(){{
                setIpAddress("234.33.56.44");
            }});
        Mockito.when(omMock.readValue(Mockito.anyString(), Mockito.eq(FullGridEntryBean.class)))
            .thenReturn(new FullGridEntryBean(){{
            
            }});
        PowerMockito.whenNew(ObjectMapper.class).withAnyArguments().thenReturn(omMock);
        
        skyHook.run();
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("Skyhook: Memory usage before"));
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("Skyhook: Read 1 entries from fullGrid."));
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("Skyhook: Time spent"));
        assertNull(Whitebox.getInternalState(innerInitCallback, "t"));
        
    }

    private void writeTestString(File f) throws IOException {
        f.createNewFile();
        FileWriter writer = new FileWriter(f);
        writer.write("{\"TEST_KEY\" : \"TEST_VALUE\"}");
        writer.close();
    }
}
