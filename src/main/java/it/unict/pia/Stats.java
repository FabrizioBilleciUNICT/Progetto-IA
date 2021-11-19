package it.unict.pia;

public class Stats {

    private double modularity;
    private int levels;
    private int partitions;
    private final long timeStart;

    public Stats() {
        timeStart = System.currentTimeMillis();
    }

    public void setModularity(double modularity) {
        this.modularity = modularity;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public String getStats() {
        return "Finished in: " + ((System.currentTimeMillis() - timeStart) / 1000) +
                " seconds, modularity: " + modularity +
                ", levels: " + levels +
                ", partitions: " + partitions;
    }

    public int getElapsedTime() {
        return (int) ((System.currentTimeMillis() - timeStart) / 1000);
    }
}
