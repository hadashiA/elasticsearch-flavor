package org.elasticsearch.plugin.flavor.strategy;

public interface ItemSimilarityStrategy {
    double similarity(String itemId1, String itemId2);
}
