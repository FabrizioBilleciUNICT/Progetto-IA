package it.unict.pia;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        Application a = new Application("yeast"); // email | yeast
        a.readNetwork();
        a.annealing();
    }
}
