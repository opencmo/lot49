package com.enremmeta.rtb.sandbox.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExecutionTimeLimit {
    int limit() default 10;

    TimeUnit unit() default TimeUnit.MILLISECONDS;
}
