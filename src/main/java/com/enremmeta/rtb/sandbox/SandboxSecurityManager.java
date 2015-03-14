package com.enremmeta.rtb.sandbox;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enremmeta.rtb.config.SecurityManagerConfig;

public class SandboxSecurityManager extends SecurityManager {

    private List<String> classesUnderControl = new ArrayList<String>();


    private List<String> packageBlackList = new ArrayList<String>();

    private List<String> packageWhiteList = new ArrayList<String>();

    private List<String> classBlackList = new ArrayList<String>();

    private List<String> classWhiteList = new ArrayList<String>();

    private Map<String, List<String>> networkAllowedMethodsMap =
                    new HashMap<String, List<String>>();


    public SandboxSecurityManager(SecurityManagerConfig securityManagerConfig) {

        // networkAllowedMethodsMap.put("com.enremmeta.rtb.api.AdImpl", "setTargetingUrlsFromUrl");
        // networkAllowedMethodsMap.put("com.enremmeta.rtb.api.AdImpl",
        // "setDomainBlacklistFromUrl");

        if (securityManagerConfig != null) {
            if (securityManagerConfig.getPackageBlackList() != null) {
                packageBlackList.addAll(securityManagerConfig.getPackageBlackList());
            }

            if (securityManagerConfig.getPackageWhiteList() != null) {
                packageWhiteList.addAll(securityManagerConfig.getPackageWhiteList());
            }

            if (securityManagerConfig.getClassBlackList() != null) {
                classBlackList.addAll(securityManagerConfig.getClassBlackList());
            }

            if (securityManagerConfig.getClassWhiteList() != null) {
                classWhiteList.addAll(securityManagerConfig.getClassWhiteList());
            }

            if (securityManagerConfig.getNetworkAllowedMethodsMap() != null) {
                networkAllowedMethodsMap
                                .putAll(securityManagerConfig.getNetworkAllowedMethodsMap());
            }
        }
    }

    public List<String> getClassesUnderControl() {
        return classesUnderControl;
    }

    public List<String> getPackageBlackList() {
        return packageBlackList;
    }

    public List<String> getPackageWhiteList() {
        return packageWhiteList;
    }

    public List<String> getClassBlackList() {
        return classBlackList;
    }

    public List<String> getClassWhiteList() {
        return classWhiteList;
    }

    public void addClassUnderControl(String className) {
        if (!classesUnderControl.contains(className)) {
            classesUnderControl.add(className);
        }
    }

    private boolean isClassUnderControlInContext() {
        for (Class<?> cls : getClassContext())
            if (classesUnderControl.contains(cls.getName()))
                return true;
        return false;
    }

    private boolean isRestricted() {
        for (Class<?> cls : getClassContext())
            if (cls.getName().equals("com.enremmeta.rtb.sandbox.EnremmetaGroovyClassLoader")
                            || cls.getClassLoader() instanceof EnremmetaGroovyClassLoader)
                return true;
        return false;
    }

    protected void isInPackageBlackList(String item) {
        int itemLength = item.length();
        for (int i = 0; i < packageBlackList.size(); i++) {
            String listItem = packageBlackList.get(i);
            if (item.startsWith(listItem)) {
                if (listItem.length() == itemLength || item.charAt(listItem.length()) == '.') {
                    throw new Lot49SandboxException("Forbidden access to package " + item);
                }
            }
        }
    }

    protected void isInPackageWhiteList(String item) {
        if (packageWhiteList.size() > 0) {
            int itemLength = item.length();
            for (int i = 0; i < packageWhiteList.size(); i++) {
                String listItem = packageWhiteList.get(i);
                if (item.startsWith(listItem)) {
                    if (listItem.length() == itemLength || item.charAt(listItem.length()) == '.') {
                        return;
                    }
                }
            }
            throw new Lot49SandboxException("Forbidden access to package " + item);
        }
    }

    protected void isInClassBlackList(String item) {
        for (int i = 0; i < classBlackList.size(); i++) {
            String listItem = classBlackList.get(i);
            if (item.endsWith(listItem)) {
                throw new Lot49SandboxException("Forbidden access to class " + item);
            }
        }
    }

    protected void isInClassWhiteList(String item) {
        if (classWhiteList.size() > 0) {
            for (int i = 0; i < classWhiteList.size(); i++) {
                String listItem = classWhiteList.get(i);
                if (item.endsWith(listItem)) {
                    return;
                }
            }
            throw new Lot49SandboxException("Forbidden access to class " + item);
        }
    }

    protected boolean checkIfNetworkOpeningAllowed() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            StackTraceElement element = stack[i];
            List<String> methods = this.networkAllowedMethodsMap.get(element.getClassName());
            if (methods != null && methods.contains(element.getMethodName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkPackageAccess(String pkg) {
        super.checkPackageAccess(pkg);

        if (isRestricted()) {
            String p;
            if (pkg.contains("$")) {
                int lastIndex = pkg.lastIndexOf('.');
                p = pkg.substring(lastIndex + 1);
                p = p.replace('$', '.');
            } else {
                p = pkg;
            }

            isInPackageWhiteList(p);
            isInPackageBlackList(p);
            isInClassWhiteList(p);
            isInClassBlackList(p);
        }
    }

    @Override
    public void checkExit(int status) {
        super.checkExit(status);
        if (isClassUnderControlInContext()) {
            throw new Lot49SandboxException("Forbidden exit call");
        }

    }

    /*
     * @Override public void checkAccess(Thread t) { super.checkAccess(t); if
     * (isClassUnderControlInContext()) { throw new Lot49SandboxException("Forbidden thread call");
     * } }
     */
    @Override
    public void checkListen(int port) {
        super.checkListen(port);
        if (isClassUnderControlInContext()) {
            throw new Lot49SandboxException("Forbidden to create socket server");
        }
    }

    @Override
    public void checkConnect(String host, int port) {
        super.checkConnect(host, port);
        if (isClassUnderControlInContext() && !checkIfNetworkOpeningAllowed()) {
            throw new Lot49SandboxException("Forbidden to create socket");
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        super.checkConnect(host, port, context);
        if (isClassUnderControlInContext() && !checkIfNetworkOpeningAllowed()) {
            throw new Lot49SandboxException("Forbidden to create socket");
        }
    }


    /*
     * @Override public void checkCreateClassLoader() { super.checkCreateClassLoader(); if
     * (isClassUnderControlInContext()) { throw new Lot49SandboxException(
     * "Forbidden to create classloader"); } }
     */
    @Override
    public void checkExec(String cmd) {
        super.checkExec(cmd);
        if (isClassUnderControlInContext()) {
            throw new Lot49SandboxException("Forbidden do exec");
        }
    }
}
