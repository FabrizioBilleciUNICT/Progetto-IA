package it.unict.pia;

import it.unict.pia.models.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class Application {

    private Graph graph;
    private final String network;

    public Application(String network) {
        this.network = network;
    }

    public void readNetwork() throws FileNotFoundException {
        FileInputStream graphInput = new FileInputStream("networks/" + network + "/" + network + ".gml");
        Scanner scan = new Scanner(graphInput);

        final Map<String, Node> nodesMap = new HashMap();
        final Map<String, Edge> edgesMap = new HashMap();

        int count = 0;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (!scan.hasNextLine()) break;

            if (count > 2) {
                String currentType = line.replace("  ", "");
                if (currentType.equals("node")) {
                    scan.nextLine();
                    String currentId = scan.nextLine().replace("    id ", "");
                    String currentLabel = scan.nextLine().replace("    label ", "").replaceAll("\"", "");
                    scan.nextLine();
                    Node node = new Node(currentId, currentLabel);
                    nodesMap.put(currentId, node);
                } else {
                    scan.nextLine();
                    String currentSource = scan.nextLine().replace("    source ", "");
                    String currentTarget = scan.nextLine().replace("    target ", "");
                    scan.nextLine();
                    Edge edge = new Edge(currentSource, currentTarget);
                    edgesMap.put(edge.getId(), edge);
                }
            }
            count++;
        }

        this.graph = new Graph(nodesMap, edgesMap);
    }

    public Partition annealing() {
        final int ct = 100; // 60000

        final Partition s_0 = createSeedPartition();
        Partition s_star = s_0;
        Partition s_i = s_0;
        int i = 0;
        for (int k = 0; k < 100; k++) {
            while (this.graph.getSize() > ct) {
                s_i = solutionGuidedCoarsening(s_i);
                s_i = localRefinement(s_i);
                i++;
            }

            while (i > 0) {
                i--;
                s_i = unCoarsening(s_i);
                s_i = localRefinement(s_i);
            }

            if (conductance(s_i) < conductance(s_star)) {
                s_star = s_i;
            }
        }

        return s_star;
    }

    private Partition createSeedPartition() {
        return new Partition(partitionGraph(this.graph, 2));
    }

    private double conductance(Partition s_i) {
        int degreeP0 = 0;
        int degreeP1 = 0;
        int cutCounter = 0;

        for (Map.Entry<String, Edge> entry : this.graph.getEdgesMap().entrySet()) {
            final Node nodeSource = this.graph.getEdgeSource(entry.getValue());
            final Node nodeTarget = this.graph.getEdgeTarget(entry.getValue());

            // cut
            if ((s_i.getP0().contains(nodeSource) && s_i.getP1().contains(nodeTarget)) || (s_i.getP1().contains(nodeSource) && s_i.getP0().contains(nodeTarget)))
                cutCounter++;

            // source
            if (s_i.getP0().contains(nodeSource)) degreeP0++;
            else degreeP1++;

            // target
            if (s_i.getP0().contains(nodeTarget)) degreeP0++;
            else degreeP1++;
        }

        return (cutCounter + 0.0) / (Math.min(degreeP0, degreeP1) + 0.0);
    }

    private Partition solutionGuidedCoarsening(Partition s_i) {
        //Compute heavy-edge maximal matching.
        LinkedList<Node> vertexOrder = new LinkedList<>(graph.nodeSet());

        Collections.shuffle(vertexOrder);

        Set<Node> verticesInMatching = new HashSet<>();
        Set<Edge> edgesInMatching = new HashSet<>();

        Edge.EdgeComparator edgeComparator = new Edge.EdgeComparator();

        for (Node curVertex : vertexOrder) {
            curVertex.setPartition(s_i.getP0().contains(curVertex));

            if (!verticesInMatching.contains(curVertex)) {
                verticesInMatching.add(curVertex);

                Set<Edge> curEdges = graph.edgesOf(curVertex);
                LinkedList<Edge> curEdgesList = new LinkedList<>(curEdges);

                curEdgesList.sort(edgeComparator);

                for (Edge curEdge : curEdgesList) {
                    Node neighbourVertex = graph.getEdgeSource(curEdge);

                    if (neighbourVertex == curVertex) {
                        neighbourVertex = graph.getEdgeTarget(curEdge);
                    }

                    if (!verticesInMatching.contains(neighbourVertex)) {
                        // We've found an edge to add to the matching
                        edgesInMatching.add(curEdge);
                        verticesInMatching.add(neighbourVertex);
                        break;
                    }
                }
            }
        }

        // now use the matching to construct the coarser graph
        Graph coarseGraph = new Graph();

        // add to the coarse graph vertices which correspond to edges in the matching
        for (Edge curEdge : edgesInMatching) {
            Node newVertex = new Node(curEdge.getId(), "--");

            Node source = graph.getEdgeSource(curEdge);
            Node target = graph.getEdgeTarget(curEdge);

            newVertex.addSubordinate(source);
            newVertex.addSubordinate(target);
            newVertex.setPartition(s_i.getP0().contains(source) && s_i.getP0().contains(target));

            coarseGraph.addNode(newVertex);

            verticesInMatching.remove(source);
            verticesInMatching.remove(target);
        }

        // verticesInMatching now only contains lone vertices,
        // those which weren't assigned a partner in the matching :(
        for (Node curVertex : verticesInMatching) {
            Node newVertex = new Node(curVertex.getId(), curVertex.getLabel());
            newVertex.setPartition(s_i.getP0().contains(curVertex));
            newVertex.addSubordinate(curVertex);
            coarseGraph.addNode(newVertex);
        }

        // the courseGraph has all the vertices it'll ever get, now it needs the edges
        for (Edge curEdge : graph.edgeSet()) {
            Node parent1 = graph.getEdgeSource(curEdge).getParent();
            Node parent2 = graph.getEdgeTarget(curEdge).getParent();

            if (parent1 != parent2) {
                int oldEdgeWeight = graph.getEdgeWeight(curEdge);
                Edge edgeInCoarseGraph = coarseGraph.getEdge(parent1, parent2);

                if (edgeInCoarseGraph != null) {
                    coarseGraph.setEdgeWeight(edgeInCoarseGraph, coarseGraph.getEdgeWeight(edgeInCoarseGraph) + oldEdgeWeight);
                } else {
                    edgeInCoarseGraph = coarseGraph.addEdge(parent1, parent2);
                    coarseGraph.setEdgeWeight(edgeInCoarseGraph, oldEdgeWeight);
                }
            }
        }

        this.graph = coarseGraph;

        return new Partition(coarseGraph); // s_i1
    }

    // TODO
    private Partition unCoarsening(Partition s_i) {
        // change something in nodes & edges
        Partition s_i1 = s_i; //new Partition();
        return s_i1;
    }

    private Partition localRefinement(Partition s_i) {
        final double T0 = (1.0 + 1e-20) / 2.0;
        double T = T0;
        final double salter = 200.0; //200000.0;
        final double ar = 0.05;
        final double theta = 0.98;

        Partition s_best = s_i;
        double mv = 0.0;
        int belowCount = 0;
        while (belowCount < 5) {
            mv = 0.0;
            for (int i = 0; i < salter; i++) {
                Node v = getRandomVertexCV(s_i);
                if (v == null) break;

                Partition s_p = relocate(s_i, v);
                final int d = delta(s_p, v);
                if (d < 0 || canRelocate(T, d)) {
                    s_i = s_p;
                    mv++;
                }

                if (conductance(s_i) < conductance(s_best)) {
                    s_best = s_i;
                }
            }

            T = T * theta;

            if ((mv / salter) < ar) belowCount++;
            else belowCount = 0;
        }

        return s_best; // s_i1
    }

    private Node getRandomVertexCV(Partition s_i) {
        Set<Node> nodes = new HashSet<>();
        for (Map.Entry<String, Edge> entry : this.graph.getEdgesMap().entrySet()) {
            final Node nodeSource = this.graph.getEdgeSource(entry.getValue());
            final Node nodeTarget = this.graph.getEdgeTarget(entry.getValue());

            if ((s_i.getP0().contains(nodeSource) && s_i.getP1().contains(nodeTarget)) || (s_i.getP1().contains(nodeSource) && s_i.getP0().contains(nodeTarget))) {
                nodes.add(nodeSource);
                nodes.add(nodeTarget);
            }
        }

        Node[] arr = nodes.toArray(Node[]::new);
        if (arr.length > 0) return arr[new Random().nextInt(nodes.size())];
        else return null;
    }

    private Partition relocate(Partition s_i, Node v) {
        Partition s_i1 = new Partition();
        s_i1.setPartition(new ArrayList<>(Set.of(s_i.getP0(), s_i1.getP1())));
        s_i1.relocateNode(v);
        return s_i1;
    }

    private int delta(Partition s_i, Node v) {
        int degree = 0;
        for (Map.Entry<String, Edge> entry : this.graph.getEdgesMap().entrySet()) {
            final Node nodeSource = this.graph.getEdgeSource(entry.getValue());
            final Node nodeTarget = this.graph.getEdgeTarget(entry.getValue());

            if (nodeSource.equals(v) || nodeTarget.equals(v))
                if (s_i.getP0().contains(v) || s_i.getP1().contains(v))
                    degree++;
        }

        return degree;
    }

    private boolean canRelocate(double T, int d) {
        if (d < 0) return true; // 1
        else {
            double prob = Math.exp(-d / T);
            return prob > 0.5;
        }
    }


    /**
     * Takes takes a graph with weighted edges and partitions it into the given number of partitions.
     * Uses the multi-level Kernighan-Lin heuristic to minimize the weight of the edges between
     * the partitions (min-cut) while ensuring the sums of the weights of the vertices on each
     * side of the partition are relatively balanced. Uses a heuristic because this problem is NP-Complete.
     *
     * @author Eric Crawford
     */
    private ArrayList<Set<Node>> partitionGraph(Graph graph, int numPartitions) {

        ArrayList<Set<Node>> partitions = new ArrayList<>();

        Set<Node> nodeSet = graph.nodeSet();

        if (numPartitions < 1) {
            return partitions;
        } else if (nodeSet.size() <= numPartitions) {

            // In this case there is no point in computing min cut,
            //just assign each node to a different partition.
            Iterator<Node> nodeIter = nodeSet.iterator();
            for (int i = 0; i < numPartitions; i++) {
                Set<Node> newSet = new HashSet<>();

                if (nodeIter.hasNext()) {
                    newSet.add(nodeIter.next());
                }

                partitions.add(newSet);
            }

            return partitions;

        } else if (numPartitions == 1) {
            partitions.add(nodeSet);
            return partitions;
        }

        Set<Node> partition = multilevelKL(graph);
        nodeSet = graph.nodeSet();

        Set<Node> leftSubgraphVertexSet = partition;
        Set<Node> rightSubgraphVertexSet = new HashSet<>(nodeSet);
        for (Node node : leftSubgraphVertexSet) {
            rightSubgraphVertexSet.remove(node);
        }

        // swap to make sure left partition is the larger one.
        if (leftSubgraphVertexSet.size() < rightSubgraphVertexSet.size()) {
            Set<Node> temp = rightSubgraphVertexSet;
            rightSubgraphVertexSet = leftSubgraphVertexSet;
            leftSubgraphVertexSet = temp;
        }

        if (numPartitions == 2) {
            partitions.add(leftSubgraphVertexSet);
            partitions.add(rightSubgraphVertexSet);
            return partitions;
        }

        Iterator<Edge> edgeIter = graph.edgeSet().iterator();

        Set<Edge> leftSubgraphEdgeSet = new HashSet<>();
        Set<Edge> rightSubgraphEdgeSet = new HashSet<>();

        while (edgeIter.hasNext()) {
            Edge edge = edgeIter.next();

            Node source = graph.getEdgeSource(edge);
            Node target = graph.getEdgeTarget(edge);

            boolean sourceInLeft = leftSubgraphVertexSet.contains(source);
            boolean targetInLeft = leftSubgraphVertexSet.contains(target);

            if (sourceInLeft && targetInLeft) {
                leftSubgraphEdgeSet.add(edge);
            } else if (!sourceInLeft && !targetInLeft) {
                rightSubgraphEdgeSet.add(edge);
            }
        }

        Graph leftSubgraph, rightSubgraph;

        leftSubgraph = new UndirectedWeightedSubgraph<>(graph, leftSubgraphVertexSet, leftSubgraphEdgeSet);

        rightSubgraph = new UndirectedWeightedSubgraph<>(graph, rightSubgraphVertexSet, rightSubgraphEdgeSet);

        int numLeftSubPartitions = (int) Math.ceil((double) numPartitions / 2);
        int numRightSubPartitions = (int) Math.floor((double) numPartitions / 2);

        ArrayList<Set<Node>> leftPartitions = partitionGraph(leftSubgraph, numLeftSubPartitions);
        ArrayList<Set<Node>> rightPartitions = partitionGraph(rightSubgraph, numRightSubPartitions);

        partitions.addAll(leftPartitions);
        partitions.addAll(rightPartitions);
        return partitions;
    }

    private Set<Node> multilevelKL(Graph graph) {
        // run the BFS algorithm several times
        double balanceFactor = 0.9;
        int graphSize = graph.getSize();
        int numReps = Math.min(20, graphSize);
        Set<Node> vertexSet = graph.nodeSet();

        double totalVertexWeight = 0;
        for (Node v : vertexSet) {
            totalVertexWeight += v.getWeight();
        }

        double minCut = Double.POSITIVE_INFINITY;
        Set<Node> minCutPartition = new HashSet<>();

        List<Node> startVertexList = new LinkedList<>(vertexSet);
        Collections.shuffle(startVertexList);

        ListIterator<Node> startVertexIter = startVertexList.listIterator();

        for (int i = 0; i < numReps; i++) {

            Node startVertex = startVertexIter.next();

            // start with a different random vertex on each rep,
            // run BFS and see which gives best partition
            double partitionVertexWeight = 0.0;
            double balance = 0.0;

            PriorityQueue<Node> queue = new PriorityQueue<>();
            Set<Node> partition = new HashSet<>();
            Set<Node> checked = new HashSet<>();
            LinkedList<Node> unchecked = new LinkedList<>(vertexSet);
            Collections.shuffle(unchecked);

            queue.add(startVertex);

            while (!queue.isEmpty() && balance < 0.5) {
                Node curVertex = queue.poll();
                double curVertexWeight = curVertex.getWeight();

                double balanceWithCurrentVertex = (partitionVertexWeight + curVertexWeight) / totalVertexWeight;
                boolean betterBalance = Math.abs(balanceWithCurrentVertex - 0.5) < Math.abs(balance - 0.5);

                // if adding the current head of the queue to
                // the partition would give a better balance, then do it
                if (betterBalance) {
                    partition.add(curVertex);

                    partitionVertexWeight += curVertexWeight;

                    balance = partitionVertexWeight / totalVertexWeight;

                    Set<Node> neighbourhood = getNeighbourhood(graph, curVertex);

                    neighbourhood.removeAll(checked);

                    queue.addAll(neighbourhood);
                }

                checked.add(curVertex);
                unchecked.remove(curVertex);

                // if the queue is empty but we don't yet have a good balance, randomly choose a vertex
                // we haven't visited yet and start from there (still on the same rep)
                if (queue.isEmpty() && !unchecked.isEmpty() && (Math.abs(balance - 0.5) > balanceFactor)) {
                    queue.add(unchecked.get(0));
                }
            }

            // find the cut value of the partition found on this rep
            double cutValue = 0.0;
            for (Edge curEdge : graph.edgeSet()) {
                Node source = graph.getEdgeSource(curEdge);
                Node target = graph.getEdgeTarget(curEdge);

                boolean sourceInPartition = partition.contains(source);
                boolean targetInPartition = partition.contains(target);

                if (sourceInPartition != targetInPartition) {
                    cutValue += curEdge.getWeight(); // graph.getEdgeWeight(curEdge);
                }
            }

            // compare current partition to the best so far, as long as current partition has decent balance
            if (Math.abs(balance - 0.5) < balanceFactor) {
                if (cutValue < minCut) {
                    minCut = cutValue;
                    minCutPartition = partition;
                }
            }
        }

        return minCutPartition;
    }


    /**
     * Find the vertices in the neighbourhood of given vertex in a given graph.
     *
     * @author Eric Crawford
     */
    private Set<Node> getNeighbourhood(Graph graph, Node vertex) {

        Set<Edge> edgeSet = graph.edgesOf(vertex);
        Set<Node> neighbourhood = new HashSet<>();

        for (Edge curEdge : edgeSet) {
            Node neighbour = graph.getEdgeSource(curEdge);

            if (neighbour == vertex) {
                neighbour = graph.getEdgeTarget(curEdge);
            }

            neighbourhood.add(neighbour);
        }

        return neighbourhood;
    }
}
