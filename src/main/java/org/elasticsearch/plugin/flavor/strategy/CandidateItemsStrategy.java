package org.elasticsearch.plugin.flavor.strategy;

import java.util.HashSet;

public interface CandidateItemsStrategy {
    public HashSet<String> candidateItemIds(String itemId);
}
