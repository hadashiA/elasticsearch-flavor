package org.elasticsearch.rest.action;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.action.ActionListener;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

public class FlavorRestAction extends BaseRestHandler {
    @Inject
    public FlavorRestAction(final Settings settings,
                            final RestController controller,
                            final Client client) {
        super(settings, controller, client);
        controller.registerHandler(GET, "/{index}/_flavor/product/{id}/similars", this);
    }

    @Override
    public void handleRequest(final RestRequest request,
                              final RestChannel channel,
                              final Client client) {
        String who = request.param("who");
        String whoSafe = (who!=null) ? who : "world";
        channel.sendResponse(new BytesRestResponse(OK, "Hello, " + whoSafe + "!"));
    }
}
