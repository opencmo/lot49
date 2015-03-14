package com.enremmeta.rtb;

/**
 * Service for orchestration -- to work with other elements of the deployment.
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */
public interface Orchestrator {

    String getRegion();

    String getDeployType();

    public int getNumberOfPeers() throws Lot49Exception;

    // public int getNumberOfPeers(String loadBalancerName) throws Lot49Exception;

    public boolean isBidder();

    public String getNodeId();
}
