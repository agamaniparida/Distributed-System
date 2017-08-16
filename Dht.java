package com.agamani.dht_Final;

import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author shipra
 */
public class Dht {
    SharedResource sharedData;

    public Dht(SharedResource sharedData) {
        this.sharedData = sharedData;
    }

    /**
     * This function is called when new node joins.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public void start() throws IOException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        final Thread serverThread = new Thread(new Server(sharedData));
        serverThread.start();

        final String reqType = sharedData.cmdInput.get("-reqType");
        //When first node starts, it creates DHT, after first node, all other nodes joins in
        if (reqType.equals("CREATE_DHT")) {
            Node node = new Node(sharedData.cmdInput.get("-myPort"), InetAddress.getLocalHost().getCanonicalHostName());
            sharedData.self = node;
            sharedData.listOfNodes.add(node);
            HashRing hashRing = new HashRing(sharedData.listOfNodes);
            sharedData.hashRing = hashRing;
            sharedData.leader = sharedData.self;
        } else if (reqType.equals("JOIN")) {
            Node meNode = new Node(sharedData.cmdInput.get("-myPort"), InetAddress.getLocalHost().getCanonicalHostName());
            sharedData.self = meNode;

            Node node = new Node(sharedData.cmdInput.get("-peerPort"), sharedData.cmdInput.get("-peerHost"));
            // client socket is created.
            MySocket socket = new MySocket(node);

            String myPort = sharedData.cmdInput.get("-myPort");

            socket.writeLine("JOIN " + myPort + " " + InetAddress.getLocalHost().getCanonicalHostName());

            String messageFromPeer = socket.readLine();
            List<Node> peerNodeList = new ArrayList<>();
            if (!messageFromPeer.equals(null)) {
                String[] nodeList = messageFromPeer.split("\\s");
                for (int i = 0; i < nodeList.length; i++) {
                    String[] pnode = nodeList[i].split(":");
                    Node peerNode = new Node(pnode[0], pnode[1]);
                    peerNodeList.add(peerNode);
                }
                sharedData.listOfNodes = peerNodeList;
                LeaderElection leaderElection = new LeaderElection(sharedData);
                leaderElection.checkForLeaderElection();
            }
        } else {
            sharedData.shutdown();
            return;
        }
        
        final Heartbeat hb = new Heartbeat(sharedData);
        sharedData.scheduledExecutor.scheduleAtFixedRate(hb, 0, 30, TimeUnit.SECONDS);
    }
}
