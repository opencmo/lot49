package com.enremmeta.rtb.caches;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class CacheObjectSerializer<T> implements StreamSerializer<CacheObject<T>> {

    public CacheObjectSerializer() {
        super();
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getTypeId() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public CacheObject<T> read(ObjectDataInput arg0) throws IOException {
        CacheObject<T> cObj = new CacheObject<T>();
        cObj.setFetchedTime(arg0.readLong());
        cObj.setObject((T) arg0.readObject());
        return cObj;
    }

    @Override
    public void write(ObjectDataOutput arg0, CacheObject<T> arg1) throws IOException {
        arg0.writeLong(arg1.getFetchedTime());
        arg0.writeObject(arg1.getObject());

    }

}
