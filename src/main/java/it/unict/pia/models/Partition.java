package it.unict.pia.models;

import java.util.*;

public class Partition {

    private ArrayList<Set<Node>> partition;
    private Set<Node> p0, p1;

    public Partition() {
        this.partition = new ArrayList<>();
        this.p0 = new HashSet<>();
        this.p1 = new HashSet<>();
    }

    public Partition(ArrayList<Set<Node>> partition) {
        this.partition = partition;
        this.p0 = this.partition.get(0);
        this.p1 = this.partition.get(1);
    }

    public Partition(Graph graph) {
        ArrayList<Set<Node>> partitions = new ArrayList<>();
        Set<Node> nodeSetP0 = graph.nodeSet();
        Set<Node> nodeSetP1 = graph.nodeSet();

        for (Map.Entry<String, Node> entry : graph.getNodesMap().entrySet()) {
            if (entry.getValue().isPartitionP0()) nodeSetP0.add(entry.getValue());
            else nodeSetP1.add(entry.getValue());
        }

        this.partition = partitions;
        this.p0 = nodeSetP0;
        this.p1 = nodeSetP1;
    }

    public ArrayList<Set<Node>> getPartition() {
        return partition;
    }

    public void setPartition(ArrayList<Set<Node>> partition) {
        this.partition = partition;
    }

    public Set<Node> getP0() {
        return p0;
    }

    public Set<Node> getP1() {
        return p1;
    }

    public void relocateNode(Node n) {
        if (this.p0.contains(n)) {  // P0 --> P1
            this.p1.add(n);
            this.p0.remove(n);
        } else {                    // P1 --> P0
            this.p0.add(n);
            this.p1.remove(n);
        }

        this.partition = new ArrayList<>();
        this.partition.add(this.p0);
        this.partition.add(this.p1);
    }
}
