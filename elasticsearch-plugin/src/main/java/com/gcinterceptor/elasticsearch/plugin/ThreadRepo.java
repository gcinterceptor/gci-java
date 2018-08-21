package com.gcinterceptor.elasticsearch.plugin;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

/**
 * Created by fireman on 2/24/17.
 */
public class ThreadRepo {
    public static ThreadLocal<RestChannel> channel = new ThreadLocal<>();
    public static ThreadLocal<RestRequest> request = new ThreadLocal<>();
}
