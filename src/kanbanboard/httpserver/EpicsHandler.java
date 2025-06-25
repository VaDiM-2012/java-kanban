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
            switch (method) {
                case "GET" -> {
                    if (path.equals("/epics")) {
                        sendText(exchange, gson.toJson(taskManager.getEpic()), 200);
                    } else if (path.startsWith("/epics/")) {
                        int id = extractIdFromPath(path, "/epics/");
                        if (id == -1) {
                            sendNotFound(exchange);
                            return;
                        }
                        if (path.endsWith("/subtasks")) {
                            try {
                                sendText(exchange, gson.toJson(taskManager.getAllSubtasksOfEpic(id)), 200);
                            } catch (NotFoundException e) {
                                sendNotFound(exchange);
                            }
                        } else {
                            try {
                                Epic epic = taskManager.getEpic(id);
                                sendText(exchange, gson.toJson(epic), 200);
                            } catch (NotFoundException e) {
                                sendNotFound(exchange);
                            }
                        }
                    }
                }
                case "POST" -> {
                    if (path.equals("/epics")) {
                        String body = readRequestBody(exchange);
                        Epic epic = gson.fromJson(body, Epic.class);
                        taskManager.createEpic(epic);
                        sendText(exchange, "{\"message\":\"Эпик создан\"}", 201);
                    }
                }
                case "DELETE" -> {
                    if (path.startsWith("/epics/")) {
                        int id = extractIdFromPath(path, "/epics/");
                        if (id == -1) {
                            sendNotFound(exchange);
                            return;
                        }
                        try {
                            taskManager.deleteEpic(id);
                            sendText(exchange, "{\"message\":\"Эпик удален\"}", 200);
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