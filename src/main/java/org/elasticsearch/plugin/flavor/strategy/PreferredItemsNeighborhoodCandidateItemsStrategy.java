package org.elasticsearch.plugin.flavor.strategy;

import java.util.HashSet;

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

import org.elasticsearch.plugin.flavor.RecommendRequest;
import org.elasticsearch.plugin.flavor.strategy.CandidateItemsStrategy;

public class PreferredItemsNeighborhoodCandidateItemsStrategy implements CandidateItemsStrategy {
    private  ESLogger logger = Loggers.getLogger(PreferredItemsNeighborhoodCandidateItemsStrategy.class);
    private RecommendRequest request;

    public PreferredItemsNeighborhoodCandidateItemsStrategy(RecommendRequest request) {
        this.request = request;
    }

    @Override
    public HashSet<String> candidateItemIds(String targetItemId) {
        int maxPreferenceSize = request.maxPreferenceSize();
        SearchResponse userIdResponse = request.client()
            .prepareSearch(request.index())
            .setTypes(request.preferenceType())
            .setQuery(QueryBuilders.termQuery("item_id", targetItemId))
            .addFields("user_id")
            .addSort("created_at", SortOrder.DESC)
            .setSize(maxPreferenceSize)
            .execute()
            .actionGet();

        final SearchHits userIdHits = userIdResponse.getHits();
        long numPreferences = userIdHits.getTotalHits();
        if (numPreferences > maxPreferenceSize) {
            logger.warn("ItemID {} has {} over {}.", targetItemId, numPreferences, maxPreferenceSize);
            numPreferences = maxPreferenceSize;
        }

        final HashSet<String> userIds = new HashSet<String>();
        for (final SearchHit hit : userIdHits) {
            final String userId = "" + hit.field("user_id").getValue();
            userIds.add(userId);
        }

        final HashSet<String> candidateIds = new HashSet<String>();
        final TermsFilterBuilder termsFilter = new TermsFilterBuilder("user_id", userIds);
        SearchResponse scroll = request.client()
            .prepareSearch(request.index())
            .setTypes(request.preferenceType())
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(request.keepAlive()))
            .setPostFilter(termsFilter)
            .addField("item_id")
            .addSort("created_at", SortOrder.DESC)
            .setSize(request.scrollSize())
            .execute()
            .actionGet();
        while (true) {
            for (SearchHit hit : scroll.getHits().getHits()) {
                final String itemId = "" + hit.field("item_id").getValue();
                candidateIds.add(itemId);
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
        return candidateIds;
    }
}
