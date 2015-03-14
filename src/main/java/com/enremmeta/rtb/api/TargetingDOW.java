package com.enremmeta.rtb.api;

import org.joda.time.DateTimeConstants;

public interface TargetingDOW extends Targeting {
    /**
     * @see Ad#validate()
     * 
     * @return result of validation
     */
    String validate();

    boolean check(int day);

    int MONDAY = DateTimeConstants.MONDAY;

    int TUESDAY = DateTimeConstants.TUESDAY;

    int WEDNESDAY = DateTimeConstants.WEDNESDAY;

    int THURSDAY = DateTimeConstants.THURSDAY;

    int FRIDAY = DateTimeConstants.FRIDAY;

    int SATURDAY = DateTimeConstants.SATURDAY;

    int SUNDAY = DateTimeConstants.SUNDAY;

    TargetingDOWRange WEEKDAY = new TargetingDOWRange(MONDAY, FRIDAY);

    TargetingDOWRange WEEKEND = new TargetingDOWRange(SATURDAY, SUNDAY);
}
