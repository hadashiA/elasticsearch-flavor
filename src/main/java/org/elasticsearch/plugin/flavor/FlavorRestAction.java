package org.elasticsearch.plugin.flavor;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
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

import org.elasticsearch.plugin.flavor.FlavorRequest;
import org.elasticsearch.plugin.flavor.ItemSimilarity;

public class FlavorRestAction extends BaseRestHandler {
    @Inject
    public FlavorRestAction(final Settings settings,
                            final RestController controller,
                            final Client client) {
        super(settings, controller, client);
        controller.registerHandler(GET, "/{index}/_flavor/{type}/{operation}/{id}", this);
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
            final ItemSimilarity temSimilarity = new ItemSimilarity(client, resource);
            client.prepareSearch(resource.index()).setTypes(resource.type())
                .setQuery(QueryBuilders.idsQuery(resource.id()))
                .execute(new ActionListener<SearchResponse>() {
                        @Override
                        public void onResponse(SearchResponse response) {
                            final XContentBuilder builder = JsonXContent.contentBuilder();
                            final SearchHits hits = response.getHits();

                            final String pretty = request.param("pretty");
                            if (pretty != null && !"false".equalsIgnoreCase(pretty)) {
                                builder.prettyPrint().lfAtEnd();
                            }

                            builder.startObject()//
                                .field("took", response.getTookInMillis())//
                                .field("timed_out", response.isTimedOut())//
                                .startObject("_shards")//
                                .field("total", response.getTotalShards())//
                                .field("successful", response.getSuccessfulShards())//
                                .field("failed", response.getFailedShards())//
                                .endObject()//
                                .startObject("hits")//
                                .field("total", hits.getTotalHits())//
                                .field("max_score", hits.getMaxScore())//
                                .startArray("hits");

                            for (final SearchHit hit : hits.getHits()) {
                                final Map<String, Object> source =
                                    expandObjects(client, hit.getSource(), info);
                                builder.startObject()//
                                    .field("_index", hit.getIndex())//
                                    .field("_type", hit.getType())//
                                    .field("_id", hit.getId())//
                                    .field("_score", hit.getScore())//
                                    .field("_source", source)//
                                    .endObject();//
                            }

                            builder.endArray()//
                                .endObject()//
                                .endObject();
                            channel.sendResponse(new BytesRestResponse(OK, builder));
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            handleErrorRequest(channel, e);
                        }
                    });

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
            // エラーレスポンスを返却する
            channel.sendResponse(new BytesRestResponse(channel, e));
        } catch (final IOException e1) {
            logger.error("Failed to send a failure response.", e1);
        }
    }
}
