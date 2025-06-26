package kanbanboard.httpserver;

import kanbanboard.model.Task;
import kanbanboard.model.Epic;
import kanbanboard.model.Subtask;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для HistoryHandler, проверяющие обработку HTTP-запросов к /history.
 */
public class HistoryHandlerTest extends BaseHttpHandlerTest {

    /**
     * Тест: Получение истории просмотра (GET /history).
     * Проверяет, что история содержит просмотренные задачи.
     */
    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // Создаем и просматриваем несколько задач разных типов
        Task task = new Task("Тестовая задача", "Описание");
        task.setStartTime(LocalDateTime.of(2025, 6, 25, 9, 0));
        task.setDuration(Duration.ofMinutes(30));
        taskManager.createTask(task);
        taskManager.getTask(task.getId()); // Просмотр задачи

        Epic epic = new Epic("Тестовый эпик", "Описание");
        taskManager.createEpic(epic);
        taskManager.getEpic(epic.getId()); // Просмотр эпика

        Subtask subtask = new Subtask("Тестовая подзадача", "Описание", epic.getId());
        subtask.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        subtask.setDuration(Duration.ofMinutes(45));
        taskManager.createSubtask(subtask);
        taskManager.getSubtask(subtask.getId()); // Просмотр подзадачи

        // Отправляем GET запрос
        HttpResponse<String> response = sendGetRequest("/history");

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(history, "Список истории не должен быть null");
        assertEquals(3, history.size(), "История должна содержать 3 элемента");

        // Проверяем, что все просмотренные задачи присутствуют в истории
        assertTrue(history.stream().anyMatch(t -> t.getId().equals(task.getId())), "Задача отсутствует в истории");
        assertTrue(history.stream().anyMatch(t -> t.getId().equals(epic.getId())), "Эпик отсутствует в истории");
        assertTrue(history.stream().anyMatch(t -> t.getId().equals(subtask.getId())), "Подзадача отсутствует в истории");
    }

    /**
     * Тест: Получение пустой истории просмотра (GET /history).
     * Проверяет, что возвращается пустой список, если нет просмотренных задач.
     */
    @Test
    public void testGetEmptyHistory() throws IOException, InterruptedException {
        // История пуста по умолчанию
        HttpResponse<String> response = sendGetRequest("/history");

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(history, "Список истории не должен быть null");
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    /**
     * Тест: Обработка не-GET запроса (например, POST) к /history.
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testPostToHistory_NotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = sendPostRequest("/history", "{}");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found) для некорректного метода");
    }
}
