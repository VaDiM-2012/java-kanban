package kanbanboard;

import kanbanboard.manager.Managers;
import kanbanboard.manager.task.TaskManager;
import kanbanboard.model.Epic;
import kanbanboard.model.Subtask;
import kanbanboard.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistoryManager());

        // Создаем задачи с временем и длительностью
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        task1.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        task2.setStartTime(LocalDateTime.of(2025, 6, 10, 12, 0));
        task2.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1); // ID = 1
        manager.createTask(task2); // ID = 2

        // Создаем эпик с подзадачами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1); // ID = 3
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", 3);
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 10, 14, 0));
        subtask1.setDuration(Duration.ofMinutes(45));
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", 3);
        subtask2.setStartTime(LocalDateTime.of(2025, 6, 10, 15, 0));
        subtask2.setDuration(Duration.ofMinutes(60));
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", 3);
        subtask3.setStartTime(LocalDateTime.of(2025, 6, 10, 16, 30));
        subtask3.setDuration(Duration.ofMinutes(90));
        manager.createSubtask(subtask1); // ID = 4
        manager.createSubtask(subtask2); // ID = 5
        manager.createSubtask(subtask3); // ID = 6

        // Создаем эпик без подзадач
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.createEpic(epic2); // ID = 7

        // Выводим список задач по приоритету
        System.out.println("\nСписок задач по приоритету:");
        for (Task task : manager.getPrioritizedTasks()) {
            System.out.println("  ID=" + task.getId() + ", Тип=" + task.getClass().getSimpleName() +
                    ", Название=" + task.getTitle() + ", Начало=" + task.getStartTime() +
                    ", Конец=" + task.getEndTime());
        }

        // Запрашиваем задачи и выводим историю
        System.out.println("\nЗапрос задач в порядке: Задача 1, Эпик 1, Подзадача 2, Эпик 2");
        manager.getTask(1);
        manager.getEpic(3);
        manager.getSubtask(5);
        manager.getEpic(7);
        printHistory(manager.getHistory());

        // Проверяем пересечение (попытка добавить пересекающуюся задачу)
        Task overlappingTask = new Task("Пересекающаяся задача", "Описание");
        overlappingTask.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 30));
        overlappingTask.setDuration(Duration.ofMinutes(60));
        try {
            manager.createTask(overlappingTask);
        } catch (IllegalArgumentException e) {
            System.out.println("\nОшибка: " + e.getMessage());
        }

        // Удаляем задачу и проверяем историю
        System.out.println("\nУдаляем Задачу 2 (ID=2)");
        manager.deleteTask(2);
        printHistory(manager.getHistory());

        // Удаляем эпик с подзадачами
        System.out.println("\nУдаляем Эпик 1 (ID=3) с подзадачами");
        manager.deleteEpic(3);
        printHistory(manager.getHistory());
    }

    private static void printHistory(List<Task> history) {
        System.out.println("История просмотров:");
        if (history.isEmpty()) {
            System.out.println("  (пусто)");
        } else {
            for (Task task : history) {
                System.out.println("  ID=" + task.getId() + ", Тип=" + task.getClass().getSimpleName() +
                        ", Название=" + task.getTitle() + ", Начало=" + task.getStartTime());
            }
        }
    }
}