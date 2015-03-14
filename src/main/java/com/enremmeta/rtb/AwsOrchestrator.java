package com.enremmeta.rtb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TagDescription;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.util.BidderCalendar;
import com.enremmeta.util.Utils;

/**
 * An Orchestrator for Amazon.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public final class AwsOrchestrator implements Orchestrator {

    public static final String DEPLOY_TYPE = "aws";

    @Override
    public String getDeployType() {
        return DEPLOY_TYPE;
    }

    public AwsOrchestrator(OrchestratorConfig config) throws Lot49Exception {
        super();
        this.config = config;
        numberOfPeers = config.getAwsFallbackNumberOfPeers();

        this.akey = config.getAwsAccessKey();

        this.skey = config.getAwsSecretKey();

        try {
            this.nodeId = discoverNodeId();
        } catch (Throwable t) {
            throw new Lot49Exception(t);
        }

        final Map<String, String> env = System.getenv();
        ec2Endpoint = env.get(KVKeysValues.ENV_EC2_ENDPOINT);
        if (ec2Endpoint == null) {
            throw new Lot49Exception("Expected environment variable "
                            + KVKeysValues.ENV_EC2_ENDPOINT + " to be set.");
        }

        elbEndpoint = env.get(KVKeysValues.ENV_ELB_ENDPOINT);
        if (elbEndpoint == null) {
            throw new Lot49Exception("Expected environment variable "
                            + KVKeysValues.ENV_ELB_ENDPOINT + " to be set.");
        }
        String tagKey = config.getTagName();

        if (akey == null) {
            LogUtils.info("AUTHENTICATION: AWS orchestrator is using ROLES.");

            InstanceProfileCredentialsProvider ipcp = new InstanceProfileCredentialsProvider();
            client = new AmazonEC2Client(ipcp);
            elbClient = new AmazonElasticLoadBalancingClient(ipcp);
        } else {
            LogUtils.info("AUTHENTICATION: AWS orchestrator is using KEYS.");
            this.creds = new BasicAWSCredentials(akey, skey);
            client = new AmazonEC2Client(this.creds);
            elbClient = new AmazonElasticLoadBalancingClient(this.creds);
        }

        client.setEndpoint(ec2Endpoint);
        String myElb = null;
        if (config.getAwsTagsOverride() == null) {

            System.out.println("Attempting to get tags for " + nodeId);

            DescribeTagsRequest descTagsReq = new DescribeTagsRequest();
            descTagsReq = descTagsReq.withFilters(new Filter("resource-id").withValues(nodeId));
            DescribeTagsResult descTagsRes = null;
            try {
                descTagsRes = client.describeTags(descTagsReq);

            } catch (AmazonServiceException ase) {
                System.err.println("Error getting tags: " + ase.getServiceName() + ": "
                                + ase.getErrorCode() + " " + ase.getErrorType() + " "
                                + ase.getErrorMessage());
                throw ase;

            }
            final List<TagDescription> tags = descTagsRes.getTags();

            if (tags.size() > 0) {
                System.out.println("Received tags: " + tags);
                for (TagDescription tagDesc : tags) {
                    final String tagKey2 = tagDesc.getKey();
                    final String tagValue2 = tagDesc.getValue();
                    System.out.println("Comparing required key <" + tagKey + "> to received <"
                                    + tagKey2 + ">.");

                    if (!tagKey.equals(tagKey2)) {
                        continue;
                    }
                    myElb = tagValue2;
                    break;
                }
            }
        } else {
            final Map<String, String> tags = config.getAwsTagsOverride();
            System.out.println("Overriding tags with " + tags);
            myElb = tags.get(tagKey);
        }

        if (myElb == null) {
            throw new Lot49Exception("Expected ELB name as tag value for tag '" + tagKey + "'");
        }

        if (myElb.equals(config.getLot49ElbName())) {
            this.isBidder = true;
            this.elbName = myElb;
        } else if (myElb.equals(config.getbElbName())) {
            this.isBidder = false;
            this.elbName = myElb;
        } else {
            throw new Lot49Exception("Expected ELB name as tag value for tag '" + tagKey
                            + "' to be one of '" + config.getLot49ElbName() + "' or '"
                            + config.getbElbName() + "', received '" + myElb + "'");
        }
        System.out.println("My ELB is: " + elbName);
        System.out.println("Am I a bidder? " + this.isBidder);

    }

    private final String elbName;

    @Override
    public String getNodeId() {
        return nodeId;
    }

    private final String elbEndpoint;

    private final String ec2Endpoint;

    private final String nodeId;

    private String discoverNodeId() throws Throwable {

        String nodeId = Utils.readUrl("http://169.254.169.254/latest/meta-data/instance-id");

        return nodeId;
    }

    private final boolean isBidder;

    @Override
    public boolean isBidder() {
        return isBidder;
    }

    private int previousNumberOfPeers = -1;

    private final OrchestratorConfig config;
    private final String skey;
    private final String akey;

    private AWSCredentials creds = null;
    private final AmazonElasticLoadBalancingClient elbClient;
    private final AmazonEC2Client client;

    private final static Object apiSynchronizer = new Object();

    private long lastAwsApiCallTs = 0;

    private int numberOfPeers = OrchestratorConfig.DEFAULT_AWS_FALLBACK_NUMBER_OF_PEERS;

    private int getNumberOfPeers(String loadBalancerName) throws Lot49Exception {
        elbClient.setEndpoint(elbEndpoint);
        List<String> loadBalancerNames = Arrays.asList(new String[] {loadBalancerName});
        try {
            DescribeLoadBalancersResult res = null;
            synchronized (apiSynchronizer) {
                long ts = BidderCalendar.getInstance().currentTimeMillis();
                boolean canCall = false;
                if (lastAwsApiCallTs > 0) {
                    long millisPassed = ts - lastAwsApiCallTs;
                    long minsPassed = millisPassed / 1000 / 60;
                    canCall = minsPassed > config.getAwsDefaultTtlPeerCheckMinutes();
                } else {
                    canCall = true;
                }
                if (canCall) {
                    lastAwsApiCallTs = ts;
                    res = elbClient.describeLoadBalancers(
                                    new DescribeLoadBalancersRequest(loadBalancerNames));

                    List<LoadBalancerDescription> descs = res.getLoadBalancerDescriptions();
                    numberOfPeers = descs.get(0).getInstances().size();

                } else {
                    LogUtils.warn("Not enough time getting number of peers, falling back on previous result "
                                    + numberOfPeers);

                }
            }
        } catch (Throwable t) {
            LogUtils.warn("Error getting number of peers, falling back on " + numberOfPeers, t);

        }
        return numberOfPeers;
    }

    @Override
    public int getNumberOfPeers() throws Lot49Exception {
        return getNumberOfPeers(this.elbName);
    }


    @Override
    public String getRegion() {
        return System.getenv(KVKeysValues.ENV_AWS_REGION);
    }

}
