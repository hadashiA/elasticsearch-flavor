package org.elasticsearch.plugin.flavor;

import org.elasticsearch.client.Client;
import org.elasticsearch.action.support.single.shard.SingleShardOperationRequest;

public class RecommendRequest extends SingleShardOperationRequest<RecommendRequest> {
    private Client client;
    private String id;
    private String preferenceType;

    public RecommendRequest(Client client, String index, String preferenceType, String id) {
        super(index);
        this.client = client;
        this.id = id;
        this.preferenceType = "preference";
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
}
