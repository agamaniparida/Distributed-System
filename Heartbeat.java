package com.agamani.dht_Final;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This is implementation of heart beat functionality.
 * 
 * @author shipra
 */
public class Heartbeat implements Runnable {
    private SharedResource sharedData;

    public Heartbeat(SharedResource sharedData) throws IOException {
        this.sharedData = sharedData;
    }

    public void run() {
        System.out.println("Inside heartbeat thread.");
        List<Future<HeartBeatResult>> futureList = new ArrayList<>();
        for (Node node : sharedData.hashRing.listOfNodes) {
            if (node.equals(sharedData.self)) {
                continue;
            }
            HeartBeatMessage hbResult = new HeartBeatMessage(node, "PING");
            futureList.add(sharedData.executors.submit(hbResult));
        }

        boolean startElection = false;
        boolean updateRing = false;
        List<Node> lostNodes = new ArrayList<>();
        for (Future<HeartBeatResult> future : futureList) {
            while (!future.isDone()) {
                continue;
            }
            try {
                HeartBeatResult result = future.get();
                if (!result.success) {
                    // if lost node was leader then Election needs to be held
                    if (result.node.equals(sharedData.leader)) {
                        startElection = true;
                        lostNodes.add(result.node);
                    } else {
                        // otherwise update the hash ring because node is lost
                        lostNodes.add(result.node);
                        updateRing = true;
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (lostNodes.isEmpty()) {
            System.out.println(sharedData.self.nodeId + " Heartbeat check over - all good.");
        }

        if (startElection) {
            System.out.println(sharedData.self.nodeId + " Lost leader node, election needs to happen.");
            sharedData.listOfNodes.removeAll(lostNodes);

            LeaderElection leaderElection = new LeaderElection(sharedData);
            try {
                leaderElection.checkForLeaderElection();
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (updateRing && sharedData.self.equals(sharedData.leader)) {
            System.out.println(sharedData.self.nodeId + " Lost a node, updating hash ring.");
            sharedData.listOfNodes.removeAll(lostNodes);
            try {
                sharedData.hashRing = new HashRing(sharedData.listOfNodes);
            } catch (NoSuchAlgorithmException e) {
                System.out.println(e);
            }

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