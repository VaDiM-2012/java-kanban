package kanbanboard.server;

import com.google.gson.Gson;
import kanbanboard.server.HttpTaskServer;
import kanbanboard.manager.Managers;
import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.manager.task.InMemoryTaskManager;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Epic;
import kanbanboard.model.Status;
import kanbanboard.model.Subtask;
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

public class SubtasksHandlerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private Gson gson;

    public SubtasksHandlerTest() throws IOException {
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
    void handleGetSubtasks_emptyManager_returnsEmptyList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        List<Subtask> subtasks = gson.fromJson(response.body(), List.class);
        assertNotNull(subtasks, "Response body is null");
        assertTrue(subtasks.isEmpty(), "Subtask list should be empty");
    }

    @Test
    void handleGetSubtaskById_existingSubtask_returnsSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId(), Status.NEW, 2, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        Subtask responseSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals("Test Subtask", responseSubtask.getTitle(), "Incorrect subtask title");
        assertEquals(epic.getId(), responseSubtask.getEpicId(), "Incorrect epic ID");
    }

    @Test
    void handleGetSubtaskById_nonexistentSubtask_returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void handlePostSubtask_newSubtask_createsSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("New Subtask", "Description", epic.getId(), Status.NEW, null, Duration.ofMinutes(5), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Unexpected status code");

        List<Subtask> subtasksFromManager = taskManager.getSubtask();
        assertEquals(1, subtasksFromManager.size(), "Incorrect number of subtasks");
        assertEquals("New Subtask", subtasksFromManager.get(0).getTitle(), "Incorrect subtask title");
    }

    @Test
    void handlePostSubtask_invalidEpicId_returns404() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Invalid Subtask", "Description", 999, Status.NEW, null, Duration.ofMinutes(5), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void handlePostSubtask_updateExistingSubtask_updatesSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Original Subtask", "Description", epic.getId(), Status.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask("Updated Subtask", "New Description", epic.getId(), Status.IN_PROGRESS, 2, Duration.ofMinutes(10), LocalDateTime.now().plusHours(1));
        String subtaskJson = gson.toJson(updatedSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Unexpected status code");

        Subtask subtaskFromManager = taskManager.getSubtask(2);
        assertEquals("Updated Subtask", subtaskFromManager.getTitle(), "Subtask title not updated");
        assertEquals(Status.IN_PROGRESS, subtaskFromManager.getStatus(), "Subtask status not updated");
    }

    @Test
    void handlePostSubtask_invalidJson_returns406() throws IOException, InterruptedException {
        String invalidJson = "{invalid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Expected 406 Not Acceptable");
    }

    @Test
    void handleDeleteSubtask_existingSubtask_deletesSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId(), Status.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        assertTrue(taskManager.getSubtask().isEmpty(), "Subtask was not deleted");
    }

    @Test
    void handleDeleteSubtask_nonexistentSubtask_returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void handleDeleteAllSubtasks_multipleSubtasks_deletesAll() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc", epic.getId(), Status.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now()));
        taskManager.createSubtask(new Subtask("Subtask 2", "Desc", epic.getId(), Status.NEW, 3, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        assertTrue(taskManager.getSubtask().isEmpty(), "Subtasks were not deleted");
    }

    @Test
    void handleInvalidMethod_returns405() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Expected 405 Method Not Allowed");
    }
}