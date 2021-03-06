package it.unict.pia.reader;

import it.unict.pia.models.Edge;
import it.unict.pia.models.Graph;
import it.unict.pia.models.Node;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GmlGraphReader2 extends GraphReader {

    public GmlGraphReader2(String path) {
        super(path);
    }

    @Override
    public Graph getGraph() throws FileNotFoundException {
        FileInputStream graphInput = new FileInputStream(path + "network.gml");
        Scanner scan = new Scanner(graphInput);

        final Map<String, Node> nodesMap = new HashMap<>();
        final Map<String, Edge> edgesMap = new HashMap<>();

        int count = 0;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (!scan.hasNextLine()) break;

            if (count > 3) {
                String currentType = line.replace("  ", "");
                if (currentType.equals("node")) {
                    scan.nextLine();
                    String currentId = scan.nextLine().replace("    id ", "");
                    scan.nextLine();
                    Node node = new Node(currentId, currentId);
                    nodesMap.put(currentId, node);
                } else {
                    scan.nextLine();
                    String currentSource = scan.nextLine().replace("    source ", "");
                    String currentTarget = scan.nextLine().replace("    target ", "");
                    scan.nextLine();
                    Edge edge = new Edge(currentSource, currentTarget, 1.0);
                    edgesMap.put(edge.getId(), edge);
                }
            }
            count++;
        }

        return new Graph(nodesMap, edgesMap);
    }
}
