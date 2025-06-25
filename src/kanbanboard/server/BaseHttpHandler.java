package kanbanboard.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new Gson();
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
    }

    protected void sendNotAcceptable(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(406, -1);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(500, -1);
    }

    protected Optional<String> readText(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return Optional.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected <T> Optional<T> parseJson(String json, Class<T> clazz) {
        try {
            return Optional.of(gson.fromJson(json, clazz));
        } catch (JsonSyntaxException e) {
            return Optional.empty();
        }
    }

    protected String toJson(Object object) {
        return gson.toJson(object);
    }

    protected Optional<Integer> parseIdFromPath(String path) {
        try {
            String[] parts = path.split("/");
            return Optional.of(Integer.parseInt(parts[parts.length - 1]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }
}