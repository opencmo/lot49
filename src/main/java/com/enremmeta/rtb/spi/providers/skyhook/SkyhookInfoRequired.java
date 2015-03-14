package com.enremmeta.rtb.spi.providers.skyhook;

import java.util.List;

import com.enremmeta.rtb.spi.providers.ProviderInfoRequired;

public class SkyhookInfoRequired implements ProviderInfoRequired {
    public SkyhookInfoRequired() {
        super();
    }

    private List<List<Integer>> inputAllTimeCategoryList;

    private List<List<Integer>> inputResidentialCategoryList;
    private List<List<Integer>> inputBusinessCategoryList;

    public List<List<Integer>> getInputAllTimeCategoryList() {
        return inputAllTimeCategoryList;
    }

    public void setInputAllTimeCategoryList(List<List<Integer>> inputAllTimeCategoryList) {
        this.inputAllTimeCategoryList = inputAllTimeCategoryList;
    }

    public List<List<Integer>> getInputResidentialCategoryList() {
        return inputResidentialCategoryList;
    }

    public void setInputResidentialCategoryList(List<List<Integer>> inputResidentialCategoryList) {
        this.inputResidentialCategoryList = inputResidentialCategoryList;
    }

    public List<List<Integer>> getInputBusinessCategoryList() {
        return inputBusinessCategoryList;
    }

    public void setInputBusinessCategoryList(List<List<Integer>> inputBusinessCategoryList) {
        this.inputBusinessCategoryList = inputBusinessCategoryList;
    }

}
