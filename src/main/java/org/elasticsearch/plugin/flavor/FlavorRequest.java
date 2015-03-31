package org.elasticsearch.plugin.flavor;

import org.elasticsearch.action.support.single.shard.SingleShardOperationRequest;

class FlavorRequest extends SingleShardOperationRequest<FlavorRequest> {
    private String type;
    private String id;
    private String preferenceType;

    public FlavorRequest(String index, String type, String id) {
        super(index);
        this.type = type;
        this.id   = id;
        this.preferenceType = "preference";
    }

    public String type() {
        return type;
    }

    public String preferenceType() {
        return preferenceType;
    }

    public String id() {
        return id;
    }
}
