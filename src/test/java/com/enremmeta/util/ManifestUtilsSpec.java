package com.enremmeta.util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ManifestUtilsSpec {
    
    @BeforeClass
    public static void setUp() throws IOException{
        File manifest = new File("target/classes/META-INF/MANIFEST.MF");
        if (!manifest.exists()) {
            manifest.createNewFile();
        }

        FileWriter fw = new FileWriter(manifest.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(       "Implementation-Version: 7.7.7\n"
                        + "Build-Branch: HAPPINESS\n"
                        + "Build-Revision: 777\n"
                        + "Build-Time: Build-Time: 2008-01-18 06:53:13\n");
        bw.close();
    }
    
    @AfterClass
    public static void tearDown(){
        File manifest = new File("target/classes/META-INF/MANIFEST.MF");
        if (manifest.exists()) {
            manifest.delete();
        }
    }

    @Ignore("desn't work with Cobertura")
    @Test
    public void testgGetBuildBranch() {
        assertEquals("HAPPINESS", ManifestUtils.getBuildBranch());
    }
    
    @Ignore("desn't work with Cobertura")
    @Test
    public void testGetVersion() {
        assertEquals("7.7.7", ManifestUtils.getVersion());
    }
    
    @Ignore("desn't work with Cobertura")
    @Test
    public void testGetBuildRevision() {
        assertEquals("777", ManifestUtils.getBuildRevision());
    }
    
    @Ignore("desn't work with Cobertura")
    @Test
    public void testGetBuildTime() {
        assertEquals("Build-Time: 2008-01-18 06:53:13", ManifestUtils.getBuildTime());
    }

}
