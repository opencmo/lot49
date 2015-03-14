package com.enremmeta.rtb;

import java.util.Map;

import com.enremmeta.rtb.config.Config;

/**
 * An Orchestrator is a component responsible for directing operations of this application with
 * regards to a particular deployment (e.g., {@link LocalOrchestrator stand-alone development}, in
 * {@link AwsOrchestrator AWS cloud}, etc.)
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class OrchestratorConfig implements Config {

    private static final long serialVersionUID = 4977834836014850372L;

    public static final int DEFAULT_AWS_FALLBACK_NUMBER_OF_PEERS = 3;

    /**
     * {@link AwsOrchestrator#getNumberOfPeers() Number of peers} to use if an AWS API call fails.
     * If this number is negative, maybe indeed fail out.
     */
    public int getAwsFallbackNumberOfPeers() {
        return awsFallbackNumberOfPeers;
    }

    public void setAwsFallbackNumberOfPeers(int awsFallbackNumberOfPeers) {
        this.awsFallbackNumberOfPeers = awsFallbackNumberOfPeers;
    }

    private int awsFallbackNumberOfPeers = DEFAULT_AWS_FALLBACK_NUMBER_OF_PEERS;

    public static final int DEFAULT_AWS_TTL_PEER_CHECK_MINUTES = 30;


    public int getAwsDefaultTtlPeerCheckMinutes() {
        return awsDefaultTtlPeerCheckMinutes;
    }

    public void setAwsDefaultTtlPeerCheckMinutes(int awsDefaultTtlPeerCheckMinutes) {
        this.awsDefaultTtlPeerCheckMinutes = awsDefaultTtlPeerCheckMinutes;
    }

    private int awsDefaultTtlPeerCheckMinutes = DEFAULT_AWS_TTL_PEER_CHECK_MINUTES;


    private String awsRoleToAssume;

    public String getAwsRoleToAssume() {
        return awsRoleToAssume;
    }

    public void setAwsRoleToAssume(String awsRoleToAssume) {
        this.awsRoleToAssume = awsRoleToAssume;
    }

    private String deployType;

    private Map<String, String> awsTagsOverride;

    /**
     * Not recommended!
     */
    public Map<String, String> getAwsTagsOverride() {
        return awsTagsOverride;
    }

    public void setAwsTagsOverride(Map<String, String> awsTagsOverride) {
        this.awsTagsOverride = awsTagsOverride;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    private String staticNodeId;

    public String getStaticNodeId() {
        return staticNodeId;
    }

    public void setStaticNodeId(String staticNodeId) {
        this.staticNodeId = staticNodeId;
    }

    private String tagName;
    private String lot49ElbName;
    private String bElbName;

    private String lot49LeaderIp;
    private String bLeaderIp;

    public String getLot49LeaderIp() {
        return lot49LeaderIp;
    }

    public void setLot49LeaderIp(String lot49LeaderIp) {
        this.lot49LeaderIp = lot49LeaderIp;
    }

    public String getbLeaderIp() {
        return bLeaderIp;
    }

    public void setbLeaderIp(String bLeaderIp) {
        this.bLeaderIp = bLeaderIp;
    }

    /**
     * AWS tag name whose value is the name of the ELB this deployment runs under (should be either
     * {@link #getLot49ElbName()} or {@link #getbElbName()}
     */
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getLot49ElbName() {
        return lot49ElbName;
    }

    public void setLot49ElbName(String lot49ElbName) {
        this.lot49ElbName = lot49ElbName;
    }

    public String getbElbName() {
        return bElbName;
    }

    public void setbElbName(String bElbName) {
        this.bElbName = bElbName;
    }

    private String awsSecretKey;

    private String awsAccessKey;

    /**
     * For use with CloudWatch.
     */
    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    /**
     * For use with CloudWatch.
     */
    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }
}
