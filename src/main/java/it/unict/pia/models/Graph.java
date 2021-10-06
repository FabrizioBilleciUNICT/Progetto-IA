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
            if (entry.getKey().contains(n.getId()))
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
}
