package com.enremmeta.rtb.bidder_initialization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.spi.codecs.DefaultSegmentToOwnerCodec;
import com.enremmeta.rtb.spi.codecs.NoopSegmentToOwnerCodec;
import com.enremmeta.rtb.spi.codecs.SegmentToOwnerCodec;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_initCodecs {
    // should initialize codec and assign it to the field 'segmentToOwnerCodec'

    private Lot49Config lot49ConfigMock;
    private ServiceRunner serviceRunnerMock;

    private class UnsuitableCodec { }
    
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        lot49ConfigMock = Mockito.mock(Lot49Config.class);
        
        serviceRunnerMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        serviceRunnerMock.setConfig(lot49ConfigMock);
    }

    @Test
    public void positiveFlow_initializesRequestedCodecIfCodecClassIsSuitable() throws Lot49Exception {
        Class<?> defaultCodecClass = DefaultSegmentToOwnerCodec.class; 
        Mockito.when(lot49ConfigMock.getSegmentToOwnerCodecClassname()).thenReturn(defaultCodecClass.getName());
        
        serviceRunnerMock.initCodecs();
        
        Class<?> codecClass = serviceRunnerMock.getSegmentToOwnerCodec().getClass();
        assertThat(codecClass, equalTo(defaultCodecClass));
    }

    @Test
    public void positiveFlow_initializesNoopCodecIfCodecClassIsUnsuitable() throws Lot49Exception {
        Class<?> unsuitableCodecClass = UnsuitableCodec.class; 
        Mockito.when(lot49ConfigMock.getSegmentToOwnerCodecClassname()).thenReturn(unsuitableCodecClass.getName());
        
        serviceRunnerMock.initCodecs();
        
        PowerMockito.verifyStatic();
        LogUtils.warn(unsuitableCodecClass.getName() + " is not a subclass of " + SegmentToOwnerCodec.class + " defaulting to Noop");

        Class<?> codecClass = serviceRunnerMock.getSegmentToOwnerCodec().getClass();
        assertThat(codecClass, equalTo(NoopSegmentToOwnerCodec.class));
    }

    @Test
    public void positiveFlow_initializesNoopCodecIfCodecClassDoesNotExist() throws Exception {
        Mockito.when(lot49ConfigMock.getSegmentToOwnerCodecClassname()).thenReturn("NonexistentClass");
        
        serviceRunnerMock.initCodecs();
        
        PowerMockito.verifyStatic();
        LogUtils.warn(isA(ClassNotFoundException.class));

        Class<?> codecClass = serviceRunnerMock.getSegmentToOwnerCodec().getClass();
        assertThat(codecClass, equalTo(NoopSegmentToOwnerCodec.class));
    }
}
