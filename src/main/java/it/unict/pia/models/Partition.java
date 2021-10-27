package it.unict.pia.models;

import java.util.*;
import java.util.stream.Collectors;

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

        Map<Integer, Set<Node>> mapPartition = new HashMap<>();

        for (Map.Entry<String, Node> entry : graph.getNodesMap().entrySet()) {
            int p = entry.getValue().getPartition();
            if (!mapPartition.containsKey(p)) mapPartition.put(p, new HashSet<>());
            mapPartition.get(p).add(entry.getValue());
        }

        for (Map.Entry<Integer, Set<Node>> partition : mapPartition.entrySet()) {
            this.partition.add(partition.getValue());
        }
    }

    public ArrayList<Set<Node>> getPartition() {
        return partition;
    }

    public void setPartition(ArrayList<Set<Node>> partition) {
        this.partition = partition;
    }

    public void relocateNode(Node n, int partitionFrom, int partitionTo) {
        this.partition.set(partitionFrom, this.partition.get(partitionFrom)
                .stream()
                .filter(x -> !x.getId().equals(n.getId()))
                .collect(Collectors.toSet())
        );

        n.setPartition(partitionTo);
        this.partition.get(partitionTo).add(n);
    }

    public void addPartition(int index, Set<Node> nodes) {
        if (this.partition.size() > index) this.partition.set(index, nodes);
        else this.partition.add(nodes);
    }

    public static Partition copyOf(Partition p) {
        ArrayList<Set<Node>> partition = new ArrayList<>();
        p.getPartition().forEach(x -> partition.add(new HashSet<>(x)));

        return new Partition(partition);
    }
}
