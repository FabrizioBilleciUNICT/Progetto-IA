package it.unict.pia;

import it.unict.pia.models.Graph;
import it.unict.pia.models.Node;
import it.unict.pia.models.Partition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Modularity {

    private double q = 0.0;
    private final Map<Integer, Double> linksMap = new HashMap<>();
    private final Map<Integer, Integer> degreesMap = new HashMap<>();
    private final int M;
    private final Graph graph;
    private Partition partition;

    public Modularity(Graph graph, int M) {
        this.graph = graph;
        this.M = M;
    }

    public void initializeQ(Partition s_i) {
        this.partition = s_i;
        q = 0.0;
        for (Map.Entry<Integer, Set<Node>> entry : s_i.getPartition().entrySet()) {
            double l_i = 0.0;
            int d_i = 0;
            Set<Node> partition = entry.getValue();

            for (Node n : partition) {
                d_i += n.getDegree() + n.getSelfDegree() * 2;
                l_i += n.getSelfDegree() + this.graph.degreeOnPartition(n) / 2.0;
            }

            this.linksMap.put(entry.getKey(), l_i);
            this.degreesMap.put(entry.getKey(), d_i);
            q += (l_i / (M * 1.0)) - Math.pow(d_i / (2.0 * M), 2);
        }
    }

    public double getQ() {
        return q;
    }

    public double updateQ(Node n, Set<Node> neighbours, int partitionFrom, int partitionTo, boolean toStore) {
        double newQ = q;

        final var l_i_from = this.linksMap.get(partitionFrom);
        final int d_i_from = this.degreesMap.get(partitionFrom);
        final var l_i_to = this.linksMap.get(partitionTo);
        final int d_i_to = this.degreesMap.get(partitionTo);

        var x_from = (l_i_from / (M * 1.0));
        var y_from = Math.pow(d_i_from / (2.0 * M), 2);
        newQ -= (x_from - y_from);
        var x_to = (l_i_to / (M * 1.0));
        var y_to = Math.pow(d_i_to / (2.0 * M), 2);
        newQ -= (x_to - y_to);

        var oldP = 0;
        var newP = 0;
        for (Node x : neighbours) {
            if (x.isPartition(partitionFrom)) {
                final String e1 = x.getId() + "-" + n.getId();
                final String e2 = n.getId() + "-" + x.getId();
                if (graph.getEdgesMap().containsKey(e1)) oldP += graph.getEdgesMap().get(e1).getWeight();
                else if (graph.getEdgesMap().containsKey(e2)) oldP += graph.getEdgesMap().get(e2).getWeight();
            }

            if (x.isPartition(partitionTo)) {
                final String e1 = x.getId() + "-" + n.getId();
                final String e2 = n.getId() + "-" + x.getId();
                if (graph.getEdgesMap().containsKey(e1)) newP += graph.getEdgesMap().get(e1).getWeight();
                else if (graph.getEdgesMap().containsKey(e2)) newP += graph.getEdgesMap().get(e2).getWeight();
            }
        }

        var newLinkFrom = l_i_from - oldP;
        int newDegreeFrom = d_i_from - (n.getDegree() + n.getSelfDegree() * 2);
        var newLinkTo = l_i_to + newP;
        int newDegreeTo = d_i_to + n.getDegree() + n.getSelfDegree() * 2;

        var n_x_from = (newLinkFrom / (M * 1.0));
        var n_y_from = Math.pow(newDegreeFrom / (2.0 * M), 2);
        newQ += (n_x_from - n_y_from);
        var n_x_to = (newLinkTo / (M * 1.0));
        var n_y_to = Math.pow(newDegreeTo / (2.0 * M), 2);
        newQ += (n_x_to - n_y_to);

        if (toStore) {
            this.partition.relocateNode(n, partitionFrom, partitionTo);
            this.degreesMap.put(partitionFrom, newDegreeFrom);
            this.degreesMap.put(partitionTo, newDegreeTo);
            this.linksMap.put(partitionFrom, newLinkFrom);
            this.linksMap.put(partitionTo, newLinkTo);
            this.q = newQ;
        }

        return newQ;
    }
}
