package com.enremmeta.rtb.spi.providers.skyhook;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
public class SkyhookProviderSpec_match {

    private ServiceRunner serviceRunnerMock;
    private SkyhookProvider skyHook;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();

        skyHook = new SkyhookProvider(serviceRunnerMock, new HashMap() {
            {
                put("enabled", true);
            }
        });
    }

    @Test
    public void testSkyhookInfoRequired_isNull() {
        assertTrue(skyHook.match(null, null));
        assertTrue(skyHook.match(new SkyhookInfoReceived(null, true, true), null));
    }

    @Test
    public void testProviderInfoReceivedGot_isNull() {
        assertFalse(skyHook.match(null, new SkyhookInfoRequired()));
    }

    @Test
    public void testRequereGetInputAllTimeCategoryList_isNull() {
        assertFalse(skyHook.match(new SkyhookInfoReceived(null, true, true),
                        new SkyhookInfoRequired()));
    }

    @Test
    public void testCatLists_areIdentical() {
        SkyhookInfoReceived rec = new SkyhookInfoReceived(new HashSet<Integer>() {
            {
                add(101);
            }
        }, true, true);


        SkyhookInfoRequired req = new SkyhookInfoRequired();
        req.setInputAllTimeCategoryList(new LinkedList<List<Integer>>() {
            {
                add(new LinkedList<Integer>() {
                    {
                        add(101);
                    }
                });
            }
        });
        assertTrue(skyHook.match(rec, req));
    }

    @Test
    public void testCatLists_areNotIdentical() {
        SkyhookInfoReceived rec = new SkyhookInfoReceived(new HashSet<Integer>() {
            {
                add(101);
            }
        }, true, true);


        SkyhookInfoRequired req = new SkyhookInfoRequired();
        req.setInputAllTimeCategoryList(new LinkedList<List<Integer>>() {
            {
                add(new LinkedList<Integer>() {
                    {
                        add(102);
                    }
                });
            }
        });
        assertFalse(skyHook.match(rec, req));
    }

    @Test
    public void testCatLists2_areIdentical() {
        SkyhookInfoReceived rec = new SkyhookInfoReceived(new HashSet<Integer>() {
            {
                add(101);
            }
        }, true, true);


        SkyhookInfoRequired req = new SkyhookInfoRequired();
        req.setInputBusinessCategoryList(new LinkedList<List<Integer>>() {
            {
                add(new LinkedList<Integer>() {
                    {
                        add(101);
                    }
                });
            }
        });
        assertTrue(skyHook.match(rec, req));
    }

    @Test
    public void testCatLists2_areNotIdentical() {
        SkyhookInfoReceived rec = new SkyhookInfoReceived(new HashSet<Integer>() {
            {
                add(101);
            }
        }, true, true);


        SkyhookInfoRequired req = new SkyhookInfoRequired();
        req.setInputBusinessCategoryList(new LinkedList<List<Integer>>() {
            {
                add(new LinkedList<Integer>() {
                    {
                        add(102);
                    }
                });
            }
        });
        assertFalse(skyHook.match(rec, req));
    }

    @Test
    public void testCatLists3_areIdentical() {
        SkyhookInfoReceived rec = new SkyhookInfoReceived(new HashSet<Integer>() {
            {
                add(101);
            }
        }, true, true);


        SkyhookInfoRequired req = new SkyhookInfoRequired();
        req.setInputResidentialCategoryList(new LinkedList<List<Integer>>() {
            {
                add(new LinkedList<Integer>() {
                    {
                        add(101);
                    }
                });
            }
        });
        assertTrue(skyHook.match(rec, req));
    }

    @Test
    public void testCatLists3_areNotIdentical() {
        SkyhookInfoReceived rec = new SkyhookInfoReceived(new HashSet<Integer>() {
            {
                add(101);
            }
        }, true, true);


        SkyhookInfoRequired req = new SkyhookInfoRequired();
        req.setInputResidentialCategoryList(new LinkedList<List<Integer>>() {
            {
                add(new LinkedList<Integer>() {
                    {
                        add(102);
                    }
                });
            }
        });
        assertFalse(skyHook.match(rec, req));
    }

}
