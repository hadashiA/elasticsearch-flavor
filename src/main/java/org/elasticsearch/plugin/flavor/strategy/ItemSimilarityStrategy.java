package org.elasticsearch.plugin.flavor.strategy;

import java.util.HashSet;
import java.util.ArrayList;

import org.elasticsearch.plugin.flavor.Similarity;

public interface ItemSimilarityStrategy {
    ArrayList<Similarity> similarities(final String targetId, final HashSet<String> itemIds);
}
