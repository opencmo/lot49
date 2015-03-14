package com.enremmeta.rtb.test.cases;

import java.io.File;

import org.junit.Ignore;

import junit.framework.TestCase;

@Ignore
public abstract class Lot49Test extends TestCase {
    protected File getTestDataFile(String name) {
        String cwd = System.getProperty("user.dir");
        File f = new File(cwd, "src/test/resources/data/" + name);
        return f;
    }
}
