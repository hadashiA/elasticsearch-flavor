package org.elasticsearch.plugin.flavor;

import java.util.ArrayList;
import java.util.Collection;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.index.query.QueryBuilders;
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

public class ElasticsearchPreloadDataModel extends AbstractDataModel {
    private ESLogger logger = Loggers.getLogger(ElasticsearchPreloadDataModel.class);
    private Client client;
    private String preferenceIndex;
    private String preferenceType;
    private GenericDataModel delegate = new GenericDataModel(new FastByIDMap<PreferenceArray>());

    private long keepAlive = 60000;
    private int scrollSize = 2000;

    public ElasticsearchPreloadDataModel(Client client,
                                         String preferenceIndex,
                                         String preferenceType) {
        this.client = client;
        this.preferenceIndex = preferenceIndex;
        this.preferenceType = preferenceType;
    }

    public void reload() throws TasteException {
        FastByIDMap<PreferenceArray> users = new FastByIDMap<PreferenceArray>();
        SearchResponse scroll = client
            .prepareSearch(preferenceIndex)
            .setTypes(preferenceType)
            .setSearchType(SearchType.SCAN)
            .setQuery(QueryBuilders.matchAllQuery())
            .addFields("user_id", "item_id", "value")
            .setSize(scrollSize)
            .setScroll(new TimeValue(keepAlive))
            .execute()
            .actionGet();

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

        this.delegate = new GenericDataModel((FastByIDMap<PreferenceArray>)users);
        // LongPrimitiveIterator iter = delegate.getUserIDs();
        // while (iter.hasNext()) {
        //     long userId = iter.nextLong();
        //     PreferenceArray user = delegate.getPreferencesFromUser(userId);
        //     logger.info("userId: {} ({})", userId, user.getIDs());
        // }

        logger.info("Reload {}/{} {} users. {} items.",
                    preferenceIndex, preferenceType,
                    delegate.getNumUsers(), delegate.getNumItems());
    }

    public Client client() {
        return client;
    }

    public String preferenceIndex() {
        return preferenceIndex;
    }

    public String preferenceType() {
        return preferenceType;
    }

    public long keepAlive() {
        return keepAlive;
    }

    public int scrollSize() {
        return scrollSize;
    }

    public void preferenceIndex(final String value) {
        this.preferenceIndex = value;
    }

    public void preferenceType(final String value) {
        this.preferenceType = value;
    }

    public void setScrollSize(final int value) {
        this.scrollSize = value;
    }

    public void setKeepAlive(final long value) {
        this.keepAlive = value;
    }

    private long getLongValue(final SearchHit hit, final String field) throws TasteException {
        final SearchHitField result = hit.field(field);
        if (result == null) {
            throw new TasteException(field + " is not found.");
        }
        final Number longValue = result.getValue();
        if (longValue == null) {
            throw new TasteException("The result of " + field + " is null.");
        }
        return longValue.longValue();
    }

    private float getFloatValue(final SearchHit hit, final String field) throws TasteException {
        final SearchHitField result = hit.field(field);
        if (result == null) {
            throw new TasteException(field + " is not found.");
        }
        final Number floatValue = result.getValue();
        if (floatValue == null) {
            throw new TasteException("The result of " + field + " is null.");
        }
        return floatValue.floatValue();
    }

    // 

    @Override
    public LongPrimitiveIterator getUserIDs() throws TasteException {
        return delegate.getUserIDs();
    }

    @Override
    public PreferenceArray getPreferencesFromUser(long userID) throws TasteException {
        return delegate.getPreferencesFromUser(userID);
    }

    @Override
    public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
        return delegate.getItemIDsFromUser(userID);
    }

    @Override
    public LongPrimitiveIterator getItemIDs() throws TasteException {
        return delegate.getItemIDs();
    }

    @Override
    public PreferenceArray getPreferencesForItem(long itemID) throws TasteException {
        return delegate.getPreferencesForItem(itemID);
    }

    @Override
    public Float getPreferenceValue(long userID, long itemID) throws TasteException {
        return delegate.getPreferenceValue(userID, itemID);
    }

    @Override
    public Long getPreferenceTime(long userID, long itemID) throws TasteException {
        return delegate.getPreferenceTime(userID, itemID);
    }

    @Override
    public int getNumItems() throws TasteException {
        return delegate.getNumItems();
    }

    @Override
    public int getNumUsers() throws TasteException {
        return delegate.getNumUsers();
    }

    @Override
    public int getNumUsersWithPreferenceFor(long itemID) throws TasteException {
        return delegate.getNumUsersWithPreferenceFor(itemID);
    }

    @Override
    public int getNumUsersWithPreferenceFor(long itemID1, long itemID2) throws TasteException {
        return delegate.getNumUsersWithPreferenceFor(itemID1, itemID2);
    }

    /**
     * Note that this method only updates the in-memory preference data that this {@link FileDataModel}
     * maintains; it does not modify any data on disk. Therefore any updates from this method are only
     * temporary, and lost when data is reloaded from a file. This method should also be considered relatively
     * slow.
     */
    @Override
    public void setPreference(long userID, long itemID, float value) throws TasteException {
        delegate.setPreference(userID, itemID, value);
    }

    /** See the warning at {@link #setPreference(long, long, float)}. */
    @Override
    public void removePreference(long userID, long itemID) throws TasteException {
        delegate.removePreference(userID, itemID);
    }

    @Override
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
        try {
            reload();
        } catch(final TasteException e) {
            logger.info("reload failed. {}", e);
        }
    }

    @Override
    public boolean hasPreferenceValues() {
        return delegate.hasPreferenceValues();
    }

    @Override
    public float getMaxPreference() {
        return delegate.getMaxPreference();
    }

    @Override
    public float getMinPreference() {
        return delegate.getMinPreference();
    }

    @Override
    public String toString() {
        return "ElasticsearchPreloadDataModel[index:" + preferenceIndex + " type:" + preferenceType + "]";
    }
}
