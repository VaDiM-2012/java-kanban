package kanbanboard.server;

import com.sun.net.httpserver.HttpExchange;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Task;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendText(exchange, toJson(taskManager.getHistory()));
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}