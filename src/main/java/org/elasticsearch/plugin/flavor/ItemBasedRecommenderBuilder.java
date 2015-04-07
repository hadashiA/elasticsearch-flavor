package org.elasticsearch.plugin.flavor;

import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.AveragingPreferenceInferrer;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;

public class ItemBasedRecommenderBuilder {
    private String similarityName;
    private String neighborhoodName;

    public ItemBasedRecommenderBuilder(String similarityName) {
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
