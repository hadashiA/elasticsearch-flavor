package org.elasticsearch.plugin.flavor;

import org.elasticsearch.client.Client;
import org.elasticsearch.action.support.single.shard.SingleShardOperationRequest;

import org.elasticsearch.plugin.flavor.Recommender;
import org.elasticsearch.plugin.flavor.SimilarItemsRecommender;

public class RecommendRequest extends SingleShardOperationRequest<RecommendRequest> {
    private Client client;
    private String id;
    private String operation;

    private String preferenceType = "preference";
    private int recommendSize = 10;
    private long keepAlive = 10000;
    private int scrollSize = 1000;
    private int maxPreferenceSize = 10000;

    public RecommendRequest(Client client, String index, String id, String operation) {
        super(index);
        this.client = client;
        this.id = id;
        this.preferenceType = "preference";
        this.operation = operation;
    }

    public Recommender createRecommender() {
        if (operation.equals("similar_items")) {
            return new SimilarItemsRecommender(this);
        } else {
            return null;
        }
    }

    public Client client() {
        return client;
    }

    public String preferenceType() {
        return preferenceType;
    }

    public String id() {
        return id;
    }

    public String operation() {
        return operation;
    }

    public long keepAlive() {
        return keepAlive;
    }

    public int scrollSize() {
        return scrollSize;
    }

    public int maxPreferenceSize() {
        return maxPreferenceSize;
    }

    public int recommendSize() {
        return recommendSize;
    }

    public RecommendRequest preferenceType(final String value) {
        preferenceType = value;
        return this;
    }

    public RecommendRequest setScrollSize(final int value) {
        scrollSize = value;
        return this;
    }

    public RecommendRequest setKeepAlive(final long value) {
        keepAlive = value;
        return this;
    }

    public RecommendRequest setMaxPreferenceSize(final int value) {
        maxPreferenceSize = value;
        return this;
    }

    public RecommendRequest recommendSize(final int value) {
        recommendSize = value;
        return this;
    }
}
