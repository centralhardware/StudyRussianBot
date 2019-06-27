package ru.AlexeyFedechkin.znatoki.StudyRussianBot.StatisticServer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.JedisData;

import java.io.IOException;
import java.io.OutputStream;

public class StatisticHandler implements HttpHandler {
    private final Logger logger = Logger.getLogger(StatisticHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("open statistic page");
        StringBuilder builder = new StringBuilder();
        builder.append("<h1>URI: ").append("bot statistic:").append("</h1>");
        Headers headers = exchange.getRequestHeaders();
        for (String str : JedisData.getInstance().getAllKeys()) {
            builder.append("<p>").append(str).append("=")
                    .append(JedisData.getInstance().getvalue(str)).append("</p>");
        }
        byte[] bytes = builder.toString().getBytes();
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
