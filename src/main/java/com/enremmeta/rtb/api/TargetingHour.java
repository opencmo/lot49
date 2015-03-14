package com.enremmeta.rtb.api;

public interface TargetingHour {

    String validate();

    boolean check(int hour);

    TargetingHourRange NIGHT = new TargetingHourRange(0, 5);

    TargetingHourRange MORNING = new TargetingHourRange(6, 11);

    TargetingHourRange AFTERNOON = new TargetingHourRange(12, 17);

    TargetingHourRange EVENING = new TargetingHourRange(18, 23);
}
