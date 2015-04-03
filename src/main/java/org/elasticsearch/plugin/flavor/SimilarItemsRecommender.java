package org.elasticsearch.plugin.flavor;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

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

    public ArrayList<RecommendedItem> recommend() {
        final String targetItemId = request.id();
        final HashSet<String> candidateItemIds =
            candidateItemsStrategy.candidateItemIds(targetItemId);
        final ArrayList<Similarity> similarities =
            itemSimilarityStrategy.similarities(targetItemId, candidateItemIds);

        Collections.sort(similarities);

        ArrayList<RecommendedItem> recommendedItems =
            new ArrayList<RecommendedItem>(request.recommendSize());
        for (int i = 0; i < request.recommendSize() && i < similarities.size(); i++) {
            Similarity similarity = similarities.get(i);

            RecommendedItem recommendedItem = new RecommendedItem("similarity",
                                                                  similarity.value(),
                                                                  "item_id",
                                                                  similarity.id());
            recommendedItems.add(recommendedItem);
        }
        return recommendedItems;
    }
}
