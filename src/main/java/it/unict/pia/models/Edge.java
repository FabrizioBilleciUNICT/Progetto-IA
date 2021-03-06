package it.unict.pia.models;

import java.util.Comparator;

public class Edge {

    private String source;
    private String target;
    private String id;
    private double weight;

    public Edge(String source, String target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.id = source + "-" + target;
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

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }


    public static class EdgeComparator implements Comparator<Edge> {

        /*Graph myGraph;

        public EdgeComparator(Graph graph){
            myGraph = graph;
        }*/

        public int compare(Edge e1, Edge e2){
            double weight1 = e1.getWeight(); //myGraph.getEdgeWeight(e1);
            double weight2 = e2.getWeight(); //myGraph.getEdgeWeight(e2);

            return Double.compare(weight1, weight2);
        }
    }
}
