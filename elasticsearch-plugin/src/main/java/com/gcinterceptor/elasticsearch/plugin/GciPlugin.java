package com.gcinterceptor.elasticsearch.plugin;

import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.NetworkPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public class GciPlugin extends Plugin implements ActionPlugin, IngestPlugin, NetworkPlugin {
    @Override
    public List<Class<? extends ActionFilter>> getActionFilters() {
        return Collections.singletonList(GciFilter.class);
    }

    @Override
    public UnaryOperator<RestHandler> getRestHandlerWrapper(ThreadContext threadContext) {
        return restHandler -> new RestHandler() {
            @Override
            public void handleRequest(RestRequest request, RestChannel channel, NodeClient client) throws Exception {
                ThreadRepo.channel.set(channel);
                ThreadRepo.request.set(request);
                restHandler.handleRequest(request, channel, client);
            }
        };
    }
}
