package it.unict.pia.models;

import java.util.ArrayList;
import java.util.Set;

public class Partition {

    private ArrayList<Set<Node>> partition;
    private Set<Node> p0, p1;

    public Partition(ArrayList<Set<Node>> partition) {
        this.partition = partition;
        this.p0 = this.partition.get(0);
        this.p1 = this.partition.get(1);
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
}
