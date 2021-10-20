package it.unict.pia.models;

import java.util.HashSet;
import java.util.Set;

public class Node implements Comparable<Node> {

    private String id;
    private String label;
    private int weight;
    private boolean partition; // true = P0, false = P1

    Set<Node> mySubordinates;
    Node myParent;

    public Node(String id, String label) {
        this.id = id;
        this.label = label;
        this.weight = 1;
        this.mySubordinates = new HashSet<>();
    }

    public Node(String id, String label, int weight) {
        this.id = id;
        this.label = label;
        this.weight = weight;
        this.mySubordinates = new HashSet<>();
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

    public boolean isPartitionP0() {
        return partition;
    }

    public void setPartition(boolean partition) {
        this.partition = partition;
        if (this.mySubordinates != null) {
            this.mySubordinates.forEach(s -> s.setPartition(this.partition));
        }
    }

    public void addSubordinate(Node sub){
        sub.setParent(this);
        mySubordinates.add(sub);
        this.weight += sub.getWeight();
    }

    public Set<Node> getSubordinates(){
        return mySubordinates;
    }

    public Node getParent() {
        return myParent;
    }

    public void setParent(Node parent) {
        this.myParent = parent;
    }
}
