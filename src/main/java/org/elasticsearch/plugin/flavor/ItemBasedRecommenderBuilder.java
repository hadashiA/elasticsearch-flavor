package org.elasticsearch.plugin.flavor;

import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsFilterBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.search.SearchResponse;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.AveragingPreferenceInferrer;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;

public class ItemBasedRecommenderBuilder {
    private String similarityName;

    public ItemBasedRecommenderBuilder(final String similarityName) {
        this.similarityName = similarityName;
    }

    public ItemBasedRecommender buildRecommender(DataModel dataModel) throws TasteException {
        ItemSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        if (similarityName != null) {
            if (similarityName.equals("PearsonCorrelationSimilarity")) {
                similarity = new PearsonCorrelationSimilarity(dataModel);
                
            } else if (similarityName.equals("EuclideanDistanceSimilarity")) {
                similarity = new EuclideanDistanceSimilarity(dataModel);
                
            } else if (similarityName.equals("LogLikelihoodSimilarity")) {
                similarity = new LogLikelihoodSimilarity(dataModel);
                
            } else if (similarityName.equals("TanimotoCoefficientSimilarity")) {
                similarity = new TanimotoCoefficientSimilarity(dataModel);
                
            } else {
                throw new TasteException("ItemSimilarity algorithm not support: " + similarityName);
            }
        }
        return new GenericItemBasedRecommender(dataModel, similarity);
    }

    // public DataModel buildDataModel(final String targetId) {

    //     HashSet<String> userIds1 = preferredUserIdsByItemId.get(targetId);
    //     if (userIds1 == null) {
    //         return similarities;
    //     }
    //     int i = 0;
    //     for (final String itemId : itemIds) {
    //         final HashSet<String> userIds2 = preferredUserIdsByItemId.get(itemId);
    //         if (userIds2 == null) {
    //             similarities.add(new Similarity(itemId, 0.0));
    //         } else {
    //             long preferring1 = userIds1.size();
    //             long preferring2 = userIds2.size();

    //             HashSet<String> userIds1and2 = new HashSet<String>(userIds1);
    //             userIds1and2.retainAll(userIds2);
    //             long preferring1and2 = userIds1and2.size();

    //             // Parameters:
    //             //     k11 The number of times the two events occurred together
    //             //     k12 The number of times the second event occurred WITHOUT the first event
    //             //     k21 The number of times the first event occurred WITHOUT the second event
    //             //     k22 The number of times something else occurred (i.e. was neither of these events
    //             // Returns:
    //             //     The raw log-likelihood ratio

    //             //     Credit to http://tdunning.blogspot.com/2008/03/surprise-and-coincidence.html for the table and the descriptions.
    //             final double logLikelihood =
    //                 LogLikelihood.logLikelihoodRatio(preferring1and2,
    //                                                  preferring2 - preferring1and2,
    //                                                  preferring1 - preferring1and2,
    //                                                  numPreferences - preferring1 - preferring2 + preferring1and2
    //                                                  );
    //             final double similarity =  1.0 - 1.0 / (1.0 + logLikelihood);
    //             similarities.add(new Similarity(itemId, similarity));
    //         }
    //     }
    //     return similarities;
    // }

    // private long countAllPreference() {
    //     SearchResponse response = request.client()
    //         .prepareSearch(request.index())
    //         .setTypes(request.preferenceType())
    //         .setQuery(QueryBuilders.matchAllQuery())
    //         .setSize(0)
    //         .execute()
    //         .actionGet();
    //     return response.getHits().getTotalHits();
    // }

    // private HashMap<String, HashSet<String>> findPreferredUserIdsByItemIds(final HashSet<String> itemIds) {
    //     final HashMap<String, HashSet<String>> userIdsByItemId = new HashMap<String, HashSet<String>>();
    //     final TermsFilterBuilder termsFilter = new TermsFilterBuilder("item_id", itemIds);
    //     SearchResponse scroll = request.client()
    //         .prepareSearch(request.index())
    //         .setTypes(request.preferenceType())
    //         .setSearchType(SearchType.SCAN)
    //         .setScroll(new TimeValue(request.keepAlive()))
    //         .setPostFilter(termsFilter)
    //         .addFields("item_id", "user_id", "value")
    //         .addSort("created_at", SortOrder.DESC)
    //         .setSize(request.scrollSize())
    //         .execute()
    //         .actionGet();

    //     while (true) {
    //         for (SearchHit hit : scroll.getHits().getHits()) {
    //             final String itemId = "" + hit.field("item_id").getValue();
    //             final String userId = "" + hit.field("user_id").getValue();
    //             HashSet<String> userIds = userIdsByItemId.get(itemId);
    //             if (userIds == null) {
    //                 userIds = new HashSet<String>();
    //                 userIdsByItemId.put(itemId, userIds);
    //             }
    //             userIds.add(userId);
    //         }
    //         //Break condition: No hits are returned
    //         scroll = request.client()
    //             .prepareSearchScroll(scroll.getScrollId())
    //             .setScroll(new TimeValue(request.keepAlive()))
    //             .execute()
    //             .actionGet();
    //         if (scroll.getHits().getHits().length == 0) {
    //             break;
    //         }
    //     }
    //     return userIdsByItemId;
    // }
}
