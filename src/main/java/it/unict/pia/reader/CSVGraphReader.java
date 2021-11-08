package it.unict.pia.reader;

import it.unict.pia.models.Edge;
import it.unict.pia.models.Graph;
import it.unict.pia.models.Node;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CSVGraphReader extends GraphReader {

    public CSVGraphReader(String path) {
        super(path);
    }

    @Override
    public Graph getGraph() throws FileNotFoundException {
        final Map<String, Node> nodesMap = readCSVNodes(this.path + "nodes.csv");
        final Map<String, Edge> edgesMap = readCSVEdges(this.path + "edges.csv");
        return new Graph(nodesMap, edgesMap);
    }

    private Map<String, Edge> readCSVEdges(String path) throws FileNotFoundException {
        FileInputStream graphInput = new FileInputStream(path);
        Scanner scan = new Scanner(graphInput);
        final Map<String, Edge> edgesMap = new HashMap<>();
        boolean header = true;
        while (scan.hasNextLine()) {
            String[] line = scan.nextLine().split(",");

            if (header) {
                header = false;
                continue;
            }

            Edge edge = new Edge(line[1], line[2], 1.0);
            edgesMap.put(edge.getId(), edge);
        }
        return edgesMap;
    }

    private Map<String, Node> readCSVNodes(String path) throws FileNotFoundException {
        FileInputStream graphInput = new FileInputStream(path);
        Scanner scan = new Scanner(graphInput);
        final Map<String, Node> nodesMap = new HashMap<>();
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
}
