package com.gcinterceptor.elasticsearch.plugin;

import com.gcinterceptor.core.RuntimeEnvironment;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.Task;


public class GciFilter implements ActionFilter {
    private static final String CH_HEADER = "ch";
	private static final String GCI_HEADERS_NAME = "gci";
    private RuntimeEnvironment runtime;

    @Inject
    public GciFilter(RuntimeEnvironment runtime) {
        this.runtime = runtime;
    }

    // GCI must be the first filter.
    @Override
    public int order() {
        return 0;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void apply(Task task, String action, Request request, ActionListener<Response> listener, ActionFilterChain<Request, Response> chain) {
        
        RestChannel channel = ThreadRepo.channel.get();
        if (channel == null) {
            System.out.println("Null channel");
            chain.proceed(task, action, request, listener);
            return;
        }

        RestRequest restRequest = ThreadRepo.request.get();
        if (restRequest == null) {
            System.out.println("Null restRequest");
            chain.proceed(task, action, request, listener);
            return;
        }
        
        String gciHeader = restRequest.header(GCI_HEADERS_NAME);
		if (gciHeader == null) {
            System.out.println("Null restRequest gci header");
			chain.proceed(task, action, request, listener);
            return;
        }

        String requestBody = "";
        switch (gciHeader) {
            case CH_HEADER:
                String heapUsageString = Long.valueOf(runtime.getYoungHeapUsage()) + "|" + Long.valueOf(runtime.getTenuredHeapUsage());
                requestBody = heapUsageString;
    
            default:
                runtime.collect();
        }

        BytesRestResponse response = new BytesRestResponse(RestStatus.OK, requestBody);
        channel.sendResponse(response);

    }

}