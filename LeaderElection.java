package com.agamani.dht_Final;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author shipra
 */
public class LeaderElection {
    private SharedResource sharedData;

    public LeaderElection(SharedResource sharedData) {
        this.sharedData = sharedData;
    }

    /**
     * Whenever new node comes in, or leader fails, this method is called.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public void checkForLeaderElection() throws IOException, InterruptedException, ExecutionException {
        System.out.println("Starting leader election on node- " + sharedData.self.nodeId);
        List<Node> higherIdNodes = new ArrayList<>();
        for (Node node : sharedData.listOfNodes) {
            if (sharedData.self.hashCode() < node.hashCode()) {
                higherIdNodes.add(node);
            }
        }

        StringBuffer sb = new StringBuffer();
        for(Node n : higherIdNodes) {
            sb.append(n.nodeId).append(" ");
        }
        if(sb.length() > 0) {
            System.out.println("Higher NodeId's on this node - " + sb.toString().trim());
        }

        // there are nodes of higher id present in the system
        if (higherIdNodes.size() != 0) {
            List<Future<String>> listOfFutureObjs = new ArrayList<>();

            // pinging all higher id nodes for starting election
            for (Node node : higherIdNodes) {
                ElectLeader electLeader = new ElectLeader(node);
                Future<String> fut = sharedData.executors.submit(electLeader);
                listOfFutureObjs.add(fut);
            }

            boolean gotOneAck = false;
            for (Future<String> fut : listOfFutureObjs) {
                while (!fut.isDone()) {
                    continue;
                }
                String response = (String) fut.get();
                if (response.equals("OK")) {
                    gotOneAck = true;
                }
            }
            if (!gotOneAck) {
                declareMyselfLeader();
            }
        } else {
            // when there are no higher id nodes in list of active nodes
            declareMyselfLeader();
        }
    }

    private void declareMyselfLeader() {
        System.out.println("I am the leader");
        // no higher id nodes are active in system
        sharedData.leader = sharedData.self;
        StringBuilder leaderUpdateMsg = new StringBuilder();
        leaderUpdateMsg.append("LEADER ");
        leaderUpdateMsg.append(sharedData.self.nodeId);
        // sending leader election result to all nodes
        for (Node node : sharedData.listOfNodes) {
            if (!node.equals(sharedData.self)) {
                System.out.println("Sending updated leader to " + node.getNodeId());
                SendElectionResult electedResult = new SendElectionResult(node, leaderUpdateMsg.toString().trim());
                sharedData.executors.submit(electedResult);
            }
        }

        // Now send the updated list of nodes to all nodes
        StringBuilder message = new StringBuilder();
        message.append("UPDATE ");
        message.append(genrateHashRingMessage());

        for (Node node : sharedData.listOfNodes) {
            if (!node.equals(sharedData.self)) {
                UpdateRing updateObj = new UpdateRing(node, message.toString());
                sharedData.executors.submit(updateObj);
            }
        }
    }

    private String genrateHashRingMessage() {
        StringBuilder activePeersList = new StringBuilder();
        for (Node n : sharedData.listOfNodes) {
            activePeersList.append(n.portNumber);
            activePeersList.append(":");
            activePeersList.append(n.hostName);
            activePeersList.append(" ");
            // Format of peer sent in list <PortNumber:hostName>
        }
        return activePeersList.toString().trim();
    }
}
