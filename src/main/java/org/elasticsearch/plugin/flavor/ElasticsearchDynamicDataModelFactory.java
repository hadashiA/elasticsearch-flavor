package org.elasticsearch.plugin.flavor;

import java.util.HashSet;

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
import org.elasticsearch.index.query.FilterBuilders;
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
    private ESLogger logger = Loggers.getLogger(ElasticsearchDynamicDataModelFactory.class);
    private Client client;

    private int scrollSize = 2000;
    private long keepAlive = 10000;

    public ElasticsearchDynamicDataModelFactory(final Client client) {
        this.client = client;
    }

    public DataModel createItemBasedDataModel(final String index,
                                              final String type,
                                              final long itemId) throws TasteException {
        SearchResponse userIdsResponse = client
            .prepareSearch(index)
            .setTypes(type)
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(keepAlive))
            .setPostFilter(FilterBuilders.termFilter("item_id", itemId))
            .addFields("user_id")
            .setSize(scrollSize)
            .execute()
            .actionGet();

        final long numUsers = userIdsResponse.getHits().getTotalHits();
        FastIDSet userIds = new FastIDSet((int)numUsers);
        while (true) {
            for (SearchHit hit : userIdsResponse.getHits().getHits()) {
                final long userId = getLongValue(hit, "user_id");
                userIds.add(userId);
            }
            //Break condition: No hits are returned
            userIdsResponse = client
                .prepareSearchScroll(userIdsResponse.getScrollId())
                .setScroll(new TimeValue(keepAlive))
                .execute()
                .actionGet();
            if (userIdsResponse.getHits().getHits().length == 0) {
                break;
            }
        }
        return createDataModelFromUserIds(index, type, userIds);
    }

    public DataModel createUserBasedDataModel(final String index,
                                              final String type,
                                              final long targetUserId) throws TasteException {
        SearchResponse itemIdsResponse = client
            .prepareSearch(index)
            .setTypes(type)
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(keepAlive))
            .setPostFilter(FilterBuilders.termFilter("user_id", targetUserId))
            .addFields("item_id")
            .setSize(scrollSize)
            .execute()
            .actionGet();
        final long numItems = itemIdsResponse.getHits().getTotalHits();
        FastIDSet itemIds = new FastIDSet((int)numItems);
        while (true) {
            for (SearchHit hit : itemIdsResponse.getHits().getHits()) {
                final long itemId = getLongValue(hit, "item_id");
                itemIds.add(itemId);
            }
            //Break condition: No hits are returned
            itemIdsResponse = client
                .prepareSearchScroll(itemIdsResponse.getScrollId())
                .setScroll(new TimeValue(keepAlive))
                .execute()
                .actionGet();
            if (itemIdsResponse.getHits().getHits().length == 0) {
                break;
            }
        }

        SearchResponse userIdsResponse = client
            .prepareSearch(index)
            .setTypes(type)
            .setSearchType(SearchType.SCAN)
            .setScroll(new TimeValue(keepAlive))
            .setPostFilter(FilterBuilders.termsFilter("item_id", itemIds))
            .addFields("user_id")
            .setSize(scrollSize)
            .execute()
            .actionGet();
        final long numUsers = userIdsResponse.getHits().getTotalHits();
        FastIDSet userIds = new FastIDSet((int)numUsers);
        while (true) {
            for (SearchHit hit : userIdsResponse.getHits().getHits()) {
                final long userId = getLongValue(hit, "user_id");
                userIds.add(userId);
            }
            //Break condition: No hits are returned
            userIdsResponse = client
                .prepareSearchScroll(userIdsResponse.getScrollId())
                .setScroll(new TimeValue(keepAlive))
                .execute()
                .actionGet();
            if (userIdsResponse.getHits().getHits().length == 0) {
                break;
            }
        }
        // logger.info("itemIds: {}, userIds: {}", itemIds, userIds);
        return createDataModelFromUserIds(index, type, userIds);
    }

    public DataModel createDataModelFromUserIds(final String index,
                                                final String type,
                                                final FastIDSet userIds) {
        SearchResponse scroll = client
            .prepareSearch(index)
            .setTypes(type)
            .setSearchType(SearchType.SCAN)
            .setPostFilter(FilterBuilders.termsFilter("user_id", userIds))
            .addFields("user_id", "item_id", "value")
            .setSize(scrollSize)
            .setScroll(new TimeValue(keepAlive))
            .execute()
            .actionGet();

        final long total = scroll.getHits().getTotalHits();
        FastByIDMap<PreferenceArray> users = new FastByIDMap<PreferenceArray>((int)total);
        while (true) {
            for (SearchHit hit : scroll.getHits().getHits()) {
                final long  userId = getLongValue(hit, "user_id");
                final long  itemId = getLongValue(hit, "item_id");
                final float value  = getFloatValue(hit, "value");

                if (users.containsKey(userId)) {
                    GenericUserPreferenceArray user = (GenericUserPreferenceArray)users.get(userId);
                    GenericUserPreferenceArray newUser = new GenericUserPreferenceArray(user.length() + 1);
                    int currentLength = user.length();
                    for (int i = 0; i < currentLength; i++) {
                        newUser.setUserID(i, user.getUserID(i));
                        newUser.setItemID(i, user.getItemID(i));
                        newUser.setValue(i, user.getValue(i));
                    }
                    newUser.setUserID(currentLength, userId);
                    newUser.setItemID(currentLength, itemId);
                    newUser.setValue(currentLength, value);
                    users.put(userId, newUser);
                    
                } else {
                    GenericUserPreferenceArray user = new GenericUserPreferenceArray(1);
                    user.setUserID(0, userId);
                    user.setItemID(0, itemId);
                    user.setValue(0, value);
                    users.put(userId, user);
                }
            }
            //Break condition: No hits are returned
            scroll = client
                .prepareSearchScroll(scroll.getScrollId())
                .setScroll(new TimeValue(keepAlive))
                .execute()
                .actionGet();
            if (scroll.getHits().getHits().length == 0) {
                break;
            }
        }
        return new GenericDataModel((FastByIDMap<PreferenceArray>)users);
    }

    private long getLongValue(final SearchHit hit, final String field) {
        final SearchHitField result = hit.field(field);
        if (result == null) {
            return 0;
        }
        final Number longValue = result.getValue();
        if (longValue == null) {
            return 0;
        }
        return longValue.longValue();
    }

    private float getFloatValue(final SearchHit hit, final String field) {
        final SearchHitField result = hit.field(field);
        if (result == null) {
            return 0;
        }
        final Number floatValue = result.getValue();
        if (floatValue == null) {
            return 0;
        }
        return floatValue.floatValue();
    }
}
