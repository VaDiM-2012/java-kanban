package kanbanboard.httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import kanbanboard.manager.task.ManagerSaveException;
import kanbanboard.manager.task.NotFoundException;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET" -> handleGetTaskRequests(exchange, path);
                case "POST" -> handlePostTaskRequests(exchange);
                case "DELETE" -> handleDeleteTaskRequests(exchange, path);
                default -> sendNotFound(exchange);
            }
        } catch (ManagerSaveException e) {
            sendInternalServerError(exchange, "Ошибка сохранения данных");
        } catch (Exception e) {
            sendInternalServerError(exchange, "Внутренняя ошибка сервера");
        } finally {
            exchange.close();
        }
    }

    // Обработка GET-запросов к /tasks
    private void handleGetTaskRequests(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            sendText(exchange, gson.toJson(taskManager.getTask()), 200);
        } else if (path.startsWith("/tasks/")) {
            int id = extractIdFromPath(path, "/tasks/");
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }

            try {
                Task task = taskManager.getTask(id);
                sendText(exchange, gson.toJson(task), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    // Обработка POST-запросов к /tasks
    private void handlePostTaskRequests(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Task task = gson.fromJson(body, Task.class);

        if (task.getId() == null) {
            try {
                taskManager.createTask(task);
                sendText(exchange, "{\"message\":\"Задача создана\"}", 201);
            } catch (IllegalArgumentException e) {
                sendHasInteractions(exchange);
            }
        } else {
            try {
                taskManager.updateTask(task);
                sendText(exchange, "{\"message\":\"Задача обновлена\"}", 201);
            } catch (IllegalArgumentException e) {
                sendHasInteractions(exchange);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        }
    }

    // Обработка DELETE-запросов к /tasks
    private void handleDeleteTaskRequests(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/tasks/")) {
            int id = extractIdFromPath(path, "/tasks/");
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }

            try {
                taskManager.deleteTask(id);
                sendText(exchange, "{\"message\":\"Задача удалена\"}", 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

}