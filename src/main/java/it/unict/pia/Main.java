package it.unict.pia;

import it.unict.pia.reader.*;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

import static it.unict.pia.Utils.saveGraph;

@CommandLine.Command(name = "CDCUM", description = "Community detection in coarse-uncoarse modularity way", mixinStandardHelpOptions = true)
public class Main implements Callable<Integer> {
    @CommandLine.Option(names = {"-n", "--network"}, required = true, description = "Network: 'yeast' | 'email' | 'power' | 'football' | 'cond-mat-2003'", defaultValue = "test1")
    String network;
    @CommandLine.Option(names = {"-w", "--weighted"}, description = "Weighted edges: 'y' | 'n'", defaultValue = "n")
    String weighted;
    @CommandLine.Option(names = {"-o", "--outputLen"}, description = "Number of CSV files to save, based on levels", defaultValue = "1")
    String outputLen;

    /*
    email Finished in: 0 seconds, modularity: 0.5557186637773307, levels: 16, partitions: 10
    yeast Finished in: 0 seconds, modularity: 0.5844241056393085, levels: 19, partitions: 43
    power Finished in: 0 seconds, modularity: 0.9345823755550606, levels: 15, partitions: 38
    football Finished in: 0 seconds, modularity: 0.6044072289092501, levels: 10, partitions: 9
    cond-mat-2003 Finished in: 8 seconds, modularity: 0.7518555078718582, levels: 23, partitions: 1646
    cond-mat-2003 (w) Finished in: 82 seconds, modularity: 0.3340850299462593, levels: 74, partitions: 12683
     */

    public Integer call() throws IOException {
        network = "cond-mat-2003";
        weighted = "y";
        outputLen = "1";
        GraphReader gr = switch (network) {
            case "yeast", "email" -> new GmlGraphReader1(network);
            case "power" -> new GmlGraphReader2(network);
            case "football" -> new GmlGraphReader3(network);
            case "cond-mat-2003" -> new GmlGraphReader4(network, weighted.equals("y"));
            default -> new CSVGraphReader(network); // test0, test1
        };

        Application a = new Application(gr.getGraph());
        String stats = a.annealing();
        System.out.println(stats);

        for (int i = 0; i < Math.min(a.graphs.size(), Integer.parseInt(outputLen)); i++)
            saveGraph(a.graphs.get(i), i);

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}