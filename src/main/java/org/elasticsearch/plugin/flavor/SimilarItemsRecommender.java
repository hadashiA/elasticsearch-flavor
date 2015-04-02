package org.elasticsearch.plugin.flavor;

import java.util.HashSet;
import java.util.Iterator;

import org.elasticsearch.client.Client;

import org.elasticsearch.plugin.flavor.Recommender;
import org.elasticsearch.plugin.flavor.RecommendRequest;
import org.elasticsearch.plugin.flavor.strategy.CandidateItemsStrategy;
import org.elasticsearch.plugin.flavor.strategy.PreferredItemsNeighborhoodCandidateItemsStrategy;
import org.elasticsearch.plugin.flavor.strategy.ItemSimilarityStrategy;
import org.elasticsearch.plugin.flavor.strategy.LogLikelihoodItemSimilarityStrategy;

public class SimilarItemsRecommender implements Recommender {
    private RecommendRequest request;
    private CandidateItemsStrategy candidateItemsStrategy;
    private ItemSimilarityStrategy itemSimilarityStrategy;

    public SimilarItemsRecommender(final RecommendRequest request) {
        this.request = request;
        this.candidateItemsStrategy = new PreferredItemsNeighborhoodCandidateItemsStrategy(request);
        this.itemSimilarityStrategy = new LogLikelihoodItemSimilarityStrategy(request);
    }

    public String[] recommend() {
        final HashSet<String> candidateItemIds = candidateItemsStrategy.candidateItemIds(request.id());
        return candidateItemIds.toArray(new String[candidateItemIds.size()]);
    }
}
