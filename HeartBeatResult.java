package com.agamani.dht_Final;

/**
 * @author shipra
 */
public class HeartBeatResult {
    public boolean success;
    public Node node;

    public HeartBeatResult(boolean success, Node node) {
        this.success = success;
        this.node = node;
    }
}
