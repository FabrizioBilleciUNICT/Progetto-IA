package it.unict.pia;

import it.unict.pia.models.Edge;
import it.unict.pia.models.Graph;
import it.unict.pia.models.Node;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public class Utils {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static void saveGraph(Graph graph, int index) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("id,label,weight,partition").append("\n");
        for (var n : graph.getNodesMap().entrySet()) {
            Node v = n.getValue();
            sb.append(n.getKey()).append(",\"").append(v.getLabel()).append("\",").append(v.getWeight()).append(",").append(v.getPartition());
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
