package org.elasticsearch.plugin.flavor;

public class Similarity implements Comparable<Similarity> {
    private String id;
    private double value;

    public Similarity(String id, double similarity) {
        this.id = id;
        this.value = value;
    }

    public String id() {
        return id;
    }

    public double value() {
        return value;
    }

    @Override
    public int compareTo(Similarity other) {
        if (value() > other.value()) {
            return 1;
        } else if (value() < other.value()) {
            return -1;
        } else {
            return 0;
        }
    }
}
