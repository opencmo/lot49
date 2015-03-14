package com.enremmeta.rtb.caches;

import static java.nio.charset.CoderResult.OVERFLOW;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.lambdaworks.redis.codec.RedisCodec;

/**
 * Codec.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class PacingRedisCodec extends RedisCodec<String, Long> {

    private final Charset charset = Charset.forName("UTF-8");
    private final CharsetDecoder decoder = charset.newDecoder();
    private CharBuffer chars = CharBuffer.allocate(1024);

    public PacingRedisCodec() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return decode(bytes);
    }

    @Override
    public Long decodeValue(ByteBuffer bytes) {
        Long retval = Long.valueOf(decode(bytes));
        if (retval == null) {
            return new Long(0);
        }
        return retval;
    }

    @Override
    public byte[] encodeKey(String key) {
        return encode(key);
    }

    @Override
    public byte[] encodeValue(Long value) {
        return encode(String.valueOf(value));
    }

    private String decode(ByteBuffer bytes) {
        chars.clear();
        bytes.mark();

        decoder.reset();
        while (decoder.decode(bytes, chars, true) == OVERFLOW || decoder.flush(chars) == OVERFLOW) {
            chars = CharBuffer.allocate(chars.capacity() * 2);
            bytes.reset();
        }

        return chars.flip().toString();
    }

    private byte[] encode(String string) {
        return string.getBytes(charset);
    }

}
