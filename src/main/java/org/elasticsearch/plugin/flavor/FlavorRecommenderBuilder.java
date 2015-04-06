package org.elasticsearch.plugin.flavor;

import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;

public class FlavorRecommenderBuilder implements RecommenderBuilder {
    private String operationName;
    private String similarityName;

    public FlavorRecommenderBuilder(String operationName, String similarityName) {
        this.operationName  = operationName;
        this.similarityName = similarityName;
    }

    public Recommender buildRecommender(DataModel dataModel) throws TasteException {
        if (operationName != null && operationName.equals("similar_items")) {
            ItemSimilarity similarity;
            if (similarityName != null && similarityName.equals("LogLikelihoodSimilarity")) {
                similarity = new LogLikelihoodSimilarity(dataModel);
            } else {
                similarity = new EuclideanDistanceSimilarity(dataModel);
            }
            GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel, similarity);
            return recommender;

        } else {
            throw new TasteException("Invalid operation : " + operationName);
        }
    }
}
