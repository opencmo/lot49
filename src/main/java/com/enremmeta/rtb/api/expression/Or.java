package com.enremmeta.rtb.api.expression;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Or<T> implements Expression<T> {
    private Set<Expression<T>> terms;

    @SafeVarargs
    public Or(Expression<T>... terms) {
        super();
        this.terms = new HashSet<Expression<T>>();
        for (Expression<T> term : terms) {
            this.terms.add(term);
        }
    }

    @SafeVarargs
    public Or(T... terms) {
        super();
        this.terms = new HashSet<Expression<T>>();
        for (T term : terms) {
            if (term == null) {
                continue;
            }
            this.terms.add(new Literal(term));
        }
    }

    @Deprecated
    /* Added for backward compatibility with userSegments */
    public Or(Set<String> terms) {
        super();
        this.terms = new HashSet<Expression<T>>();
        for (String term : terms) {
            if (term.isEmpty()) {
                continue;
            }
            this.terms.add(new Literal(term));
        }
    }

    public Or(Collection<Expression<T>> terms) {
        super();
        this.terms = new HashSet<Expression<T>>();
        for (Expression<T> term : terms) {
            if (term == null) {
                continue;
            }
            this.terms.add(term);
        }
    }

    /**
     * This is for building fluent interfaces.
     * 
     * @param terms
     *            list of terms
     * @return OR expression
     */
    @SafeVarargs
    public final Or<T> or(T... terms) {
        this.terms = new HashSet<Expression<T>>();
        for (T term : terms) {
            if (term == null) {
                continue;
            }
            this.terms.add(new Literal(term));
        }
        return this;
    }

    /**
     * This is for building fluent interfaces.
     * 
     * @param expTerms
     *            list of expTerms
     * @return AND expression
     */
    public final Or<T> or(Expression<T>... expTerms) {
        this.terms = new HashSet<Expression<T>>();
        for (Expression<T> expTerm : expTerms) {

            this.terms.add(expTerm);
        }
        return this;
    }

    @Override
    public boolean eval(Set<T> received) {
        if (terms.size() == 0) {
            return true;
        }
        return terms.parallelStream().anyMatch(t -> t.eval(received));
    }

    public String toString() {
        String retval = "";
        for (Expression<T> term : terms) {
            if (retval.length() > 0) {
                retval += ", ";
            }
            retval += term == null ? null : term.toString();
        }
        return getName() + "(" + retval + ")";
    }

    @Override
    public String getName() {
        return "OR";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Or)) {
            return false;
        }
        Set<Expression<T>> t2 = ((Or) o).terms;

        boolean retval = terms.equals(t2);
        log("Comparing " + terms + " to " + t2 + ": " + retval);
        return retval;
    }
}
