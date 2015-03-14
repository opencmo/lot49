package com.enremmeta.rtb.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IABCategoriesSpec {

    @Test
    public void shouldGetIABCodeByCategoryName() {
        IABCategories iabCategories = new IABCategories();
        assertEquals("IAB_20_25", iabCategories.getIabCategoryId("Theme Parks"));
        assertEquals("IAB_17_15", iabCategories.getIabCategoryId("Golf"));
        assertEquals("IAB_13_7", iabCategories.getIabCategoryId("Investing"));
    }

}
