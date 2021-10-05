package it.unict.pia;

import it.unict.pia.models.Edge;
import it.unict.pia.models.Node;
import it.unict.pia.models.Partition;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Application {

    private final Map<String, Node> nodesMap = new HashMap();
    private final Map<String, Edge> edgesMap = new HashMap();

    private final String network;

    public Application(String network) {
        this.network = network;
    }

    public void readNetwork() throws FileNotFoundException {
        FileInputStream graphInput = new FileInputStream("networks/" + network + "/" + network + ".gml");
        Scanner scan = new Scanner(graphInput);

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
    }

    public Partition annealing() {
        final int ct = 60000;

        final Partition s_0 = createSeedPartition();
        Partition s_star = s_0;
        int i = 0;
        for (int k = 0; k < 100; k++) {
            Partition s_i = new Partition();
            while (this.nodesMap.size() > ct) {
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
        return new Partition();
    }

    private int conductance(Partition s_i) {
        return 0;
    }

    private Partition solutionGuidedCoarsening(Partition s_i) {
        // change something in nodes & edges
        Partition s_i1 = new Partition();
        return s_i1;
    }

    private Partition unCoarsening(Partition s_i) {
        // change something in nodes & edges
        Partition s_i1 = new Partition();
        return s_i1;
    }

    private Partition localRefinement(Partition s_i) {
        final double T0 = (1.0 + 1e-20) / 2.0;
        double T = T0;
        final double salter = 200000.0;
        final double ar = 0.05;
        final double theta = 0.98;

        Partition s_best = s_i;
        double mv = 0.0;
        int belowCount = 0;
        while (belowCount < 5) {
            mv = 0.0;
            for (int i = 0; i < salter; i++) {
                Node v = getRandomVertexCV(s_i);
                Partition s_p = relocate(s_i, v);
                final int d = delta(v);
                if (d < 0 || canRelocate(T, d)) {
                    s_i = s_p;
                    mv++;
                }

                if (conductance(s_i) < conductance(s_best)) {
                    s_best = s_i;
                }
            }

            T = T * theta;

            if ((mv/salter) < ar) belowCount++;
            else belowCount = 0;
        }

        Partition s_i1 = new Partition();
        return s_i1;
    }

    private Node getRandomVertexCV(Partition s_i) {
        return this.nodesMap.values().stream().collect(Collectors.toUnmodifiableList()).get(0);
    }

    private Partition relocate(Partition s_i, Node v) {
        return new Partition();
    }

    private int delta(Node v) {
        return 0;
    }

    private boolean canRelocate(double T, int d) {
        if (d < 0) return true; // 1
        else {
            double prob = Math.exp(-d/T);
            return prob > 0.5;
        }
    }
}
