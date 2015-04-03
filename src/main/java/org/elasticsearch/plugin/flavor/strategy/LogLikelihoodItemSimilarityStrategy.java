package org.elasticsearch.plugin.flavor.strategy;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsFilterBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.search.SearchResponse;

import org.apache.mahout.math.stats.LogLikelihood;

import org.elasticsearch.plugin.flavor.RecommendRequest;
import org.elasticsearch.plugin.flavor.strategy.ItemSimilarityStrategy;

public class LogLikelihoodItemSimilarityStrategy implements ItemSimilarityStrategy {
    private  ESLogger logger = Loggers.getLogger(LogLikelihoodItemSimilarityStrategy.class);
    private RecommendRequest request;

    public LogLikelihoodItemSimilarityStrategy(final RecommendRequest request) {
        this.request = request;
    }

    public HashMap<String, Double> similarities(final String targetId, final HashSet<String> itemIds) {
        final HashMap<String, Double> similarities = new HashMap<String, Double>();

        final HashSet<String> allItemIds = new HashSet<String>(itemIds);
        allItemIds.add(targetId);

        final long numPreferences = countAllPreference();
        final HashMap<String, Integer> numPreferredUsersByItemId = countPreferredUsersByItemIds(allItemIds);
        logger.info("numPreferredUsersByItemId {}", numPreferredUsersByItemId);

        Integer numPreferredUsers1 = numPreferredUsersByItemId.get(targetId);
        if (numPreferredUsers1 == null) {
            numPreferredUsers1 = 0;
        }
        for (final String itemId : itemIds) {
            Integer numPreferredUsers2 = numPreferredUsersByItemId.get(itemId);
            if (numPreferredUsers2 == null) {
                similarities.put(itemId, 0.0);
            } else {
                long preferring1 = numPreferredUsers1.longValue();
                long preferring2 = numPreferredUsers2.longValue();
                // この値の出しかたが違う
                long preferring1and2 = preferring1 + preferring2;

                // Parameters:
                //     k11 The number of times the two events occurred together
                //     k12 The number of times the second event occurred WITHOUT the first event
                //     k21 The number of times the first event occurred WITHOUT the second event
                //     k22 The number of times something else occurred (i.e. was neither of these events
                // Returns:
                //     The raw log-likelihood ratio

                //     Credit to http://tdunning.blogspot.com/2008/03/surprise-and-coincidence.html for the table and the descriptions.
                final double logLikelihood =
                    LogLikelihood.logLikelihoodRatio(preferring1and2,
                                                     preferring1,
                                                     preferring2,
                                                     numPreferences
                                                     );
                logger.info("logLikelihood {}", logLikelihood);
                final double similarity =  1.0 - 1.0 / (1.0 + logLikelihood);
                logger.info("similarity {}", similarity);
                similarities.put(itemId, similarity);
            }
        }
        return similarities;
    }

    private long countAllPreference() {
        SearchResponse response = request.client()
            .prepareSearch(request.index())
            .setTypes(request.preferenceType())
            .setQuery(QueryBuilders.matchAllQuery())
            .setSize(0)
            .execute()
            .actionGet();
        return response.getHits().getTotalHits();
    }

    private HashMap<String, Integer> countPreferredUsersByItemIds(final HashSet<String> itemIds) {
        final HashMap<String, HashSet<String>> userIdsByItemId = new HashMap<String, HashSet<String>>();
        final TermsFilterBuilder termsFilter = new TermsFilterBuilder("item_id", itemIds);
        SearchResponse scroll = request.client()
            .prepareSearch(request.index())
            .setTypes(request.preferenceType())
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(request.keepAlive()))
            .setPostFilter(termsFilter)
            .addFields("item_id", "user_id", "value")
            .addSort("created_at", SortOrder.DESC)
            .setSize(request.scrollSize())
            .execute()
            .actionGet();

        while (true) {
            for (SearchHit hit : scroll.getHits().getHits()) {
                final String itemId = "" + hit.field("item_id").getValue();
                final String userId = "" + hit.field("user_id").getValue();
                HashSet<String> userIds = userIdsByItemId.get(itemId);
                if (userIds == null) {
                    userIds = new HashSet<String>();
                    userIdsByItemId.put(itemId, userIds);
                }
                userIds.add(userId);
            }
            //Break condition: No hits are returned
            scroll = request.client()
                .prepareSearchScroll(scroll.getScrollId())
                .setScroll(new TimeValue(request.keepAlive()))
                .execute()
                .actionGet();
            if (scroll.getHits().getHits().length == 0) {
                break;
            }
        }
        HashMap<String, Integer> preferrings = new HashMap<String, Integer>();

        for (Map.Entry<String, HashSet<String>> entry : userIdsByItemId.entrySet()) {
            preferrings.put(entry.getKey(), entry.getValue().size());
        }
        return preferrings;
    }
}
