package com.enremmeta.rtb.sandbox.aspects;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.AdImpl;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;

@Aspect
public final class MethodInterrupter {

    private final transient ExecutorService executor;


    public MethodInterrupter() {
        this.executor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(), new ThreadFactory() {
                            private final ThreadGroup group = new ThreadGroup("worker-tg");

                            @Override
                            public Thread newThread(Runnable runnable) {
                                Thread thread = new Thread(this.group, runnable);
                                thread.setName("worker");
                                thread.setDaemon(true); // we need all this stuff to make thread as
                                                        // daemon, so app will shutdown properly
                                return thread;
                            }
                        });
    }

    @Around("execution(* * (..)) && @annotation(com.enremmeta.rtb.sandbox.aspects.ExecutionTimeLimit)")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
        final ExecutionTimeLimit executionTimeLimit =
                        method.getAnnotation(ExecutionTimeLimit.class);

        Future<Object> future = executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    return point.proceed();
                } catch (Throwable t) {
                    throw new ExecutionException(t);
                }
            }
        });

        try {
            return future.get(executionTimeLimit.limit(), executionTimeLimit.unit());
        } catch (InterruptedException e) { // <-- possible error cases
            throw e;
        } catch (ExecutionException e) {
            throw e;
        } catch (TimeoutException e) {
            future.cancel(true); // <-- interrupt the job
            Object o = point.getThis();
            if (o != null && o instanceof AdImpl) {
                Map<String, Long> map = ServiceRunner.getInstance().getAdCache().getTimeoutMap();
                String id = ((Ad) o).getId();
                Long count = map.get(id);
                if (count == null) {
                    map.put(id, 1L);
                } else {
                    count += 1;
                    map.put(id, count);
                }
                error("Timeout Exception on " + method.getName() + " call for ad: " + id);
            }

            throw e;
        }
    }

    private final static String logPrefix() {
        return BidderCalendar.getInstance().currentDate() + " MethodInterrupter: ";
    }

    private final static String error(String s) {
        String retval = logPrefix() + s;
        LogUtils.error(retval);
        return retval + "\n";
    }
}
