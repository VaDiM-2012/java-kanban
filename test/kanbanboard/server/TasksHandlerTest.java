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

public class TasksHandlerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private Gson gson;

    public TasksHandlerTest() throws IOException {
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
    void handleGetTasks_emptyManager_returnsEmptyList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        List<Task> tasks = gson.fromJson(response.body(), List.class);
        assertNotNull(tasks, "Response body is null");
        assertTrue(tasks.isEmpty(), "Task list should be empty");
    }

    @Test
    void handleGetTaskById_existingTask_returnsTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description", Status.NEW, 1, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertEquals("Test Task", responseTask.getTitle(), "Incorrect task title");
    }

    @Test
    void handleGetTaskById_nonexistentTask_returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void handlePostTask_newTask_createsTask() throws IOException, InterruptedException {
        Task task = new Task("New Task", "Description", Status.NEW, null, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Unexpected status code");

        List<Task> tasksFromManager = taskManager.getTask();
        assertEquals(1, tasksFromManager.size(), "Incorrect number of tasks");
        assertEquals("New Task", tasksFromManager.get(0).getTitle(), "Incorrect task title");
    }

    @Test
    void handlePostTask_updateExistingTask_updatesTask() throws IOException, InterruptedException {
        Task task = new Task("Original Task", "Description", Status.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createTask(task);

        Task updatedTask = new Task("Updated Task", "New Description", Status.IN_PROGRESS, 1, Duration.ofMinutes(10), LocalDateTime.now().plusHours(1));
        String taskJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Unexpected status code");

        Task taskFromManager = taskManager.getTask(1);
        assertEquals("Updated Task", taskFromManager.getTitle(), "Task title not updated");
        assertEquals(Status.IN_PROGRESS, taskFromManager.getStatus(), "Task status not updated");
    }

    @Test
    void handlePostTask_invalidJson_returns406() throws IOException, InterruptedException {
        String invalidJson = "{invalid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Expected 406 Not Acceptable");
    }

    @Test
    void handleDeleteTask_existingTask_deletesTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description", Status.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        assertTrue(taskManager.getTask().isEmpty(), "Task was not deleted");
    }

    @Test
    void handleDeleteTask_nonexistentTask_returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void handleDeleteAllTasks_multipleTasks_deletesAll() throws IOException, InterruptedException {
        taskManager.createTask(new Task("Task 1", "Desc", Status.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now()));
        taskManager.createTask(new Task("Task 2", "Desc", Status.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        assertTrue(taskManager.getTask().isEmpty(), "Tasks were not deleted");
    }

    @Test
    void handleInvalidMethod_returns405() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Expected 405 Method Not Allowed");
    }
}