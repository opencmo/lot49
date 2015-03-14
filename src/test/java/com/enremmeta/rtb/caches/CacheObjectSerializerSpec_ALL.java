package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.enremmeta.util.BidderCalendar;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class CacheObjectSerializerSpec_ALL {
    private final Long fetchedTime = BidderCalendar.getInstance().currentTimeMillis();
    private final String object = "TestObject";
    private CacheObjectSerializer<String> cacheObjectSerializer;

    @Before
    public void setUp() throws Exception {
        cacheObjectSerializer = new CacheObjectSerializer<String>();
    }

    @Test
    public void positiveFlow_read() throws IOException {
        ObjectDataInput objectDataInputMock = Mockito.mock(ObjectDataInput.class);
        Mockito.when(objectDataInputMock.readLong()).thenReturn(fetchedTime);
        Mockito.when(objectDataInputMock.readObject()).thenReturn(object);
        
        CacheObject<String> cacheObject = cacheObjectSerializer.read(objectDataInputMock); /// act
        
        assertThat(cacheObject.getFetchedTime(), equalTo(fetchedTime));
        assertThat(cacheObject.getObject(), equalTo(object));
    }

    @Test
    public void positiveFlow_write() throws IOException {
        ObjectDataOutput objectDataOutputMock = Mockito.mock(ObjectDataOutput.class);
        CacheObject<String> cacheObject = new CacheObject<String>(object);
        cacheObject.setFetchedTime(fetchedTime);
        
        cacheObjectSerializer.write(objectDataOutputMock, cacheObject); /// act
        
        Mockito.verify(objectDataOutputMock).writeLong(fetchedTime);
        Mockito.verify(objectDataOutputMock).writeObject(object);
    }
}
