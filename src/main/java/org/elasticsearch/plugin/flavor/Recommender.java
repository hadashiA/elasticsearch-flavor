package org.elasticsearch.plugin.flavor;

import java.util.ArrayList;

import org.elasticsearch.plugin.flavor.RecommendedItem;

public interface Recommender {
    ArrayList<RecommendedItem> recommend();
}
