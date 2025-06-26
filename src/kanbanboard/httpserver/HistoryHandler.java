package kanbanboard.httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import kanbanboard.manager.task.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (method.equals("GET") && path.equals("/history")) {
                sendText(exchange, gson.toJson(taskManager.getHistory()), 200);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange, "Внутренняя ошибка сервера");
        } finally {
            exchange.close();
        }
    }
}