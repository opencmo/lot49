package com.enremmeta.rtb.config;

import java.util.List;

import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;

/**
 * Part of {@link Lot49Config configuration} responsible for configuring {@link Hazelcast}.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class HazelcastServiceConfig extends DbConfig {

    private boolean embedded = true;

    private String groupName;
    private String groupPassword;
    private List<String> addresses;

    private boolean useL0Cache = false;

    public boolean isUseL0Cache() {
        return useL0Cache;
    }

    public void setUseL0Cache(boolean useL0Cache) {
        this.useL0Cache = useL0Cache;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupPassword() {
        return groupPassword;
    }

    public void setGroupPassword(String groupPassword) {
        this.groupPassword = groupPassword;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public String getAwsTagValue() {
        return awsTagValue;
    }

    public void setAwsTagValue(String awsTagValue) {
        this.awsTagValue = awsTagValue;
    }

    public HazelcastServiceConfig() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Default value for {@link #getConnTimeoutSeconds() connTimeoutSeconds}.
     */
    public static final int DEFAULT_CONN_TIMEOUT_SECONDS = 20;

    /**
     * @see #getConnTimeoutSeconds()
     */
    private int connTimeoutSeconds = DEFAULT_CONN_TIMEOUT_SECONDS;

    /**
     * Defaults to {@link #DEFAULT_CONN_TIMEOUT_SECONDS}.
     */
    public int getConnTimeoutSeconds() {
        return connTimeoutSeconds;
    }

    public void setConnTimeoutSeconds(int connTimeoutSeconds) {
        this.connTimeoutSeconds = connTimeoutSeconds;
    }

    private String awsRegion = null;

    public String getAwsRegion() {
        return awsRegion;
    }

    private String awsTagKey;

    public String getAwsTagKey() {
        return awsTagKey;
    }

    public void setAwsTagKey(String awsTagKey) {
        this.awsTagKey = awsTagKey;
    }

    private String awsTagValue;

    private String awsSecurityGroupName;

    private String managementUrl;

    public String getAwsSecurityGroupName() {
        return awsSecurityGroupName;
    }

    public void setAwsSecurityGroupName(String awsSecurityGroupName) {
        this.awsSecurityGroupName = awsSecurityGroupName;
    }

    public String getManagementUrl() {
        return managementUrl;
    }

    public void setManagementUrl(String managementUrl) {
        this.managementUrl = managementUrl;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    private String awsSecretKey;

    private String awsAccessKey;

    private int heapPercentage;

    /**
     * For {@link AwsConfig}
     * 
     * @see AwsConfig
     */
    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    /**
     * For {@link AwsConfig}
     * 
     * @see AwsConfig
     */
    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    /**
     * Constraint for {@link MaxSizeConfig}
     */
    public int getHeapPercentage() {
        return heapPercentage;
    }

    public void setHeapPercentage(int heapPercentage) {
        this.heapPercentage = heapPercentage;
    }

}
