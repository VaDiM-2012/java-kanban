package kanbanboard.httpserver;

import kanbanboard.model.Task;
import kanbanboard.model.Status;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для TasksHandler, проверяющие обработку HTTP-запросов к /tasks.
 */
public class TasksHandlerTest extends BaseHttpHandlerTest {

    /**
     * Тест: Создание новой задачи (POST /tasks).
     * Проверяет успешное создание задачи и ее наличие в менеджере.
     */
    @Test
    public void testCreateTask() throws IOException, InterruptedException {
        // Создаем тестовую задачу
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        task.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        task.setDuration(Duration.ofMinutes(60));
        String taskJson = gson.toJson(task);

        // Отправляем POST запрос
        HttpResponse<String> response = sendPostRequest("/tasks", taskJson);

        // Проверяем статус ответа
        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 (Created)");

        // Проверяем, что задача появилась в менеджере
        List<Task> tasksFromManager = taskManager.getTask();
        assertNotNull(tasksFromManager, "Список задач не должен быть null");
        assertEquals(1, tasksFromManager.size(), "Должна быть одна задача");
        assertEquals("Тестовая задача", tasksFromManager.get(0).getTitle(), "Название задачи не совпадает");
    }

    /**
     * Тест: Обновление существующей задачи (POST /tasks).
     * Проверяет успешное обновление задачи и ее измененные данные в менеджере.
     */
    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        // Создаем задачу через менеджер, чтобы получить ID
        Task existingTask = new Task("Старая задача", "Старое описание");
        existingTask.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        existingTask.setDuration(Duration.ofMinutes(60));
        taskManager.createTask(existingTask);

        // Создаем обновленную задачу с тем же ID
        Task updatedTask = new Task("Обновленная задача", "Новое описание");
        updatedTask.setId(existingTask.getId()); // Важно установить ID для обновления
        updatedTask.setStatus(Status.DONE);
        updatedTask.setStartTime(LocalDateTime.of(2025, 6, 25, 11, 0));
        updatedTask.setDuration(Duration.ofMinutes(30));
        String updatedTaskJson = gson.toJson(updatedTask);

        // Отправляем POST запрос на обновление
        HttpResponse<String> response = sendPostRequest("/tasks", updatedTaskJson);

        // Проверяем статус ответа
        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 (Created)");

        // Проверяем, что задача обновлена в менеджере
        Task retrievedTask = taskManager.getTask(existingTask.getId());
        assertNotNull(retrievedTask, "Обновленная задача не найдена");
        assertEquals("Обновленная задача", retrievedTask.getTitle(), "Название задачи не обновлено");
        assertEquals(Status.DONE, retrievedTask.getStatus(), "Статус задачи не обновлен");
        assertEquals(Duration.ofMinutes(30), retrievedTask.getDuration(), "Длительность задачи не обновлена");
        assertEquals(LocalDateTime.of(2025, 6, 25, 11, 0), retrievedTask.getStartTime(), "Время начала задачи не обновлено");
    }

    /**
     * Тест: Получение всех задач (GET /tasks).
     * Проверяет, что возвращается корректный список задач.
     */
    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        // Добавляем несколько задач в менеджер
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setStartTime(LocalDateTime.of(2025, 6, 25, 9, 0));
        task1.setDuration(Duration.ofMinutes(30));
        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        task2.setDuration(Duration.ofMinutes(45));
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Отправляем GET запрос
        HttpResponse<String> response = sendGetRequest("/tasks");

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        List<Task> tasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(tasks, "Список задач не должен быть null");
        assertEquals(2, tasks.size(), "Должно быть две задачи");
        // Проверяем, что задачи в списке соответствуют созданным
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Задача 1")), "Задача 1 отсутствует");
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Задача 2")), "Задача 2 отсутствует");
    }

    /**
     * Тест: Получение задачи по ID (GET /tasks/{id}).
     * Проверяет успешное получение задачи.
     */
    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        // Создаем задачу через менеджер, чтобы получить ID
        Task task = new Task("Задача по ID", "Описание по ID");
        task.setStartTime(LocalDateTime.of(2025, 6, 25, 12, 0));
        task.setDuration(Duration.ofMinutes(90));
        taskManager.createTask(task);

        // Отправляем GET запрос по ID
        HttpResponse<String> response = sendGetRequest("/tasks/" + task.getId());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");

        // Проверяем содержимое ответа
        Task retrievedTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(retrievedTask, "Полученная задача не должна быть null");
        assertEquals(task.getId(), retrievedTask.getId(), "ID задачи не совпадает");
        assertEquals("Задача по ID", retrievedTask.getTitle(), "Название задачи не совпадает");
    }

    /**
     * Тест: Получение несуществующей задачи по ID (GET /tasks/{id}).
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testGetTaskById_NotFound() throws IOException, InterruptedException {
        // Отправляем GET запрос с несуществующим ID
        HttpResponse<String> response = sendGetRequest("/tasks/999");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
        assertTrue(response.body().contains("Ресурс не найден"), "Сообщение об ошибке не соответствует");
    }

    /**
     * Тест: Удаление задачи по ID (DELETE /tasks/{id}).
     * Проверяет успешное удаление задачи.
     */
    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        // Создаем задачу через менеджер
        Task task = new Task("Задача для удаления", "Описание");
        taskManager.createTask(task);

        // Отправляем DELETE запрос
        HttpResponse<String> response = sendDeleteRequest("/tasks/" + task.getId());

        // Проверяем статус ответа
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 (OK)");
        assertTrue(response.body().contains("Задача удалена"), "Сообщение об успехе не соответствует");

        // Проверяем, что задача удалена из менеджера
        assertNull(taskManager.getTask(task.getId()), "Задача должна быть удалена из менеджера");
    }

    /**
     * Тест: Удаление несуществующей задачи по ID (DELETE /tasks/{id}).
     * Проверяет, что возвращается статус 404.
     */
    @Test
    public void testDeleteTaskById_NotFound() throws IOException, InterruptedException {
        // Отправляем DELETE запрос с несуществующим ID
        HttpResponse<String> response = sendDeleteRequest("/tasks/999");

        // Проверяем статус ответа
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (Not Found)");
        assertTrue(response.body().contains("Ресурс не найден"), "Сообщение об ошибке не соответствует");
    }

    /**
     * Тест: Создание задачи с пересечением по времени (POST /tasks).
     * Проверяет, что возвращается статус 406.
     */
    @Test
    public void testCreateTask_Overlap() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        taskManager.createTask(task1);

        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setStartTime(LocalDateTime.of(2025, 6, 25, 10, 30)); // Пересекается с task1
        task2.setDuration(Duration.ofMinutes(30));
        String task2Json = gson.toJson(task2);

        HttpResponse<String> response = sendPostRequest("/tasks", task2Json);
        assertEquals(406, response.statusCode(), "Код ответа должен быть 406 (Not Acceptable)");
        assertTrue(response.body().contains("Задача пересекается с существующими"), "Сообщение об ошибке не соответствует");
    }
}
