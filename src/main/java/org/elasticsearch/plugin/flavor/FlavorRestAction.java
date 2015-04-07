package org.elasticsearch.plugin.flavor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

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


import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

import org.elasticsearch.plugin.flavor.DataModelBuilder;
import org.elasticsearch.plugin.flavor.FlavorRecommenderBuilder;

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
        controller.registerHandler(GET,  "/_flavor/status", this);
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
                renderStatus(channel);
                
            } catch (final Exception e) {
                handleErrorRequest(channel, e);
            }
            break;
        case GET:
            if (request.path().equals("/_flavor/status")) {
                renderStatus(channel);
            } else {
                try {
                    final String operation = request.param("operation");
                    final long id = request.paramAsLong("id", 0);
                    final int size = request.paramAsInt("size", 10);
            
                    final RecommenderBuilder recommenderBuilder =
                        new FlavorRecommenderBuilder(operation, request.param("similarity"));
                    final Recommender recommender = recommenderBuilder.buildRecommender(dataModel);
            
                    List<RecommendedItem> items;
                    final long startTime = System.currentTimeMillis();
                    if (operation.equals("similar_items")) {
                        items = ((ItemBasedRecommender)recommender).mostSimilarItems(id, size);
                    } else {
                        items = recommender.recommend(id, size);
                    }
            
                    final XContentBuilder builder = JsonXContent.contentBuilder();
                    builder
                        .startObject()
                        .field("took", System.currentTimeMillis() - startTime)
                        .startObject("hits")
                        .field("total", items.size())
                        .startArray("hits");
                    for (final RecommendedItem item : items) {
                        builder
                            .startObject()
                            .field("item_id", item.getItemID())
                            .field("value", item.getValue())
                            .endObject();
                    }
                    builder
                        .endArray()
                        .endObject();
                    channel.sendResponse(new BytesRestResponse(OK, builder));

                } catch(final NoSuchItemException e) {
                    renderNotFound(channel, e.getMessage());
                } catch(final Exception e) {
                    handleErrorRequest(channel, e);
                }
            }
            break;
        default:
            renderNotFound(channel, "No such action");
            break;
        }
    }

    private void renderNotFound(final RestChannel channel, final String message) {
        try {
            // 404
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder
                .startObject()
                .field("error", message)
                .field("status", 404)
                .endObject();
            channel.sendResponse(new BytesRestResponse(NOT_FOUND, builder));
        } catch (final IOException e) {
            handleErrorRequest(channel, e);
        }
    }

    private void renderStatus(final RestChannel channel) {
        try {
            final XContentBuilder builder = JsonXContent.contentBuilder();
            builder
                .startObject()
                .field("dataModel", dataModel.toString())
                .field("total_users", dataModel.getNumUsers())
                .field("total_items", dataModel.getNumItems())
                .endObject();
            channel.sendResponse(new BytesRestResponse(OK, builder));
        } catch (final Exception e) {
            handleErrorRequest(channel, e);
        }
    }

    private void handleErrorRequest(final RestChannel channel, final Throwable e) {
        try {
            channel.sendResponse(new BytesRestResponse(channel, e));
        } catch (final IOException e1) {
            logger.error("Failed to send a failure response.", e1);
        }
    }
}
