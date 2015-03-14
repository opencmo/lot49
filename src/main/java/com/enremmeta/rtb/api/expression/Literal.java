package com.enremmeta.rtb.api.expression;

import java.util.Set;

/**
 * literal.
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public class Literal<T> implements Expression<T> {
    private T literal;

    public Literal(T literal) {
        super();
        this.literal = literal;
    }

    public boolean eval(Set<T> received) {
        return received != null && received.contains(literal);
    }

    public String toString() {
        return literal.toString();
    }

    @Override
    public String getName() {
        return "LITERAL";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        T t1 = literal;
        T t2 = null;
        if (o instanceof Literal) {
            try {
                t2 = (T) ((Literal) o).literal;
                return t1.equals(t2);
            } catch (ClassCastException e) {
                return false;
            }
        }
        return t1.equals(o);
    }
}
