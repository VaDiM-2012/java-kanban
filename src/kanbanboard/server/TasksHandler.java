package kanbanboard.server;

import com.sun.net.httpserver.HttpExchange;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Task;

import java.io.IOException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {
    public TasksHandler(TaskManager taskManager) {
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
        if (path.equals("/tasks")) {
            // Получение всех задач
            sendText(exchange, toJson(taskManager.getTask()));
        } else {
            // Получение задачи по ID
            Optional<Integer> taskId = parseIdFromPath(path);
            if (taskId.isEmpty()) {
                sendNotFound(exchange);
                return;
            }

            Task task = taskManager.getTask(taskId.get());
            if (task == null) {
                sendNotFound(exchange);
            } else {
                sendText(exchange, toJson(task));
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<String> body = readText(exchange);
        if (body.isEmpty()) {
            sendNotAcceptable(exchange);
            return;
        }

        Optional<Task> taskOptional = parseJson(body.get(), Task.class);
        if (taskOptional.isEmpty()) {
            sendNotAcceptable(exchange);
            return;
        }

        Task task = taskOptional.get();
        try {
            if (task.getId() == null) {
                // Создание новой задачи
                Task createdTask = taskManager.createTask(task);
                sendCreated(exchange, toJson(createdTask));
            } else {
                // Обновление существующей задачи
                Task updatedTask = taskManager.updateTask(task);
                if (updatedTask == null) {
                    sendNotFound(exchange);
                } else {
                    sendCreated(exchange, toJson(updatedTask));
                }
            }
        } catch (IllegalArgumentException e) {
            sendNotAcceptable(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            // Удаление всех задач
            taskManager.deleteTask();
            sendText(exchange, "Все задачи удалены");
        } else {
            // Удаление задачи по ID
            Optional<Integer> taskId = parseIdFromPath(path);
            if (taskId.isEmpty()) {
                sendNotFound(exchange);
                return;
            }

            Task task = taskManager.getTask(taskId.get());
            if (task == null) {
                sendNotFound(exchange);
            } else {
                taskManager.deleteTask(taskId.get());
                sendText(exchange, "Задача с ID " + taskId.get() + " удалена");
            }
        }
    }
}