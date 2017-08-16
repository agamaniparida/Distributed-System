package com.agamani.dht_Final;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class contains the server side code of a node
 * @author shipra
 */
public class Server implements Runnable {
    private int port;
    private final int backlog = 100;
    SharedResource sharedData;

    public Server(SharedResource sharedData) throws IOException {
        this.sharedData = sharedData;
        String portStr = sharedData.cmdInput.get("-myPort");
        port = Integer.parseInt(portStr); //converts the string into integer
        sharedData.serverSocket = new ServerSocket(port, backlog); //starts a server at specified port# with specified backlog
    }

    @Override
    public void run() {
        while (true) {
            Socket sockFd;
            try {
                sockFd = sharedData.serverSocket.accept();
                
                if (sockFd != null) {
                    if (sockFd != null) {
                        Thread requestThread = new Thread(new RequestHandler(sharedData, sockFd));
                        requestThread.start();
                    }
                }
            } catch(SocketException e) {
            	System.out.println("Socket closed exiting.");
            	break;
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
