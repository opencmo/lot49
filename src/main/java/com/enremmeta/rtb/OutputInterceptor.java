package com.enremmeta.rtb;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class OutputInterceptor extends OutputStream {

    public OutputInterceptor() {
        super();
    }

    private StringBuilder exception = new StringBuilder(8192);

    private char prevChar;

    private boolean inStackTrace = false;

    private final Map<String, Long> previouslyLogged = new HashMap<String, Long>();

    @Override
    public void write(int b) throws IOException {
        // char c = (char) b;
        // if (c == ' ' && prevChar == ' ') {
        // return;
        // }
        //
        // sb.append(c);
        // if (c == '\n') {
        // final String str = sb.toString();
        // if (str.indexOf("Exception") > -1) {
        // exception.append(str);
        // inStackTrace = true;
        // sb.delete(0, sb.length());
        // } else if (inStackTrace && str.startsWith("\tat ")) {
        // inStackTrace = true;
        // exception.append(str);
        // sb.delete(0, sb.length());
        // } else if (inStackTrace) {
        // final long curTime = BidderCalendar.getInstance().currentTimeMillis();
        // final String toLog = exception.toString();
        // final long lastLogged = previouslyLogged
        // .getOrDefault(toLog, 0l);
        // final long diff = curTime - lastLogged;
        // if (diff > 10000) {
        // LogUtils.error(toLog);
        // }
        // previouslyLogged.put(str, curTime);
        // inStackTrace = false;
        // exception.delete(0, sb.length());
        // sb.delete(0, sb.length());
        // } else {
        // inStackTrace = false;
        // exception.delete(0, sb.length());
        // sb.delete(0, sb.length());
        // }
        //
        // }
        // prevChar = c;
    }

    private StringBuilder sb = new StringBuilder(8192);

}
