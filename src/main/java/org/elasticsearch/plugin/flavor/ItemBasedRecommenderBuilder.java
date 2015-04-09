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
}
