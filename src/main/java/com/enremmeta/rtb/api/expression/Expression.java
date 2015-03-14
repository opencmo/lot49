package com.enremmeta.rtb.api.expression;

import java.util.Set;

import com.enremmeta.rtb.LogUtils;

/**
 * A quick implementation of an expression. All are commutative. Syntax is OP(arg1 arg2 ...) where
 * an arg is either T or another Expression.
 * 
 * TODO this whole thing may make for a better DSL experience if using properly designed fluent
 * interfaces but we'll never be able to do this. Ideally we'd allow subclasses to just do or("XYZ",
 * and("ZYX", not("fff")) But that'll never happen )
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public interface Expression<T> {
    default void log(String s) {
        LogUtils.trace(s);
        System.out.println(s);
    }

    boolean eval(Set<T> received);

    String getName();

}
