package kanbanboard.server;

import com.sun.net.httpserver.HttpServer;
import kanbanboard.manager.Managers;
import kanbanboard.manager.history.HistoryManager;
import kanbanboard.manager.task.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        HistoryManager historyManager = Managers.getDefaultHistoryManager();
        this.taskManager = Managers.getDefault(historyManager);
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        configureRoutes();
    }

    private void configureRoutes() {
        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту");
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
    }
}