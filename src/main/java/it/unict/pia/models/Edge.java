package it.unict.pia.models;

public class Edge {

    private String source;
    private String target;
    private String id;
    private int weight;

    public Edge(String source, String target) {
        this.source = source;
        this.target = target;
        this.id = source + "-" + target;
        this.weight = 1;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getId() {
        return id;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
