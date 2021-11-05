package it.unict.pia;

import it.unict.pia.reader.*;

import java.io.IOException;

import static it.unict.pia.Utils.saveGraph;

public class Main {

    //(0.582761 - 0.5217472412531976)/1.5*100
    /**
     * test1    --> Finished in: 0 seconds,   modularity: 0.411242603550296,  levels: 7,   partitions: 2
     * yeast    --> Finished in: 1 seconds,   modularity: 0.5678863240716703, levels: 28,  partitions: 87
     * email    --> Finished in: 1 seconds,   modularity: 0.5211278574270243, levels: 20,  partitions: 37
     * power    --> Finished in: 1 seconds,   modularity: 0.9294606134067894, levels: 54,  partitions: 50
     * football --> Finished in: 0 seconds,   modularity: 0.5835752816224862, levels: 9,   partitions: 12
     * cond-mat --> Finished in: 175 seconds, modularity: 0.2338610542628626, levels: 166, partitions: 19148
     */
    public static void main(String[] args) throws IOException {
        //GraphReader gr = new CSVGraphReader("test1"); // test0 | test1
        //GraphReader gr = new GmlGraphReader1("email"); // email | yeast
        //GraphReader gr = new GmlGraphReader2("power");
        //GraphReader gr = new GmlGraphReader3("football");
        GraphReader gr = new GmlGraphReader4("cond-mat-2003");

        Application a = new Application(gr.getGraph());
        String stats = a.annealing();
        System.out.println(stats);

        for (int i = 0; i < 1; i++)
            saveGraph(a.graphs.get(i), i);
    }
}
