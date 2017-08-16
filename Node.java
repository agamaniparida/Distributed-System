package com.agamani.dht_Final;

public class Node {
    String portNumber;
    String hostName;
    String nodeId;

    public Node(String portNumber, String hostName) {
        this.portNumber = portNumber;
        this.hostName = hostName;
        nodeId = portNumber + ":" + hostName;
    }

    @Override
    public int hashCode() {
        int result = hostName.hashCode();
        result = 31 * result + portNumber.hashCode();
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Node)) {
            return false;
        }
        Node obj1 = (Node) obj;
        if (!this.portNumber.equals(obj1.portNumber) || !this.hostName.equals(obj1.hostName)) {
            return false;
        }
        return true;
    }

    public String getNodeId() {
        return nodeId;
    }
}
