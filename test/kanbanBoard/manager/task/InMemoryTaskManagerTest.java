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
    void null_returnNull_subtaskIdIsNull() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        //Создаём подзадачу
        Subtask subtask = new Subtask("Создать тестовую подзадачу", "Описание тестовой подзадачи", 1);
        subtask.setId(1);

        //Проверяем, что подзадачу нельзя сделать своим Эпиком
        assertNull(subtask.getId());
    }

    @Test
    void null_returnNotNull_getTaskAndGetEpicAndGetSubtaskIsNotNull() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        //Создаём Задачу
        Task task = new Task("Создать тестовую Задачу", "Описание тестовой Задачи");
        manager.createTask(task);

        //Создаём Эпик
        Epic epic = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic);

        //Создаём подзадачу
        Subtask subtask = new Subtask("Создать тестовую подзадачу", "Описание тестовой подзадачи", 2);
        manager.createSubtask(subtask);

        assertNotNull(manager.getTask(1), "Задача не найдена");
        assertNotNull(manager.getEpic(2), "Эпик не найден");
        assertNotNull(manager.getSubtask(3), "Подзадача не найдена");
    }

    @Test
    void null_returnNotNull_updateTaskAndUpdateEpicAndUpdateSubtaskIsNotNull() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        //Создаём Задачу 1
        Task task1 = new Task("Создать тестовую Задачу", "Описание тестовой Задачи");
        manager.createTask(task1);

        //Создаём Задачу 2
        Task task2 = new Task("Создать тестовую Задачу 2", "Описание тестовой Задачи 2");
        task2.setId(1);

        //Заменяем Задачу 1 на Задачу 2
        assertNotNull(manager.updateTask(task2));
        assertEquals(task2.getTitle(), manager.getTask(1).getTitle());

        manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        // Создаём Эпик 1
        Epic epic1 = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic1);

        // Создаём Эпик 2
        Epic epic2 = new Epic("Создать тестовый Эпик 2", "Описание тестового Эпика 2");
        epic2.setId(1);

        // Заменяем Эпик 1 на Эпик 2
        assertNotNull(manager.updateEpic(epic2));
        assertEquals(epic2.getTitle(), manager.getEpic(1).getTitle());

        // Создаём Подзадачу 1
        Subtask subtask1 = new Subtask("Создать тестовую Подзадачу", "Описание тестовой Подзадачи", 1);
        manager.createSubtask(subtask1);

        // Создаём Подзадачу 2
        Subtask subtask2 = new Subtask("Создать тестовую Подзадачу 2", "Описание тестовой Подзадачи 2", 1);
        subtask2.setId(2);

        // Заменяем Подзадачу 1 на Подзадачу 2
        assertNotNull(manager.updateSubtask(subtask2));
        assertEquals(subtask2.getTitle(), manager.getSubtask(2).getTitle());
    }

    @Test
    void equals_returnTrue_TaskAndEpicAndSubtaskAreSame() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        //Создаём Задачу
        Task task = new Task("Создать тестовую Задачу", "Описание тестовой Задачи");
        manager.createTask(task);

        //Проверяем, что менеджер не изменит Задачу
        assertEquals(task.getTitle(), manager.getTask(1).getTitle());
        assertEquals(task.getDescription(), manager.getTask(1).getDescription());

        manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        // Создаём Эпик
        Epic epic = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        manager.createEpic(epic);

        // Проверяем, что менеджер не изменит Эпик
        assertEquals(epic.getTitle(), manager.getEpic(1).getTitle());
        assertEquals(epic.getDescription(), manager.getEpic(1).getDescription());

        //Проверяем пустой список подзадач
        assertTrue(epic.getSubtasksIds().equals(manager.getEpic(1).getSubtasksIds()));

        // Создаём Подзадачу
        Subtask subtask = new Subtask("Создать тестовую Подзадачу", "Описание тестовой Подзадачи", 1);
        manager.createSubtask(subtask);

        // Проверяем, что менеджер не изменит Подзадачу
        assertEquals(subtask.getTitle(), manager.getSubtask(2).getTitle());
        assertEquals(subtask.getDescription(), manager.getSubtask(2).getDescription());

        //Проверяем не пустой список подзадач
        assertTrue(epic.getSubtasksIds().equals(manager.getEpic(1).getSubtasksIds()));
    }
}
