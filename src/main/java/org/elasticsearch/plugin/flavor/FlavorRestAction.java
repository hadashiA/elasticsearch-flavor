package org.elasticsearch.plugin.flavor;

import java.io.IOException;

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

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.NOT_FOUND;

import org.elasticsearch.plugin.flavor.FlavorRequest;

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
        if (operation  == "similar_items") {
            
        } else {
            try {
                // 404
                final XContentBuilder builder = JsonXContent.contentBuilder();
                builder.startObject();
                builder.field("error", "Invalid operation:" + operation);
                builder.field("status", 404);
                builder.endObject();
                channel.sendResponse(new BytesRestResponse(NOT_FOUND, builder));
            } catch (final IOException e) {
                handleErrorRequest(channel, e);
            }            
        }

        final String who = request.param("who");
        final String whoSafe = (who!=null) ? who : "world";
        channel.sendResponse(new BytesRestResponse(OK, "Hello, " + whoSafe + "!"));
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
