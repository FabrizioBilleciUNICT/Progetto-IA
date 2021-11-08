package it.unict.pia.reader;

import it.unict.pia.models.Graph;

import java.io.FileNotFoundException;

public abstract class GraphReader {

    protected final String path;

    public GraphReader(String path) {
        this.path = "networks/" + path + "/";
    }

    public abstract Graph getGraph() throws FileNotFoundException;
}
