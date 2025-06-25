package kanbanboard.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private Gson gson;

    public HttpTaskServerTest() throws IOException {
        taskManager = new InMemoryTaskManager(new InMemoryHistoryManager());
        taskServer = new HttpTaskServer();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
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
    void startServer_invalidEndpoint_returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/invalid");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void startServer_tasksEndpoint_createsTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description", Status.NEW, null, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Unexpected status code");

        assertEquals(1, taskManager.getTask().size(), "Task was not created");
        assertEquals("Test Task", taskManager.getTask().get(0).getTitle(), "Incorrect task title");
    }

    @Test
    void stopServer_afterStart_serverStops() throws IOException, InterruptedException {
        taskServer.stop();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            fail("Expected IOException due to stopped server");
        } catch (IOException e) {
            // Expected behavior: server is stopped
        }
    }

    @Test
    void startServer_multipleRequests_handlesConcurrently() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc", Status.NEW, null, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc", Status.NEW, null, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1));
        String task1Json = gson.toJson(task1);
        String task2Json = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .build();
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .build();

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response1.statusCode(), "Unexpected status code for request 1");
        assertEquals(201, response2.statusCode(), "Unexpected status code for request 2");
        assertEquals(2, taskManager.getTask().size(), "Incorrect number of tasks created");
    }
}