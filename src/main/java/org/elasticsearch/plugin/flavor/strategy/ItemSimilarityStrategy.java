package org.elasticsearch.plugin.flavor.strategy;

import java.util.HashSet;

import org.elasticsearch.plugin.flavor.Similarity;

public interface ItemSimilarityStrategy {
    Similarity[] similarities(final String targetId, final HashSet<String> itemIds);
}
