package it.unict.pia;

import it.unict.pia.models.Edge;
import it.unict.pia.models.Graph;
import it.unict.pia.models.Node;
import it.unict.pia.models.Partition;

import java.util.*;

import static it.unict.pia.Utils.randomString;

public class Application {

    public Stack<Graph> graphs = new Stack<>();

    public Application(Graph graph) {
        this.graphs.add(graph);
        this.graphs.get(0).setNeighbors();
    }

    public String annealing() {
        Stats stats = new Stats();
        Partition s_i = createSeedPartition();
        int i = 0;
        double currentModularity = -0.5;
        int counter = 0;
        while (counter < 5) {
            s_i = solutionGuidedCoarsening(i);
            s_i = setBestPartition(s_i, i+1);
            i++;

            if (s_i.getModularity() > currentModularity + 0.0001) {
                currentModularity = s_i.getModularity();
                counter = 0;
            }
            else counter++;
        }

        stats.setLevels(i);
        stats.setModularity(currentModularity);

        while (i > 0) {
            s_i = unCoarsening(s_i, i);
            s_i = setBestPartition(s_i, i-1);
            i--;
        }

        stats.setPartitions(s_i.getPartition().size());

        return stats.getStats();
    }

    private Partition createSeedPartition() {
        int partition = 0;
        for (Map.Entry<String, Node> entry : this.graphs.get(0).getNodesMap().entrySet())
            entry.getValue().setPartition(partition++);

        return new Partition(this.graphs.get(0));
    }

    private Partition solutionGuidedCoarsening(int level) {
        LinkedList<Node> nodes = new LinkedList<>(this.graphs.get(level).nodeSet());
        Collections.shuffle(nodes);

        Set<Node> verticesInMatching = new HashSet<>();
        Set<Edge> edgesInMatching = new HashSet<>();

        for (Node n : nodes) {
            if (!verticesInMatching.contains(n)) {
                verticesInMatching.add(n);

                Optional<Node> opt = this.graphs.get(level).adjOf(n)
                        .stream()
                        .filter(x -> x.isPartition(n.getPartition()) && !verticesInMatching.contains(x))
                        .max(Comparator.comparingDouble(o1 -> o1.getSelfDegree() / (o1.getDegree() + 1.0))); // maybe l_i / d_i ?
                if (opt.isPresent()) {
                    Edge e = this.graphs.get(level).getEdge(n, opt.get());
                    verticesInMatching.add(opt.get());
                    edgesInMatching.add(e);
                }
            }
        }

        Graph coarseGraph = new Graph();

        for (Edge curEdge : edgesInMatching) {
            Node source = this.graphs.get(level).getEdgeSource(curEdge);
            Node target = this.graphs.get(level).getEdgeTarget(curEdge);

            Node node = new Node(randomString(6), curEdge.getId(), 0);
            node.addSubordinate(source);
            node.addSubordinate(target);
            node.setPartition(source.getPartition());
            node.setSelfDegree((int) (source.getSelfDegree() + target.getSelfDegree() + curEdge.getWeight()));

            coarseGraph.addNode(node);
            verticesInMatching.remove(source);
            verticesInMatching.remove(target);
        }

        for (Node curNode : verticesInMatching) {
            Node node = new Node(curNode.getId(), curNode.getLabel(), 0);
            node.setPartition(curNode.getPartition());

            node.setSelfDegree(curNode.getSelfDegree());
            node.addSubordinate(curNode);
            coarseGraph.addNode(node);
        }

        for (Edge curEdge : this.graphs.get(level).edgeSet()) {
            Node parent1 = this.graphs.get(level).getEdgeSource(curEdge).getParent();
            Node parent2 = this.graphs.get(level).getEdgeTarget(curEdge).getParent();

            if (parent1 != parent2) {
                var oldEdgeWeight = this.graphs.get(level).getEdgeWeight(curEdge);
                Edge edgeInCoarseGraph = coarseGraph.getEdge(parent1, parent2);
                var newWeight = oldEdgeWeight;
                if (edgeInCoarseGraph != null) newWeight += coarseGraph.getEdgeWeight(edgeInCoarseGraph);
                else edgeInCoarseGraph = coarseGraph.addEdge(parent1, parent2);
                coarseGraph.setEdgeWeight(edgeInCoarseGraph, newWeight);

                coarseGraph.getNodesMap().get(parent1.getId()).increaseDegree((int) oldEdgeWeight);
                coarseGraph.getNodesMap().get(parent2.getId()).increaseDegree((int) oldEdgeWeight);
            }
        }

        if (this.graphs.size() > level + 1) this.graphs.set(level + 1, coarseGraph);
        else this.graphs.add(coarseGraph);

        return new Partition(coarseGraph); // s_i1
    }

    private Partition unCoarsening(Partition s_i1, int level) {
        Partition s_i = new Partition();
        for (Map.Entry<Integer, Set<Node>> entry : s_i1.getPartition().entrySet()) {
            Set<Node> partition = entry.getValue();
            Set<Node> newPartition = new HashSet<>(2 * partition.size());
            for (Node node : partition) {
                Set<Node> contractedNodes = node.getSubordinates();
                newPartition.addAll(contractedNodes);
                for (Node nc : contractedNodes) {
                    nc.setPartition(entry.getKey());
                    this.graphs.get(level - 1).getNodesMap().put(nc.getId(), nc);
                }
            }
            s_i.addPartition(entry.getKey(), newPartition);
        }
        return s_i;
    }

    private Partition setBestPartition(Partition s_i, int level) {
        this.graphs.get(level).setNeighbors();

        Modularity mod = new Modularity(this.graphs.get(level), this.graphs.get(0).getEdgesMap().size());
        mod.initializeQ(s_i);
        Partition s_best = Partition.copyOf(s_i);
        double currentQ = mod.getQ();
        Set<Node> nodesCV = getNodesCV(level);

        for (Node v : nodesCV) {
            final int partitionFrom = v.getPartition();
            final Set<Node> neighbours = this.graphs.get(level).adjOf(v);
            int bestP = partitionFrom;
            double bestQ = currentQ;
            for (Node n : neighbours) {
                if (!v.isPartition(n.getPartition())) { // prova a spostare v nella partizione target
                    final double q = mod.updateQ(v, neighbours, partitionFrom, n.getPartition(), false);
                    if (q > bestQ) {
                        bestP = n.getPartition();
                        bestQ = q;
                    }
                }
            }

            if (bestQ > currentQ) {
                currentQ = bestQ;
                s_best.relocateNode(v, partitionFrom, bestP);
                mod.updateQ(v, neighbours, partitionFrom, bestP, true);

                this.graphs.get(level).getNodesMap().get(v.getId()).setPartition(bestP);
            }
        }

        s_best.setModularity(mod.getQ());

        return s_best;
    }

    private Set<Node> getNodesCV(int level) {
        Set<Node> nodes = new HashSet<>();
        for (Map.Entry<String, Edge> entry : this.graphs.get(level).getEdgesMap().entrySet()) {
            final Node nodeSource = this.graphs.get(level).getEdgeSource(entry.getValue());
            final Node nodeTarget = this.graphs.get(level).getEdgeTarget(entry.getValue());

            if (!nodeSource.isPartition(nodeTarget.getPartition())) {
                nodes.add(nodeSource);
                nodes.add(nodeTarget);
            }
        }
        return nodes;
    }
}
