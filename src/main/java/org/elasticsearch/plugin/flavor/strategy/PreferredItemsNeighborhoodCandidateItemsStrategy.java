package org.elasticsearch.plugin.flavor.strategy;

import java.util.HashSet;

import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsFilterBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.search.SearchResponse;

import org.elasticsearch.plugin.flavor.RecommendRequest;
import org.elasticsearch.plugin.flavor.strategy.CandidateItemsStrategy;

public class PreferredItemsNeighborhoodCandidateItemsStrategy implements CandidateItemsStrategy {
    private  ESLogger logger = Loggers.getLogger(PreferredItemsNeighborhoodCandidateItemsStrategy.class);
    private RecommendRequest request;
    private int maxPreferenceSize = 10000;

    public PreferredItemsNeighborhoodCandidateItemsStrategy(RecommendRequest request) {
        this.request = request;
    }

    @Override
    public HashSet<String> candidateItemIds(String targetItemId) {
        SearchResponse response = request.client()
            .prepareSearch(request.index())
            .setTypes(request.preferenceType())
            .setQuery(QueryBuilders.termQuery("item_id", targetItemId))
            .addFields("user_id", "item_id", "value")
            .setSize(maxPreferenceSize).execute().actionGet();

        final SearchHits hits = response.getHits();
        long numPreferences = hits.getTotalHits();
        if (numPreferences > maxPreferenceSize) {
            logger.warn("ItemID {} has {} over {}.", targetItemId, numPreferences, maxPreferenceSize);
            numPreferences = maxPreferenceSize;
        }

        final HashSet<String> userIds = new HashSet<String>();
        for (final SearchHit hit : hits) {
            final String userId = hit.field("user_id").getValue();
            userIds.add(userId);
        }

        final HashSet<String> candidateIds = new HashSet<String>();
        final long keepAlive = 10000; // 10sec.
        final int size = 1000;
        final TermsFilterBuilder termsFilter = new TermsFilterBuilder("user_id", userIds);
        response = request.client()
            .prepareSearch(request.index())
            .setTypes(request.preferenceType())
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(keepAlive))
            .setPostFilter(termsFilter)
            .addField("item_id")
            .setSize(size)
            .execute()
            .actionGet();
        while (true) {
            for (SearchHit hit : response.getHits().getHits()) {
                final String itemId = hit.field("item_id").getValue();
                candidateIds.add(itemId);
            }
            //Break condition: No hits are returned
            if (response.getHits().getHits().length == 0) {
                break;
            }
        }
        return candidateIds;
    }
}
