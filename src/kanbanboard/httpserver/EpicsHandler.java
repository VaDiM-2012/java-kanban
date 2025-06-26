package kanbanboard.httpserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import kanbanboard.manager.task.ManagerSaveException;
import kanbanboard.manager.task.NotFoundException;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Epic;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            String[] pathParts = extractPathParts(path);

            if (pathParts.length > 0 && "epics".equals(pathParts[0])) {
                int id = extractIdFromPath(pathParts);
                if (id == -1 && pathParts.length > 1) {
                    sendNotFound(exchange);
                    return;
                }

                switch (method) {
                    case "GET" -> handleGetEpicRequests(exchange, pathParts, id);
                    case "POST" -> handlePostEpicRequests(exchange, pathParts);
                    case "DELETE" -> handleDeleteEpicRequests(exchange, pathParts, id);
                    default -> sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (ManagerSaveException e) {
            sendInternalServerError(exchange, "Ошибка сохранения данных");
        } catch (Exception e) {
            sendInternalServerError(exchange, "Внутренняя ошибка сервера");
        } finally {
            exchange.close();
        }
    }

    // Вспомогательные методы
    private String[] extractPathParts(String path) {
        return path.substring(1).split("/");
    }

    private int extractIdFromPath(String[] pathParts) {
        if (pathParts.length > 1) {
            try {
                return Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private void handleGetEpicRequests(HttpExchange exchange, String[] pathParts, int id) throws IOException {
        if (pathParts.length == 1) {
            sendText(exchange, gson.toJson(taskManager.getEpic()), 200);
        } else if (pathParts.length == 2) {
            try {
                Epic epic = taskManager.getEpic(id);
                sendText(exchange, gson.toJson(epic), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        } else if (pathParts.length == 3 && "subtasks".equals(pathParts[2])) {
            try {
                sendText(exchange, gson.toJson(taskManager.getAllSubtasksOfEpic(id)), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostEpicRequests(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 1) {
            String body = readRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);
            taskManager.createEpic(epic);
            sendText(exchange, "{\"message\":\"Эпик создан\"}", 201);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteEpicRequests(HttpExchange exchange, String[] pathParts, int id) throws IOException {
        if (pathParts.length == 2) {
            try {
                taskManager.deleteEpic(id);
                sendText(exchange, "{\"message\":\"Эпик удален\"}", 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        } else if (pathParts.length == 1) {
            sendNotFound(exchange); // Удаление всех эпиков не реализовано
        } else {
            sendNotFound(exchange);
        }
    }

}
