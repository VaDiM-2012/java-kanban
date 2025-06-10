package kanbanboard.manager.task;

import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.model.Epic;
import kanbanboard.model.Subtask;
import kanbanboard.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    protected InMemoryTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @Test
    void getTask_returnNotNull_taskExists() {
        Task task = new Task("Задача", "Описание");
        manager.createTask(task);

        assertNotNull(manager.getTask(1), "Задача не найдена");
    }

    @Test
    void getEpic_returnNotNull_epicExists() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);

        assertNotNull(manager.getEpic(1), "Эпик не найден");
    }

    @Test
    void getSubtask_returnNotNull_subtaskExists() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", 1);
        manager.createSubtask(subtask);

        assertNotNull(manager.getSubtask(2), "Подзадача не найдена");
    }

    @Test
    void updateTask_returnNotNullAndUpdatesFields_taskExists() {
        Task task1 = new Task("Задача", "Описание");
        manager.createTask(task1);

        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setId(1);

        assertNotNull(manager.updateTask(task2));
        assertEquals("Задача 2", manager.getTask(1).getTitle());
    }

    @Test
    void updateEpic_returnNotNullAndUpdatesFields_epicExists() {
        Epic epic1 = new Epic("Эпик", "Описание");
        manager.createEpic(epic1);

        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        epic2.setId(1);

        assertNotNull(manager.updateEpic(epic2));
        assertEquals("Эпик 2", manager.getEpic(1).getTitle());
    }

    @Test
    void updateSubtask_returnNotNullAndUpdatesFields_subtaskExists() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача", "Описание", 1);
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", 1);
        subtask2.setId(2);

        assertNotNull(manager.updateSubtask(subtask2));
        assertEquals("Подзадача 2", manager.getSubtask(2).getTitle());
    }

    @Test
    void getTask_returnSameFields_taskExists() {
        Task task = new Task("Задача", "Описание");
        manager.createTask(task);

        Task retrieved = manager.getTask(1);
        assertEquals(task.getTitle(), retrieved.getTitle());
        assertEquals(task.getDescription(), retrieved.getDescription());
    }

    @Test
    void getEpic_returnSameFields_epicExists() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);

        Epic retrieved = manager.getEpic(1);
        assertEquals(epic.getTitle(), retrieved.getTitle());
        assertEquals(epic.getDescription(), retrieved.getDescription());
        assertEquals(epic.getSubtasksIds(), retrieved.getSubtasksIds());
    }

    @Test
    void getSubtask_returnSameFields_subtaskExists() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", 1);
        manager.createSubtask(subtask);

        Subtask retrieved = manager.getSubtask(2);
        assertEquals(subtask.getTitle(), retrieved.getTitle());
        assertEquals(subtask.getDescription(), retrieved.getDescription());
        assertEquals(epic.getSubtasksIds(), manager.getEpic(1).getSubtasksIds());
    }

    @Test
    void deleteTask_removesTaskFromHistory_taskExists() {
        Task task = new Task("Задача", "Описание");
        manager.createTask(task);
        manager.getTask(1); // Добавляем в историю

        manager.deleteTask(1);
        assertTrue(manager.getHistory().isEmpty(), "Задача не удалена из истории");
    }

    @Test
    void deleteEpic_removesEpicAndSubtasksFromHistory() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание", 1);
        manager.createSubtask(subtask);

        manager.getEpic(1); // Добавляем эпик в историю
        manager.getSubtask(2); // Добавляем подзадачу в историю

        manager.deleteEpic(1);
        assertTrue(manager.getHistory().isEmpty(), "Эпик или подзадачи не удалены из истории");
    }

    @Test
    void deleteSubtask_removesSubtaskFromHistoryAndEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание", 1);
        manager.createSubtask(subtask);

        manager.getSubtask(2); // Добавляем в историю

        manager.deleteSubtask(2);
        assertTrue(manager.getHistory().isEmpty(), "Подзадача не удалена из истории");
        assertFalse(manager.getEpic(1).getSubtasksIds().contains(2), "ID подзадачи остался в эпике");
    }

    @Test
    void setTaskIdOutsideManager_doesNotAffectManager() {
        Task task = new Task("Задача", "Описание");
        manager.createTask(task); // ID = 1
        task.setId(999); // Меняем ID через сеттер

        assertNotNull(manager.getTask(1), "Задача не найдена по оригинальному ID");
        assertNull(manager.getTask(999), "Задача найдена по новому ID");
    }

    @Test
    void setSubtaskIdOutsideManager_doesNotAffectEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание", 1);
        manager.createSubtask(subtask); // ID = 2

        subtask.setId(999); // Меняем ID через сеттер

        assertTrue(epic.getSubtasksIds().contains(2), "Оригинальный ID подзадачи не сохранен в эпике");
        assertFalse(epic.getSubtasksIds().contains(999), "Новый ID подзадачи добавлен в эпик");
    }
}