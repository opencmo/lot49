package com.enremmeta.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class BidderCalendar {

    DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");

    private static final BidderCalendar INSTANCE = new BidderCalendar();

    private int seconds;

    private int minutes;

    private int hours;

    private BidderCalendar() {
        reset();
    }

    public static final BidderCalendar getInstance() {
        return INSTANCE;
    }

    public final long currentTimeMillis() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        calendar.add(Calendar.HOUR, hours);
        calendar.add(Calendar.MINUTE, minutes);
        calendar.add(Calendar.SECOND, seconds);

        return calendar.getTimeInMillis();
    }

    public final long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }

    public final Date currentDate() {
        return new Date(currentTimeMillis());
    }

    public final String currentDateString() {
        return new Date(currentTimeMillis()).toString();
    }

    public final DateTime currentDateTime() {
        return new DateTime(currentTimeMillis(), DateTimeZone.UTC);
    }

    public final DateTime toDateTime(String dateTime) {
        return new DateTime(dateTime, DateTimeZone.UTC);
    }

    public final DateTime toDateTime(Long dateTime) {
        return new DateTime(dateTime, DateTimeZone.UTC);
    }

    public void addSeconds(final int amount) {
        seconds += amount;
    }

    public void addMinutes(final int amount) {
        minutes += amount;
    }

    public void addHours(final int amount) {
        hours += amount;
    }

    public void reset() {
        seconds = 0;
        minutes = 0;
        hours = 0;
    }

    public String toString() {
        final DateTime now = new DateTime(currentTimeMillis(), DateTimeZone.UTC);
        return now.toString(DTF);
    }

}
