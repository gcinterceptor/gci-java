package com.gcinterceptor.agent;

import com.gcinterceptor.core.RuntimeEnvironment;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

public class Agent {
    private static final String CH_HEADER = "ch";
    private static final String GCI_HEADERS_NAME = "gci";
    public static final RuntimeEnvironment runtime = new RuntimeEnvironment();

    public static void agentmain(String agentArgument, Instrumentation instrumentation) throws Exception {
        premain(agentArgument, instrumentation);
    }

    public static void premain(String agentArgument, Instrumentation instrumentation) throws Exception {
        System.out.println("Boooo: " + agentArgument);
        int port = Integer.parseInt(agentArgument);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(Agent::handleRequest);
        server.start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String gciHeader = exchange.getRequestHeaders().getFirst(GCI_HEADERS_NAME);
        if (gciHeader == null) {
            System.out.println("No GCI headers");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
            return;
        }
        switch (gciHeader) {
        case CH_HEADER:
            byte[] heapUsage = new StringBuffer().append(Long.valueOf(runtime.getYoungHeapUsage())).append("|")
                    .append(Long.valueOf(runtime.getTenuredHeapUsage())).toString().getBytes();
            exchange.sendResponseHeaders(200, heapUsage.length);
            exchange.getResponseBody().write(heapUsage);
            exchange.getResponseBody().close();
            System.out.println("CH: " + new String(heapUsage));
            return;

        default:
            runtime.collect();
            System.out.println("Collect");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
            return;
        }
    }
}