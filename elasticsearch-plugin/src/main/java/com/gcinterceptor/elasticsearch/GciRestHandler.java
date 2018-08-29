package com.gcinterceptor.elasticsearch;

import static org.elasticsearch.rest.RestRequest.Method.GET;

import java.io.IOException;
import java.io.OutputStream;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
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
	public String getName() {
		return "GCI Rest Handler";
	}

	@Override
	protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
		String responseBody = "";
		String gciHeader = request.header(GCI_HEADERS_NAME);
		if (gciHeader != null) {
			switch (gciHeader) {
			case CH_HEADER:
				responseBody = getHeapUsageString();
				break;

			default:
				runtime.collect();
				break;
			}
		}

		String body = responseBody;
		return channel -> {
			BytesRestResponse response = buildResponse(body, channel);
			channel.sendResponse(response);
		};
	}

	private String getHeapUsageString() {
		String heapUsageString = Long.valueOf(runtime.getYoungHeapUsage()) + "|"
				+ Long.valueOf(runtime.getTenuredHeapUsage());
		return heapUsageString;
	}

	private BytesRestResponse buildResponse(String body, RestChannel channel) throws IOException {
		XContentBuilder builder = channel.newBuilder();
		OutputStream outPutStream = builder.getOutputStream();
		outPutStream.write(body.getBytes());
		outPutStream.flush();
		outPutStream.close();
		BytesRestResponse response = new BytesRestResponse(RestStatus.OK, builder);
		return response;
	}
}
