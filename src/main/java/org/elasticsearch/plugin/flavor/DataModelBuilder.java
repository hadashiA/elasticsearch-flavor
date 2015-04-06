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

import org.elasticsearch.plugin.flavor.ElasticsearchPreloadDataModel;

public class DataModelBuilder {
    private ESLogger logger = Loggers.getLogger(DataModelBuilder.class);
    private Client client;
    private JsonObject settings;

    public DataModelBuilder(final Client client, final JsonObject settings) {
        this.client   = client;
        this.settings = settings;
    }

    public DataModel build() throws TasteException {
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
        final ElasticsearchPreloadDataModel dataModel =
            new ElasticsearchPreloadDataModel(client, preferenceIndex, preferenceType);
        // if (settings.containsKey("keepAlive")) {
        //     dataModel.setKeepAlive(settings.get("keepAlive"));
        // }
        // if (settings.containsKey("scrollSize")) {
        //     dataModel.setScrollSize(settings.get("scrollSize"));
        // }

        dataModel.reload();
        return dataModel;
    }
}
