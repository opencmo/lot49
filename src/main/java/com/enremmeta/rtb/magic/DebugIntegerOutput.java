package com.enremmeta.rtb.magic;

import java.nio.ByteBuffer;

import com.enremmeta.rtb.LogUtils;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.IntegerOutput;

public class DebugIntegerOutput<K, V> extends IntegerOutput<K, V> {
    public DebugIntegerOutput(RedisCodec<K, V> codec) {

        super(codec);
        LogUtils.debug("DebugIntegerOutput: Created");
    }

    @Override
    public Long get() {
        // TODO Auto-generated method stub
        return super.get();
    }

    @Override
    public void setError(ByteBuffer error) {
        LogUtils.debug("DebugIntegerOutput: setError(" + error + ")");
        super.setError(error);
    }

    @Override
    public void setError(String error) {
        LogUtils.debug("DebugIntegerOutput: setError(" + error + ")");
        super.setError(error);
    }

    @Override
    public boolean hasError() {

        return super.hasError();
    }

    @Override
    public String getError() {

        return super.getError();
    }

    @Override
    public void complete(int depth) {
        LogUtils.debug("DebugIntegerOutput: complete(" + depth + ")");
        super.complete(depth);
    }

    @Override
    protected String decodeAscii(ByteBuffer bytes) {
        String retval = super.decodeAscii(bytes);
        LogUtils.debug("DebugIntegerOutput: decodeAscii(" + error + "): " + retval);
        return retval;
    }

    @Override
    public void set(long integer) {
        LogUtils.debug("DebugIntegerOutput: Setting with integer(" + integer + ")");
        output = integer;
    }

    @Override
    public void set(ByteBuffer bytes) {
        LogUtils.debug("DebugIntegerOutput: Setting with ByteBuffer(" + bytes + ")");
        output = null;
    }
}
