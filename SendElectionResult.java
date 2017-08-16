package com.agamani.dht_Final;

import java.util.concurrent.Callable;

/**
 * @author shipra
 */
public class SendElectionResult implements Callable<Boolean>{
    private Node node;
    private String leader;
    
    //this is for updating all nodes in hashRing about newly elected leader
    public SendElectionResult(Node node, String message) {
        this.node = node;
        leader = message;
    }

    @Override
    public Boolean call() throws Exception {
        MySocket socket = new MySocket(node);
        socket.writeLine(leader);
        socket.close();
        return true;
    }
}
