package it.unict.pia.models;

public class Node implements Comparable<Node> {

    private String id;
    private String label;
    private int weight;

    public Node(String id, String label) {
        this.id = id;
        this.label = label;
        this.weight = 1;
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
}
