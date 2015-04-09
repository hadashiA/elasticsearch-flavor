package org.elasticsearch.plugin.flavor;

import java.util.Map;
import java.security.InvalidParameterException;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import org.elasticsearch.plugin.flavor.DataModelFactory;
import org.elasticsearch.plugin.flavor.ElasticsearchPreloadDataModel;

public class ElasticsearchPreloadDataModelFactory implements DataModelFactory {
    private ESLogger logger = Loggers.getLogger(ElasticsearchPreloadDataModelFactory.class);
    private Client client;
    private String index = "preference";
    private String type = "preference";
    private DataModel dataModel;

    public ElasticsearchPreloadDataModelFactory(final Client client, final JsonObject settings) {
        this.client = client;

        final JsonElement preferenceSettingsElement = settings.getAsJsonObject("preference");
        if (preferenceSettingsElement.isJsonNull()) {
            throw new InvalidParameterException("preference key not found.");
        }

        final JsonObject preferenceSettings = preferenceSettingsElement.getAsJsonObject();
        JsonElement preferenceIndexElement = preferenceSettings.get("index");
        if (preferenceIndexElement == null || preferenceIndexElement.isJsonNull()) {
            throw new InvalidParameterException("preference.index is null.");
        } else {
            this.index = preferenceIndexElement.getAsString();
        }

        JsonElement preferenceTypeElement = preferenceSettings.get("type");
        if (preferenceTypeElement != null && !preferenceTypeElement.isJsonNull()) {
            this.type = preferenceTypeElement.getAsString();
        }
        // if (settings.containsKey("keepAlive")) {
        //     dataModel.setKeepAlive(settings.get("keepAlive"));
        // }
        // if (settings.containsKey("scrollSize")) {
        //     dataModel.setScrollSize(settings.get("scrollSize"));
        // }

    }

    public DataModel createItemBasedDataModel(final String _index,
                                              final String _type,
                                              final long _itemId) throws TasteException {
        if (dataModel == null) {
            ElasticsearchPreloadDataModel preloadDataModel = new ElasticsearchPreloadDataModel(client, index, type);
            preloadDataModel.reload();
            this.dataModel = preloadDataModel;
        }

        return dataModel;
    }

    public DataModel createUserBasedDataModel(final String _index,
                                              final String _type,
                                              final long _userId) throws TasteException {
        if (dataModel == null) {
            ElasticsearchPreloadDataModel preloadDataModel = new ElasticsearchPreloadDataModel(client, index, type);
            preloadDataModel.reload();
            this.dataModel = preloadDataModel;
        }
        return dataModel;
    }
}
