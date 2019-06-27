package ru.AlexeyFedechkin.znatoki.StudyRussianBot.StatisticServer;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    private final Logger logger = Logger.getLogger(Server.class);
    private HttpServer httpServer;
    private HttpContext httpContext;

    public void init() {
        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(5556), 0);
            httpContext = server.createContext("/", new StatisticHandler());
            server.setExecutor(null);
            server.start();
            logger.info("server start");
        } catch (IOException e) {
            logger.warn("server create error", e);
        }
    }
}
