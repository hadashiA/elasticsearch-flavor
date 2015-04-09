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
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

import org.elasticsearch.plugin.flavor.DataModelFactory;
import org.elasticsearch.plugin.flavor.ElasticsearchPreloadDataModelFactory;
import org.elasticsearch.plugin.flavor.ElasticsearchDynamicDataModelFactory;
import org.elasticsearch.plugin.flavor.ItemBasedRecommenderBuilder;
import org.elasticsearch.plugin.flavor.UserBasedRecommenderBuilder;

public class FlavorRestAction extends BaseRestHandler {
    private DataModelFactory dataModelFactory;
    private ESLogger logger = Loggers.getLogger(FlavorRestAction.class);

    @Inject
    public FlavorRestAction(final Settings settings,
                            final RestController controller,
                            final Client client) {
        super(settings, controller, client);

        this.dataModelFactory = new ElasticsearchDynamicDataModelFactory(client);
        controller.registerHandler(POST, "/_flavor/preload", this);
        controller.registerHandler(GET,  "/{index}/{type}/_flavor/{operation}/{id}", this);
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

                ElasticsearchPreloadDataModelFactory factory = new ElasticsearchPreloadDataModelFactory(client, json);
                this.dataModelFactory = factory;
                final DataModel preload = factory.createItemBasedDataModel(null, null, 0);
                renderStatus(channel, preload);
                
            } catch (final Exception e) {
                handleErrorRequest(channel, e);
            }
            break;
        case GET:
            try {
                final String operation = request.param("operation");
                final String index     = request.param("index");
                final String type      = request.param("type");
                final long id          = request.paramAsLong("id", 0);
                final int size         = request.paramAsInt("size", 10);
                
                final long startTime = System.currentTimeMillis();
                
                if (operation.equals("similar_items")) {
                    DataModel dataModel = dataModelFactory.createItemBasedDataModel(index, type, id);
                    ItemBasedRecommenderBuilder builder = new ItemBasedRecommenderBuilder(request.param("similarity"));
                    ItemBasedRecommender recommender = builder.buildRecommender(dataModel);
                    List<RecommendedItem> items = recommender.mostSimilarItems(id, size);
                    renderRecommendedItems(channel, items, startTime);
                    
                } else if (operation.equals("similar_users")) {
                    DataModel dataModel = dataModelFactory.createUserBasedDataModel(index, type, id);
                    UserBasedRecommenderBuilder builder =
                        new UserBasedRecommenderBuilder(request.param("similarity"),
                                                        request.param("neighborhood"));
                    UserBasedRecommender recommender = builder.buildRecommender(dataModel);
                    long[] userIds = recommender.mostSimilarUserIDs(id, size);
                    renderUserIds(channel, userIds, startTime);
                    
                } else {
                    renderNotFound(channel, "Invalid operation: " + operation);
                }
                
            } catch(final NoSuchItemException e) {
                renderNotFound(channel, e.toString());
            } catch(final Exception e) {
                handleErrorRequest(channel, e);
            }
            break;
        default:
            renderNotFound(channel, "No such action");
            break;
        }
    }

    private void renderRecommendedItems(final RestChannel channel,
                                        final List<RecommendedItem> items,
                                        final long startTime) {
        try {
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

        } catch(final Exception e) {
            handleErrorRequest(channel, e);
        }
    }

    private void renderUserIds(final RestChannel channel,
                               final long[] userIds,
                               final long startTime) {
        try {
            final XContentBuilder builder = JsonXContent.contentBuilder();
            builder
                .startObject()
                .field("took", System.currentTimeMillis() - startTime)
                .startObject("hits")
                .field("total", userIds.length)
                .startArray("hits");
            for (int i = 0; i < userIds.length; i++) {
                builder
                    .startObject()
                    .field("user_id", userIds[i])
                    .endObject();
            }
            builder
                .endArray()
                .endObject();
            channel.sendResponse(new BytesRestResponse(OK, builder));

        } catch(final Exception e) {
            handleErrorRequest(channel, e);
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

    private void renderStatus(final RestChannel channel, final DataModel dataModel) {
        try {
            final XContentBuilder builder = JsonXContent.contentBuilder();
            builder
                .startObject()
                .field("preloadDataModel", dataModel.toString())
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
