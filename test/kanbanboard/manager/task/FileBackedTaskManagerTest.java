package kanbanboard.manager.task;


import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.model.Epic;
import kanbanboard.model.Status;
import kanbanboard.model.Subtask;
import kanbanboard.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private InMemoryHistoryManager historyManager;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        historyManager = new InMemoryHistoryManager();
        manager = new FileBackedTaskManager(historyManager, tempFile);
    }

    @Test
    void createTask_TaskCreatedAndSavedToFile() {
        Task task = new Task("Задача 1", "Описание задачи 1");
        Task createdTask = manager.createTask(task);

        assertNotNull(createdTask, "Задача не создана");
        assertEquals(1, createdTask.getId(), "ID задачи не установлен");
        assertEquals(task.getTitle(), createdTask.getTitle(), "Заголовок задачи не совпадает");

        // Проверяем, что задача сохранена в файл
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertEquals(1, loadedManager.getTask().size(), "Задача не сохранена в файл");
        assertEquals(task.getTitle(), loadedManager.getTask(1).getTitle(), "Заголовок задачи в файле не совпадает");
    }

    @Test
    void createEpic_EpicCreatedAndSavedToFile() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        Epic createdEpic = manager.createEpic(epic);

        assertNotNull(createdEpic, "Эпик не создан");
        assertEquals(1, createdEpic.getId(), "ID эпика не установлен");
        assertEquals(epic.getTitle(), createdEpic.getTitle(), "Заголовок эпика не совпадает");

        // Проверяем, что эпик сохранен в файл
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertEquals(1, loadedManager.getEpic().size(), "Эпик не сохранен в файл");
        assertEquals(epic.getTitle(), loadedManager.getEpic(1).getTitle(), "Заголовок эпика в файле не совпадает");
    }

    @Test
    void createSubtask_SubtaskCreatedAndSavedToFile() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        Subtask createdSubtask = manager.createSubtask(subtask);

        assertNotNull(createdSubtask, "Подзадача не создана");
        assertEquals(2, createdSubtask.getId(), "ID подзадачи не установлен");
        assertEquals(subtask.getTitle(), createdSubtask.getTitle(), "Заголовок подзадачи не совпадает");
        assertEquals(epic.getId(), createdSubtask.getEpicId(), "EpicId подзадачи не совпадает");

        // Проверяем, что подзадача сохранена в файл
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertEquals(1, loadedManager.getSubtask().size(), "Подзадача не сохранена в файл");
        assertEquals(subtask.getTitle(), loadedManager.getSubtask(2).getTitle(), "Заголовок подзадачи в файле не совпадает");
    }

    @Test
    void getTask_TaskReturnedFromMemory() {
        Task task = manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task retrievedTask = manager.getTask(1);

        assertNotNull(retrievedTask, "Задача не найдена");
        assertEquals(task.getTitle(), retrievedTask.getTitle(), "Заголовок задачи не совпадает");
        assertEquals(task.getId(), retrievedTask.getId(), "ID задачи не совпадает");
    }

    @Test
    void getEpic_EpicReturnedFromMemory() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Epic retrievedEpic = manager.getEpic(1);

        assertNotNull(retrievedEpic, "Эпик не найден");
        assertEquals(epic.getTitle(), retrievedEpic.getTitle(), "Заголовок эпика не совпадает");
        assertEquals(epic.getId(), retrievedEpic.getId(), "ID эпика не совпадает");
    }

    @Test
    void getSubtask_SubtaskReturnedFromMemory() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId()));
        Subtask retrievedSubtask = manager.getSubtask(2);

        assertNotNull(retrievedSubtask, "Подзадача не найдена");
        assertEquals(subtask.getTitle(), retrievedSubtask.getTitle(), "Заголовок подзадачи не совпадает");
        assertEquals(subtask.getId(), retrievedSubtask.getId(), "ID подзадачи не совпадает");
        assertEquals(epic.getId(), retrievedSubtask.getEpicId(), "EpicId подзадачи не совпадает");
    }

    @Test
    void updateTask_TaskUpdatedAndSavedToFile() {
        Task task = manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task updatedTask = new Task("Обновленная задача", "Новое описание", Status.DONE, task.getId());
        Task result = manager.updateTask(updatedTask);

        assertNotNull(result, "Задача не обновлена");
        assertEquals(updatedTask.getTitle(), manager.getTask(task.getId()).getTitle(), "Заголовок задачи не обновлен");
        assertEquals(Status.DONE, manager.getTask(task.getId()).getStatus(), "Статус задачи не обновлен");

        // Проверяем, что обновление сохранено в файл
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertEquals(updatedTask.getTitle(), loadedManager.getTask(task.getId()).getTitle(), "Заголовок задачи в файле не совпадает");
    }

    @Test
    void updateEpic_EpicUpdatedAndSavedToFile() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Epic updatedEpic = new Epic("Обновленный эпик", "Новое описание");
        updatedEpic.setId(epic.getId());
        Epic result = manager.updateEpic(updatedEpic);

        assertNotNull(result, "Эпик не обновлен");
        assertEquals(updatedEpic.getTitle(), manager.getEpic(epic.getId()).getTitle(), "Заголовок эпика не обновлен");

        // Проверяем, что обновление сохранено в файл
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertEquals(updatedEpic.getTitle(), loadedManager.getEpic(epic.getId()).getTitle(), "Заголовок эпика в файле не совпадает");
    }

    @Test
    void updateSubtask_SubtaskUpdatedAndSavedToFile() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId()));
        Subtask updatedSubtask = new Subtask("Обновленная подзадача", "Новое описание", epic.getId(), Status.DONE, subtask.getId());
        Subtask result = manager.updateSubtask(updatedSubtask);

        assertNotNull(result, "Подзадача не обновлена");
        assertEquals(updatedSubtask.getTitle(), manager.getSubtask(subtask.getId()).getTitle(), "Заголовок подзадачи не обновлен");
        assertEquals(Status.DONE, manager.getSubtask(subtask.getId()).getStatus(), "Статус подзадачи не обновлен");

        // Проверяем, что обновление сохранено в файл
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertEquals(updatedSubtask.getTitle(), loadedManager.getSubtask(subtask.getId()).getTitle(), "Заголовок подзадачи в файле не совпадает");
    }

    @Test
    void deleteTask_TaskDeletedAndFileUpdated() {
        Task task = manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        manager.deleteTask(task.getId());

        assertNull(manager.getTask(task.getId()), "Задача не удалена");

        // Проверяем, что задача удалена из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertTrue(loadedManager.getTask().isEmpty(), "Задача не удалена из файла");
    }

    @Test
    void deleteEpic_EpicAndSubtasksDeletedAndFileUpdated() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId()));
        manager.deleteEpic(epic.getId());

        assertNull(manager.getEpic(epic.getId()), "Эпик не удален");
        assertNull(manager.getSubtask(subtask.getId()), "Подзадача не удалена");

        // Проверяем, что эпик и подзадача удалены из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertTrue(loadedManager.getEpic().isEmpty(), "Эпик не удален из файла");
        assertTrue(loadedManager.getSubtask().isEmpty(), "Подзадача не удалена из файла");
    }

    @Test
    void deleteSubtask_SubtaskDeletedAndFileUpdated() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId()));
        manager.deleteSubtask(subtask.getId());

        assertNull(manager.getSubtask(subtask.getId()), "Подзадача не удалена");
        assertTrue(manager.getEpic(epic.getId()).getSubtasksIds().isEmpty(), "Подзадача не удалена из эпика");

        // Проверяем, что подзадача удалена из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertTrue(loadedManager.getSubtask().isEmpty(), "Подзадача не удалена из файла");
    }

    @Test
    void deleteTask_AllTasksDeletedAndFileUpdated() {
        manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        manager.createTask(new Task("Задача 2", "Описание задачи 2"));
        manager.deleteTask();

        assertTrue(manager.getTask().isEmpty(), "Задачи не удалены");

        // Проверяем, что задачи удалены из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertTrue(loadedManager.getTask().isEmpty(), "Задачи не удалены из файла");
    }

    @Test
    void deleteEpic_AllEpicsAndSubtasksDeletedAndFileUpdated() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId()));
        manager.deleteEpic();

        assertTrue(manager.getEpic().isEmpty(), "Эпики не удалены");
        assertTrue(manager.getSubtask().isEmpty(), "Подзадачи не удалены");

        // Проверяем, что эпики и подзадачи удалены из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertTrue(loadedManager.getEpic().isEmpty(), "Эпики не удалены из файла");
        assertTrue(loadedManager.getSubtask().isEmpty(), "Подзадачи не удалены из файла");
    }

    @Test
    void deleteSubtask_AllSubtasksDeletedAndFileUpdated() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId()));
        manager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId()));
        manager.deleteSubtask();

        assertTrue(manager.getSubtask().isEmpty(), "Подзадачи не удалены");
        assertTrue(manager.getEpic(epic.getId()).getSubtasksIds().isEmpty(), "Подзадачи не удалены из эпика");

        // Проверяем, что подзадачи удалены из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);
        assertTrue(loadedManager.getSubtask().isEmpty(), "Подзадачи не удалены из файла");
    }

    @Test
    void getAllSubtasksOfEpic_SubtasksReturnedForEpic() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId()));
        ArrayList<Subtask> subtasks = manager.getAllSubtasksOfEpic(epic.getId());

        assertEquals(2, subtasks.size(), "Неверное количество подзадач");
        assertTrue(subtasks.contains(subtask1), "Подзадача 1 не найдена");
        assertTrue(subtasks.contains(subtask2), "Подзадача 2 не найдена");
    }

    @Test
    void getHistory_HistoryReturnedFromMemory() {
        Task task = manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        manager.getTask(task.getId()); // Добавляем в историю
        List<Task> history = manager.getHistory();

        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task.getTitle(), history.get(0).getTitle(), "Задача в истории не совпадает");
    }

    @Test
    void save_FileCreatedWithCorrectContent() throws IOException {
        Task task = manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        manager.save(); // Явный вызов save

        String content = Files.readString(tempFile.toPath());
        String expectedHeader = "id,type,name,status,description,epic";
        String expectedTask = String.format("%d,TASK,%s,%s,%s", task.getId(), task.getTitle(), task.getStatus(), task.getDescription());

        assertTrue(content.contains(expectedHeader), "Заголовок CSV не найден");
        assertTrue(content.contains(expectedTask), "Задача не сохранена в файл");
    }

    @Test
    void loadFromFile_ManagerRestoredFromFile() {
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId()));
        subtask.setStatus(Status.DONE);
        manager.updateSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);

        assertEquals(1, loadedManager.getEpic().size(), "Эпик не загружен");
        assertEquals(1, loadedManager.getSubtask().size(), "Подзадача не загружена");
        assertEquals(epic.getTitle(), loadedManager.getEpic(epic.getId()).getTitle(), "Заголовок эпика не совпадает");
        assertEquals(subtask.getTitle(), loadedManager.getSubtask(subtask.getId()).getTitle(), "Заголовок подзадачи не совпадает");
        assertEquals(Status.DONE, loadedManager.getEpic(epic.getId()).getStatus(), "Статус эпика не совпадает");
    }

    @Test
    void saveAndLoadEmptyFile_returnEmptyCollections() {
        // Проверяем сохранение и загрузку пустого файла
        manager.save(); // Явно вызываем save для создания файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(historyManager, tempFile);

        assertTrue(loadedManager.getTask().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getEpic().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getSubtask().isEmpty(), "Список подзадач должен быть пустым");
    }
}