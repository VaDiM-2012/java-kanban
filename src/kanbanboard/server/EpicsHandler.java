package kanbanboard.server;

import com.sun.net.httpserver.HttpExchange;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Epic;

import java.io.IOException;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {
    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            sendText(exchange, toJson(taskManager.getEpic()));
        } else if (path.contains("/subtasks")) {
            // Получение подзадач эпика
            Optional<Integer> epicId = parseIdFromPath(path);
            if (epicId.isEmpty()) {
                sendNotFound(exchange);
                return;
            }

            Epic epic = taskManager.getEpic(epicId.get());
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                sendText(exchange, toJson(taskManager.getAllSubtasksOfEpic(epicId.get())));
            }
        } else {
            // Получение эпика по ID
            Optional<Integer> epicId = parseIdFromPath(path);
            if (epicId.isEmpty()) {
                sendNotFound(exchange);
                return;
            }

            Epic epic = taskManager.getEpic(epicId.get());
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                sendText(exchange, toJson(epic));
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<String> body = readText(exchange);
        if (body.isEmpty()) {
            sendNotAcceptable(exchange);
            return;
        }

        Optional<Epic> epicOptional = parseJson(body.get(), Epic.class);
        if (epicOptional.isEmpty()) {
            sendNotAcceptable(exchange);
            return;
        }

        Epic epic = epicOptional.get();
        if (epic.getId() == null) {
            Epic createdEpic = taskManager.createEpic(epic);
            sendCreated(exchange, toJson(createdEpic));
        } else {
            Epic updatedEpic = taskManager.updateEpic(epic);
            if (updatedEpic == null) {
                sendNotFound(exchange);
            } else {
                sendCreated(exchange, toJson(updatedEpic));
            }
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            taskManager.deleteEpic();
            sendText(exchange, "Все эпики удалены");
        } else {
            Optional<Integer> epicId = parseIdFromPath(path);
            if (epicId.isEmpty()) {
                sendNotFound(exchange);
                return;
            }

            Epic epic = taskManager.getEpic(epicId.get());
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                taskManager.deleteEpic(epicId.get());
                sendText(exchange, "Эпик с ID " + epicId.get() + " удален");
            }
        }
    }
}