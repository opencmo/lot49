package com.enremmeta.rtb.api;

import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.HashMap;

import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyClassLoader;

public class Lot49GroovyClassLoader extends GroovyClassLoader {

    public Lot49GroovyClassLoader() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Lot49GroovyClassLoader(ClassLoader parent, CompilerConfiguration config,
                    boolean useConfigurationClasspath) {
        super(parent, config, useConfigurationClasspath);
        // TODO Auto-generated constructor stub
    }

    public Lot49GroovyClassLoader(ClassLoader loader, CompilerConfiguration config) {
        super(loader, config);
        // TODO Auto-generated constructor stub
    }

    public Lot49GroovyClassLoader(ClassLoader loader) {
        super(loader);
        // TODO Auto-generated constructor stub
    }

    public Lot49GroovyClassLoader(GroovyClassLoader parent) {
        super(parent);
        // TODO Auto-generated constructor stub
    }

    // HashMap that maps CodeSource to ProtectionDomain
    // @GuardedBy("pdcache")
    private final HashMap<CodeSource, ProtectionDomain> pdcache = new HashMap<>(11);

    static {
        ClassLoader.registerAsParallelCapable();
    }

    /**
     * Returns the permissions for the given CodeSource object.
     * <p>
     * This method is invoked by the defineClass method which takes a CodeSource as an argument when
     * it is constructing the ProtectionDomain for the class being defined.
     * <p>
     * 
     * @param codesource
     *            the codesource.
     *
     * @return the permissions granted to the codesource.
     *
     */
    protected PermissionCollection getPermissions(CodeSource codesource) {

        return new Permissions(); // ProtectionDomain defers the binding
    }

    /*
     * Returned cached ProtectionDomain for the specified CodeSource.
     */
    private ProtectionDomain getProtectionDomain(CodeSource cs) {
        if (cs == null)
            return null;

        ProtectionDomain pd = null;
        synchronized (pdcache) {
            pd = pdcache.get(cs);
            if (pd == null) {
                PermissionCollection perms = getPermissions(cs);
                pd = new ProtectionDomain(cs, perms, this, null);
                pdcache.put(cs, pd);

            }
        }
        return pd;
    }

}
