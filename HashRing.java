package com.agamani.dht_Final;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @author shipra
 */
public class HashRing {
    private NavigableMap<Long, Node> hashRingNodes;
    List<Node> listOfNodes;

    public HashRing(List<Node> listOfNodes) throws NoSuchAlgorithmException {
        hashRingNodes = new TreeMap<>();
        this.listOfNodes = listOfNodes;
        createHashRing();
    }

    /**
     * Creates a hash ring using the list of active nodes
     * 
     * @throws NoSuchAlgorithmException
     */
    private void createHashRing() throws NoSuchAlgorithmException {
        for (Node node : listOfNodes) {
            for (int i = 1; i <= 100; i++) {
                for (int kNum = 0; kNum <= 3; kNum++) {
                    long hashValue = hash(node.getNodeId() + i, kNum);
                    hashRingNodes.put(hashValue, node);
                }
            }
        }

    }

    /**
     * ketama hashing algorithm used for consistent hashing and mapping keys to
     * nodes.
     * 
     * @param nodeId
     * @param ketama
     * @return
     * @throws NoSuchAlgorithmException
     */
    public Long hash(final String nodeId, final int ketama) throws NoSuchAlgorithmException {

        byte[] digest = MessageDigest.getInstance("MD5").digest(nodeId.getBytes(StandardCharsets.UTF_8));
        final Long ketamaNumber = ((long) (digest[3 + ketama * 4] & 0xFF) << 24) | ((long) (digest[2 + ketama * 4] & 0xFF) << 16) | ((long) (digest[1 + ketama * 4] & 0xFF) << 8)
                | (digest[ketama * 4] & 0xFF);

        return ketamaNumber;
    }

    /**
     * @param key
     * @return the node in hashRing to which given key is mapped
     * @throws NoSuchAlgorithmException
     */
    public Node getNode(String key) throws NoSuchAlgorithmException {
        if (hashRingNodes.isEmpty()) {
            return null;
        }
        long ceilKey;
        long sumHashOfKey = 0;
        for (int kNum = 0; kNum <= 3; kNum++) {
            sumHashOfKey += hash(key, kNum);
        }
        long avgHashOfKey = sumHashOfKey / 4;
        if (avgHashOfKey > hashRingNodes.lastKey()) {
            ceilKey = hashRingNodes.firstKey();
        } else {
            ceilKey = hashRingNodes.ceilingKey(avgHashOfKey);
        }
        return hashRingNodes.get(ceilKey);
    }

    /**
     * @param key
     * @return the replica node where the given key to be stored.
     * @throws NoSuchAlgorithmException
     */
    public Node getReplicaNode(String key) throws NoSuchAlgorithmException {
        if (hashRingNodes.isEmpty()) {
            return null;
        }
        long replicaNode;
        long sumHashOfKey = 0;
        for (int kNum = 0; kNum <= 3; kNum++) {
            sumHashOfKey += hash(key, kNum);
        }
        long avgHashOfKey = sumHashOfKey / 4;
        if (avgHashOfKey < hashRingNodes.firstKey()) {
            replicaNode = hashRingNodes.lastKey();
        } else {
            replicaNode = hashRingNodes.lowerKey(avgHashOfKey);
        }

        return hashRingNodes.get(replicaNode);
    }
}
