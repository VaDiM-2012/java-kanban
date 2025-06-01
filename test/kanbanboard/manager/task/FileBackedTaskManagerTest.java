package kanbanboard.manager.task;

import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.model.Epic;
import kanbanboard.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        // Создаем временный файл для каждого теста
        tempFile = File.createTempFile("tasks", ".csv");
        // Инициализируем менеджер через loadFromFile с пустым файлом
        manager = FileBackedTaskManager.loadFromFile(new InMemoryHistoryManager(), tempFile);
    }

    @Test
    void save_afterTaskCreation_fileContainsTaskData() throws IOException {
        Task task = new Task("Test Task", "Description");
        manager.createTask(task);

        String fileContent = Files.readString(tempFile.toPath());
        assertTrue(fileContent.contains("Test Task"), "Файл должен содержать название задачи");
        assertTrue(fileContent.contains("Description"), "Файл должен содержать описание задачи");
    }

    @Test
    void loadFromFile_withTasks_restoresManagerState() throws IOException {
        // Создаем тестовые данные
        Task task = new Task("Test Task", "Description");
        manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        // Загружаем из файла в новый менеджер
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(
                new InMemoryHistoryManager(), tempFile);

        // Проверяем восстановление состояния
        assertNotNull(loadedManager.getTask(task.getId()), "Задача не восстановлена из файла");
        assertNotNull(loadedManager.getEpic(epic.getId()), "Эпик не восстановлен из файла");
        assertEquals(2, loadedManager.countId, "Счетчик ID должен быть восстановлен");
    }

    @Test
    void loadFromFile_emptyFile_createsEmptyManager() throws IOException {
        // Очищаем файл
        Files.write(tempFile.toPath(), new byte[0]);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(
                new InMemoryHistoryManager(), tempFile);

        assertTrue(loadedManager.getTask().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.getEpic().isEmpty(), "Менеджер должен быть пустым");
        assertEquals(0, loadedManager.countId, "Счетчик ID должен быть 0");
    }
}