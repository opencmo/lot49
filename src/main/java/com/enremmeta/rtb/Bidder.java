package com.enremmeta.rtb;

import java.io.File;
import java.io.PrintStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.Security;
import java.util.Enumeration;
import java.util.TimeZone;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

import com.enremmeta.rtb.api.BidPriceCalculatorImpl;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.impl.netty.HttpServerPipelineInitializer;
import com.enremmeta.rtb.jersey.AuctionsSvc;
import com.enremmeta.rtb.jersey.protobuf.ProtobufMessageReader;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.util.ServiceRunner;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * This is currently the main class of this package, responsible for running the whole ship. For
 * configuration documentation, see {@link Lot49Config}.
 * 
 * <h1>Environment variables</h1> TODO
 * 
 * @see Lot49Config
 *
 * @see ServiceRunner
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 *
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public final class Bidder extends ServiceRunner {

    static {
        runner = new Bidder();
    }

    private interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary) Native.loadLibrary("c", CLibrary.class);

        int getpid();
    }

    public static enum SystemExitCode {
        SYSTEM_EXIT_ERROR_INITIALIZATION(1), SYSTEM_EXIT_ERROR_HAZELCAST(2);

        SystemExitCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        private final int code;
    };

    public static final String CURRENT_VERSION;

    public static final long PID;

    static {
        String curVer = "UNKNOWN";
        try {
            URL myUrl = Bidder.class.getClassLoader().getResource(
                            Bidder.class.getPackage().getName().replaceAll("\\.", "/"));
            URI uri = myUrl.toURI();
            File f = new File(uri);
            if (f.toString().endsWith(".jar")) {
                curVer = "Do 'git info' yourself in " + f;
            } else {
                // This is most likely development environment...
                curVer = "Do 'git info' yourself in " + f;
            }
        } catch (Throwable t) {
            curVer = "Could not determine: " + t.getMessage();
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            PID = Kernel32.INSTANCE.GetCurrentProcessId();
        } else {
            PID = CLibrary.INSTANCE.getpid();
        }

        CURRENT_VERSION = curVer;
    }

    public static final long SERVICE_NUMBER;

    static {
        String ip;
        long serviceNumber = ByteBuffer.wrap(new byte[] {0, 0, 0, 0, 127, 0, 0, 1}).getLong();
        boolean found = false;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet6Address) {
                        continue;
                    }
                    byte[] b = addr.getAddress();
                    byte[] forBb = new byte[8];

                    forBb[4] = b[0];
                    forBb[5] = b[1];
                    forBb[6] = b[2];
                    forBb[7] = b[3];

                    ByteBuffer bb = ByteBuffer.wrap(forBb);
                    serviceNumber = bb.getLong();
                    found = true;
                    break;
                }
                if (found) {
                    break;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        SERVICE_NUMBER = serviceNumber;
    }

    static {
        System.out.println("Setting timezone to UTC.");
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] argv) {
        try {
            System.out.println("Parsing command line arguments.");
            runner.parseCommandLineArgs(argv);
            System.out.println("Loading config.");
            runner.loadConfig();
            runner.updateSubcriptionData();

            runBidder();
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            e.printStackTrace(System.err);
            System.out.flush();
            System.err.flush();
            try {
                LogUtils.fatal(e);
            } catch (Throwable e2) {

            }
            System.exit(SystemExitCode.SYSTEM_EXIT_ERROR_INITIALIZATION.getCode());
        }
    }

    public static void test(Lot49Config config) throws Throwable {
        runner.setConfig(config);
        runBidder();
    }

    private static void runBidder() throws Throwable {
        System.out.println("Running bidder with config: " + runner.getConfig());
        runner.initExecutors();
        runner.initOrchestrator();
        runner.initLogging();
        LogUtils.info("Starting...");
        // http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/java-dg-jvm-ttl.html
        String dnsCacheValue = "" + runner.getConfig().getDnsCacheTtlSeconds();
        String dnsCacheKey = "networkaddress.cache.ttl";
        LogUtils.info("Setting Security property " + dnsCacheKey + " to " + dnsCacheValue);
        Security.setProperty(dnsCacheKey, dnsCacheValue);
        runner.initAdapters();
        runner.scheduleLogRolloverEnforcer();
        runner.initDbServices();
        runner.initSecurityManager();
        runner.initCaches();
        runner.initGeo();
        runner.initProviders();
        runner.initIntegral();
        runner.initCodecs();
        LogUtils.info("Loading " + BidPriceCalculatorImpl.class);
        Runtime.getRuntime().addShutdownHook(((Bidder) runner).shutdownHook);
        String container = runner.getConfig().getContainer();
        switch (container.toLowerCase()) {
            case "netty":
                ((Bidder) runner).startServerNetty();
                break;
            case "jetty":
                ((Bidder) runner).startServerJetty();
                break;
            case "none":
                break;
            default:
                throw new IllegalArgumentException("Unknown container: " + container);
        }

    }

    private final Thread shutdownHook = new ShutdownHook();

    public Bidder() {
        super();

    }

    private void startServerNetty() throws Lot49Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            HttpServerPipelineInitializer initer = new HttpServerPipelineInitializer();
            b.option(ChannelOption.SO_BACKLOG, 65536);
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                            .childHandler(initer);

            Channel ch = b.bind(config.getPort()).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException ie) {
            LogUtils.error(ie);
            Thread.currentThread().interrupt();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void startServerJetty() throws Throwable {
        // Log.setLog(new JettyLogger());
        Server server = new Server();
        ServerConnector con = new ServerConnector(server);
        con.setAcceptQueueSize(config.getJettyAcceptQueueSize());
        con.setPort(config.getPort());
        con.setIdleTimeout(config.getKeepAliveTimeoutSeconds() * 1000);
        con.setSoLingerTime(0);
        ServletContextHandler context =
                        new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        ServletHolder sh = context.addServlet(ServletContainer.class, "/*");

        String svcPkgs = AuctionsSvc.class.getPackage().getName() + ";"
                        + OpenRtbRequest.class.getPackage().getName() + ";"
                        + ProtobufMessageReader.class.getPackage().getName();
        sh.setInitParameter(ServerProperties.PROVIDER_PACKAGES,
                        "com.fasterxml.jackson.jaxrs.json;" + svcPkgs);
        sh.setInitOrder(1);
        sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        sh.setInitParameter(ServerProperties.MOXY_JSON_FEATURE_DISABLE, "true");

        sh.setInitParameter(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, "true");

        if (config.isJettyTracing()) {
            sh.setInitParameter(ServerProperties.TRACING, "ALL");
            sh.setInitParameter(ServerProperties.TRACING_THRESHOLD, "VERBOSE");
        }

        server.start();
        con.start();
        String msg = "Lot49 version information:\n" + CURRENT_VERSION + "\n";
        msg += "Lot49 is listening on " + config.getPort() + "...";
        LogUtils.info(msg);
        System.out.println(msg);
        System.out.flush();
        System.setOut(new PrintStream(new OutputInterceptor()));
        System.setErr(new PrintStream(new OutputInterceptor()));

        AdXAdapter a = (AdXAdapter) ExchangeAdapterFactory.getExchangeAdapter("adx");
        a.parse("VbkSGAAB2AsKaReKAAIZn67UIYViBeAxU1_hpw", 0);
        try {
            server.join();
        } catch (InterruptedException e) {
            throw new Lot49Exception(e);
        }

    }

}
