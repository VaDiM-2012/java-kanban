package kanbanBoard;

import kanbanBoard.manager.Managers;
import kanbanBoard.manager.task.TaskManager;
import kanbanBoard.model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistoryManager());
        manager.createTask(new Task("Название задачи 1", "Описание задачи 1"));
        manager.createTask(new Task("Название задачи 2", "Описание задачи 2"));
        manager.createEpic(new Epic("Название эпика 1", "Описание эпика 1"));
        manager.createSubtask(new Subtask("Название подзадачи 1", "Описание подзадачи 1", 3));
        manager.createSubtask(new Subtask("Название подзадачи 2", "Описание подзадачи 2", 3));
        manager.createEpic(new Epic("Название эпика 2", "Описание эпика 2"));
        manager.createSubtask(new Subtask("Название подзадачи 3", "Описание подзадачи 3", 6));

        System.out.println(manager.getTask());
        System.out.println(manager.getEpic());
        System.out.println(manager.getSubtask());
        System.out.println();

        manager.updateTask(new Task("Название задачи 1", "Описание задачи 1", Status.DONE, 1));
        manager.updateTask(new Task("Название задачи 2", "Описание задачи 2",Status.IN_PROGRESS, 2));
        manager.updateSubtask(new Subtask("Название подзадачи 1", "Описание подзадачи 1", 3, Status.DONE, 4));
        manager.updateSubtask(new Subtask("Название подзадачи 2", "Описание подзадачи 2", 3,Status.IN_PROGRESS, 5));
        manager.updateSubtask(new Subtask("Название подзадачи 3", "Описание подзадачи 3", 6, Status.DONE, 7));

        System.out.println(manager.getTask());
        System.out.println(manager.getEpic());
        System.out.println(manager.getSubtask());
        System.out.println();

        manager.deleteTask(1);
        manager.deleteEpic(3);

        System.out.println(manager.getTask(2));
        System.out.println(manager.getEpic(5));
        System.out.println(manager.getSubtask(6));

        printAllTasks(manager);

    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTask()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpic()) {
            System.out.println(epic);

            for (Task task : manager.getAllSubtasksOfEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtask()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
