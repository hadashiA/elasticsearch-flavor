package org.elasticsearch.plugin.flavor.strategy;

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

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
import org.elasticsearch.plugin.flavor.Similarity;
import org.elasticsearch.plugin.flavor.strategy.ItemSimilarityStrategy;

public class LogLikelihoodItemSimilarityStrategy implements ItemSimilarityStrategy {
    private  ESLogger logger = Loggers.getLogger(LogLikelihoodItemSimilarityStrategy.class);
    private RecommendRequest request;

    public LogLikelihoodItemSimilarityStrategy(final RecommendRequest request) {
        this.request = request;
    }

    public ArrayList<Similarity> similarities(final String targetId, final HashSet<String> itemIds) {
        final ArrayList<Similarity> similarities = new ArrayList<Similarity>(itemIds.size());

        final HashSet<String> allItemIds = new HashSet<String>(itemIds);
        allItemIds.add(targetId);

        final long numPreferences = countAllPreference();
        final HashMap<String, HashSet<String>> preferredUserIdsByItemId = findPreferredUserIdsByItemIds(allItemIds);

        HashSet<String> userIds1 = preferredUserIdsByItemId.get(targetId);
        if (userIds1 == null) {
            return similarities;
        }
        int i = 0;
        for (final String itemId : itemIds) {
            final HashSet<String> userIds2 = preferredUserIdsByItemId.get(itemId);
            if (userIds2 == null) {
                similarities.add(new Similarity(itemId, 0.0));
            } else {
                long preferring1 = userIds1.size();
                long preferring2 = userIds2.size();

                HashSet<String> userIds1and2 = new HashSet<String>(userIds1);
                userIds1and2.retainAll(userIds2);
                long preferring1and2 = userIds1and2.size();

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
                                                     preferring2 - preferring1and2,
                                                     preferring1 - preferring1and2,
                                                     numPreferences - preferring1 - preferring2 + preferring1and2
                                                     );
                final double similarity =  1.0 - 1.0 / (1.0 + logLikelihood);
                similarities.add(new Similarity(itemId, similarity));
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

    private HashMap<String, HashSet<String>> findPreferredUserIdsByItemIds(final HashSet<String> itemIds) {
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
        return userIdsByItemId;
    }
}