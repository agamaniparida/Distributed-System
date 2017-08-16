package com.agamani.dht_Final;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * This class in the main entry point into the system. Contains the main().
 * 
 * @author shipra
 */
public class StartClass {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        if (args.length % 2 != 0) {
            System.out.println("Invalid request.");
        }

        SharedResource sharedData = new SharedResource();
        Map<String, String> cmdInput = new HashMap<>();
        for (int i = 0; i < args.length; i = i + 2) {
            cmdInput.put(args[i], args[i + 1]);
        }

        sharedData.cmdInput = cmdInput;
        Dht dht = new Dht(sharedData);

        dht.start();
    }
}
