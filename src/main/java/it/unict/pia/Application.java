package it.unict.pia;

import it.unict.pia.models.Edge;
import it.unict.pia.models.Graph;
import it.unict.pia.models.Node;
import it.unict.pia.models.Partition;

import java.security.SecureRandom;
import java.util.*;

public class Application {

    public Stack<Graph> graphs = new Stack<>();

    public Application(Graph graph) {
        this.graphs.add(graph);
    }

    public Partition annealing() {
        final int ct = this.graphs.get(0).getSize() / 4; // 60000

        final Partition s_0 = createSeedPartition();
        Partition s_star = s_0;
        Partition s_i = s_0;
        int i = 0; // level
        for (int k = 0; k < 1; k++) {
            while (this.graphs.get(i).getSize() > ct) {
                s_i = solutionGuidedCoarsening(s_i, i);
                System.err.println(s_i.getPartition().size());
                s_i = setBestPartition(s_i, i+1);
                i++;
            }

            while (i > 0) {
                s_i = unCoarsening(s_i, i);
                s_i = setBestPartition(s_i, i);
                i--;
            }

            var ci = 0.0; //conductance(s_i, i);
            var cs = 0.1; //conductance(s_star, i);
            if (ci < cs) {
                s_star = s_i;
            }
        }

        return s_star;
    }

    private Partition createSeedPartition() {
        int partition = 0;
        for (Map.Entry<String, Node> entry : this.graphs.get(0).getNodesMap().entrySet())
            entry.getValue().setPartition(partition++);

        return new Partition(this.graphs.get(0));
    }

    private double conductance(Partition s_i, int level) {
        int degreeP0 = 0;
        int degreeP1 = 0;
        int cutCounter = 0;

        for (Map.Entry<String, Edge> entry : this.graphs.get(level).getEdgesMap().entrySet()) {
            final Node nodeSource = this.graphs.get(level).getEdgeSource(entry.getValue());
            final Node nodeTarget = this.graphs.get(level).getEdgeTarget(entry.getValue());

            // cut
            if ((s_i.getPartition().get(0).contains(nodeSource) && s_i.getPartition().get(1).contains(nodeTarget)) || (s_i.getPartition().get(1).contains(nodeSource) && s_i.getPartition().get(0).contains(nodeTarget)))
                cutCounter++;

            // source
            if (s_i.getPartition().get(0).contains(nodeSource)) degreeP0++;
            else degreeP1++;

            // target
            if (s_i.getPartition().get(0).contains(nodeTarget)) degreeP0++;
            else degreeP1++;
        }

        return (cutCounter + 0.0) / (Math.min(degreeP0, degreeP1) + 0.0);
    }

    private Partition solutionGuidedCoarsening(Partition s_i, int level) {
        LinkedList<Node> nodes = new LinkedList<>(this.graphs.get(level).nodeSet());
        Collections.shuffle(nodes);

        Set<Node> verticesInMatching = new HashSet<>();
        Set<Edge> edgesInMatching = new HashSet<>();
        Edge.EdgeComparator edgeComparator = new Edge.EdgeComparator();

        for (Node curNode : nodes) {
            // curNode.setPartition(s_i.getPartition().get(0).contains(curNode));
            if (!verticesInMatching.contains(curNode)) {
                verticesInMatching.add(curNode);
                Set<Edge> curEdges = this.graphs.get(level).edgesOf(curNode);
                LinkedList<Edge> curEdgesList = new LinkedList<>(curEdges);
                curEdgesList.sort(edgeComparator);

                for (Edge curEdge : curEdgesList) {
                    Node neighbourNode = this.graphs.get(level).getEdgeSource(curEdge);
                    if (neighbourNode == curNode) {
                        neighbourNode = this.graphs.get(level).getEdgeTarget(curEdge);
                    }

                    if (!verticesInMatching.contains(neighbourNode)) {
                        edgesInMatching.add(curEdge);
                        verticesInMatching.add(neighbourNode);
                        break;
                    }
                }
            }
        }

        Graph coarseGraph = new Graph();
        final double dt = 0.5;

        for (Edge curEdge : edgesInMatching) {
            Node source = this.graphs.get(level).getEdgeSource(curEdge);
            Node target = this.graphs.get(level).getEdgeTarget(curEdge);

            if (source.isPartition(target.getPartition())) { // pure partitions
                Node node = new Node(randomString(5), curEdge.getId(), 0);
                node.addSubordinate(source);
                node.addSubordinate(target);
                node.setPartition(source.getPartition());
                node.setSelfDegree(source.getSelfDegree() + target.getSelfDegree() + curEdge.getWeight()*2); // *2

                coarseGraph.addNode(node);
                verticesInMatching.remove(source);
                verticesInMatching.remove(target);
            } else { // p(source) != p(target)

                // nodo 0: d_p1 = 1, d_p2 = 1, ..    y(1) <-- x(0) --> z(2), k(2)
                // d_x = 3
                // d_x_p1 = 1
                // d_x_p2 = 2
                // 2/3 superiore ad una certa soglia, si fa ugualmente il coarsening
            }
        }

        for (Node curNode : verticesInMatching) {
            Node node = new Node(curNode.getId(), curNode.getLabel(), 0);
            node.setPartition(curNode.getPartition());
            //node.setDegree(curNode.getDegree());
            node.setSelfDegree(curNode.getSelfDegree());
            node.addSubordinate(curNode);
            coarseGraph.addNode(node);
        }

        for (Edge curEdge : this.graphs.get(level).edgeSet()) {
            Node parent1 = this.graphs.get(level).getEdgeSource(curEdge).getParent();
            Node parent2 = this.graphs.get(level).getEdgeTarget(curEdge).getParent();

            if (parent1 != parent2) {
                int oldEdgeWeight = this.graphs.get(level).getEdgeWeight(curEdge);
                Edge edgeInCoarseGraph = coarseGraph.getEdge(parent1, parent2);
                var newWeight = oldEdgeWeight;
                if (edgeInCoarseGraph != null) newWeight += coarseGraph.getEdgeWeight(edgeInCoarseGraph);
                else edgeInCoarseGraph = coarseGraph.addEdge(parent1, parent2);
                coarseGraph.setEdgeWeight(edgeInCoarseGraph, newWeight);

                coarseGraph.getNodesMap().get(parent1.getId()).increaseDegree(newWeight);
                coarseGraph.getNodesMap().get(parent2.getId()).increaseDegree(newWeight);
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

    /**
     * 1. Prendere un nodo random dal CV
     * 2. Calcolare la modularità assegnando al nodo ogni partizione disponibile --> partizioni "vicine"
     * 3. Massimizzare la modularità: trovare la partizione assegnata al nodo che massimizza la mod.
     * 4. Assegnare la partizione al nodo
     *
     * TODO: salvare nel nodo il valore dei link interni così che questa informazione venga utilizzata nel calcolo di Q
     */
    private Partition setBestPartition(Partition s_i, int level) {
        Modularity mod = new Modularity(this.graphs.get(level), level);
        mod.initializeQ(s_i);
        Partition s_best = Partition.copyOf(s_i);
        double currentQ = mod.getQ();
        Set<Node> nodesCV = getNodesCV(level);

        int i = 0;
        for (Node v : nodesCV) {
            final int partitionFrom = v.getPartition();
            final Set<Node> neighbours = this.graphs.get(level).adjOf(v);
            int bestP = partitionFrom;
            double bestQ = mod.getQ();
            Map<Integer, Double> qMap = new HashMap<>();
            for (Node n : neighbours) {
                if (!v.isPartition(n.getPartition())) { // prova a spostare v nella partizione target
                    qMap.put(n.getPartition(), mod.updateQ(v, neighbours, partitionFrom, n.getPartition(), false));
                }
            }

            for (Map.Entry<Integer, Double> partitions : qMap.entrySet()) {
                if (partitions.getValue() > bestQ) {
                    bestQ = partitions.getValue();
                    bestP = partitions.getKey();
                }
            }

            //System.out.printf("[level] %d, [iter] %d - {%f} == {%f}%n", level, i, currentQ, bestQ);
            if (bestQ > currentQ) {
                currentQ = bestQ;
                System.out.printf("[level] %d, [iter] %d - {%f} == {%f}%n", level, i, currentQ, bestQ);
                s_best.relocateNode(v, partitionFrom, bestP);
                mod.updateQ(v, neighbours, partitionFrom, bestP, true);

                this.graphs.get(level).getNodesMap().get(v.getId()).setPartition(bestP);
            }

            i++;
        }

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

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
}
