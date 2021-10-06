package it.unict.pia.models;

import java.util.Set;

public class UndirectedWeightedSubgraph<V, E> extends Graph {

    private Graph graph;
    private Set<V> nodes;
    private Set<E> edges;

    public UndirectedWeightedSubgraph(Graph graph, Set<V> nodes, Set<E> edges) {
        super();
        this.graph = graph;
        this.nodes = nodes;
        this.edges = edges;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Set<V> getNodes() {
        return nodes;
    }

    public void setNodes(Set<V> nodes) {
        this.nodes = nodes;
    }

    public Set<E> getEdges() {
        return edges;
    }

    public void setEdges(Set<E> edges) {
        this.edges = edges;
    }
}
