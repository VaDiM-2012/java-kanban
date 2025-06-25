package kanbanboard.server;

import com.google.gson.Gson;
import kanbanboard.server.HttpTaskServer;
import kanbanboard.manager.Managers;
import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.manager.task.InMemoryTaskManager;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Epic;
import kanbanboard.model.Subtask;
import kanbanboard.model.Status;
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

public class EpicsHandlerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private Gson gson;

    public EpicsHandlerTest() throws IOException {
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
    void handleGetEpics_emptyManager_returnsEmptyList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        List<Epic> epics = gson.fromJson(response.body(), List.class);
        assertNotNull(epics, "Response body is null");
        assertTrue(epics.isEmpty(), "Epic list should be empty");
    }

    @Test
    void handleGetEpicById_existingEpic_returnsEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        Epic responseEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals("Test Epic", responseEpic.getTitle(), "Incorrect epic title");
    }

    @Test
    void handleGetEpicById_nonexistentEpic_returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void handleGetEpicSubtasks_existingEpicWithSubtasks_returnsSubtaskList() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId(), Status.NEW, 2, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        List<Subtask> subtasks = gson.fromJson(response.body(), List.class);
        assertNotNull(subtasks, "Response body is null");
        assertEquals(1, subtasks.size(), "Incorrect number of subtasks");
    }

    @Test
    void handleGetEpicSubtasks_nonexistentEpic_returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void handlePostEpic_newEpic_createsEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("New Epic", "Epic Description");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Unexpected status code");

        List<Epic> epicsFromManager = taskManager.getEpic();
        assertEquals(1, epicsFromManager.size(), "Incorrect number of epics");
        assertEquals("New Epic", epicsFromManager.get(0).getTitle(), "Incorrect epic title");
    }

    @Test
    void handlePostEpic_updateExistingEpic_updatesEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Original Epic", "Description");
        taskManager.createEpic(epic);

        Epic updatedEpic = new Epic("Updated Epic", "New Description");
        updatedEpic.setId(1);
        String epicJson = gson.toJson(updatedEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Unexpected status code");

        Epic epicFromManager = taskManager.getEpic(1);
        assertEquals("Updated Epic", epicFromManager.getTitle(), "Epic title not updated");
    }

    @Test
    void handlePostEpic_invalidJson_returns406() throws IOException, InterruptedException {
        String invalidJson = "{invalid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Expected 406 Not Acceptable");
    }

    @Test
    void handleDeleteEpic_existingEpic_deletesEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        taskManager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        assertTrue(taskManager.getEpic().isEmpty(), "Epic was not deleted");
    }

    @Test
    void handleDeleteEpic_nonexistentEpic_returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found");
    }

    @Test
    void handleDeleteAllEpics_multipleEpics_deletesAll() throws IOException, InterruptedException {
        taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createEpic(new Epic("Epic 2", "Desc"));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Unexpected status code");

        assertTrue(taskManager.getEpic().isEmpty(), "Epics were not deleted");
    }

    @Test
    void handleInvalidMethod_returns405() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Expected 405 Method Not Allowed");
    }
}