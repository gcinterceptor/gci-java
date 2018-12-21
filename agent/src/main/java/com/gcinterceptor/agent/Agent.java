package com.gcinterceptor.agent;

import com.gcinterceptor.core.RuntimeEnvironment;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Agent {
    private static final String GCI_HEADERS_NAME = "gci";
    public static final RuntimeEnvironment runtime = new RuntimeEnvironment();

    public static void agentmain(String agentArgument, Instrumentation instrumentation) throws Exception {
        premain(agentArgument, instrumentation);
    }

    public static void premain(String agentArgument, Instrumentation instrumentation) throws Exception {
        int port = Integer.parseInt(agentArgument);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(Agent::handleRequest);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        System.out.println("rs:" + System.currentTimeMillis());
        String gciHeader = exchange.getRequestHeaders().getFirst(GCI_HEADERS_NAME);
        try (OutputStream out = exchange.getResponseBody()) {
            boolean shouldGC = runtime.getYoungHeapUsage() >= Long.parseLong(gciHeader);
            if (shouldGC) {
                System.out.println("gc:" + System.currentTimeMillis());
                runtime.collect();
            }
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.getResponseHeaders().set("Content-Length", "1");
            exchange.sendResponseHeaders(200, 1);
            out.write(shouldGC ? 1 : 0);
        } finally {
            exchange.close();
        }
        System.out.println("rf:" + System.currentTimeMillis());
    }
}
