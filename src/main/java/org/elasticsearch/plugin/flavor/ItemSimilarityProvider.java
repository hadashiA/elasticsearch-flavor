package org.elasticsearch.plugin.flavor;

import java.util.HashSet;
import java.util.Iterator;

import org.elasticsearch.client.Client;

import org.elasticsearch.plugin.flavor.FlavorRequest;

public class ItemSimilarityProvider {
    private Client client;
    private FlavorRequest request;

    public ItemSimilarityProvider(final Client client, final FlavorRequest request) {
        this.client = client;
        this.request = request;
    }

    public String[] similarIds() {
        return new String[]{ request.id() };
    }

    public 

    public double countUsersWithPreferenceFor(final long itemId) {
        return 0;
    }
}
