package com.agamani.dht_Final;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * This class object is passed in ExecutorService. Node send "Alive" message to
 * all higher id nodes during leader election.
 * 
 * @author shipra
 *
 */
public class ElectLeader implements Callable<String> {
    MySocket socket;

    public ElectLeader(Node node) throws IOException {
        socket = new MySocket(node);
    }

    @Override
    public String call() throws Exception {
        socket.writeLine("Alive");
        // waiting for OK message from higher id nodes
        String response = socket.readLine();
        // desired response is OK
        if (response.equals(null)) {
            response = null;
        }
        socket.close();
        return response;
    }
}
