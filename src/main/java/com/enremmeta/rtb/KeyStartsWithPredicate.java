package com.enremmeta.rtb;

import java.util.Map.Entry;

import com.hazelcast.query.Predicate;

@Deprecated
public class KeyStartsWithPredicate implements Predicate {
    public KeyStartsWithPredicate(String prefix) {
        super();
        this.prefix = prefix;
    }

    private final String prefix;

    @Override
    public boolean apply(Entry mapEntry) {
        return mapEntry.getKey().toString().startsWith(prefix);
    }
}
