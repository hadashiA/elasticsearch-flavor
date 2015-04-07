package org.elasticsearch.plugin.flavor;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.AveragingPreferenceInferrer;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;

public class UserBasedRecommenderBuilder {
    private String similarityName;
    private String neighborhoodName;

    public UserBasedRecommenderBuilder(String similarityName,
                                       String neighborhoodName) {
        this.similarityName   = similarityName;
        this.neighborhoodName = neighborhoodName;
    }

    public UserBasedRecommender buildRecommender(DataModel dataModel) throws TasteException {
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
}
