package kanbanboard.manager.history;

import kanbanboard.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void add_addsCopyOfTask() {
        Task task = new Task("Task 1", "Description");
        task.setId(1);
        historyManager.add(task);
        task.setTitle("Modified Task");

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals("Task 1", history.get(0).getTitle(), "Задача должна быть копией оригинала");
    }

    @Test
    void add_removesDuplicate_maintainsLatest() {
        Task task = new Task("Task 1", "Description");
        task.setId(1);
        historyManager.add(task);
        task.setTitle("Updated Task");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Дубликат не удален");
        assertEquals("Updated Task", history.get(0).getTitle(), "Последняя версия задачи не сохранена");
    }

    @Test
    void getHistory_emptyHistory_returnsEmptyList() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История не пуста при инициализации");
    }

    @Test
    void remove_fromBeginning_maintainsOrder() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверный размер истории после удаления");
        assertEquals(2, history.get(0).getId(), "Неверный порядок после удаления из начала");
        assertEquals(3, history.get(1).getId(), "Неверный порядок после удаления из начала");
    }

    @Test
    void remove_fromMiddle_maintainsOrder() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверный размер истории после удаления");
        assertEquals(1, history.get(0).getId(), "Неверный порядок после удаления из середины");
        assertEquals(3, history.get(1).getId(), "Неверный порядок после удаления из середины");
    }

    @Test
    void remove_fromEnd_maintainsOrder() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверный размер истории после удаления");
        assertEquals(1, history.get(0).getId(), "Неверный порядок после удаления с конца");
        assertEquals(2, history.get(1).getId(), "Неверный порядок после удаления с конца");
    }

    @Test
    void remove_nonExistentTask_doesNothing() {
        Task task = new Task("Task 1", "Description");
        task.setId(1);
        historyManager.add(task);
        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История изменена при удалении несуществующей задачи");
        assertEquals(1, history.get(0).getId(), "Неверная задача в истории");
    }
}