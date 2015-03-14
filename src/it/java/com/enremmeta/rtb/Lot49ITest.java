package com.enremmeta.rtb;

import java.io.File;

import org.junit.Ignore;

import com.enremmeta.rtb.test.utils.Lot49TestUtils;

import junit.framework.TestCase;

@Ignore
public abstract class Lot49ITest extends TestCase {

    protected Lot49TestUtils testUtils = new Lot49TestUtils();

    protected static File getTestDataFile(String name) {
        String cwd = System.getProperty("user.dir");
        File f = new File(cwd, "src/it/resources/data/" + name);
        return f;
    }
}
