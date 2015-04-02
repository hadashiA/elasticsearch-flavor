package org.elasticsearch.plugin.flavor;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.action.ActionListener;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.NOT_FOUND;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.elasticsearch.plugin.flavor.RecommendRequest;
import org.elasticsearch.plugin.flavor.Recommender;
import org.elasticsearch.plugin.flavor.SimilarItemsRecommender;

public class FlavorRestAction extends BaseRestHandler {
    Logger logger = LoggerFactory.getLogger(FlavorRestAction.class);

    @Inject
    public FlavorRestAction(final Settings settings,
                            final RestController controller,
                            final Client client) {
        super(settings, controller, client);
        controller.registerHandler(GET, "/{index}/_flavor/{operation}/{id}", this);
    }

    @Override
    public void handleRequest(final RestRequest request,
                              final RestChannel channel,
                              final Client client) {
        
        RecommendRequest recommendRequest =
            new RecommendRequest(client,
                                 request.param("index"),
                                 "preference",
                                 request.param("id"));

        Recommender recommender;

        final String operation = request.param("operation");
        if (operation.equals("similar_items")) {
            recommender = new SimilarItemsRecommender(recommendRequest);
        } else {
            try {
                // 404
                XContentBuilder builder = JsonXContent.contentBuilder();
                builder
                    .startObject()
                    .field("error", "Invalid _flavor operation: " + operation)
                    .field("status", 404)
                    .endObject();
                channel.sendResponse(new BytesRestResponse(NOT_FOUND, builder));
            } catch (final IOException e) {
                handleErrorRequest(channel, e);
            }
            return;
        }

        final long startTime = System.currentTimeMillis();
        final String[] ids = recommender.recommend();
        try{
            final XContentBuilder builder = JsonXContent.contentBuilder();
            builder
                .startObject()
                .field("took", System.currentTimeMillis() - startTime)
                .startObject("hits")
                .field("total", ids.length)
                .startArray("hits");
            for (String id: ids) {
                builder
                    .startObject()
                    .field("_index", recommendRequest.index())
                    .field("_operation", operation)
                    .field("_id", recommendRequest.id())
                    .field("_score", 1)
                    .endObject();
            }
            builder
                .endArray()
                .endObject();
            channel.sendResponse(new BytesRestResponse(OK, builder));
        } catch (final IOException e) {
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
