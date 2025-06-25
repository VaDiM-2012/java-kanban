package kanbanboard.httpserver;

import kanbanboard.model.Epic;
import kanbanboard.model.Subtask;
import kanbanboard.model.Status;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для EpicsHandler, проверяющие обработку HTTP-запросов к /epics.
 */
public class EpicsHandlerTest extends BaseHttpHandlerTest {

    /**
     * Тест: Создание нового эпика (POST /epics).
     * Проверяет успешное создание эпика и его наличие в менеджере.
     */
    @Test
    public void testCreateEpic() throws IOException, InterruptedException {
        // Создаем тестовый эпик
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        String epicJson = gson.toJson(epic);

        // Отправляем POST запрос
        HttpResponse<String> response = sendPostRequest("/epics", epicJson);

        // Проверяем статус ответа
        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 (Created)");

        // Проверяем, что эпик появился в менеджере
        List<Epic> epicsFromManager = taskManager.getEpic();
        assertNotNull(epicsFromManager, "Список эпиков не должен быть null");
        assertEquals(1, epicsFromManager.size(), "Должен быть один эпик");
        assertEquals("Тестовый эпик", epicsFromManager.get(0).getTitle(), "Название эпика не совпадает");
        assertEquals(Status.NEW, epicsFromManager.get(0).getStatus(), "Новый эпик должен иметь статус NEW");
    }

    /**
     * Тест: Обновление существующего эпика (POST /epics).
     * Проверяет успешное обновление эпика.
     * (Обратите внимание: в вашем коде POST /epics используется только для создания,
     * обновление эпика возможно через updateEpic в менеджере, но не через POST /epics в EpicsHandler).
     * Этот тест покажет, что POST /epics создает новый эпик, даже если передать ID.
     */
    @Test
    public void testUpdateEpicViaPost_CreatesNew() throws IOException, InterruptedException {
        // Создаем эпик через менеджер
        Epic existingEpic = new Epic("Старый эпик", "Старое описание");
        taskManager.createEpic(existingEpic);

        // Создаем обновленный эпик с тем же ID, но EpicsHandler POST не обрабатывает обновление по ID.
        // Он будет создавать новый, если ID не 0 или null, но не найдет существующий.
        Epic updatedEpic = new Epic("Обновленный эпик", "Новое описание");
        updatedEpic.setId(existingEpic.getId());
        String updatedEpicJson = gson.toJson(updatedEpic);

        // Отправляем POST запрос на обновление
        HttpResponse<String> response = sendPostRequest("/epics", updatedEpicJson);

        // Проверяем статус ответа
        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 (Created)");

        // Проверяем, что в менеджере теперь два эпика
        List<Epic> epicsFromManager = taskManager.getEpic();
        assertEquals(2, epicsFromManager.size(), "Должно быть два эпика (создался новый)");
        assertTrue(epicsFromManager.stream().anyMatch(e -> e.getTitle().equals("Обновленный эпик") && !e.getId().equals(existingEpic.getId())));
    }


    /**
     * Тест: Получение всех эпиков (GET /epics).
     * Проверяет, что возвращается корректный список эпиков.
     */
    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        // Добавляем несколько эпиков в менеджер
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        // Отправляем GET запрос
        HttpResponse<String> response = sendGetRequest("/epics");

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        List<Epic> epics = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Epic>>() {}.getType());
        assertNotNull(epics, "Список эпиков не должен быть null");
        assertEquals(2, epics.size(), "Должно быть два эпика");
        assertTrue(epics.stream().anyMatch(e -> e.getTitle().equals("Эпик 1")), "Эпик 1 отсутствует");
        assertTrue(epics.stream().anyMatch(e -> e.getTitle().equals("Эпик 2")), "Эпик 2 отсутствует");
    }

    /**
     * Тест: Получение эпика по ID (GET /epics/{id}).
     * Проверяет успешное получение эпика.
     */
    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        // Создаем эпик через менеджер, чтобы получить ID
        Epic epic = new Epic("Эпик по ID", "Описание по ID");
        taskManager.createEpic(epic);

        // Отправляем GET запрос по ID
        HttpResponse<String> response = sendGetRequest("/epics/" + epic.getId());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(retrievedEpic, "Полученный эпик не должен быть null");
        assertEquals(epic.getId(), retrievedEpic.getId(), "ID эпика не совпадает");
        assertEquals("Эпик по ID", retrievedEpic.getTitle(), "Название эпика не совпадает");
    }

    /**
     * Тест: Получение несуществующего эпика по ID (GET /epics/{id}).
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testGetEpicById_NotFound() throws IOException, InterruptedException {
        // Отправляем GET запрос с несуществующим ID
        HttpResponse<String> response = sendGetRequest("/epics/999");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
        assertTrue(response.body().contains("Ресурс не найден"), "Сообщение об ошибке не соответствует");
    }

    /**
     * Тест: Получение всех подзадач эпика по ID (GET /epics/{id}/subtasks).
     * Проверяет успешное получение подзадач и их соответствие.
     */
    @Test
    public void testGetAllSubtasksOfEpic() throws IOException, InterruptedException {
        // Создаем эпик и несколько подзадач
        Epic epic = new Epic("Эпик с подзадачами", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(30));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2025, 6, 25, 11, 0));
        subtask2.setDuration(Duration.ofMinutes(45));
        taskManager.createSubtask(subtask2);

        // Отправляем GET запрос на /epics/{id}/subtasks
        HttpResponse<String> response = sendGetRequest("/epics/" + epic.getId() + "/subtasks");

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        List<Subtask> subtasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Subtask>>() {}.getType());
        assertNotNull(subtasks, "Список подзадач не должен быть null");
        assertEquals(2, subtasks.size(), "Должно быть две подзадачи");
        assertTrue(subtasks.stream().anyMatch(s -> s.getTitle().equals("Подзадача 1")), "Подзадача 1 отсутствует");
        assertTrue(subtasks.stream().anyMatch(s -> s.getTitle().equals("Подзадача 2")), "Подзадача 2 отсутствует");
    }

    /**
     * Тест: Получение подзадач для несуществующего эпика (GET /epics/{id}/subtasks).
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testGetAllSubtasksOfEpic_NotFound() throws IOException, InterruptedException {
        // Отправляем GET запрос с несуществующим ID эпика
        HttpResponse<String> response = sendGetRequest("/epics/999/subtasks");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
        assertTrue(response.body().contains("Ресурс не найден"), "Сообщение об ошибке не соответствует");
    }

    /**
     * Тест: Удаление эпика по ID (DELETE /epics/{id}).
     * Проверяет успешное удаление эпика и всех его подзадач.
     */
    @Test
    public void testDeleteEpicById() throws IOException, InterruptedException {
        // Создаем эпик и несколько подзадач
        Epic epic = new Epic("Эпик для удаления", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача для удаления 1", "Описание", epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(30));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача для удаления 2", "Описание", epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2025, 6, 25, 11, 0));
        subtask2.setDuration(Duration.ofMinutes(30));
        taskManager.createSubtask(subtask2);

        // Отправляем DELETE запрос
        HttpResponse<String> response = sendDeleteRequest("/epics/" + epic.getId());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");
        assertTrue(response.body().contains("Эпик удален"), "Сообщение об успехе не соответствует");

        // Проверяем, что эпик и подзадачи удалены из менеджера
        assertNull(taskManager.getEpic(epic.getId()), "Эпик должен быть удален из менеджера");
        assertNull(taskManager.getSubtask(subtask1.getId()), "Подзадача 1 должна быть удалена");
        assertNull(taskManager.getSubtask(subtask2.getId()), "Подзадача 2 должна быть удалена");
    }

    /**
     * Тест: Удаление несуществующего эпика по ID (DELETE /epics/{id}).
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testDeleteEpicById_NotFound() throws IOException, InterruptedException {
        // Отправляем DELETE запрос с несуществующим ID
        HttpResponse<String> response = sendDeleteRequest("/epics/999");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
        assertTrue(response.body().contains("Ресурс не найден"), "Сообщение об ошибке не соответствует");
    }
}
