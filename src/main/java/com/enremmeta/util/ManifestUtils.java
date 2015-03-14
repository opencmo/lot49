package com.enremmeta.util;

import java.net.URL;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.enremmeta.rtb.LogUtils;

public class ManifestUtils {

    private static String DEFAULT_VALUE = "";
    public static String VERSION_KEY = "Implementation-Version";
    public static String BUILD_BRANCH_KEY = "Build-Branch";
    public static String BUILD_REVISION_KEY = "Build-Revision";
    public static String BUILD_TIME_KEY = "Build-Time";

    private static Properties PROPERTIES = null;

    public static final Properties getManifestDataInstance() {
        if (PROPERTIES == null) {
            PROPERTIES = fetchManifestData();
        }
        return PROPERTIES;
    }

    private static Properties fetchManifestData() {

        Properties props = new Properties();

        try {
            Manifest manifest = null;
            Enumeration<URL> resources = ManifestUtils.class.getClassLoader()
                            .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.toURI().toString().indexOf("Lot49") > 0) {
                    manifest = new Manifest(url.openStream());
                }
            }

            if (manifest != null) {

                for (Entry<Object, Object> entry : manifest.getMainAttributes().entrySet()) {
                    props.setProperty(entry.getKey().toString(), entry.getValue().toString());
                }

                for (Entry<String, Attributes> entry : manifest.getEntries().entrySet()) {
                    for (Entry<Object, Object> attr : entry.getValue().entrySet()) {
                        props.setProperty(attr.getKey().toString(), attr.getValue().toString());
                    }
                }

            }

        } catch (Exception e) {
            LogUtils.error("Error processing MANIFEST.MF", e);
        }

        return props;
    }

    public static final String getVersion() {
        return getManifestDataInstance().getProperty(VERSION_KEY, DEFAULT_VALUE);
    }

    public static final String getBuildBranch() {
        return getManifestDataInstance().getProperty(BUILD_BRANCH_KEY, DEFAULT_VALUE);
    }

    public static final String getBuildRevision() {
        return getManifestDataInstance().getProperty(BUILD_REVISION_KEY, DEFAULT_VALUE);
    }

    public static final String getBuildTime() {
        return getManifestDataInstance().getProperty(BUILD_TIME_KEY, DEFAULT_VALUE);
    }

    public static final String getAll() {
        return getManifestDataInstance().toString();
    }

}
