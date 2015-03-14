package com.enremmeta.rtb;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * For local deployment (e.g., dev servers).
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public final class LocalOrchestrator implements Orchestrator {

    private final OrchestratorConfig config;

    public LocalOrchestrator(OrchestratorConfig config) {
        super();
        this.config = config;
        final String staticNodeId = config.getStaticNodeId();

        if (staticNodeId != null && !staticNodeId.trim().equals("")) {
            this.nodeId = staticNodeId;
        } else {
            String nodeTmp;
            try {
                nodeTmp = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                nodeTmp = "LOCALNODE";
            }
            this.nodeId = nodeTmp;
        }
    }

    private final String nodeId;

    @Override
    public int getNumberOfPeers() {
        return 1;
    }

    //
    // @Override
    // public int getNumberOfPeers(String loadBalancerName) {
    // return 1;
    // }

    @Override
    public boolean isBidder() {
        return true;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    public static final String DEPLOY_TYPE = "local";

    @Override
    public String getDeployType() {
        return DEPLOY_TYPE;
    }

    @Override
    public String getRegion() {
        return "local";
    }

}
