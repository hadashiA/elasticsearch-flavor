package org.elasticsearch.plugin.flavor;

public class RecommendedItem {
    private String scoreLabel;
    private double score;
    private String idLabel;
    private String id;

    public RecommendedItem(String scoreLabel, double score,
                           String idLabel, String id) {
        this.scoreLabel = scoreLabel;
        this.score = score;
        this.idLabel = idLabel;
        this.id = id;
    }

    public String scoreLabel() {
        return scoreLabel();
    }

    public double score() {
        return score;
    }

    public String idLabel() {
        return idLabel;
    }

    public String id() {
        return id;
    }
}
