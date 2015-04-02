package org.elasticsearch.plugin.flavor.strategy;

import org.apache.mahout.math.stats.LogLikelihood;

import org.elasticsearch.plugin.flavor.RecommendRequest;
import org.elasticsearch.plugin.flavor.strategy.ItemSimilarityStrategy;

public class LogLikelihoodItemSimilarityStrategy implements ItemSimilarityStrategy {
    private RecommendRequest request;

    public LogLikelihoodItemSimilarityStrategy(RecommendRequest request) {
        this.request = request;
    }

    public double similarity(String itemId1, String itemId2) {
        return 1;
    }
}
