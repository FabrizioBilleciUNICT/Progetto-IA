package it.unict.pia.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

    private final Map<String, Node> nodesMap;
    private final Map<String, Edge> edgesMap;

    public Graph(Map<String, Node> nodesMap, Map<String, Edge> edgesMap) {
        this.nodesMap = nodesMap;
        this.edgesMap = edgesMap;
    }

    public Graph(Set<Node> nodeSet, Set<Edge> edgeSet) {
        this.nodesMap = new HashMap<>();
        this.edgesMap = new HashMap<>();
        for (Node n : nodeSet) this.nodesMap.put(n.getId(), n);
        for (Edge e : edgeSet) this.edgesMap.put(e.getId(), e);
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

    public Set<Edge> edgesOf(Node n) {
        Set<Edge> neighbourhood = new HashSet<>();

        for (Map.Entry<String, Edge> entry : this.edgesMap.entrySet()) {
            String[] keys = entry.getKey().split("-");
            if (n.getId().equals(keys[0]) || n.getId().equals(keys[1]))
                neighbourhood.add(entry.getValue());
        }

        return neighbourhood;
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

    public int getEdgeWeight(Edge e) {
        return this.edgesMap.get(e.getId()).getWeight();
    }

    public void setEdgeWeight(Edge e, int newWeight) {
        Edge newE = this.edgesMap.get(e.getId());
        newE.setWeight(newWeight);
        this.edgesMap.put(e.getId(), newE);
    }

    public Edge addEdge(Node n1, Node n2) {
        Edge newEdge = new Edge(n1.getId(), n2.getId());
        this.edgesMap.put(newEdge.getId(), newEdge);
        return newEdge;
    }

    public void addNode(Node n) {
        this.nodesMap.put(n.getId(), n);
    }
}
