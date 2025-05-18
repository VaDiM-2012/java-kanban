package kanbanBoard.manager.history;

import kanbanBoard.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void add_addCopyOfObject() {
        Task task = new Task("Заголовок", "Описание");
        task.setId(1);

        historyManager.add(task);
        task.setTitle("Новый заголовок");
        task.setDescription("Новое описание");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Дублирующий просмотр не удален");
        assertEquals("Новый заголовок", history.get(0).getTitle(), "Заголовок не обновлен");
        assertEquals("Новое описание", history.get(0).getDescription(), "Описание не обновлено");
    }

    @Test
    void add_maintainsOrder_multipleTasks() {
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание 3");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Неверный размер истории");
        assertEquals(1, history.get(0).getId(), "Неверный порядок: задача 1");
        assertEquals(2, history.get(1).getId(), "Неверный порядок: задача 2");
        assertEquals(3, history.get(2).getId(), "Неверный порядок: задача 3");
    }

    @Test
    void add_removesDuplicateView_sameTaskAddedTwice() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);

        historyManager.add(task);
        task.setTitle("Обновленная задача");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Дублирующий просмотр не удален");
        assertEquals("Обновленная задача", history.get(0).getTitle(), "Последний просмотр не сохранен");
    }

    @Test
    void remove_removesTaskFromHistory_taskExists() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);
        historyManager.add(task);

        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "Задача не удалена из истории");
    }

    @Test
    void remove_doesNothing_taskNotInHistory() {
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.remove(2); // Удаляем несуществующую задачу

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История изменена при удалении несуществующей задачи");
        assertEquals(1, history.get(0).getId(), "Неверная задача в истории");
    }

    @Test
    void getHistory_returnsEmptyList_historyIsEmpty() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История не пуста при инициализации");
    }

    @Test
    void addAndRemove_multipleTasks_maintainsCorrectOrder() {
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание 3");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2); // Удаляем задачу 2

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверный размер истории после удаления");
        assertEquals(1, history.get(0).getId(), "Неверный порядок: задача 1");
        assertEquals(3, history.get(1).getId(), "Неверный порядок: задача 3");
    }
}