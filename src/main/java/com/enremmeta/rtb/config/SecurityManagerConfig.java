package com.enremmeta.rtb.config;


import java.util.List;
import java.util.Map;

public class SecurityManagerConfig implements Config {

    private List<String> packageBlackList;

    private List<String> packageWhiteList;

    private List<String> classBlackList;

    private List<String> classWhiteList;

    private Map<String, List<String>> networkAllowedMethodsMap;


    public List<String> getPackageBlackList() {
        return packageBlackList;
    }

    public void setPackageBlackList(List<String> packageBlackList) {
        this.packageBlackList = packageBlackList;
    }

    public List<String> getPackageWhiteList() {
        return packageWhiteList;
    }

    public void setPackageWhiteList(List<String> packageWhiteList) {
        this.packageWhiteList = packageWhiteList;
    }

    public List<String> getClassBlackList() {
        return classBlackList;
    }

    public void setClassBlackList(List<String> classBlackList) {
        this.classBlackList = classBlackList;
    }

    public List<String> getClassWhiteList() {
        return classWhiteList;
    }

    public void setClassWhiteList(List<String> classWhiteList) {
        this.classWhiteList = classWhiteList;
    }

    public Map<String, List<String>> getNetworkAllowedMethodsMap() {
        return networkAllowedMethodsMap;
    }

    public void setNetworkAllowedMethodsMap(Map<String, List<String>> networkAllowedMethodsMap) {
        this.networkAllowedMethodsMap = networkAllowedMethodsMap;
    }
}
