package com.agamani.dht_Final;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * This class object is passed in ExecutorService for heart beat.
 * 
 * @author shipra
 *
 */
public class HeartBeatMessage implements Callable<HeartBeatResult> {
    private Node node;
    private String message;

    public HeartBeatMessage(Node node, String message) {
        this.node = node;
        this.message = message;
    }

    @Override
    public HeartBeatResult call() {
        MySocket socket;
        try {
            socket = new MySocket(node);
            socket.writeLine(message);
            String response = socket.readLine();
            socket.close();
            if (response != null && response.equals("OK")) {
                return new HeartBeatResult(true, node);
            } else {
                return new HeartBeatResult(false, node);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage() + " meaning node is lost.");
        }
        return new HeartBeatResult(false, node);
    }
}