package com.gcinterceptor.elasticsearch;

import java.util.function.UnaryOperator;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;

public class GciElasticsearchPlugin extends Plugin implements ActionPlugin {

	@Override
	public UnaryOperator<RestHandler> getRestHandlerWrapper(ThreadContext threadContext) {
		return restHandler -> new RestHandler() {
			@Override
			public void handleRequest(RestRequest request, RestChannel channel, NodeClient client) throws Exception {
				System.out.println("THE GCI ELASTICSEARCH PLUGIN IS RUNNING!!!");
				restHandler.handleRequest(request, channel, client);
			}
		};
	}
}
