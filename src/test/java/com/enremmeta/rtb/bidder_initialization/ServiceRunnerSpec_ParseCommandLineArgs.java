package com.enremmeta.rtb.bidder_initialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.mockito.Mockito;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.util.ServiceRunner;


public class ServiceRunnerSpec_ParseCommandLineArgs {
    // should initialize with configuration file given by command line arguments

    private final String PATH_TO_CONFIG_FILE = "path_and_file";
    private Options opts;
    private ServiceRunner serviceRunnerSimpleMock;
    private String[] argv;


    @Before
    public void beforeEach() {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        argv = new String[] {"-c", PATH_TO_CONFIG_FILE};
        opts = new Options();
        opts.addOption("c", true, "Config file.");
    }


    @Test
    public void positiveFlow() throws Lot49Exception {
        // needs command line options set before call

        serviceRunnerSimpleMock.setOpts(opts);

        serviceRunnerSimpleMock.parseCommandLineArgs(argv);

        assertEquals(PATH_TO_CONFIG_FILE, serviceRunnerSimpleMock.getCl().getOptionValue("c"));

        // TODO:
        // REFACTORING IDEAS:
        // METHOD: parseCommandLineArgs: null-pointer possible for
        // line in :
        // configFilename = cl.getOptionValue("c");
        // METHOD: getConfigFilename(): public access for tests
        // desired
        // METHOD: parseCommandLineArgs: not needed throws
        // Lot49Exception
    }


    @Test
    public void noOptionsNegativeFlow() throws Lot49Exception {

        // otherwise should fail with NullPointerException
        try {
            serviceRunnerSimpleMock.parseCommandLineArgs(argv);
            fail("My method didn't throw when I expected it to");
        } catch (NullPointerException expectedException) {
        }
        // TODO:
        // REFACTORING IDEAS:
        // METHOD: parseCommandLineArgs: catch NullPointerException and re-throw
        // Lot49Exception
    }

    @Test
    public void noArgumentsNegativeFlow() throws Lot49Exception {
        // should set ::configFilename to null if no command line arguments passed

        serviceRunnerSimpleMock.setOpts(opts);

        serviceRunnerSimpleMock.parseCommandLineArgs(null);

        assertEquals(null, serviceRunnerSimpleMock.getCl().getOptionValue("c"));
        // TODO:
        // investigate if null on ::configFilename is used somehow. otherwise this is code smell

    }

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void invalidArgumentsNegativeFlow() throws Exception {
        // should type usage and exit JVM in case of wrong command line structure

        argv = new String[] {"-wrong-key", "wrong parameter"};
        serviceRunnerSimpleMock.setOpts(opts);

        exit.expectSystemExit();

        serviceRunnerSimpleMock.parseCommandLineArgs(argv);

        // TODO:
        // REFACTORING IDEAS:
        // METHOD: usage: very low testability due call to System.exit - even PowerMock can't be
        // used
        // because mocking System.exit changes logical flow of System Under Test. so useless.
        // throw exception instead.
    }
}
