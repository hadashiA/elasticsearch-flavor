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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.SearchHit;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.NOT_FOUND;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.elasticsearch.plugin.flavor.FlavorRequest;
import org.elasticsearch.plugin.flavor.ItemSimilarityProvider;

public class FlavorRestAction extends BaseRestHandler {
    Logger logger = LoggerFactory.getLogger(FlavorRestAction.class);

    @Inject
    public FlavorRestAction(final Settings settings,
                            final RestController controller,
                            final Client client) {
        super(settings, controller, client);
        controller.registerHandler(GET, "/{index}/_flavor/{operation}/{type}/{id}", this);
    }

    @Override
    public void handleRequest(final RestRequest request,
                              final RestChannel channel,
                              final Client client) {
        
        FlavorRequest resource = new FlavorRequest(request.param("index"),
                                                   request.param("type"),
                                                   request.param("id"));
        final String operation = request.param("operation");
        if (operation.equals("similar_items")) {
            final ItemSimilarityProvider provider = new ItemSimilarityProvider(client, resource);
            final long startTime = System.currentTimeMillis();
            final String[] similarIds = provider.similarIds();

            try{
                final XContentBuilder builder = JsonXContent.contentBuilder();
                builder.startObject()
                    .field("took", System.currentTimeMillis() - startTime)
                    .startObject("hits")
                    .field("total", similarIds.length)
                    .startArray("hits");
                for (String id: similarIds) {
                    builder.startObject()
                        .field("_index", resource.index())
                        .field("_type", resource.type())
                        .field("_id", resource.id())
                        .endObject();
                }
                builder.endArray()
                    .endObject();
                channel.sendResponse(new BytesRestResponse(OK, builder));
            } catch (final IOException e) {
                handleErrorRequest(channel, e);
            }
        } else {
            try {
                // 404
                XContentBuilder builder = JsonXContent.contentBuilder();
                builder.startObject();
                builder.field("error", "Invalid _flavor operation: " + operation);
                builder.field("status", 404);
                builder.endObject();
                channel.sendResponse(new BytesRestResponse(NOT_FOUND, builder));
            } catch (final IOException e) {
                handleErrorRequest(channel, e);
            }            
        }
    }

    private void handleErrorRequest(final RestChannel channel, final Throwable e) {
        try {
            // 繧ｨ繝ｩ繝ｼ繝ｬ繧ｹ繝昴Φ繧ｹ繧定ｿ泌唆縺吶ｋ
            channel.sendResponse(new BytesRestResponse(channel, e));
        } catch (final IOException e1) {
            logger.error("Failed to send a failure response.", e1);
        }
    }
}
