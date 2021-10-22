package it.unict.pia;

import it.unict.pia.models.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.util.*;

public class Application {

    //private Graph graph;
    public Stack<Graph> graphs = new Stack<>();
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

        this.graphs.add(new Graph(nodesMap, edgesMap));
    }

    private Map<String, Edge> readCSVEdges(String path) throws FileNotFoundException {
        FileInputStream graphInput = new FileInputStream(path);
        Scanner scan = new Scanner(graphInput);
        final Map<String, Edge> edgesMap = new HashMap();
        boolean header = true;
        while (scan.hasNextLine()) {
            String[] line = scan.nextLine().split(",");

            if (header) {
                header = false;
                continue;
            }

            Edge edge = new Edge(line[1], line[2]);
            edgesMap.put(edge.getId(), edge);
        }
        return edgesMap;
    }

    private Map<String, Node> readCSVNodes(String path) throws FileNotFoundException {
        FileInputStream graphInput = new FileInputStream(path);
        Scanner scan = new Scanner(graphInput);
        final Map<String, Node> nodesMap = new HashMap();
        boolean header = true;
        while (scan.hasNextLine()) {
            String[] line = scan.nextLine().split(",");

            if (header) {
                header = false;
                continue;
            }

            Node node = new Node(line[0], line[1]);
            nodesMap.put(line[0], node);
        }
        return nodesMap;
    }

    public void readCSVNetwork() throws FileNotFoundException {
        final Map<String, Node> nodesMap = readCSVNodes("output/test_output_nodes.csv");
        final Map<String, Edge> edgesMap = readCSVEdges("output/test_output_edges.csv");
        this.graphs.add(new Graph(nodesMap, edgesMap));
    }

    public Partition annealing() {
        final int ct = 100; // 60000

        final Partition s_0 = createSeedPartition();
        Partition s_star = s_0;
        Partition s_i = s_0;
        int i = 0; // level
        for (int k = 0; k < 1; k++) {
            while (this.graphs.get(i).getSize() > ct) {
                s_i = solutionGuidedCoarsening(s_i, i);
                s_i = localRefinement(s_i, i);
                i++;
            }

            while (i > 0) {
                s_i = unCoarsening(s_i, i);
                s_i = localRefinement(s_i, i);
                i--;
            }

            var ci = conductance(s_i, i);
            var cs = conductance(s_star, i);
            if (ci < cs) {
                s_star = s_i;
            }
        }

        return s_star;
    }

    private Partition createSeedPartition() {
        return new Partition(partitionGraph(this.graphs.get(0), 2));
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
            curNode.setPartition(s_i.getPartition().get(0).contains(curNode));
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

        int p0 = 0, p1 = 0;
        for (Edge curEdge : edgesInMatching) {
            Node node = new Node(randomString(5), curEdge.getId(), 0); // curEdge.getId()
            //Node node = new Node(curEdge.getId(), curEdge.getId());

            Node source = this.graphs.get(level).getEdgeSource(curEdge);
            Node target = this.graphs.get(level).getEdgeTarget(curEdge);

            node.addSubordinate(source);
            node.addSubordinate(target);

            boolean partition = s_i.getPartition().get(0).contains(source) && s_i.getPartition().get(0).contains(target);
            // balance factor
            //if (p0 < 10) partition = true;
            //if (p1 < 10) partition = false;

            //if (partition) p0++;
            //else p1++;
            // balance factor
            node.setPartition(partition);

            coarseGraph.addNode(node);

            verticesInMatching.remove(source);
            verticesInMatching.remove(target);
        }

        for (Node curNode : verticesInMatching) {
            Node node = new Node(curNode.getId(), curNode.getLabel(), 0);
            node.setPartition(s_i.getPartition().get(0).contains(curNode));
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
            }
        }

        if (this.graphs.size() > level + 1) this.graphs.set(level + 1, coarseGraph);
        else this.graphs.add(coarseGraph);

        return new Partition(coarseGraph); // s_i1
    }

    private Partition unCoarsening(Partition s_i1, int level) {
        Partition s_i = new Partition();
        //Graph unCoarsenGraph = new Graph();

        for (int index = 0; index < s_i1.getPartition().size(); index++) {
            Set<Node> partition = s_i1.getPartition().get(index);
            Set<Node> newPartition = new HashSet<>(2 * partition.size());
            for (Node node : partition) {
                Set<Node> contractedNodes = node.getSubordinates();
                newPartition.addAll(contractedNodes);
                for (Node nc : contractedNodes) {
                    nc.setPartition(index == 0);
                    this.graphs.get(level-1).getNodesMap().put(nc.getId(), nc);
                }
            }
            s_i.addPartition(index, newPartition);
        }
        //this.graphs.set(--level, unCoarsenGraph);

        return s_i;
    }

    private Partition localRefinement(Partition s_i, int level) {
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
                Node v = getRandomNodeCV(s_i, level);
                if (v == null) break;

                Partition s_p = relocate(s_i, v);
                final int d = delta(s_p, v, level);
                if (d < 0 || canRelocate(T, d)) {
                    s_i = s_p;
                    mv++;
                }

                if (conductance(s_i, level) < conductance(s_best, level)) {
                    s_best = s_i;
                }
            }

            T = T * theta;

            if ((mv / salter) < ar) belowCount++;
            else belowCount = 0;
        }

        return s_best; // s_i1
    }

    private Node getRandomNodeCV(Partition s_i, int level) { // nodes near a cut
        Set<Node> nodes = new HashSet<>();
        for (Map.Entry<String, Edge> entry : this.graphs.get(level).getEdgesMap().entrySet()) {
            final Node nodeSource = this.graphs.get(level).getEdgeSource(entry.getValue());
            final Node nodeTarget = this.graphs.get(level).getEdgeTarget(entry.getValue());

            if ((s_i.getPartition().get(0).contains(nodeSource) && s_i.getPartition().get(1).contains(nodeTarget)) || (s_i.getPartition().get(1).contains(nodeSource) && s_i.getPartition().get(0).contains(nodeTarget))) {
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
        s_i1.setPartition(new ArrayList<>(Set.of(s_i.getPartition().get(0), s_i.getPartition().get(1))));
        s_i1.relocateNode(v);
        return s_i1;
    }

    private int delta(Partition s_i, Node v, int level) {
        int degree = 0;
        for (Map.Entry<String, Edge> entry : this.graphs.get(level).getEdgesMap().entrySet()) {
            final Node nodeSource = this.graphs.get(level).getEdgeSource(entry.getValue());
            final Node nodeTarget = this.graphs.get(level).getEdgeTarget(entry.getValue());

            if (nodeSource.equals(v) || nodeTarget.equals(v))
                if (s_i.getPartition().get(0).contains(v) || s_i.getPartition().get(1).contains(v))
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

    private ArrayList<Set<Node>> partitionGraph(Graph graph, int numPartitions) {
        ArrayList<Set<Node>> partitions = new ArrayList<>();
        Set<Node> nodeSet = graph.nodeSet();

        if (numPartitions < 1) {
            return partitions;
        } else if (numPartitions > nodeSet.size()) {
            Iterator<Node> nodeIter = nodeSet.iterator();
            for (int i = 0; i < numPartitions; i++) {
                Set<Node> newSet = new HashSet<>();
                if (nodeIter.hasNext())
                    newSet.add(nodeIter.next());
                partitions.add(newSet);
            }
            return partitions;

        } else if (numPartitions == 1) {
            partitions.add(nodeSet);
            return partitions;
        }

        Set<Node> partition = createPartition(graph);
        nodeSet = graph.nodeSet();

        Set<Node> leftSubgraphNodeSet = partition;
        Set<Node> rightSubgraphNodeSet = new HashSet<>(nodeSet);
        for (Node node : leftSubgraphNodeSet) {
            rightSubgraphNodeSet.remove(node);
            node.setPartition(true);
        }

        if (leftSubgraphNodeSet.size() < rightSubgraphNodeSet.size()) {
            Set<Node> temp = rightSubgraphNodeSet;
            rightSubgraphNodeSet = leftSubgraphNodeSet;
            leftSubgraphNodeSet = temp;
        }

        if (numPartitions == 2) {
            partitions.add(leftSubgraphNodeSet);
            partitions.add(rightSubgraphNodeSet);
            return partitions;
        }

        Iterator<Edge> edgeIter = graph.edgeSet().iterator();
        Set<Edge> leftSubgraphEdgeSet = new HashSet<>();
        Set<Edge> rightSubgraphEdgeSet = new HashSet<>();

        while (edgeIter.hasNext()) {
            Edge edge = edgeIter.next();
            Node source = graph.getEdgeSource(edge);
            Node target = graph.getEdgeTarget(edge);

            boolean sourceInLeft = leftSubgraphNodeSet.contains(source);
            boolean targetInLeft = leftSubgraphNodeSet.contains(target);

            if (sourceInLeft && targetInLeft) {
                leftSubgraphEdgeSet.add(edge);
            } else if (!sourceInLeft && !targetInLeft) {
                rightSubgraphEdgeSet.add(edge);
            }
        }

        Graph leftSubgraph, rightSubgraph;

        leftSubgraph = new Graph(leftSubgraphNodeSet, leftSubgraphEdgeSet);
        rightSubgraph = new Graph(rightSubgraphNodeSet, rightSubgraphEdgeSet);

        int numLeftSubPartitions = (int) Math.ceil((double) numPartitions / 2);
        int numRightSubPartitions = (int) Math.floor((double) numPartitions / 2);

        ArrayList<Set<Node>> leftPartitions = partitionGraph(leftSubgraph, numLeftSubPartitions);
        ArrayList<Set<Node>> rightPartitions = partitionGraph(rightSubgraph, numRightSubPartitions);

        partitions.addAll(leftPartitions);
        partitions.addAll(rightPartitions);
        return partitions;
    }

    private Set<Node> createPartition(Graph graph) { // BFS
        double balanceFactor = 0.1;
        int graphSize = graph.getSize();
        int numReps = Math.min(20, graphSize);
        Set<Node> vertexSet = graph.nodeSet();

        double totalVertexWeight = 0;
        for (Node v : vertexSet) {
            totalVertexWeight += v.getWeight();
        }

        double minCut = Double.POSITIVE_INFINITY;
        Set<Node> minCutPartition = new HashSet<>();

        List<Node> startNodesList = new LinkedList<>(vertexSet);
        Collections.shuffle(startNodesList);
        ListIterator<Node> startNodeIter = startNodesList.listIterator();

        for (int i = 0; i < numReps; i++) {
            Node startVertex = startNodeIter.next();

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

                if (queue.isEmpty() && !unchecked.isEmpty() && (Math.abs(balance - 0.5) > balanceFactor)) {
                    queue.add(unchecked.get(0));
                }
            }

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

            if (Math.abs(balance - 0.5) < balanceFactor) {
                if (cutValue < minCut) {
                    minCut = cutValue;
                    minCutPartition = partition;
                }
            }
        }

        return minCutPartition;
    }


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


    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
}
