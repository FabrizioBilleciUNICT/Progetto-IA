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

    public Integer call() throws IOException {
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