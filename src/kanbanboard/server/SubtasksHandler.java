package kanbanboard.server;

import com.sun.net.httpserver.HttpExchange;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Subtask;

import java.io.IOException;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {
    public SubtasksHandler(TaskManager taskManager) {
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
                    exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            sendText(exchange, toJson(taskManager.getSubtask()));
        } else {
            Optional<Integer> subtaskId = parseIdFromPath(path);
            if (subtaskId.isEmpty()) {
                sendNotFound(exchange);
                return;
            }

            Subtask subtask = taskManager.getSubtask(subtaskId.get());
            if (subtask == null) {
                sendNotFound(exchange);
            } else {
                sendText(exchange, toJson(subtask));
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<String> body = readText(exchange);
        if (body.isEmpty()) {
            sendNotAcceptable(exchange);
            return;
        }

        Optional<Subtask> subtaskOptional = parseJson(body.get(), Subtask.class);
        if (subtaskOptional.isEmpty()) {
            sendNotAcceptable(exchange);
            return;
        }

        Subtask subtask = subtaskOptional.get();
        try {
            if (subtask.getId() == null) {
                Subtask createdSubtask = taskManager.createSubtask(subtask);
                if (createdSubtask == null) {
                    sendNotFound(exchange);
                } else {
                    sendCreated(exchange, toJson(createdSubtask));
                }
            } else {
                Subtask updatedSubtask = taskManager.updateSubtask(subtask);
                if (updatedSubtask == null) {
                    sendNotFound(exchange);
                } else {
                    sendCreated(exchange, toJson(updatedSubtask));
                }
            }
        } catch (IllegalArgumentException e) {
            sendNotAcceptable(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            taskManager.deleteSubtask();
            sendText(exchange, "Все подзадачи удалены");
        } else {
            Optional<Integer> subtaskId = parseIdFromPath(path);
            if (subtaskId.isEmpty()) {
                sendNotFound(exchange);
                return;
            }

            Subtask subtask = taskManager.getSubtask(subtaskId.get());
            if (subtask == null) {
                sendNotFound(exchange);
            } else {
                taskManager.deleteSubtask(subtaskId.get());
                sendText(exchange, "Подзадача с ID " + subtaskId.get() + " удалена");
            }
        }
    }
}