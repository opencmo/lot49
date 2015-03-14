package com.enremmeta.rtb.spi.providers.ip.blacklist;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

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

import sun.net.util.IPAddressUtil;

/**
 * Created by amiroshn on 4/22/2016.
 */
public class IpBlackListProvider extends ProviderImpl implements Runnable {

    public static final String IP_BLACKLIST_PROVIDER_NAME = "ipblacklist";

    private IpBlackListConfig config;

    private boolean enabled;

    private Set<Long> blackListedIps = new HashSet<Long>();

    private Set<BigInteger> blackListedIpV6s = new HashSet<BigInteger>();

    private SimpleCallback cb;

    public IpBlackListProvider(ServiceRunner runner, Map configMap) {
        super(runner, configMap);
        this.config = new IpBlackListConfig(configMap);
        this.enabled = config.isEnabled();
    }

    @Override
    public String getName() {
        return IP_BLACKLIST_PROVIDER_NAME;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean isInitAsync() {
        return this.config.getInitAsync();
    }

    @Override
    public void init() throws Lot49Exception {
        initInternal(null);
    }

    @Override
    public void initAsync(SimpleCallback callback) throws Lot49Exception {
        this.cb = callback;
        ServiceRunner.getInstance().getScheduledExecutor().schedule(this, 1, TimeUnit.NANOSECONDS);
    }

    @Override
    public void run() {
        try {
            initInternal(cb);
        } catch (Lot49Exception e) {
            e.printStackTrace();
        }
    }

    private void initInternal(SimpleCallback simpleCallback) throws Lot49Exception {

        if (!this.enabled) {
            LogUtils.info("IpBlackListProvider is not enabled, doing nothing for initialization.");
            return;
        }

        try {

            info("IpBlackListProvider: Memory usage before: "
                            + Runtime.getRuntime().freeMemory() / 1000000 + "/"
                            + Runtime.getRuntime().totalMemory() / 1000000);
            long t0 = BidderCalendar.getInstance().currentTimeMillis();

            String path = this.config.getFilePath();

            GzipCompressorInputStream gzipCompressorInputStream =
                            new GzipCompressorInputStream(new FileInputStream(path));
            BufferedReader br =
                            new BufferedReader(new InputStreamReader(gzipCompressorInputStream));

            BigInteger zero = BigInteger.valueOf(0);
            String line;
            while ((line = br.readLine()) != null) {
                String[] array = line.split(" ");

                for (String ipStr : array) {
                    if (ipStr == null || ipStr.isEmpty()) {
                        LogUtils.debug("Problem during parsing of ip-file line: " + line);
                        continue;
                    }

                    if(!ipStr.contains(":")) {
                        if (!IPAddressUtil.isIPv4LiteralAddress(ipStr)) {
                            LogUtils.debug("Invalid IP: " + ipStr);
                        }
                        Long val = Utils.ipToLong(ipStr);
                        if(val != 0L) {
                            this.blackListedIps.add(val);
                        }
                    } else {
                        if (!IPAddressUtil.isIPv6LiteralAddress(ipStr)) {
                            LogUtils.debug("Invalid IPv6: " + ipStr);
                        }
                        BigInteger val = Utils.ipv6ToBigInteger(ipStr);
                        if(!val.equals(zero)) {
                            this.blackListedIpV6s.add(val);
                        }
                    }
                }

            }

            System.gc();

            info("IpBlackListProvider: Read " + this.blackListedIps.size()
                            + " entries into main map.");
            info("IpBlackListProvider: Memory usage after: "
                            + Runtime.getRuntime().freeMemory() / 1000000 + "/"
                            + Runtime.getRuntime().totalMemory() / 1000000);
            info("IpBlackListProvider: Time spent: "
                            + ((BidderCalendar.getInstance().currentTimeMillis() - t0) / 1000)
                            + " seconds.");

            if (simpleCallback != null) {
                simpleCallback.done(null);
            }
        } catch (Throwable e) {
            LogUtils.error("Error during IpBlackListProvider initialization", e);
            enabled = false;

            String errMsg = "Could not initialize IpBlackListProvider";

            if (simpleCallback != null) {
                cb.done(new Lot49Exception(errMsg, e));
            } else {
                throw new Lot49Exception(errMsg, e);
            }
        }
    }

    private IpBlackListInfoReceived getDetails(String ipStr) {
        if (!enabled) {
            return null;
        }
        if (ipStr == null) {
            return null;
        }

        boolean ret;
        if(!ipStr.contains(":")) {
            ret = blackListedIps.contains(Utils.ipToLong(ipStr));
        } else {
            ret = blackListedIpV6s.contains(Utils.ipv6ToBigInteger(ipStr));
        }

        return new IpBlackListInfoReceived(ret);
    }

    @Override
    public ProviderInfoReceived getProviderInfo(OpenRtbRequest req) {
        final Device dev = req.getDevice();
        if (dev == null) {
            return null;
        }
        return getDetails(dev.getIp());
    }

    @Override
    public ProviderInfoRequired parse(String json) throws Lot49Exception {
        try {
            return Utils.MAPPER.readValue(json, IpBlackListInfoRequired.class);
        } catch (IOException e) {
            throw new Lot49Exception(e);
        }
    }

    @Override
    public boolean match(ProviderInfoReceived rec, ProviderInfoRequired req) {

        IpBlackListInfoReceived received = (IpBlackListInfoReceived) rec;
        IpBlackListInfoRequired required = (IpBlackListInfoRequired) req;

        if (required == null) {
            return true;
        }
        if (received == null) {
            return false;
        }

        return required.isFound() == received.isFound();
    }


}
