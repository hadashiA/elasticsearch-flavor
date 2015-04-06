package org.elasticsearch.plugin.flavor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.action.ActionListener;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.NOT_FOUND;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

import org.elasticsearch.plugin.flavor.DataModelBuilder;

public class FlavorRestAction extends BaseRestHandler {
    private DataModel dataModel = new GenericDataModel(new FastByIDMap<PreferenceArray>());
    private ESLogger logger = Loggers.getLogger(FlavorRestAction.class);

    @Inject
    public FlavorRestAction(final Settings settings,
                            final RestController controller,
                            final Client client) {
        super(settings, controller, client);
        controller.registerHandler(POST, "/_flavor/reload", this);
        controller.registerHandler(GET,  "/_flavor/{operation}/{id}", this);
    }

    @Override
    public void handleRequest(final RestRequest request,
                              final RestChannel channel,
                              final Client client) {
        
        switch (request.method()) {
        case POST:
            try {
                final String jsonString = XContentHelper.convertToJson(request.content(), true);
                JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);

                final DataModelBuilder dataModelBuilder = new DataModelBuilder(client, json);
                this.dataModel = dataModelBuilder.build();
                final XContentBuilder builder = JsonXContent.contentBuilder();
                builder
                    .startObject()
                    .field("acknowledged", true)
                    .endObject();
                channel.sendResponse(new BytesRestResponse(OK, builder));
                
            } catch (final Exception e) {
                handleErrorRequest(channel, e);
            }
            break;
        case GET:
            try {
                LongPrimitiveIterator iter = dataModel.getUserIDs();
                while (iter.hasNext()) {
                    long userId = iter.nextLong();
                    PreferenceArray user = dataModel.getPreferencesFromUser(userId);
                    logger.info("userId: {} ({})", userId, user.getIDs());
                }
                final XContentBuilder builder = JsonXContent.contentBuilder();
                builder
                    .startObject()
                    .field("acknowledged", true)
                    .endObject();
                channel.sendResponse(new BytesRestResponse(OK, builder));
                break;
            } catch(final Exception e) {
                handleErrorRequest(channel, e);
            }
        default:
            notFound(channel);
            break;
        }
        // try{
        //     final XContentBuilder builder = JsonXContent.contentBuilder();
        //     builder
        //         .startObject()
        //         .field("took", System.currentTimeMillis() - startTime)
        //         .startObject("hits")
        //         .field("total", recommendedItems.size())
        //         .startArray("hits");
        //     for (final RecommendedItem recommendedItem : recommendedItems) {
        //         builder
        //             .startObject()
        //             .field(recommendedItem.idLabel(), recommendedItem.id())
        //             .field(recommendedItem.scoreLabel(), recommendedItem.score())
        //             .endObject();
        //     }
        //     builder
        //         .endArray()
        //         .endObject();
        //     channel.sendResponse(new BytesRestResponse(OK, builder));
        // } catch (final IOException e) {
        //     handleErrorRequest(channel, e);
        // }
    }

    private void handleErrorRequest(final RestChannel channel, final Throwable e) {
        try {
            channel.sendResponse(new BytesRestResponse(channel, e));
        } catch (final IOException e1) {
            logger.error("Failed to send a failure response.", e1);
        }
    }

    private void notFound(final RestChannel channel) {
        try {
            // 404
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder
                .startObject()
                .field("error", "Invalid operatioin")
                .field("status", 404)
                .endObject();
            channel.sendResponse(new BytesRestResponse(NOT_FOUND, builder));
        } catch (final IOException e) {
            handleErrorRequest(channel, e);
        }
    }
}
