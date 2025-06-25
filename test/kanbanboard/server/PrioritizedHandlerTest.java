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

public class PrioritizedHandlerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private Gson gson;

    public PrioritizedHandlerTest() throws IOException {
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
    void handleGetPrioritized_emptyManager_returnsEmptyList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        List<Task> tasks = gson.fromJson(response.body(), List.class);
        assertNotNull(tasks, "Response body is null");
        assertTrue(tasks.isEmpty(), "Prioritized task list should be empty");
    }

    @Test
    void handleGetPrioritized_tasksWithStartTime_returnsSortedList() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Desc", Status.NEW, 1, Duration.ofMinutes(30), now.plusHours(2));
        Task task2 = new Task("Task 2", "Desc", Status.NEW, 2, Duration.ofMinutes(30), now.plusHours(1));
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        Task[] responseTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, responseTasks.length, "Incorrect number of tasks");
        assertEquals("Task 2", responseTasks[0].getTitle(), "Tasks not sorted by start time");
        assertEquals("Task 1", responseTasks[1].getTitle(), "Tasks not sorted by start time");
    }

    @Test
    void handleInvalidMethod_returns405() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Expected 405 Method Not Allowed");
    }
}