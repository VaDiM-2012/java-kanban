package kanbanBoard.manager.history;

import kanbanBoard.manager.task.InMemoryTaskManager;
import kanbanBoard.model.Epic;
import kanbanBoard.model.Subtask;
import kanbanBoard.model.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    @Test
    void add_addCopyOfObject() {

        //Начальные данные
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Заголовок", "Описание");

        //Добавление задачи в историю и ее изменение
        historyManager.add(task);
        task.setTitle("Новый заголовок");
        task.setDescription("Новое описание");
        historyManager.add(task);

        //Проверка неизменности задачи в истории после обновления
        List<Task> history = historyManager.getHistory();
        assertNotEquals(history.get(0).getTitle(), history.get(1).getTitle(),"Заголвки должны быть разными");
        assertNotEquals(history.get(0).getDescription(), history.get(1).getDescription(),"Описания должны быть разными");
    }

    @Test
    public void add_removeFirstElement_elementsCountMoreThanTen() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Заголовок", "Описание");

        // Добавляем задачу в историю 11 раз
        for (int i = 0; i < 11; i++) {
            historyManager.add(task);
        }

        // Получаем историю и проверяем её размер
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "Размер истории не соответствует ожидаемому значению");
    }


}