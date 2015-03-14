package com.enremmeta.rtb.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.enremmeta.rtb.config.SecurityManagerConfig;


public class SandboxSecurityManagerTest {


    @Test
    public void construct() {
        SandboxSecurityManager sandboxSecurityManager;
        SecurityManagerConfig securityManagerConfig;

        sandboxSecurityManager = new SandboxSecurityManager(null);
        assertEquals(0, sandboxSecurityManager.getPackageWhiteList().size());
        assertEquals(0, sandboxSecurityManager.getPackageBlackList().size());
        assertEquals(0, sandboxSecurityManager.getClassWhiteList().size());
        assertEquals(0, sandboxSecurityManager.getClassBlackList().size());


        securityManagerConfig = new SecurityManagerConfig();
        sandboxSecurityManager = new SandboxSecurityManager(securityManagerConfig);
        assertEquals(0, sandboxSecurityManager.getPackageWhiteList().size());
        assertEquals(0, sandboxSecurityManager.getPackageBlackList().size());
        assertEquals(0, sandboxSecurityManager.getClassWhiteList().size());
        assertEquals(0, sandboxSecurityManager.getClassBlackList().size());


        securityManagerConfig = new SecurityManagerConfig();
        securityManagerConfig.setPackageWhiteList(Arrays.asList("com.ibm"));
        securityManagerConfig.setPackageBlackList(Arrays.asList("com.ibm.appender"));
        securityManagerConfig.setClassWhiteList(Arrays.asList("com.ibm.AClass"));
        securityManagerConfig.setClassBlackList(Arrays.asList("com.ibm.BClass"));
        sandboxSecurityManager = new SandboxSecurityManager(securityManagerConfig);
        assertEquals(1, sandboxSecurityManager.getPackageWhiteList().size());
        assertEquals(1, sandboxSecurityManager.getPackageBlackList().size());
        assertEquals(1, sandboxSecurityManager.getClassWhiteList().size());
        assertEquals(1, sandboxSecurityManager.getClassBlackList().size());
    }

    @Test
    public void isInPackageBlackList() {
        SandboxSecurityManager sandboxSecurityManager;
        SecurityManagerConfig securityManagerConfig;

        securityManagerConfig = new SecurityManagerConfig();
        securityManagerConfig.setPackageBlackList(Arrays.asList("com.ibm.appender"));
        sandboxSecurityManager = new SandboxSecurityManager(securityManagerConfig);

        try {
            sandboxSecurityManager.isInPackageBlackList("com.ibm");
            sandboxSecurityManager.isInPackageBlackList("com.ibm.appenders");
        } catch (Throwable e) {
            assertTrue(false);
        }

        try {
            sandboxSecurityManager.isInPackageBlackList("com.ibm.appender");
        } catch (Throwable e) {
            assertNotNull(e);
            assertEquals("Forbidden access to package com.ibm.appender", e.getMessage());
        }

        try {
            sandboxSecurityManager.isInPackageBlackList("com.ibm.appender.logging");
        } catch (Throwable e) {
            assertNotNull(e);
            assertEquals("Forbidden access to package com.ibm.appender.logging", e.getMessage());
        }
    }

    @Test
    public void isInPackageWhiteList() {
        SandboxSecurityManager sandboxSecurityManager;
        SecurityManagerConfig securityManagerConfig;

        securityManagerConfig = new SecurityManagerConfig();
        sandboxSecurityManager = new SandboxSecurityManager(securityManagerConfig);
        try {
            sandboxSecurityManager.isInPackageWhiteList("com.ibm");
        } catch (Throwable e) {
            assertTrue(false);
        }

        securityManagerConfig = new SecurityManagerConfig();
        securityManagerConfig.setPackageWhiteList(Arrays.asList("com.ibm.appender"));
        sandboxSecurityManager = new SandboxSecurityManager(securityManagerConfig);

        try {
            sandboxSecurityManager.isInPackageWhiteList("com.ibm");
        } catch (Throwable e) {
            assertNotNull(e);
            assertEquals("Forbidden access to package com.ibm", e.getMessage());;
        }

        try {
            sandboxSecurityManager.isInPackageWhiteList("com.ibm.appenders");
        } catch (Throwable e) {
            assertNotNull(e);
            assertEquals("Forbidden access to package com.ibm.appenders", e.getMessage());;
        }

        try {
            sandboxSecurityManager.isInPackageWhiteList("com.ibm.appender");
        } catch (Throwable e) {
            assertTrue(false);
        }

        try {
            sandboxSecurityManager.isInPackageWhiteList("com.ibm.appender.logging");
        } catch (Throwable e) {
            assertTrue(false);
        }
    }

}
