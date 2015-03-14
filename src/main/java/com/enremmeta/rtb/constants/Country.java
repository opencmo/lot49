package com.enremmeta.rtb.constants;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public enum Country {

    UnitedStates("united states of america", "us");
    /**
     * The state's name.
     */
    private String name;

    /**
     * The state's abbreviation.
     */
    private String abbreviation;

    /**
     * The set of states addressed by abbreviations.
     */
    private static final Map<String, Country> COUNTRIES_BY_ABBR = new HashMap<String, Country>();

    /* static initializer */
    static {
        for (Country country : values()) {
            COUNTRIES_BY_ABBR.put(country.getAbbreviation().toLowerCase(), country);
        }
    }

    /**
     * Constructs a new state.
     *
     * @param name
     *            the state's name.
     * @param abbreviation
     *            the state's abbreviation.
     */
    Country(String name, String abbreviation) {
        this.name = name;
        this.abbreviation = abbreviation;
    }

    /**
     * Returns the state's abbreviation.
     *
     * @return the state's abbreviation.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Gets the enum constant with the specified abbreviation.
     *
     * @param abbr
     *            the state's abbreviation.
     * @return the enum constant with the specified abbreviation.
     */
    public static Country valueOfAbbreviation(final String abbr) {
        if (abbr == null) {
            return null;
        }
        return COUNTRIES_BY_ABBR.get(abbr.toLowerCase());
    }

    public static String nameByAbbreviation(final String abbr) {
        Country country = valueOfAbbreviation(abbr);
        return country == null ? null : country.name;
    }

    public static Country valueOfName(final String name) {
        final String enumName = StringUtils.replace(name.toLowerCase(), " ", "_");
        try {
            return valueOf(enumName);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
