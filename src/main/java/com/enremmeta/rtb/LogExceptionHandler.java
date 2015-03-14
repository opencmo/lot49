package com.enremmeta.rtb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;

import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.util.BidderCalendar;
import com.lmax.disruptor.ExceptionHandler;

public class LogExceptionHandler<T> implements ExceptionHandler<T> {

    private static PrintWriter writer;

    private static final AtomicLong incidentCount = new AtomicLong(0);

    public static void init() throws Lot49Exception {
        try {
            String home = System.getenv(KVKeysValues.ENV_LOT49_HOME);
            System.out.println(KVKeysValues.ENV_LOT49_HOME + ": " + home);
            File logDir;
            if (home == null || home.length() == 0) {
                logDir = new File("/tmp");
            } else {
                logDir = new File(home, "log");
            }

            File fname = new File(logDir, "oops.log");
            System.out.println("Writing to " + fname);
            writer = new PrintWriter(new FileWriter(fname));
            writer.println(BidderCalendar.getInstance().currentDate() + ": Hello.");
            writer.flush();
        } catch (IOException ioe) {
            throw new Lot49Exception(ioe);
        }
    }

    public LogExceptionHandler() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleEventException(Throwable arg0, long arg1, T arg2) {
        writer.println(BidderCalendar.getInstance().currentDate() + ": "
                        + incidentCount.incrementAndGet());
        if (arg0 != null) {
            arg0.printStackTrace(writer);
        }
        writer.println(arg1);
        writer.println(arg2);
        writer.flush();

    }

    @Override
    public void handleOnShutdownException(Throwable arg0) {
        writer.println(BidderCalendar.getInstance().currentDate() + ": shutdown");
        if (arg0 != null) {
            arg0.printStackTrace(writer);
        }
        writer.flush();


    }

    @Override
    public void handleOnStartException(Throwable arg0) {
        writer.println(BidderCalendar.getInstance().currentDate() + ": start");
        if (arg0 != null) {
            arg0.printStackTrace(writer);
        }
    }

}
