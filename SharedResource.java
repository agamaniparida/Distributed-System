package com.agamani.dht_Final;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author shipra
 */
public class SharedResource {
    Map<String, String> cmdInput;
    List<Node> listOfNodes;
    Map<String, String> data;
    Node self;
    ExecutorService executors;
    ScheduledExecutorService scheduledExecutor;
    HashRing hashRing;
    Node leader;
    ServerSocket serverSocket;

    public SharedResource() throws NoSuchAlgorithmException {
        listOfNodes = new ArrayList<>();
        data = new HashMap<>();
        executors = Executors.newFixedThreadPool(10);
        hashRing = new HashRing(listOfNodes);
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void shutdown() {
        System.out.println("Shutdown called.");
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (executors != null) {
            executors.shutdown();
        }
    }
}
