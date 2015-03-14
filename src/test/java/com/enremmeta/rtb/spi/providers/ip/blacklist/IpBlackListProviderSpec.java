package com.enremmeta.rtb.spi.providers.ip.blacklist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.SimpleCallback;
import com.enremmeta.util.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, GzipCompressorInputStream.class, IpBlackListProvider.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class IpBlackListProviderSpec {
    
    private ServiceRunner serviceRunnerSimpleMock;

    @Before
    public void setUp(){
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance())
            .thenReturn(serviceRunnerSimpleMock);
        
        readLineCount = 0;
    }
    
    @Test
    public void initInternal_readLineNull() throws Exception {
        
        
        PowerMockito.whenNew(GzipCompressorInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(GzipCompressorInputStream.class));
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(FileInputStream.class));
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments()
            .thenReturn(Mockito.mock(BufferedReader.class));
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        org.powermock.reflect.Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);
        
        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertTrue(ipBLP.isEnabled());
    }
    
    @Test
    public void initInternal_exceptionThrown() throws Exception {
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        Whitebox.setInternalState(ipBLP, "cb", simpleCallbackMarker);
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());
        
        org.powermock.reflect.Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);

        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertFalse(ipBLP.isEnabled());
    }

    public static int readLineCount;
    
    @Test
    public void initInternal_readLineNotNull_correctIPV4() throws Exception {
        
        
        PowerMockito.whenNew(GzipCompressorInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(GzipCompressorInputStream.class));
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(FileInputStream.class));
        BufferedReader brMock = Mockito.mock(BufferedReader.class);
        Mockito.when(brMock.readLine()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if(readLineCount == 0){
                    readLineCount++;
                    return "254.123.254.254";
                }
                else
                    return null;
            }});
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments()
            .thenReturn(brMock);
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        
        //before
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        
        //act
        Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);
        
        //after
        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertTrue(ipBLP.isEnabled());
        assertEquals(1L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        assertEquals(4269539070L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).toArray()[0]);
    }
    
    @Test
    public void initInternal_readLineNotNull_illegalIPV4() throws Exception {
        
        
        PowerMockito.whenNew(GzipCompressorInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(GzipCompressorInputStream.class));
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(FileInputStream.class));
        BufferedReader brMock = Mockito.mock(BufferedReader.class);
        Mockito.when(brMock.readLine()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if(readLineCount == 0){
                    readLineCount++;
                    return "254.123.254.";
                }
                else
                    return null;
            }});
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments()
            .thenReturn(brMock);
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        
        //before
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        
        //act
        Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);
        
        //after
        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertTrue(ipBLP.isEnabled());
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
    }
    
    @Test
    public void initInternal_readLineNotNull_illegalIPV4_2() throws Exception {
        
        
        PowerMockito.whenNew(GzipCompressorInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(GzipCompressorInputStream.class));
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(FileInputStream.class));
        BufferedReader brMock = Mockito.mock(BufferedReader.class);
        Mockito.when(brMock.readLine()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if(readLineCount == 0){
                    readLineCount++;
                    return "254.123.254.999";
                }
                else
                    return null;
            }});
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments()
            .thenReturn(brMock);
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        
        //before
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        
        //act
        Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);
        
        //after
        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertTrue(ipBLP.isEnabled());
        assertEquals(1L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        assertEquals(4269539303L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).toArray()[0]);
        
        //TODO:
        //refactoring: throw exception instead of converting illegal ip
    }
    
    @Test
    public void initInternal_readLineNotNull_correctIPV6() throws Exception {
        
        
        PowerMockito.whenNew(GzipCompressorInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(GzipCompressorInputStream.class));
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(FileInputStream.class));
        BufferedReader brMock = Mockito.mock(BufferedReader.class);
        Mockito.when(brMock.readLine()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if(readLineCount == 0){
                    readLineCount++;
                    return "FE80:0000:0000:0000:0202:B3FF:FE1E:8329";
                }
                else
                    return null;
            }});
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments()
            .thenReturn(brMock);
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        
        //before
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        
        //act
        Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);
        
        //after
        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertTrue(ipBLP.isEnabled());
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        assertEquals(1L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIpV6s")).size());
        assertEquals(new BigInteger("338288524927261089654163772891438416681"), ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIpV6s")).toArray()[0]);
    }
    
    @Test
    public void initInternal_readLineNotNull_correctIPV6_compressionForm() throws Exception {
        
        
        PowerMockito.whenNew(GzipCompressorInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(GzipCompressorInputStream.class));
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(FileInputStream.class));
        BufferedReader brMock = Mockito.mock(BufferedReader.class);
        Mockito.when(brMock.readLine()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if(readLineCount == 0){
                    readLineCount++;
                    return "FE80:0:0:0:202:B3FF:FE1E:8329";
                }
                else
                    return null;
            }});
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments()
            .thenReturn(brMock);
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        
        //before
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        
        //act
        Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);
        
        //after
        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertTrue(ipBLP.isEnabled());
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        assertEquals(1L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIpV6s")).size());
        assertEquals(new BigInteger("338288524927261089654163772891438416681"), ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIpV6s")).toArray()[0]);
    }
    
    @Test
    public void initInternal_readLineNotNull_correctIPV6_zeroCompression() throws Exception {
        
        
        PowerMockito.whenNew(GzipCompressorInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(GzipCompressorInputStream.class));
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(FileInputStream.class));
        BufferedReader brMock = Mockito.mock(BufferedReader.class);
        Mockito.when(brMock.readLine()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if(readLineCount == 0){
                    readLineCount++;
                    return "FE80::0202:B3FF:FE1E:8329";
                }
                else
                    return null;
            }});
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments()
            .thenReturn(brMock);
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        
        //before
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        
        //act
        Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);
        
        //after
        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertTrue(ipBLP.isEnabled());
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        assertEquals(1L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIpV6s")).size());
        assertEquals(new BigInteger("338288524927261089654163772891438416681"), ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIpV6s")).toArray()[0]);
    }
    
    @Test
    public void initInternal_readLineNotNull_illegalIPV6() throws Exception {
        
        
        PowerMockito.whenNew(GzipCompressorInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(GzipCompressorInputStream.class));
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments()
            .thenReturn(Mockito.mock(FileInputStream.class));
        BufferedReader brMock = Mockito.mock(BufferedReader.class);
        Mockito.when(brMock.readLine()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if(readLineCount == 0){
                    readLineCount++;
                    return "FE80:0202";
                }
                else
                    return null;
            }});
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments()
            .thenReturn(brMock);
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        
        //before
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        
        //act
        Whitebox.invokeMethod(ipBLP, "initInternal", simpleCallbackMarker);
        
        //after
        assertEquals(1, simpleCallbackMarker.getCallCounter());
        assertTrue(ipBLP.isEnabled());
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIps")).size());
        assertEquals(0L, ((Set<Long>)Whitebox.getInternalState(ipBLP, "blackListedIpV6s")).size());
    }
    
    public class SimpleCallbackMarker implements SimpleCallback {
        private int callCounter = 0;
        
        public int getCallCounter() {
            return callCounter;
        }
        
        @Override
        public void done(Throwable t) {
            callCounter++;
        }
    };
    
    @Test
    public void InitAsync_shouldCall_schedule() throws Exception {
        
        ScheduledExecutorService ses = Mockito.mock(ScheduledExecutorService.class);
        Mockito.when(ses.schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(Mockito.mock(ScheduledFuture.class));
        Mockito.when(serviceRunnerSimpleMock.getScheduledExecutor()).thenReturn(ses);

       
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        SimpleCallbackMarker simpleCallbackMarker = new SimpleCallbackMarker();
        ipBLP.initAsync(simpleCallbackMarker);

        Mockito.verify(ses, Mockito.times(1))
            .schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.any(TimeUnit.class));
    }
    
    private final String NOT_IN_BLLIST = "123.44.12.34";
    private final String IN_BLLIST = "123.44.12.36";
    
    @Test
    public void getProviderInfo_ShouldReturn_ProviderInfoReceived_emptyList() throws Exception {
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setDevice(new Device(){{
            setIp(NOT_IN_BLLIST);
        }});
        
        assertEquals("com.enremmeta.rtb.spi.providers.ip.blacklist.IpBlackListInfoReceived", 
                        ipBLP.getProviderInfo(req).getClass().getCanonicalName());
        assertFalse(((IpBlackListInfoReceived)ipBLP.getProviderInfo(req)).isFound());
    }

    @Test
    public void getProviderInfo_IP_found() throws Exception {
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        Whitebox.setInternalState(ipBLP, "blackListedIps", new HashSet<Long>(){{
            add(Utils.ipToLong(IN_BLLIST));
        }});
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setDevice(new Device(){{
            setIp(IN_BLLIST);
        }});
        
        assertEquals("com.enremmeta.rtb.spi.providers.ip.blacklist.IpBlackListInfoReceived", 
                        ipBLP.getProviderInfo(req).getClass().getCanonicalName());
        assertTrue(((IpBlackListInfoReceived)ipBLP.getProviderInfo(req)).isFound());
    }
    
    @Test
    public void getProviderInfo_deviceIsNull() throws Exception {
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        Whitebox.setInternalState(ipBLP, "blackListedIps", new HashSet<Long>(){{
            add(Utils.ipToLong(IN_BLLIST));
        }});
        
        OpenRtbRequest req = new OpenRtbRequest(){{
            setDevice(null);
        }};
        
        assertNull(ipBLP.getProviderInfo(req));
    }
    
    @Test
    public void match_receivedIsNull() throws Exception {
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        
        assertFalse(ipBLP.match(null, new IpBlackListInfoRequired()));
    }
    
    @Test
    public void match_requiredIsNull() throws Exception {
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        
        assertTrue(ipBLP.match(new IpBlackListInfoReceived(false), null));
    }
    
    @Test
    public void match_receivedIsNotFound_requiredIsFound() throws Exception {
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        
        assertFalse(ipBLP.match(new IpBlackListInfoReceived(false), 
                        new IpBlackListInfoRequired(){{
                            setFound(true);
                        }}));
    }
    
    @Test
    public void match_receivedIsFound_requiredIsFound() throws Exception {
        
        IpBlackListProvider ipBLP = new IpBlackListProvider(
                        serviceRunnerSimpleMock, new HashMap());
        
        assertTrue(ipBLP.match(new IpBlackListInfoReceived(true), 
                        new IpBlackListInfoRequired(){{
                            setFound(true);
                        }}));
    }
   
}
