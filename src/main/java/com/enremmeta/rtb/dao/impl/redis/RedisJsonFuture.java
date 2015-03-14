package com.enremmeta.rtb.dao.impl.redis;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.dao.JsonFuture;
import com.enremmeta.util.Utils;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.CommandOutput;

public class RedisJsonFuture<T> extends JsonFuture<T> {

    public RedisJsonFuture(Future<String> f, Class<T> type, RedisService svc) {
        super(f, type);
        this.svc = svc;
    }

    private final RedisService svc;

    public static final <T> T getSafe(Future<T> f, RedisService svc) throws Lot49Exception {
        try {
            if (f == null) {
                return null;
            } else if (f instanceof Command) {
                final Command<?, ?, T> c = (Command<?, ?, T>) f;
                final CommandOutput<?, ?, T> o = c.getOutput();
                String error = o.getError();
                if (error == null) {
                    return c.get();
                } else {
                    throw new Lot49Exception("Error executing Redis operation on "
                                    + svc.getConfig().getHost() + ":" + svc.getConfig().getPort()
                                    + ": " + error);
                }
            } else {
                return f.get();
            }
        } catch (InterruptedException ie) {
            LogUtils.error(ie);
            Thread.currentThread().interrupt();
            throw new Lot49Exception(ie);
        } catch (ExecutionException ee) {
            LogUtils.error(ee);
            throw new Lot49Exception(ee.getCause());
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        T retval;
        try {
            String s = getSafe(getUnderlyingFuture(), svc);

            LogUtils.debug("Dao: " + getClass().getName() + ":  get(): " + s);
            if (s == null) {
                return null;
            }
            retval = Utils.MAPPER.readValue(s, getType());
            LogUtils.debug("Dao: " + getClass().getName() + ":  " + s + " -> " + retval);
        } catch (IOException | Lot49Exception e) {
            throw new ExecutionException(e);
        }
        return retval;
    }
}
