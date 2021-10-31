package it.unict.pia;

import it.unict.pia.models.Edge;
import it.unict.pia.models.Graph;
import it.unict.pia.models.Node;
import it.unict.pia.reader.CSVGraphReader;
import it.unict.pia.reader.GmlGraphReader1;
import it.unict.pia.reader.GmlGraphReader2;
import it.unict.pia.reader.GraphReader;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        //GraphReader gr = new CSVGraphReader("test1"); // test0 | test1
        //GraphReader gr = new GmlGraphReader1("yeast"); // email | yeast
        GraphReader gr = new GmlGraphReader2("power");

        Application a = new Application(gr.getGraph());
        a.annealing();

        for (int i = 0; i < a.graphs.size(); i++)
            saveGraph(a.graphs.get(i), i);
    }

    private static void saveGraph(Graph graph, int index) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("id,label,weight,partition").append("\n");
        for (var n : graph.getNodesMap().entrySet()) {
            Node v = n.getValue();
            sb.append(n.getKey()).append(",").append(v.getLabel()).append(",").append(v.getWeight()).append(",").append(v.getPartition());
            sb.append("\n");
        }
        saveToFile("output/" + index + "_output_nodes.csv", sb);

        sb = new StringBuilder();
        sb.append("id,source,target,weight").append("\n");
        for (var n : graph.getEdgesMap().entrySet()) {
            Edge v = n.getValue();
            sb.append(n.getKey()).append(",").append(v.getSource()).append(",").append(v.getTarget()).append(",").append(v.getWeight());
            sb.append("\n");
        }
        saveToFile("output/" + index + "_output_edges.csv", sb);
    }

    private static void saveToFile(String fn, StringBuilder sb) throws IOException {
        String s = sb.toString();
        FileOutputStream outputStream = new FileOutputStream(fn);
        byte[] strToBytes = s.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
    }
}
