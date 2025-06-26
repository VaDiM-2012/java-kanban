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
 * Тесты для PrioritizedHandler, проверяющие обработку HTTP-запросов к /prioritized.
 */
public class PrioritizedHandlerTest extends BaseHttpHandlerTest {

    /**
     * Тест: Получение списка приоритезированных задач (GET /prioritized).
     * Проверяет, что задачи возвращаются отсортированными по времени начала.
     */
    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        // Добавляем задачи с разным временем начала
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setStartTime(LocalDateTime.of(2025, 6, 25, 11, 0));
        task1.setDuration(Duration.ofMinutes(60));
        taskManager.createTask(task1);

        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        task2.setDuration(Duration.ofMinutes(30));
        taskManager.createTask(task2);

        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 25, 9, 0));
        subtask1.setDuration(Duration.ofMinutes(45));
        taskManager.createSubtask(subtask1);

        // Отправляем GET запрос
        HttpResponse<String> response = sendGetRequest("/prioritized");

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        List<Task> prioritizedTasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(prioritizedTasks, "Список приоритезированных задач не должен быть null");
        assertEquals(3, prioritizedTasks.size(), "Должно быть три приоритезированные задачи/подзадачи");

        // Проверяем порядок задач: subtask1 (9:00), task2 (10:00), task1 (11:00)
        assertEquals(subtask1.getId(), prioritizedTasks.get(0).getId(), "Первая задача должна быть subtask1");
        assertEquals(task2.getId(), prioritizedTasks.get(1).getId(), "Вторая задача должна быть task2");
        assertEquals(task1.getId(), prioritizedTasks.get(2).getId(), "Третья задача должна быть task1");
    }

    /**
     * Тест: Получение пустого списка приоритезированных задач (GET /prioritized).
     * Проверяет, что возвращается пустой список, если нет задач со временем начала.
     */
    @Test
    public void testGetEmptyPrioritizedTasks() throws IOException, InterruptedException {
        // Создаем задачи без времени начала
        Task task1 = new Task("Задача без времени", "Описание");
        taskManager.createTask(task1); // Не будет в приоритезированных

        Epic epic = new Epic("Эпик без подзадач со временем", "Описание");
        taskManager.createEpic(epic); // Не будет в приоритезированных

        // Отправляем GET запрос
        HttpResponse<String> response = sendGetRequest("/prioritized");

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        List<Task> prioritizedTasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(prioritizedTasks, "Список приоритезированных задач не должен быть null");
        assertTrue(prioritizedTasks.isEmpty(), "Список приоритезированных задач должен быть пустым");
    }

    /**
     * Тест: Обработка не-GET запроса (например, POST) к /prioritized.
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testPostToPrioritized_NotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = sendPostRequest("/prioritized", "{}");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found) для некорректного метода");
    }
}
