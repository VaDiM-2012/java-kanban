package kanbanboard.manager.task;

import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.model.Epic;
import kanbanboard.model.Subtask;
import kanbanboard.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    @BeforeEach
    public void setUp() {
        try {
            tempFile = File.createTempFile("tasks", ".csv");
            historyManager = new InMemoryHistoryManager();
            manager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании временного файла: " + e.getMessage(), e);
        }
    }

    @Test
    void save_afterTaskCreation_fileContainsTaskData() throws IOException {
        Task task = new Task("Test Task", "Description");
        task.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        task.setDuration(Duration.ofMinutes(60));
        manager.createTask(task);

        String fileContent = Files.readString(tempFile.toPath());
        assertTrue(fileContent.contains("Test Task"), "Файл не содержит название задачи");
        assertTrue(fileContent.contains("Description"), "Файл не содержит описание задачи");
        assertTrue(fileContent.contains("60"), "Файл не содержит длительность задачи");
        assertTrue(fileContent.contains("2025-06-10T10:00:00"), "Файл не содержит время начала задачи");
    }

    @Test
    void loadFromFile_withTasks_restoresManagerState() throws IOException {
        Task task = new Task("Test Task", "Description");
        task.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        task.setDuration(Duration.ofMinutes(60));
        manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        subtask.setStartTime(LocalDateTime.of(2025, 6, 10, 12, 0));
        subtask.setDuration(Duration.ofMinutes(30));
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(new InMemoryHistoryManager(), tempFile);

        assertNotNull(loadedManager.getTask(task.getId()), "Задача не восстановлена");
        assertEquals(Duration.ofMinutes(60), loadedManager.getTask(task.getId()).getDuration(), "Длительность задачи не восстановлена");
        assertNotNull(loadedManager.getEpic(epic.getId()), "Эпик не восстановлен");
        assertNotNull(loadedManager.getSubtask(subtask.getId()), "Подзадача не восстановлена");
        assertEquals(3, loadedManager.countId, "Счетчик ID не восстановлен");
    }

    @Test
    void loadFromFile_emptyFile_createsEmptyManager() throws IOException {
        Files.write(tempFile.toPath(), new byte[0]);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(new InMemoryHistoryManager(), tempFile);

        assertTrue(loadedManager.getTask().isEmpty(), "Список задач должен быть пуст");
        assertTrue(loadedManager.getEpic().isEmpty(), "Список эпиков должен быть пуст");
        assertTrue(loadedManager.getSubtask().isEmpty(), "Список подзадач должен быть пуст");
        assertEquals(0, loadedManager.countId, "Счетчик ID должен быть 0");
    }

    @Test
    void loadFromFile_invalidFile_throwsManagerSaveException() {
        File invalidFile = new File("/invalid/path/tasks.csv");
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(new InMemoryHistoryManager(), invalidFile),
                "Должно быть выброшено исключение при загрузке из несуществующего файла");
    }

    @Test
    void save_invalidFile_throwsManagerSaveException() {
        File invalidFile = new File("/invalid/path/tasks.csv");
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(new InMemoryHistoryManager(), invalidFile);
        Task task = new Task("Test Task", "Description");
        assertThrows(ManagerSaveException.class, () -> invalidManager.createTask(task),
                "Должно быть выброшено исключение при сохранении в недоступный файл");
    }
}