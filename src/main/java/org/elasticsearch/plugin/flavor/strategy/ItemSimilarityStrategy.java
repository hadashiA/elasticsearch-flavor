package org.elasticsearch.plugin.flavor.strategy;

import java.util.HashMap;
import java.util.HashSet;

public interface ItemSimilarityStrategy {
    HashMap<String, Double> similarities(final String targetId,
                                         final HashSet<String> itemIds);
}
