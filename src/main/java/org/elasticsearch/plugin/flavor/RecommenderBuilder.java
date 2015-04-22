package org.elasticsearch.plugin.flavor;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.AveragingPreferenceInferrer;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;

public class RecommenderBuilder {
    private String similarityName;
    private String neighborhoodName;
    private DataModel dataModel;

    public static RecommenderBuilder builder() {
        return new RecommenderBuilder();
    }

    public RecommenderBuilder dataModel(final DataModel dataModel) {
        this.dataModel = dataModel;
        return this;
    }

    public RecommenderBuilder similarity(final String similarityName) {
        this.similarityName = similarityName;
        return this;
    }

    public RecommenderBuilder neighborhood(final String neighborhoodName) {
        this.neighborhoodName = neighborhoodName;
        return this;
    }

    public UserBasedRecommender userBasedRecommender() throws TasteException {
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        if (similarityName != null) {
            if (similarityName.equals("PearsonCorrelationSimilarity")) {
                similarity = new PearsonCorrelationSimilarity(dataModel);

            } else if (similarityName.equals("EuclideanDistanceSimilarity")) {
                similarity = new EuclideanDistanceSimilarity(dataModel);

            } else if (similarityName.equals("TanimotoCoefficientSimilarity")) {
                similarity = new TanimotoCoefficientSimilarity(dataModel);

            } else if (similarityName.equals("LogLikelihoodSimilarity")) {
                similarity = new LogLikelihoodSimilarity(dataModel);

            } else {
                throw new TasteException("UserSimilarity algorithm has not been supported: " + similarityName);
            }
        }
            
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, dataModel);
        if (neighborhoodName != null) {
            if (neighborhood.equals("NearestNUserNeighborhood")) {
                neighborhood = new NearestNUserNeighborhood(10, similarity, dataModel);

            } else if (neighborhoodName.equals("ThresholdUserNeighborhood")) {
                neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);

            } else {
                throw new TasteException("UserNeighborhood algorithm has not been supported: " + neighborhoodName);
            }
        }

        similarity.setPreferenceInferrer(new AveragingPreferenceInferrer(dataModel));
        return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
    }

    public ItemBasedRecommender itemBasedRecommender() throws TasteException {
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
