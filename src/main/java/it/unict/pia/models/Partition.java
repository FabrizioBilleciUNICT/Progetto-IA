package it.unict.pia.models;

import java.util.*;

public class Partition {

    private ArrayList<Set<Node>> partition;

    public Partition() {
        this.partition = new ArrayList<>();
    }

    public Partition(ArrayList<Set<Node>> partition) {
        this.partition = partition;
    }

    public Partition(Graph graph) {
        this.partition = new ArrayList<>();
        Set<Node> nodeSetP0 = new HashSet<>(); // graph.nodeSet();
        Set<Node> nodeSetP1 = new HashSet<>(); // graph.nodeSet();

        for (Map.Entry<String, Node> entry : graph.getNodesMap().entrySet()) {
            if (entry.getValue().isPartitionP0()) nodeSetP0.add(entry.getValue());
            else nodeSetP1.add(entry.getValue());
        }

        this.partition.add(nodeSetP0);
        this.partition.add(nodeSetP1);
    }

    public ArrayList<Set<Node>> getPartition() {
        return partition;
    }

    public void setPartition(ArrayList<Set<Node>> partition) {
        this.partition = partition;
    }

    public void relocateNode(Node n) {
        if (this.partition.get(0).contains(n)) {  // P0 --> P1
            this.partition.get(1).add(n);
            this.partition.get(0).remove(n);
        } else {                    // P1 --> P0
            this.partition.get(0).add(n);
            this.partition.get(1).remove(n);
        }
    }

    public void addPartition(int index, Set<Node> nodes) {
        if (this.partition.size() > index) this.partition.set(index, nodes);
        else this.partition.add(nodes);
    }
}
