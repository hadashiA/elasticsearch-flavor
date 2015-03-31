package org.elasticsearch.plugin.flavor;

import org.elasticsearch.client.Client;

import org.elasticsearch.plugin.flavor.FlavorRequest;

public class ItemSimilarity {
    private Client client;
    private FlavorRequest request;

    public ItemSimilarity(final Client client, final FlavorRequest request) {
        this.client = client;
        this.request = request;
    }

    public double countUsersWithPreferenceFor(final long itemId) {
        return 0;
    }
}
