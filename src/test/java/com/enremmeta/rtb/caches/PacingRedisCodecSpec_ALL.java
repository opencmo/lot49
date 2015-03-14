package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class PacingRedisCodecSpec_ALL {
    PacingRedisCodec pacingRedisCodec;
    Charset charset;
    
    String key = "Test";
    Long value = 123L;
    
    byte[] keyBytes;
    byte[] valueBytes;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        pacingRedisCodec = new PacingRedisCodec();
        charset = Whitebox.getInternalState(pacingRedisCodec, "charset");
        
        keyBytes = key.getBytes(charset);
        valueBytes = value.toString().getBytes(charset);
    }

    @Test
    public void decodeKey_returnsExpectedValue() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(keyBytes.length);
        byteBuffer.put(keyBytes);
        byteBuffer.flip();
        
        String result = pacingRedisCodec.decodeKey(byteBuffer); /// act
        
        assertThat(result, equalTo(key));
    }

    @Test
    public void decodeKey_returnsExpectedValueIfBufferContainsMoreThan1024Bytes() {
        int repeat = 500;
        String longKey = stringRepeat(key, repeat);
        byte[] longKeyBytes = arrayRepeat(keyBytes, repeat);
        
        assertThat(longKeyBytes.length > 1024, is(true));
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(longKeyBytes.length);
        byteBuffer.put(longKeyBytes);
        byteBuffer.flip();
        
        String result = pacingRedisCodec.decodeKey(byteBuffer); /// act
        
        assertThat(result, equalTo(longKey));
    }
    
    private String stringRepeat(String src, int n) {
        String[] stingArray = new String[n];
        Arrays.fill(stingArray, src);
        
        return String.join("", stingArray);
    }
    
    private byte[] arrayRepeat(byte[] src, int n) {
        byte[] dest = new byte[src.length * n];
        
        for (int i = 0; i < n; i++) {
            System.arraycopy(src, 0, dest, i * src.length, src.length);
        }
        
        return dest;
    }

    @Test
    public void decodeValue_returnsExpectedValue() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(valueBytes.length);
        byteBuffer.put(valueBytes);
        byteBuffer.flip();
        
        Long result = pacingRedisCodec.decodeValue(byteBuffer); /// act
        
        assertThat(result, equalTo(value));
    }

    @Test
    public void decodeValue_throwsExceptionIfBufferContainsBadValue() {
        exceptionRule.expect(NumberFormatException.class);
        
        valueBytes = "Bad Long".getBytes(charset);
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(valueBytes.length);
        byteBuffer.put(valueBytes);
        byteBuffer.flip();
        
        pacingRedisCodec.decodeValue(byteBuffer); /// act
    }

    @Test
    public void encodeKey_returnsExpectedValue() {
        byte[] result = pacingRedisCodec.encodeKey(key); /// act
        
        assertThat(result, equalTo(keyBytes));
    }

    @Test
    public void encodeValue_returnsExpectedValue() {
        byte[] result = pacingRedisCodec.encodeValue(value); /// act
        
        assertThat(result, equalTo(valueBytes));
    }
}
