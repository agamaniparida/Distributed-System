package com.agamani.dht_Final;

import java.util.concurrent.Callable;

/**
 * @author shipra
 */
public class UpdateRing implements Callable<Boolean>{
    private Node node;
    private String listOfActivePeers;

    //this is for updating all nodes in hashRing about current active nodes in a system
    public UpdateRing(Node node, String listOfActivePeers) {
        this.node = node;
        this.listOfActivePeers = listOfActivePeers;
    }

    @Override
    public Boolean call() throws Exception {
        //this is for updating all nodes in hashRing about newly elected leader
    	MySocket socket = new MySocket(node);
        socket.writeLine(listOfActivePeers);
        socket.close();
        return true;
    }
}
