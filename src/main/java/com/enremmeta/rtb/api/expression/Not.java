package com.enremmeta.rtb.api.expression;

import java.util.Set;

/**
 * Not.
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */

public class Not<T> implements Expression<T> {
    private Expression<T> term;

    public Not(Expression<T> term) {
        super();
        this.term = term;
    }

    public Not(T term) {
        super();
        this.term = new Literal(term);

    }

    @Override
    public boolean eval(Set<T> received) {
        if (term == null) {
            return !(received == null || received.size() == 0);
        }
        if (received == null || received.size() == 0) {
            return true;
        }
        return !term.eval(received);
    }

    @Override
    public String toString() {
        return getName() + "(" + (this.term == null ? null : this.term.toString()) + ")";
    }

    @Override
    public String getName() {
        return "NOT";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Not)) {
            return false;
        }
        return term.equals(((Not) o).term);
    }
}
