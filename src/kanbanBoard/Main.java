package kanbanBoard;

import kanbanBoard.manager.Managers;
import kanbanBoard.manager.task.TaskManager;
import kanbanBoard.model.Epic;
import kanbanBoard.model.Subtask;
import kanbanBoard.model.Task;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Создаем менеджер задач
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistoryManager());

        // Шаг 1: Создаем две задачи
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        manager.createTask(task1); // ID = 1
        manager.createTask(task2); // ID = 2

        // Создаем эпик с тремя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1); // ID = 3
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", 3);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", 3);
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", 3);
        manager.createSubtask(subtask1); // ID = 4
        manager.createSubtask(subtask2); // ID = 5
        manager.createSubtask(subtask3); // ID = 6

        // Создаем эпик без подзадач
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.createEpic(epic2); // ID = 7

        // Шаг 2: Запрашиваем задачи в разном порядке и выводим историю
        System.out.println("Запрос задач в порядке: Задача 1, Эпик 1, Подзадача 2, Эпик 2");
        manager.getTask(1);
        manager.getEpic(3);
        manager.getSubtask(5);
        manager.getEpic(7);
        printHistory(manager.getHistory());

        System.out.println("\nПовторный запрос: Подзадача 2, Задача 2, Эпик 1");
        manager.getSubtask(5); // Повторный запрос, должен обновить позицию в истории
        manager.getTask(2);
        manager.getEpic(3); // Повторный запрос
        printHistory(manager.getHistory());

        System.out.println("\nЗапрос: Задача 1, Подзадача 1");
        manager.getTask(1); // Повторный запрос
        manager.getSubtask(4);
        printHistory(manager.getHistory());

        // Шаг 3: Удаляем задачу (Задача 2) и проверяем историю
        System.out.println("\nУдаляем Задачу 2 (ID=2)");
        manager.deleteTask(2);
        printHistory(manager.getHistory());

        // Шаг 4: Удаляем эпик с подзадачами (Эпик 1) и проверяем историю
        System.out.println("\nУдаляем Эпик 1 (ID=3) с подзадачами");
        manager.deleteEpic(3);
        printHistory(manager.getHistory());
    }

    // Метод для вывода истории
    private static void printHistory(List<Task> history) {
        System.out.println("История просмотров:");
        if (history.isEmpty()) {
            System.out.println("  (пусто)");
        } else {
            for (Task task : history) {
                System.out.println("  ID=" + task.getId() + ", Тип=" + task.getClass().getSimpleName() + ", Название=" + task.getTitle());
            }
        }
    }
}