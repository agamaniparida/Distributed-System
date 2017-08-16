package com.agamani.dht_Final;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This class handles all requests identified by the request type, that the node
 * receives from its peers or client.
 * 
 * @author shipra
 */
public class RequestHandler implements Runnable {
    private MySocket socket;
    private SharedResource sharedData;

    public RequestHandler(SharedResource sharedData, Socket sockFd) throws IOException {
        // opens input and output stream on given socket
        this.socket = new MySocket(sockFd);
        this.sharedData = sharedData;
    }

    @Override
    public void run() {
        try {
            String messageRecv = socket.readLine();
            if (!messageRecv.equals(null)) {
                String[] msgArray = messageRecv.split("\\s");
                System.out.println("Got request of type: " + msgArray[0]);

                switch (msgArray[0]) {

                case "PING":
                    handlePing();
                    break;

                case "GET":
                    handleGet(messageRecv, msgArray);
                    break;

                case "REPLICA":
                    handleReplica(messageRecv, msgArray);
                    break;

                case "PUT":
                    handlePut(messageRecv, msgArray);
                    break;

                case "JOIN":
                    handleJoin(msgArray);
                    break;

                case "GET_RING":
                    sendRingToClient();
                    break;

                case "UPDATE":
                    // this node receives Update message from leader
                    updateMyRing(msgArray);
                    break;

                case "Alive":
                    replyToLeaderElectMsg();
                    break;

                case "LEADER":
                    updateMyLeader(msgArray);
                    break;

                case "GET_LEADER":
                    getLeader();
                    break;

                case "LEAVE":
                    if (sharedData.self.equals(sharedData.leader)) {
                        handleLeave(msgArray);
                    } else {
                        // redirecting all leaving requests to leader
                        MySocket socketToLeader = new MySocket(sharedData.leader);
                        socketToLeader.writeLine(messageRecv);
                        socketToLeader.close();
                    }
                    break;

                case "U_LEAVE":
                    selfLeave();
                    if (sharedData.serverSocket != null) {
                        sharedData.serverSocket.close();
                    }
                    break;

                case "INITIATE_LEADER_ELECTION":
                    startElection();
                    break;
                }
            }
        } catch (IOException | NoSuchAlgorithmException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    /**
     * Only handles case when node receives message from leader to start
     * election. This is case when leader node is being taken out of system
     * intentionally
     */
    private void startElection() {
        LeaderElection leaderElection = new LeaderElection(sharedData);
        try {
            leaderElection.checkForLeaderElection();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This node is leaving
     * 
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void selfLeave() throws NoSuchAlgorithmException, IOException {
        for (String key : sharedData.data.keySet()) {
            try {
                Node node = sharedData.hashRing.getNode(key);
                String message = "PUT " + key + " " + sharedData.data.get(key);
                MySocket socketToNode = new MySocket(node);
                socketToNode.writeLine(message);
                socketToNode.close();

            } catch (ArrayIndexOutOfBoundsException e1) {
                System.out.println("Exception thrown  :" + e1);
                e1.printStackTrace();
            }
        }

    }

    /**
     * @return the active node with the highest id in hash ring
     */
    public Node getHighestIdActiveNode() {
        Node highestIdNode = sharedData.listOfNodes.get(0);
        for (int i = 1; i < sharedData.listOfNodes.size(); i++) {
            Node currentNode = sharedData.listOfNodes.get(i);
            if (currentNode.hashCode() > highestIdNode.hashCode()) {
                highestIdNode = currentNode;
            }
        }
        return highestIdNode;
    }

    /**
     * leave request will be sent to leader
     * 
     * @param msgArray
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void handleLeave(String[] msgArray) throws NoSuchAlgorithmException, IOException {
        String[] leavingNodeStr = msgArray[1].split(":");
        Node leavingNode = new Node(leavingNodeStr[0], leavingNodeStr[1]);

        for (int i = 0; i < sharedData.listOfNodes.size(); i++) {
            if (leavingNode.equals(sharedData.listOfNodes.get(i))) {
                sharedData.listOfNodes.remove(leavingNode);
                System.out.println("Node Removed");
                HashRing hashRing = new HashRing(sharedData.listOfNodes);
                sharedData.hashRing = hashRing;
                sendUpdateRingMsg();
                break;
            }
        }

        if (leavingNode.equals(sharedData.self)) {
            for (Node n : sharedData.listOfNodes) {
                MySocket socket = new MySocket(n);
                // handles only the case when leader is leaving (not failing);
                // leader tells next higher id node to start election
                socket.writeLine("INITIATE_LEADER_ELECTION");
                socket.close();
            }
            selfLeave();
            sharedData.shutdown();
        } else {
            MySocket socketToLeavingNode = new MySocket(leavingNode);
            String leaveMessage = "U_LEAVE";
            socketToLeavingNode.writeLine(leaveMessage);
            socketToLeavingNode.close();
        }
    }

    private void getLeader() {
        socket.writeLine(sharedData.leader.nodeId);
    }

    /**
     * When node receives election result from the leader, it updates the new
     * leader.
     * 
     * @param msgArray
     */
    private void updateMyLeader(String[] msgArray) {
        String[] leaderNodeId = msgArray[1].split(":");
        Node leaderNode = new Node(leaderNodeId[0], leaderNodeId[1]);
        sharedData.leader = leaderNode;
    }

    /**
     * Whenever state of system changes in churning, node get update message
     * from its leader and it updates its record.
     * 
     * @param msgArray
     * @throws NoSuchAlgorithmException
     */
    private void updateMyRing(String[] msgArray) throws NoSuchAlgorithmException {
        List<Node> nodeListReceived = new ArrayList<>();
        for (int i = 1; i < msgArray.length; i++) {
            String[] host = msgArray[i].split(":");
            Node node = new Node(host[0], host[1]);
            nodeListReceived.add(node);
        }
        sharedData.listOfNodes = nodeListReceived;
        HashRing hashRing = new HashRing(sharedData.listOfNodes);
        sharedData.hashRing = hashRing;
    }

    /**
     * If this node is the leader, it generates the update message consisting of
     * all active nodes in the system.
     * 
     * @return List of active nodes in a system
     */
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

    private void sendRingToClient() {
        socket.writeLine(genrateHashRingMessage());

    }

    /**
     * When a new node joins in, it sends JOIN request to the leader of the
     * system.
     * 
     * @param msgArray
     * @throws NoSuchAlgorithmException
     */
    private void handleJoin(String[] msgArray) throws NoSuchAlgorithmException {
        String portNumber = msgArray[1];
        String hostname = msgArray[2];
        // node object of new incoming node
        Node node = new Node(portNumber, hostname);

        sharedData.listOfNodes.add(node);
        HashRing hashRing = new HashRing(sharedData.listOfNodes);
        sharedData.hashRing = hashRing;
        sendRingToClient();

        // update the nodes in ring to all except himself
        sendUpdateRingMsg();
    }

    private void sendUpdateRingMsg() {
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

    /**
     * Stores data at replica of node
     * 
     * @param messageRecv
     * @param msgArray
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void handleReplica(String messageRecv, String[] msgArray) throws NoSuchAlgorithmException, IOException {
        String key = msgArray[1];
        String value = msgArray[2];
        sharedData.data.put(key, value);
        socket.writeLine("OK");
    }

    /**
     * This function handles PUT request.
     * 
     * @param messageRecv
     * @param msgArray
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void handlePut(String messageRecv, String[] msgArray) throws NoSuchAlgorithmException, IOException {
        String key = msgArray[1];
        String value = msgArray[2];
        Node node = sharedData.hashRing.getNode(key);
        // handle null node case
        if (node.equals(sharedData.self)) {
            sharedData.data.put(key, value);
            // Send replica message to next node as well
            StringBuilder replicaMsg = new StringBuilder();
            replicaMsg.append("REPLICA ");
            replicaMsg.append(key + " ");
            replicaMsg.append(value);

            Node replicaNode = sharedData.hashRing.getReplicaNode(key);
            System.out.println("trying to connect to replica " + replicaNode.nodeId);
            MySocket socketToNode = new MySocket(replicaNode);
            socketToNode.writeLine(replicaMsg.toString().trim());
            socketToNode.close();

        } else {
            System.out.println("trying to connect to " + node.nodeId);
            MySocket socketToNode = new MySocket(node);
            socketToNode.writeLine(messageRecv);
            socketToNode.close();
        }

        socket.writeLine("OK");
    }

    /**
     * This method handles GET request.
     * 
     * @param messageRecv
     * @param msgArray
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void handleGet(String messageRecv, String[] msgArray) throws NoSuchAlgorithmException, IOException {
        String key = msgArray[1];
        String value = null;
        Node node = sharedData.hashRing.getNode(key);
        if (node.equals(sharedData.self)) {
            value = sharedData.data.get(key);
        } else {
            MySocket socketToNode = new MySocket(node);
            socketToNode.writeLine(messageRecv);
            value = socketToNode.readLine();
            socketToNode.close();
        }
        socket.writeLine(value);
    }

   
    private void replyToLeaderElectMsg() throws IOException, InterruptedException, ExecutionException {
        socket.writeLine("OK");
        LeaderElection leader = new LeaderElection(sharedData);
        leader.checkForLeaderElection();
    }

    /**
     * Node replies to every heart beat message
     */
    private void handlePing() {
        socket.writeLine("OK");
    }
}
