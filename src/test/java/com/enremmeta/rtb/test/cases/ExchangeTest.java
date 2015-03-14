package com.enremmeta.rtb.test.cases;

import java.io.File;

import org.junit.After;
import org.junit.Before;

import com.amazonaws.util.StringInputStream;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.test.utils.Lot49TestUtils;
import com.enremmeta.util.Utils;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

public abstract class ExchangeTest extends Lot49Test {

    protected Lot49TestUtils testUtils = new Lot49TestUtils();

    protected String loadContents(File f) throws Exception {
        return Utils.readFile(f);
    }

    protected Object getJsonObject(String name) throws Exception {
        File path = getTestDataFile(name);
        System.out.println("Path for request " + name + ": " + path);
        String content = loadContents(path);
        System.out.println("Loaded:\n\n" + content + "\n\n");
        Object o = Utils.MAPPER.readValue(content, Object.class);
        return o;
    }

    protected Message getProtobufObject(String name, Message.Builder builder) throws Exception {
        File path = getTestDataFile(name);
        System.out.println("Path for request " + name + ": " + path);
        String jsonContent = loadContents(path);
        System.out.println("Loaded:\n\n" + jsonContent + "\n\n");
        JsonFormat jf = new JsonFormat();
        jf.merge(new StringInputStream(jsonContent), builder);
        Message msg = builder.build();
        return msg;
    }

    @Before
    public void setUp() throws Exception {
        // Lot49Config config = (Lot49Config) Utils.loadConfig(configFilename, Lot49Config.class);
        Lot49Config config = new Lot49Config();
        config.setBaseUrl("http://localhost:10000");
        config.setStatsUrl("http://localhost:10000");

        testUtils.setConfig(config);
        // testUtils.loadConfig();
        // ServiceRunner bidder = Bidder.getInstance();
        // bidder.initExecutors();
        // testUtils.refreshFileAdCache();
        // AdCache cache = testUtils.getAdCache();
    }

    @After
    public void tearDown() throws Exception {}

}
