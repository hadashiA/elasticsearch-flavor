package org.elasticsearch.plugin.flavor;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsFilterBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.search.SearchResponse;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.AbstractDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.AbstractDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import org.elasticsearch.plugin.flavor.DataModelFactory;

public class ElasticsearchDynamicDataModelFactory implements DataModelFactory {
    private ESLogger logger = Loggers.getLogger(ElasticsearchPreloadDataModel.class);
    private Client client;
    private String preferenceIndex;
    private String preferenceType;

    public ElasticsearchDynamicDataModelFactory(final Client client,
                                                final String preferenceIndex,
                                                final String preferenceType) {
        this.client = client;
        this.preferenceIndex = preferenceIndex;
        this.preferenceType = preferenceType;
    }

    public DataModel createItemBasedDataModel(final long itemId) throws TasteException {
        return new GenericDataModel(new FastByIDMap<PreferenceArray>());
    }

    public DataModel createUserBasedDataModel(final long userId) throws TasteException {
        return new GenericDataModel(new FastByIDMap<PreferenceArray>());
    }
}
