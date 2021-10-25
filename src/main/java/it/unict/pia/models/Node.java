package it.unict.pia.models;

import java.util.HashSet;
import java.util.Set;

public class Node implements Comparable<Node> {

    private String id;
    private String label;
    private int weight;
    private int partition; // true = P0, false = P1

    Set<Node> subordinates;
    Node parent;

    public Node(String id, String label) {
        this.id = id;
        this.label = label;
        this.weight = 1;
        this.subordinates = new HashSet<>();
    }

    public Node(String id, String label, int weight) {
        this.id = id;
        this.label = label;
        this.weight = weight;
        this.subordinates = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Node o) {
        return 0;
    }

    public boolean isPartition(int partition) {
        return this.partition == partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
        if (this.subordinates != null) {
            this.subordinates.forEach(s -> s.setPartition(this.partition));
        }
    }

    public int getPartition() {
        return partition;
    }

    public void addSubordinate(Node sub){
        sub.setParent(this);
        subordinates.add(sub);
        this.weight += sub.getWeight();
    }

    public Set<Node> getSubordinates(){
        return subordinates;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
