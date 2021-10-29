package it.unict.pia.models;

import java.util.*;
import java.util.stream.Collectors;

public class Partition {

    private Map<Integer, Set<Node>> partition;

    public Partition() {
        this.partition = new HashMap<>();
    }

    public Partition(Map<Integer, Set<Node>> partition) {
        this.partition = partition;
    }

    public Partition(Graph graph) {
        this.partition = new HashMap<>();

        for (Map.Entry<String, Node> entry : graph.getNodesMap().entrySet()) {
            int p = entry.getValue().getPartition();
            if (!partition.containsKey(p)) partition.put(p, new HashSet<>());
            partition.get(p).add(entry.getValue());
        }
    }

    public Map<Integer, Set<Node>> getPartition() {
        return partition;
    }

    public void setPartition(Map<Integer, Set<Node>> partition) {
        this.partition = partition;
    }

    public void relocateNode(Node n, int partitionFrom, int partitionTo) {
        this.partition.put(partitionFrom, this.partition.get(partitionFrom)
                .stream()
                .filter(x -> !x.getId().equals(n.getId()))
                .collect(Collectors.toSet())
        );

        n.setPartition(partitionTo);
        this.partition.get(partitionTo).add(n);
    }

    public void addPartition(int index, Set<Node> nodes) {
        this.partition.put(index, nodes);
    }

    public static Partition copyOf(Partition p) {
        Map<Integer, Set<Node>> partition = new HashMap<>();
        p.getPartition().forEach((k, v) -> partition.put(k, new HashSet<>(v)));

        return new Partition(partition);
    }
}
