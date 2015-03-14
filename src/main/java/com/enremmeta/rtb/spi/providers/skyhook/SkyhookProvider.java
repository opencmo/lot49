package com.enremmeta.rtb.spi.providers.skyhook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.spi.providers.ProviderImpl;
import com.enremmeta.rtb.spi.providers.ProviderInfoReceived;
import com.enremmeta.rtb.spi.providers.ProviderInfoRequired;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.SimpleCallback;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.InetAddresses;

/**
 * 
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class SkyhookProvider extends ProviderImpl implements Runnable {
    private final SkyhookConfig config;

    public static final String SKYHOOK_PROVIDER_NAME = "skyhook";

    @Override
    public String getName() {
        return SKYHOOK_PROVIDER_NAME;
    }

    public SkyhookProvider(ServiceRunner runner, Map configMap) {
        super(runner, configMap);
        this.config = new SkyhookConfig(configMap);
        this.enabled = config.isEnabled();

    }

    private boolean enabled;

    public SkyhookInfoReceived getDetails(String ipAddr) {
        if (!enabled) {
            return null;
        }
        if (ipAddr == null) {
            return null;
        }
        if (ipAddr.trim().length() == 0) {
            return null;
        }
        InetAddress addr = InetAddresses.forString(ipAddr);
        int ip = InetAddresses.coerceToInteger(addr);
        SkyhookInfoReceived d = map.get(ip);
        return d;
    }

    private Map<Integer, SkyhookInfoReceived> map = new HashMap<Integer, SkyhookInfoReceived>();

    private final BufferedReader getBufferedReader(File f) throws IOException {
        InputStreamReader ir;
        FileInputStream fis = new FileInputStream(f);
        InputStream is;
        if (f.getName().endsWith(".gz") || f.getName().endsWith(".gzip")) {
            is = new GZIPInputStream(fis);
        } else {
            is = fis;
        }
        ir = new InputStreamReader(is);
        return new BufferedReader(ir);
    }

    @Override
    public boolean isInitAsync() {
        return true;
    }

    private class InitCallback implements SimpleCallback {
        public Throwable t;
        public boolean isDone;

        @Override
        public void done(Throwable t) {
            if (t != null) {
                this.t = t;
            }
            isDone = true;
        }
    }

    public void init() throws Lot49Exception {
        InitCallback cb = new InitCallback();
        initAsync(cb);
        while (true) {
            try {
                if (cb.isDone) {
                    if (cb.t != null) {
                        if (cb.t instanceof Lot49Exception) {
                            throw (Lot49Exception) cb.t;
                        } else {
                            throw new Lot49Exception(cb.t);
                        }
                    }
                    break;
                }
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

    }

    public void initAsync(SimpleCallback callback) throws Lot49Exception {
        this.cb = callback;
        ServiceRunner.getInstance().getScheduledExecutor().schedule(this, 1, TimeUnit.NANOSECONDS);
    }

    private SimpleCallback cb;

    public void run() {

        // System.out.println("Entering Utils.loadConfig()");

        if (!enabled) {
            LogUtils.info("Skyhook is not enabled, doing nothing for initialization.");
            return;
        }
        info("Skyhook: Memory usage before: " + Runtime.getRuntime().freeMemory() / 1000000 + "/"
                        + Runtime.getRuntime().totalMemory() / 1000000);
        long t0 = BidderCalendar.getInstance().currentTimeMillis();

        String skyhookMap = config.getSkyhookMap();
        int lineNo = -1;
        File curFile = null;

        try {

            if (skyhookMap != null) {
                LogUtils.info("Reading serialized map from " + skyhookMap);
                ObjectInputStream ois =
                                new ObjectInputStream(new FileInputStream(new File(skyhookMap)));
                this.map = (Map) ois.readObject();
                ois.close();
            } else {
                JsonFactory jsonFactory = new JsonFactory();
                jsonFactory.configure(Feature.ALLOW_COMMENTS, true);
                ObjectMapper mapper = new ObjectMapper(jsonFactory);

                // Map<String, Integer> ipToGrid = new HashMap<String,
                // Integer>();
                Map<Long, SkyhookInfoReceived> gridToDetails =
                                new HashMap<Long, SkyhookInfoReceived>();
                String curJson = "";

                curFile = new File(config.getFullGrid());
                BufferedReader br = getBufferedReader(curFile);

                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    lineNo++;
                    if (line.indexOf("NumberLong") > -1) {
                        // Stupid git
                        line = line.replace("NumberLong(\"", "").replace("\")", "");
                    }

                    curJson += line;
                    curJson = curJson.trim();

                    if (curJson.startsWith("{") && curJson.endsWith("}")) {
                        FullGridEntryBean entry =
                                        mapper.readValue(curJson, FullGridEntryBean.class);
                        final Set<Integer> catSet = new HashSet<Integer>();
                        if (entry.getCategories() != null) {
                            catSet.addAll(entry.getCategories());
                        }
                        SkyhookInfoReceived d = new SkyhookInfoReceived(catSet,
                                        entry.getActiveForBusinesslHours() == 1,
                                        entry.getActiveForResidentialHours() == 1);
                        gridToDetails.put(entry.getGridId(), d);
                        curJson = "";
                    }
                }
                br.close();
                LogUtils.info("Skyhook: Read " + gridToDetails.size() + " entries from fullGrid.");
                curFile = new File(config.getSkyhook());
                br = getBufferedReader(curFile);
                lineNo = 0;
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    lineNo++;
                    if (line.indexOf("NumberLong") > -1) {
                        // Stupid git
                        line = line.replace("NumberLong(\"", "").replace("\")", "");
                    }
                    SkyhookEntryBean entry = mapper.readValue(line, SkyhookEntryBean.class);
                    String ipStr = entry.getIpAddress();
                    InetAddress addr = InetAddresses.forString(ipStr);

                    int ip = InetAddresses.coerceToInteger(addr);
                    SkyhookInfoReceived d = gridToDetails.get(entry.getGridId());

                    map.put(ip, d);
                }
            }
            System.gc();
            info("Skyhook: Read " + map.size() + " entries into main map.");
            info("Skyhook: Memory usage after: " + Runtime.getRuntime().freeMemory() / 1000000 + "/"
                            + Runtime.getRuntime().totalMemory() / 1000000);
            info("Skyhook: Time spent: "
                            + ((BidderCalendar.getInstance().currentTimeMillis() - t0) / 1000)
                            + " seconds.");
            cb.done(null);
        } catch (Throwable e) {
            enabled = false;
            String errMsg = "Could not initialize Skyhook";
            if (lineNo > -1) {
                errMsg += "error at line "

                                + lineNo + " of " + curFile;
            }

            cb.done(new Lot49Exception(errMsg, e));
        }
    }

    @Override
    public ProviderInfoReceived getProviderInfo(OpenRtbRequest req) {
        final Device dev = req.getDevice();
        if (dev == null) {
            return null;
        }
        final String ip = dev.getIp();
        return getDetails(ip);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public static void main(String[] argv) throws Exception {
        Options opts = new Options();
        opts.addOption("c", true, "Command (one of: 'prep'");
        opts.addOption("g", true, "Full grid path");
        opts.addOption("s", true, "Skyhook path");
        opts.addOption("o", true, "Output path");
        Map configMap = new HashMap();
        configMap.put("enabled", true);
        configMap.put("required", true);
        CommandLineParser parser = new PosixParser();
        CommandLine cl;

        cl = parser.parse(opts, argv);
        String op = cl.getOptionValue("c");
        switch (op) {
            case "prep":
                String skyhookPath = cl.getOptionValue("s");
                configMap.put("skyhook", skyhookPath);
                String fullGridPath = cl.getOptionValue("g");
                configMap.put("fullGrid", fullGridPath);
                SkyhookProvider me = new SkyhookProvider(null, configMap);
                me.init();
                String outFile = cl.getOptionValue("o");
                System.out.println("Writing to " + outFile);
                ObjectOutputStream oos =
                                new ObjectOutputStream(new FileOutputStream(new File(outFile)));
                oos.writeObject(me.map);
                oos.close();
                break;
            default:
                System.err.println("Unknown operation '" + op + "'");
                System.exit(1);
        }
    }

    @Override
    public boolean match(ProviderInfoReceived received, ProviderInfoRequired required) {
        SkyhookInfoReceived got = (SkyhookInfoReceived) received;
        SkyhookInfoRequired needed = (SkyhookInfoRequired) required;
        if (needed == null) {
            return true;
        }
        if (got == null) {
            return false;
        }
        Set<Integer> gotCats = got.getCats();
        if (needed.getInputAllTimeCategoryList() != null) {
            for (List<Integer> list : needed.getInputAllTimeCategoryList()) {
                boolean listOk = true;
                for (int cat : list) {
                    if (cat < 0) {
                        if (gotCats.contains(cat)) {
                            return false;
                        }
                    } else if (!gotCats.contains(cat)) {
                        listOk = false;
                        break;
                    }
                }
                if (listOk) {
                    return true;
                }
            }
        }
        if (needed.getInputBusinessCategoryList() != null
                        && !needed.getInputBusinessCategoryList().isEmpty()) {
            if (!got.isBusinessActive()) {
                return false;
            }
            for (List<Integer> list : needed.getInputBusinessCategoryList()) {
                boolean listOk = true;
                for (int cat : list) {
                    if (cat < 0) {
                        if (gotCats.contains(cat)) {
                            return false;
                        }
                    } else if (!gotCats.contains(cat)) {
                        listOk = false;
                        break;
                    }
                }
                if (listOk) {
                    return true;
                }
            }
        }
        if (needed.getInputResidentialCategoryList() != null
                        && !needed.getInputResidentialCategoryList().isEmpty()) {
            if (!got.isResidentActive()) {
                return false;
            }
            for (List<Integer> list : needed.getInputResidentialCategoryList()) {
                boolean listOk = true;
                for (int cat : list) {
                    if (cat < 0) {
                        if (gotCats.contains(cat)) {
                            return false;
                        }
                    } else if (!gotCats.contains(cat)) {
                        listOk = false;
                        break;
                    }
                }
                if (listOk) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ProviderInfoRequired parse(String json) throws Lot49Exception {
        try {
            return Utils.MAPPER.readValue(json, SkyhookInfoRequired.class);
        } catch (IOException e) {
            throw new Lot49Exception(e);
        }
    }
}
