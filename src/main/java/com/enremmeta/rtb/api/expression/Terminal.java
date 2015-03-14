package com.enremmeta.rtb.api.expression;

import java.util.Set;

public class Terminal<T> implements Expression<T> {
    private T terminal;

    public Terminal(T terminal) {
        super();
        this.terminal = terminal;
    }

    public boolean eval(Set<T> received) {
        return received != null && received.contains(terminal);
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "";
    }

    public String toString() {
        return terminal == null ? null : terminal.toString();
    }
}
