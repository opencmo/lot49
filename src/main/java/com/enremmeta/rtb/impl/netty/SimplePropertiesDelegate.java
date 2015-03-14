package com.enremmeta.rtb.impl.netty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.internal.PropertiesDelegate;

public class SimplePropertiesDelegate implements PropertiesDelegate {

    private final Map<String, Object> store;

    public SimplePropertiesDelegate() {
        this.store = new HashMap<String, Object>();
    }

    public SimplePropertiesDelegate(Map<String, Object> store) {
        this.store = store;
    }

    public SimplePropertiesDelegate(PropertiesDelegate that) {
        if (that instanceof SimplePropertiesDelegate) {
            this.store = new HashMap<String, Object>(((SimplePropertiesDelegate) that).store);
        } else {
            this.store = new HashMap<String, Object>();
            for (String name : that.getPropertyNames()) {
                this.store.put(name, that.getProperty(name));
            }
        }
    }

    @Override
    public Object getProperty(String name) {
        return store.get(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.unmodifiableCollection(store.keySet());
    }

    @Override
    public void setProperty(String name, Object value) {
        store.put(name, value);
    }

    @Override
    public void removeProperty(String name) {
        store.remove(name);
    }
}
