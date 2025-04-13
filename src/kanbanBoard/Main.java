package kanbanBoard;

import kanbanBoard.manager.task.TaskManager;
import kanbanBoard.model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
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

        System.out.println(manager.getTask());
        System.out.println(manager.getEpic());
        System.out.println(manager.getSubtask());

    }
}
