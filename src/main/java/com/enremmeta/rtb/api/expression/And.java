package com.enremmeta.rtb.api.expression;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class And<T> implements Expression<T> {
    private Set<Expression<T>> terms;

    public And(Collection<Expression<T>> terms) {
        super();
        this.terms = new HashSet<Expression<T>>();
        this.terms.addAll(terms);

    }

    @Deprecated
    /* Added for backward compatibility with userSegments */
    public And(Set<String> terms) {
        super();
        this.terms = new HashSet<Expression<T>>();
        for (String term : terms) {
            if (term.isEmpty()) {
                continue;
            }
            this.terms.add(new Literal(term));
        }
    }

    @SafeVarargs
    public And(Expression<T>... expTerms) {
        super();
        this.terms = new HashSet<Expression<T>>();
        for (Expression<T> expTerm : expTerms) {
            this.terms.add(expTerm);

        }
    }

    @SafeVarargs
    public And(T... terms) {
        super();
        this.terms = new HashSet<Expression<T>>();
        for (T term : terms) {
            if (term == null) {
                continue;
            }
            this.terms.add(new Literal(term));
        }
    }

    /**
     * This is for building fluent interfaces.
     * 
     * @param terms
     *            list of terms
     * @return AND expression
     */
    @SafeVarargs
    public final And<T> and(T... terms) {
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
    public final And<T> and(Expression<T>... expTerms) {
        this.terms = new HashSet<Expression<T>>();
        for (Expression<T> expTerm : expTerms) {
            this.terms.add(expTerm);
        }
        return this;

    }

    @Override
    public boolean eval(Set<T> received) {
        if (terms.size() == 0) {
            return false;
        }
        return terms.parallelStream().allMatch(t -> t.eval(received));
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
        return "AND";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof And)) {
            return false;
        }
        Set<Expression<T>> t2 = ((And) o).terms;
        boolean retval = terms.equals(t2);
        log("Comparing " + terms + " to " + t2 + ": " + retval);
        return retval;
    }
}
