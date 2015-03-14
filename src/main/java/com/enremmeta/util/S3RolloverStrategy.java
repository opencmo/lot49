package com.enremmeta.util;

import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

/**
 * A rollover strategy to move files to s3.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public final class S3RolloverStrategy extends DefaultRolloverStrategy {

    protected S3RolloverStrategy(int minIndex, int maxIndex, boolean useMax, int compressionLevel,
                    StrSubstitutor subst) {
        super(minIndex, maxIndex, useMax, compressionLevel, subst);
    }

    @Override
    public RolloverDescription rollover(RollingFileManager manager) throws SecurityException {
        // TODO Auto-generated method stub
        return super.rollover(manager);
    }

}
