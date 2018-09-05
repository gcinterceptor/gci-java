package com.gcinterceptor.elasticsearch;

import static org.elasticsearch.rest.RestRequest.Method.GET;

import java.io.IOException;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

import com.gcinterceptor.core.RuntimeEnvironment;

public class GciRestHandler extends BaseRestHandler {
	private static final String CH_HEADER = "ch";
	private static final String GCI_HEADERS_NAME = "gci";
	private RuntimeEnvironment runtime = new RuntimeEnvironment();

	GciRestHandler(final Settings settings, final RestController controller) {
		super(settings);
		controller.registerHandler(GET, "/gci", this);
	}

	@Override
	protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
		String gciHeader = request.header(GCI_HEADERS_NAME);
		if (gciHeader != null) {
			switch (gciHeader) {
			case CH_HEADER:
				String body = getHeapUsageString();
				return channel -> {
					BytesRestResponse response = new BytesRestResponse(RestStatus.OK,
							BytesRestResponse.TEXT_CONTENT_TYPE, body);
					channel.sendResponse(response);
				};
			default:
				runtime.collect();
				break;
			}
		}
		return channel -> {
			channel.sendResponse(new BytesRestResponse(RestStatus.OK,
					BytesRestResponse.TEXT_CONTENT_TYPE,  ""));
		};
	}

	private String getHeapUsageString() {
		String heapUsageString = Long.valueOf(runtime.getYoungHeapUsage()) + "|"
				+ Long.valueOf(runtime.getTenuredHeapUsage());
		return heapUsageString;
	}
}
