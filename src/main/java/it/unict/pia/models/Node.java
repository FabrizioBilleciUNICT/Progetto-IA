package it.unict.pia.models;

import java.util.HashSet;
import java.util.Set;

public class Node implements Comparable<Node> {

    private String id;
    private String label;
    private double weight;
    private int partition;
    private double selfDegree;
    private double degree;
    Set<Node> subordinates;
    Node parent;

    public Node(String id, String label) {
        this.id = id;
        this.label = label;
        this.weight = 1;
        this.subordinates = new HashSet<>();
        this.selfDegree = 0;
        this.degree = 0;
    }

    public Node(String id, String label, int weight) {
        this.id = id;
        this.label = label;
        this.weight = weight;
        this.subordinates = new HashSet<>();
        this.selfDegree = 0;
        this.degree = 0;
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

    public double getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Node o) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        return ((Node)o).getId().equals(this.id);
    }

    public boolean isPartition(int partition) {
        return this.partition == partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
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

    public double getSelfDegree() {
        return selfDegree;
    }

    public void setSelfDegree(double selfDegree) {
        this.selfDegree = selfDegree;
    }

    public double getDegree() {
        return degree;
    }

    public void setDegree(double degree) {
        this.degree = degree;
    }

    public void increaseDegree(double degree) {
        this.degree += degree;
    }
}
