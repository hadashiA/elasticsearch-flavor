package org.elasticsearch.plugin.flavor;

import org.elasticsearch.rest.*;

import org.elasticsearch.common.inject.Inject;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

public class FlavorRestHandler implements RestHandler {
    @Inject
    public FlavorRestHandler(RestController restController) {
        restController.registerHandler(GET, "/_flavor", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        String who = request.param("who");
        String whoSafe = (who!=null) ? who : "world";
        channel.sendResponse(new BytesRestResponse(OK, "Hello, " + whoSafe + "!"));
    }
}








