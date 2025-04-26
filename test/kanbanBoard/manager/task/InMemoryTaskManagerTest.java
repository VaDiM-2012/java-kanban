package kanbanBoard.manager.task;

import kanbanBoard.manager.history.InMemoryHistoryManager;
import kanbanBoard.model.Epic;
import kanbanBoard.model.Subtask;
import kanbanBoard.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    @Test
    void getTask_returnNotNull_taskExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task("Создать тестовую Задачу", "Описание тестовой Задачи");
        manager.createTask(task);

        assertNotNull(manager.getTask(1), "Задача не найдена");
    }

    @Test
    void getEpic_returnNotNull_epicExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic);

        assertNotNull(manager.getEpic(1), "Эпик не найден");
    }

    @Test
    void getSubtask_returnNotNull_subtaskExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Создать тестовую подзадачу", "Описание тестовой подзадачи", 1);
        manager.createSubtask(subtask);

        assertNotNull(manager.getSubtask(2), "Подзадача не найдена");
    }

    @Test
    void updateTask_returnNotNullAndUpdatesFields_taskExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        // Создаём исходную задачу
        Task task1 = new Task("Создать тестовую Задачу", "Описание тестовой Задачи");
        manager.createTask(task1);

        // Создаём обновлённую версию задачи
        Task task2 = new Task("Создать тестовую Задачу 2", "Описание тестовой Задачи 2");
        task2.setId(1);

        // Проверяем обновление
        assertNotNull(manager.updateTask(task2));
        assertEquals(task2.getTitle(), manager.getTask(1).getTitle());
    }

    @Test
    void updateEpic_returnNotNullAndUpdatesFields_epicExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        // Создаём исходный эпик
        Epic epic1 = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic1);

        // Создаём обновлённую версию эпика
        Epic epic2 = new Epic("Создать тестовый Эпик 2", "Описание тестового Эпика 2");
        epic2.setId(1);

        // Проверяем обновление
        assertNotNull(manager.updateEpic(epic2));
        assertEquals(epic2.getTitle(), manager.getEpic(1).getTitle());
    }

    @Test
    void updateSubtask_returnNotNullAndUpdatesFields_subtaskExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        // Создаём эпик для подзадачи
        Epic epic = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic);

        // Создаём исходную подзадачу
        Subtask subtask1 = new Subtask("Создать тестовую Подзадачу", "Описание тестовой Подзадачи", 1);
        manager.createSubtask(subtask1);

        // Создаём обновлённую версию подзадачи
        Subtask subtask2 = new Subtask("Создать тестовую Подзадачу 2", "Описание тестовой Подзадачи 2", 1);
        subtask2.setId(2);

        // Проверяем обновление
        assertNotNull(manager.updateSubtask(subtask2));
        assertEquals(subtask2.getTitle(), manager.getSubtask(2).getTitle());
    }

    @Test
    void getTask_returnSameFields_taskExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task("Создать тестовую Задачу", "Описание тестовой Задачи");
        manager.createTask(task);

        assertEquals(task.getTitle(), manager.getTask(1).getTitle());
        assertEquals(task.getDescription(), manager.getTask(1).getDescription());
    }

    @Test
    void getEpic_returnSameFields_epicExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic);

        assertEquals(epic.getTitle(), manager.getEpic(1).getTitle());
        assertEquals(epic.getDescription(), manager.getEpic(1).getDescription());
        assertTrue(epic.getSubtasksIds().equals(manager.getEpic(1).getSubtasksIds()));
    }

    @Test
    void getSubtask_returnSameFields_subtaskExists() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        // Создаем эпик для подзадачи
        Epic epic = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic);

        // Создаем подзадачу
        Subtask subtask = new Subtask("Создать тестовую Подзадачу", "Описание тестовой Подзадачи", 1);
        manager.createSubtask(subtask);

        assertEquals(subtask.getTitle(), manager.getSubtask(2).getTitle());
        assertEquals(subtask.getDescription(), manager.getSubtask(2).getDescription());
        assertTrue(epic.getSubtasksIds().equals(manager.getEpic(1).getSubtasksIds()));
    }
}
