package com.enremmeta.rtb.api;

import java.util.Map;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;

public abstract class BidPriceCalculatorImpl implements BidPriceCalculator {

    private String id;
    private String name;

    protected double getScore(Map<String, Map<String, String>> segments, String segment) {
        Map<String, String> userData = segments.get(segment);
        if (userData != null) {
            String scoreStr = userData.get(USER_DATA_SCORE_KEY);
            if (scoreStr != null) {
                try {
                    return Double.parseDouble(scoreStr);
                } catch (NumberFormatException nfe) {
                    LogUtils.error("Error parsing score " + scoreStr + " from " + userData);
                    return 0.;
                }
            }
        }
        return 0.;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public BidPriceCalculatorImpl() throws Lot49Exception {
        String[] classNameElts = getClass().getSimpleName().split("_");
        if (classNameElts.length != 2) {
            throw new Lot49Exception("Incorrect naming of " + getClass().getName()
                            + ", expected BidPriceCalculator_<Id>");
        }
        if (!classNameElts[0].equals("BidPriceCalculator")) {
            throw new Lot49Exception("Incorrect naming of " + getClass().getName()
                            + ", expected BidPriceCalculator_<Id>");
        }
        this.id = classNameElts[1];
        this.name = this.id;

    }

}
