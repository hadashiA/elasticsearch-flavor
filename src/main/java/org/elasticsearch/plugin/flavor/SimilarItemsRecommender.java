package org.elasticsearch.plugin.flavor;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import org.elasticsearch.plugin.flavor.Recommender;
import org.elasticsearch.plugin.flavor.RecommendRequest;
import org.elasticsearch.plugin.flavor.RecommendedItem;
import org.elasticsearch.plugin.flavor.Similarity;
import org.elasticsearch.plugin.flavor.strategy.CandidateItemsStrategy;
import org.elasticsearch.plugin.flavor.strategy.PreferredItemsNeighborhoodCandidateItemsStrategy;
import org.elasticsearch.plugin.flavor.strategy.ItemSimilarityStrategy;
import org.elasticsearch.plugin.flavor.strategy.LogLikelihoodItemSimilarityStrategy;

public class SimilarItemsRecommender implements Recommender {
    private  ESLogger logger = Loggers.getLogger(SimilarItemsRecommender.class);
    private RecommendRequest request;
    private CandidateItemsStrategy candidateItemsStrategy;
    private ItemSimilarityStrategy itemSimilarityStrategy;

    public SimilarItemsRecommender(final RecommendRequest request) {
        this.request = request;
        this.candidateItemsStrategy = new PreferredItemsNeighborhoodCandidateItemsStrategy(request);
        this.itemSimilarityStrategy = new LogLikelihoodItemSimilarityStrategy(request);
    }

    public RecommendedItem[] recommend() {
        final String targetItemId = request.id();
        final HashSet<String> candidateItemIds =
            candidateItemsStrategy.candidateItemIds(targetItemId);
        final Similarity[] similarities =
            itemSimilarityStrategy.similarities(targetItemId, candidateItemIds);

        Arrays.sort(similarities);

        RecommendedItem[] recommendedItems = new RecommendedItem[similarities.length];
        for (int i = 0; i < request.recommendSize(); i++) {
            Similarity similarity = similarities[i];
            RecommendedItem recommendedItem = new RecommendedItem("similarity", similarity.value(), "item_id", similarity.id());
            recommendedItems[i] = recommendedItem;
        }
        return recommendedItems;
    }
}
