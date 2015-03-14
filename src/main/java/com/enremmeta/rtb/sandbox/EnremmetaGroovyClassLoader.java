package com.enremmeta.rtb.sandbox;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyClassLoader;

public class EnremmetaGroovyClassLoader extends GroovyClassLoader {

    private SandboxSecurityManager sandboxSecurityManager;

    public EnremmetaGroovyClassLoader(SandboxSecurityManager sandboxSecurityManager) {
        this.sandboxSecurityManager = sandboxSecurityManager;
    }

    public EnremmetaGroovyClassLoader(ClassLoader loader,
                    SandboxSecurityManager sandboxSecurityManager) {
        super(loader);
        this.sandboxSecurityManager = sandboxSecurityManager;
    }

    public EnremmetaGroovyClassLoader(GroovyClassLoader parent,
                    SandboxSecurityManager sandboxSecurityManager) {
        super(parent);
        this.sandboxSecurityManager = sandboxSecurityManager;
    }

    public EnremmetaGroovyClassLoader(ClassLoader loader, CompilerConfiguration config,
                    SandboxSecurityManager sandboxSecurityManager) {
        super(loader, config);
        this.sandboxSecurityManager = sandboxSecurityManager;
    }

    public EnremmetaGroovyClassLoader(ClassLoader parent, CompilerConfiguration config,
                    boolean useConfigurationClasspath,
                    SandboxSecurityManager sandboxSecurityManager) {
        super(parent, config, useConfigurationClasspath);
        this.sandboxSecurityManager = sandboxSecurityManager;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        if (this.sandboxSecurityManager != null) {
            sandboxSecurityManager.checkPackageAccess(name);
        }
        return super.loadClass(name);
    }

    @Override
    public Class loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript)
                    throws ClassNotFoundException, CompilationFailedException {
        if (this.sandboxSecurityManager != null) {
            sandboxSecurityManager.checkPackageAccess(name);
        }
        return super.loadClass(name, lookupScriptFiles, preferClassOverScript);
    }

    @Override
    public Class loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript,
                    boolean resolve) throws ClassNotFoundException, CompilationFailedException {

        if (this.sandboxSecurityManager != null) {
            sandboxSecurityManager.checkPackageAccess(name);
        }
        return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve);
    }

    @Override
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (this.sandboxSecurityManager != null) {
            sandboxSecurityManager.checkPackageAccess(name);
        }
        return super.loadClass(name, resolve);
    }

    @Override
    public Class parseClass(File file) throws CompilationFailedException, IOException {
        @SuppressWarnings("rawtypes")
        Class clzz = super.parseClass(file);

        if (this.sandboxSecurityManager != null) {
            sandboxSecurityManager.addClassUnderControl(clzz.getName());
        }
        return clzz;
    }

    @Override
    public Class parseClass(String text, String fileName) throws CompilationFailedException {
        Class clzz = super.parseClass(text, fileName);
        if (this.sandboxSecurityManager != null) {
            sandboxSecurityManager.addClassUnderControl(clzz.getName());
        }
        return clzz;
    }

    @Override
    public Class parseClass(String text) throws CompilationFailedException {
        Class clzz = super.parseClass(text);
        if (this.sandboxSecurityManager != null) {
            sandboxSecurityManager.addClassUnderControl(clzz.getName());
        }
        return clzz;
    }
}
