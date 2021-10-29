package it.unict.pia;

import it.unict.pia.models.Edge;
import it.unict.pia.models.Graph;
import it.unict.pia.models.Node;
import it.unict.pia.models.Partition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Modularity {

    private double q = 0.0;
    private Map<Integer, Integer> linksMap = new HashMap<>();
    private Map<Integer, Integer> degreesMap = new HashMap<>();
    private final int M;
    private Graph graph;
    private int level;
    private Partition partition;

    public Modularity(Graph graph, int level) {
        this.level = level;
        this.graph = graph;
        this.M = this.graph.getEdgesMap().size();
    }

    public void initializeQ(Partition s_i) {
        this.partition = s_i;
        q = 0.0;
        for (Map.Entry<Integer, Set<Node>> entry : s_i.getPartition().entrySet()) {
            int l_i = 0;
            int d_i = 0;
            Set<Node> partition = entry.getValue();
            Set<String> keys = partition.stream().map(Node::getId).collect(Collectors.toSet());

            for (Node n : partition) {
                Set<Edge> curEdges = graph.edgesOf(n);
                d_i += curEdges.size();
                l_i += curEdges.stream().filter(e -> keys.contains(e.getSource()) && keys.contains(e.getTarget())).count();
                // TODO: set l_i on node
            }

            l_i /= 2.0;

            this.linksMap.put(entry.getKey(), l_i);
            this.degreesMap.put(entry.getKey(), d_i);

            //if (l_i > 0)
            var __x = (l_i / (M * 1.0));
            var __y = Math.pow(d_i / (2.0 * M), 2);

            q += __x - __y;
        }
    }

    public double getQ() {
        return q;
    }

    public double updateQ(Set<Node> neighbours, int partitionFrom, int partitionTo) {
        final int nodeDegree = neighbours.size();
        double newQ = q;

        final int l_i_from = this.linksMap.get(partitionFrom);
        final int d_i_from = this.degreesMap.get(partitionFrom);
        final int l_i_to = this.linksMap.get(partitionTo);
        final int d_i_to = this.degreesMap.get(partitionTo);

        var x_from = (l_i_from / (M * 1.0));
        var y_from = Math.pow(d_i_from / (2.0 * M), 2);
        newQ -= (x_from - y_from);
        var x_to = (l_i_to / (M * 1.0));
        var y_to = Math.pow(d_i_to / (2.0 * M), 2);
        newQ -= (x_to - y_to);

        int newLinkFrom = l_i_from - (int) neighbours.stream().filter(x -> x.isPartition(partitionFrom)).count();
        int newDegreeFrom = d_i_from - nodeDegree;
        int newLinkTo = l_i_to + (int) neighbours.stream().filter(x -> x.isPartition(partitionTo)).count();
        int newDegreeTo = d_i_to + nodeDegree;

        var n_x_from = (newLinkFrom / (M * 1.0));
        var n_y_from = Math.pow(newDegreeFrom / (2.0 * M), 2);
        newQ += (n_x_from - n_y_from);
        var n_x_to = (newLinkTo / (M * 1.0));
        var n_y_to = Math.pow(newDegreeTo / (2.0 * M), 2);
        newQ += (n_x_to - n_y_to);

        return newQ;
    }

    public void relocateNode(Node n, int partitionFrom, int partitionTo) {
        this.partition.relocateNode(n, partitionFrom, partitionTo);
    }
}
