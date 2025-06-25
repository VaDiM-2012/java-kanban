package kanbanboard.server;

import com.google.gson.Gson;
import kanbanboard.server.HttpTaskServer;
import kanbanboard.manager.Managers;
import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.manager.task.InMemoryTaskManager;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Status;
import kanbanboard.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryHandlerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private Gson gson;

    public HistoryHandlerTest() throws IOException {
        taskManager = new InMemoryTaskManager(new InMemoryHistoryManager());
        taskServer = new HttpTaskServer();
        gson = HttpTaskServer.getGson();
    }

    @BeforeEach
    public void setUp() {
        taskManager.deleteTask();
        taskManager.deleteSubtask();
        taskManager.deleteEpic();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    void handleGetHistory_emptyHistory_returnsEmptyList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        List<Task> history = gson.fromJson(response.body(), List.class);
        assertNotNull(history, "Response body is null");
        assertTrue(history.isEmpty(), "History should be empty");
    }

    @Test
    void handleGetHistory_viewedTasks_returnsHistoryList() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc", Status.NEW, 1, Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc", Status.NEW, 2, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.getTask(1); // Add to history
        taskManager.getTask(2); // Add to history

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        Task[] responseHistory = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, responseHistory.length, "Incorrect number of tasks in history");
        assertEquals("Task 1", responseHistory[0].getTitle(), "Incorrect task order in history");
        assertEquals("Task 2", responseHistory[1].getTitle(), "Incorrect task order in history");
    }

    @Test
    void handleInvalidMethod_returns405() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Expected 405 Method Not Allowed");
    }
}