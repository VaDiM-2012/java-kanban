package kanbanboard.httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import kanbanboard.manager.task.ManagerSaveException;
import kanbanboard.manager.task.NotFoundException;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET" -> {
                    if (path.equals("/subtasks")) {
                        sendText(exchange, gson.toJson(taskManager.getSubtask()), 200);
                    } else if (path.startsWith("/subtasks/")) {
                        int id = extractIdFromPath(path, "/subtasks/");
                        if (id == -1) {
                            sendNotFound(exchange);
                            return;
                        }
                        try {
                            Subtask subtask = taskManager.getSubtask(id);
                            sendText(exchange, gson.toJson(subtask), 200);
                        } catch (NotFoundException e) {
                            sendNotFound(exchange);
                        }
                    }
                }
                case "POST" -> {
                    String body = readRequestBody(exchange);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    if (subtask.getId() == null || subtask.getId() == 0) {
                        try {
                            Subtask created = taskManager.createSubtask(subtask);
                            sendText(exchange, "{\"message\":\"Подзадача создана\"}", 201);
                        } catch (IllegalArgumentException e) {
                            sendHasInteractions(exchange);
                        } catch (NotFoundException e) {
                            sendNotFound(exchange);
                        }
                    } else {
                        try {
                            Subtask updated = taskManager.updateSubtask(subtask);
                            sendText(exchange, "{\"message\":\"Подзадача обновлена\"}", 201);
                        } catch (IllegalArgumentException e) {
                            sendHasInteractions(exchange);
                        } catch (NotFoundException e) {
                            sendNotFound(exchange);
                        }
                    }
                }
                case "DELETE" -> {
                    if (path.startsWith("/subtasks/")) {
                        int id = extractIdFromPath(path, "/subtasks/");
                        if (id == -1) {
                            sendNotFound(exchange);
                            return;
                        }
                        try {
                            taskManager.deleteSubtask(id);
                            sendText(exchange, "{\"message\":\"Подзадача удалена\"}", 200);
                        } catch (NotFoundException e) {
                            sendNotFound(exchange);
                        }
                    }
                }
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
}