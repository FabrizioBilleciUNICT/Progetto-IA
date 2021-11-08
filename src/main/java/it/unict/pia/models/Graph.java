package it.unict.pia.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

    private final Map<String, Node> nodesMap;
    private final Map<String, Edge> edgesMap;
    private Map<String, Set<String>> neighborsSet;

    public Graph(Map<String, Node> nodesMap, Map<String, Edge> edgesMap) {
        this.nodesMap = nodesMap;
        this.edgesMap = edgesMap;
    }

    public Graph() {
        this.nodesMap = new HashMap<>();
        this.edgesMap = new HashMap<>();
    }

    public Node getEdgeSource(Edge e) {
        return this.nodesMap.get(e.getSource());
    }

    public Node getEdgeTarget(Edge e) {
        return this.nodesMap.get(e.getTarget());
    }

    public void setNeighbors() {
        this.neighborsSet = new HashMap<>();

        for (Map.Entry<String, Edge> entry : this.edgesMap.entrySet()) {
            String[] keys = entry.getKey().split("-");

            Set<String> set0 = this.neighborsSet.getOrDefault(keys[0], new HashSet<>());
            set0.add(keys[1]);
            this.neighborsSet.put(keys[0], set0);

            Set<String> set1 = this.neighborsSet.getOrDefault(keys[1], new HashSet<>());
            set1.add(keys[0]);
            this.neighborsSet.put(keys[1], set1);
        }
    }

    public Set<Edge> edgesOf(Node n) {
        Set<Edge> neighbourhood = new HashSet<>();
        Set<String> set = this.neighborsSet.getOrDefault(n.getId(), new HashSet<>());

        for (String s : set) {
            String e1 = n.getId() + "-" + s;
            String e2 = s + "-" + n.getId();
            if (this.edgesMap.containsKey(e1))
                neighbourhood.add(this.edgesMap.get(e1));
            else if (this.edgesMap.containsKey(e2))
                neighbourhood.add(this.edgesMap.get(e2));
        }

        return neighbourhood;
    }

    public Set<Node> adjOf(Node n) {
        Set<Node> neighbourhood = new HashSet<>();
        Set<String> set = this.neighborsSet.getOrDefault(n.getId(), new HashSet<>());

        for (String s : set)
            neighbourhood.add(this.nodesMap.get(s));

        return neighbourhood;
    }

    public int degreeOnPartition(Node n) {
        int degree = 0;

        for (Edge x : this.edgesOf(n)) {
            String[] keys = x.getId().split("-");
            if (n.getId().equals(keys[0]) && this.nodesMap.get(keys[1]).isPartition(n.getPartition()))
                degree += x.getWeight();
            else if (n.getId().equals(keys[1]) && this.nodesMap.get(keys[0]).isPartition(n.getPartition()))
                degree += x.getWeight();
        }

        return degree;
    }

    public int getSize() {
        return this.nodesMap.size();
    }

    public Set<Edge> edgeSet() {
        return new HashSet<>(this.edgesMap.values());
    }

    public Set<Node> nodeSet() {
        return new HashSet<>(this.nodesMap.values());
    }

    public Map<String, Node> getNodesMap() {
        return nodesMap;
    }

    public Map<String, Edge> getEdgesMap() {
        return edgesMap;
    }

    public Edge getEdge(Node n1, Node n2) {
        final String t1 = n1.getId() + "-" + n2.getId();
        final String t2 = n2.getId() + "-" + n1.getId();

        if (this.edgesMap.containsKey(t1)) return this.edgesMap.get(t1);
        else return this.edgesMap.getOrDefault(t2, null);
    }

    public double getEdgeWeight(Edge e) {
        return this.edgesMap.get(e.getId()).getWeight();
    }

    public void setEdgeWeight(Edge e, double newWeight) {
        Edge newE = this.edgesMap.get(e.getId());
        newE.setWeight(newWeight);
        this.edgesMap.put(e.getId(), newE);
    }

    public Edge addEdge(Node n1, Node n2) {
        Edge newEdge = new Edge(n1.getId(), n2.getId(), 1.0);
        this.edgesMap.put(newEdge.getId(), newEdge);
        return newEdge;
    }

    public void addNode(Node n) {
        this.nodesMap.put(n.getId(), n);
    }
}
