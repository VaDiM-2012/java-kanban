package kanbanboard.httpserver;

import kanbanboard.manager.task.NotFoundException;
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
 * Тесты для SubtasksHandler, проверяющие обработку HTTP-запросов к /subtasks.
 */
public class SubtasksHandlerTest extends BaseHttpHandlerTest {

    /**
     * Тест: Создание новой подзадачи (POST /subtasks).
     * Проверяет успешное создание подзадачи и ее наличие в менеджере,
     * а также обновление статуса и времени эпика.
     */
    @Test
    public void testCreateSubtask() throws IOException, InterruptedException {
        // Создаем эпик, к которому будет привязана подзадача
        Epic epic = new Epic("Эпик для подзадачи", "Описание");
        taskManager.createEpic(epic);

        // Создаем тестовую подзадачу
        Subtask subtask = new Subtask("Тестовая подзадача", "Описание подзадачи", epic.getId());
        subtask.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        subtask.setDuration(Duration.ofMinutes(30));
        String subtaskJson = gson.toJson(subtask);

        // Отправляем POST запрос
        HttpResponse<String> response = sendPostRequest("/subtasks", subtaskJson);

        // Проверяем статус ответа
        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 (Created)");

        // Проверяем, что подзадача появилась в менеджере
        List<Subtask> subtasksFromManager = taskManager.getSubtask();
        assertNotNull(subtasksFromManager, "Список подзадач не должен быть null");
        assertEquals(1, subtasksFromManager.size(), "Должна быть одна подзадача");
        assertEquals("Тестовая подзадача", subtasksFromManager.get(0).getTitle(), "Название подзадачи не совпадает");

        // Проверяем, что эпик обновился (статус и время)
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.NEW, updatedEpic.getStatus(), "Статус эпика должен быть NEW");
        assertEquals(Duration.ofMinutes(30), updatedEpic.getDuration(), "Длительность эпика не совпадает");
        assertEquals(LocalDateTime.of(2025, 6, 25, 10, 0), updatedEpic.getStartTime(), "Время начала эпика не совпадает");
        assertEquals(LocalDateTime.of(2025, 6, 25, 10, 30), updatedEpic.getEndTime(), "Время окончания эпика не совпадает");
    }

    /**
     * Тест: Создание подзадачи с несуществующим ID эпика (POST /subtasks).
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testCreateSubtask_InvalidEpicId() throws IOException, InterruptedException {
        // Создаем подзадачу с несуществующим ID эпика
        Subtask subtask = new Subtask("Неверная подзадача", "Описание", 999);
        String subtaskJson = gson.toJson(subtask);

        // Отправляем POST запрос
        HttpResponse<String> response = sendPostRequest("/subtasks", subtaskJson);

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
        assertTrue(response.body().contains("Ресурс не найден"), "Сообщение об ошибке не соответствует");
    }

    /**
     * Тест: Обновление существующей подзадачи (POST /subtasks).
     * Проверяет успешное обновление подзадачи и ее измененные данные в менеджере,
     * а также обновление статуса и времени эпика.
     */
    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Эпик для обновления", "Описание");
        taskManager.createEpic(epic);

        // Создаем подзадачу через менеджер, чтобы получить ID
        Subtask existingSubtask = new Subtask("Старая подзадача", "Старое описание", epic.getId());
        existingSubtask.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        existingSubtask.setDuration(Duration.ofMinutes(30));
        taskManager.createSubtask(existingSubtask);

        // Создаем обновленную подзадачу с тем же ID
        Subtask updatedSubtask = new Subtask("Обновленная подзадача", "Новое описание", epic.getId());
        updatedSubtask.setId(existingSubtask.getId()); // Важно установить ID для обновления
        updatedSubtask.setStatus(Status.DONE);
        updatedSubtask.setStartTime(LocalDateTime.of(2025, 6, 25, 11, 0));
        updatedSubtask.setDuration(Duration.ofMinutes(45));
        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        // Отправляем POST запрос на обновление
        HttpResponse<String> response = sendPostRequest("/subtasks", updatedSubtaskJson);

        // Проверяем статус ответа
        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 (Created)");

        // Проверяем, что подзадача обновлена в менеджере
        Subtask retrievedSubtask = taskManager.getSubtask(existingSubtask.getId());
        assertNotNull(retrievedSubtask, "Обновленная подзадача не найдена");
        assertEquals("Обновленная подзадача", retrievedSubtask.getTitle(), "Название подзадачи не обновлено");
        assertEquals(Status.DONE, retrievedSubtask.getStatus(), "Статус подзадачи не обновлен");
        assertEquals(Duration.ofMinutes(45), retrievedSubtask.getDuration(), "Длительность подзадачи не обновлена");
        assertEquals(LocalDateTime.of(2025, 6, 25, 11, 0), retrievedSubtask.getStartTime(), "Время начала подзадачи не обновлено");

        // Проверяем, что эпик обновился (статус и время)
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.DONE, updatedEpic.getStatus(), "Статус эпика должен быть DONE");
        assertEquals(Duration.ofMinutes(45), updatedEpic.getDuration(), "Длительность эпика не совпадает после обновления подзадачи");
        assertEquals(LocalDateTime.of(2025, 6, 25, 11, 0), updatedEpic.getStartTime(), "Время начала эпика не совпадает после обновления подзадачи");
        assertEquals(LocalDateTime.of(2025, 6, 25, 11, 45), updatedEpic.getEndTime(), "Время окончания эпика не совпадает после обновления подзадачи");
    }

    /**
     * Тест: Получение всех подзадач (GET /subtasks).
     * Проверяет, что возвращается корректный список подзадач.
     */
    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Эпик для списка", "Описание");
        taskManager.createEpic(epic);

        // Добавляем несколько подзадач в менеджер
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 25, 9, 0));
        subtask1.setDuration(Duration.ofMinutes(30));
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2025, 6, 25, 9, 45));
        subtask2.setDuration(Duration.ofMinutes(45));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        // Отправляем GET запрос
        HttpResponse<String> response = sendGetRequest("/subtasks");

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
     * Тест: Получение подзадачи по ID (GET /subtasks/{id}).
     * Проверяет успешное получение подзадачи.
     */
    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Эпик для подзадачи по ID", "Описание");
        taskManager.createEpic(epic);

        // Создаем подзадачу через менеджер
        Subtask subtask = new Subtask("Подзадача по ID", "Описание по ID", epic.getId());
        subtask.setStartTime(LocalDateTime.of(2025, 6, 25, 12, 0));
        subtask.setDuration(Duration.ofMinutes(90));
        taskManager.createSubtask(subtask);

        // Отправляем GET запрос по ID
        HttpResponse<String> response = sendGetRequest("/subtasks/" + subtask.getId());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(retrievedSubtask, "Полученная подзадача не должна быть null");
        assertEquals(subtask.getId(), retrievedSubtask.getId(), "ID подзадачи не совпадает");
        assertEquals("Подзадача по ID", retrievedSubtask.getTitle(), "Название подзадачи не совпадает");
        assertEquals(epic.getId(), retrievedSubtask.getEpicId(), "ID эпика подзадачи не совпадает");
    }

    /**
     * Тест: Получение несуществующей подзадачи по ID (GET /subtasks/{id}).
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testGetSubtaskById_NotFound() throws IOException, InterruptedException {
        // Отправляем GET запрос с несуществующим ID
        HttpResponse<String> response = sendGetRequest("/subtasks/999");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
        assertTrue(response.body().contains("Ресурс не найден"), "Сообщение об ошибке не соответствует");
    }

    /**
     * Тест: Удаление подзадачи по ID (DELETE /subtasks/{id}).
     * Проверяет успешное удаление подзадачи и обновление эпика.
     */
    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Эпик для удаления подзадачи", "Описание");
        taskManager.createEpic(epic);

        // Создаем подзадачу через менеджер
        Subtask subtask = new Subtask("Подзадача для удаления", "Описание", epic.getId());
        taskManager.createSubtask(subtask);

        // Отправляем DELETE запрос
        HttpResponse<String> response = sendDeleteRequest("/subtasks/" + subtask.getId());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");
        assertTrue(response.body().contains("Подзадача удалена"), "Сообщение об успехе не соответствует");

        // Проверяем, что подзадача удалена из менеджера
        assertThrows(NotFoundException.class, () ->taskManager.getSubtask(subtask.getId()), "Подзадача должна быть удалена из менеджера");

        // Проверяем, что подзадача удалена из списка подзадач эпика и эпик обновился
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertFalse(updatedEpic.getSubtasksIds().contains(subtask.getId()), "Подзадача должна быть удалена из эпика");
        assertEquals(Status.NEW, updatedEpic.getStatus(), "Статус эпика должен быть NEW после удаления единственной подзадачи");
        assertNull(updatedEpic.getStartTime(), "Время начала эпика должно быть null после удаления подзадач");
        assertEquals(Duration.ZERO, updatedEpic.getDuration(), "Длительность эпика должна быть ZERO после удаления подзадач");
        assertNull(updatedEpic.getEndTime(), "Время окончания эпика должно быть null после удаления подзадач");
    }

    /**
     * Тест: Удаление несуществующей подзадачи по ID (DELETE /subtasks/{id}).
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testDeleteSubtaskById_NotFound() throws IOException, InterruptedException {
        // Отправляем DELETE запрос с несуществующим ID
        HttpResponse<String> response = sendDeleteRequest("/subtasks/999");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
        assertTrue(response.body().contains("Ресурс не найден"), "Сообщение об ошибке не соответствует");
    }

    /**
     * Тест: Создание подзадачи с пересечением по времени (POST /subtasks).
     * Проверяет, что возвращается статус 406.
     */
    @Test
    public void testCreateSubtask_Overlap() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик с пересекающимися подзадачами", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(60));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 30)); // Пересекается с subtask1
        subtask2.setDuration(Duration.ofMinutes(30));
        String subtask2Json = gson.toJson(subtask2);

        HttpResponse<String> response = sendPostRequest("/subtasks", subtask2Json);
        assertEquals(406, response.statusCode(), "Код ответа должен быть 406 (Not Acceptable)");
        assertTrue(response.body().contains("Задача пересекается с существующими"), "Сообщение об ошибке не соответствует");
    }
}
