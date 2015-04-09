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
    private String index;
    private String type;
    private DataModel dataModel;

    public ElasticsearchPreloadDataModelFactory(final Client client, final JsonObject settings) {
        this.client = client;

        final JsonElement preferenceSettingsElement = settings.getAsJsonObject("preference");
        if (preferenceSettingsElement.isJsonNull()) {
            throw new InvalidParameterException("preference key not found.");
        }

        String preferenceIndex = "";
        String preferenceType  = "preference";

        final JsonObject preferenceSettings = preferenceSettingsElement.getAsJsonObject();
        JsonElement preferenceIndexElement = preferenceSettings.get("index");
        if (preferenceIndexElement == null || preferenceIndexElement.isJsonNull()) {
            throw new InvalidParameterException("preference.index is null.");
        } else {
            preferenceIndex = preferenceIndexElement.getAsString();
        }

        JsonElement preferenceTypeElement = preferenceSettings.get("type");
        if (preferenceTypeElement != null && !preferenceTypeElement.isJsonNull()) {
            preferenceType = preferenceTypeElement.getAsString();
        }
        // if (settings.containsKey("keepAlive")) {
        //     dataModel.setKeepAlive(settings.get("keepAlive"));
        // }
        // if (settings.containsKey("scrollSize")) {
        //     dataModel.setScrollSize(settings.get("scrollSize"));
        // }

        this.index = preferenceIndex;
        this.type  = preferenceType;
    }

    public DataModel createItemBasedDataModel(final long itemId) throws TasteException {
        logger.info("{} {}", index, type);

        if (dataModel == null) {
            ElasticsearchPreloadDataModel preloadDataModel = new ElasticsearchPreloadDataModel(client, index, type);
            preloadDataModel.reload();
            this.dataModel = preloadDataModel;
        }
        return dataModel;
    }

    public DataModel createUserBasedDataModel(final long userId) throws TasteException {
        if (dataModel == null) {
            ElasticsearchPreloadDataModel preloadDataModel = new ElasticsearchPreloadDataModel(client, index, type);
            preloadDataModel.reload();
            this.dataModel = preloadDataModel;
        }
        return dataModel;
    }
}
