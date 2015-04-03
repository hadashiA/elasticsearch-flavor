package org.elasticsearch.plugin.flavor;

import org.elasticsearch.plugin.flavor.RecommendedItem;

public interface Recommender {
    RecommendedItem[] recommend();
}
