package org.elasticsearch.plugin.flavor;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import org.elasticsearch.plugin.flavor.Recommender;
import org.elasticsearch.plugin.flavor.RecommendRequest;
import org.elasticsearch.plugin.flavor.RecommendedItem;
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
        final HashMap<String, Double> similarities =
            itemSimilarityStrategy.similarities(targetItemId, candidateItemIds);

        logger.info("similarities {}", similarities);
        RecommendedItem[] recommendedItems = new RecommendedItem[similarities.size()];
        return recommendedItems;
    }
}
