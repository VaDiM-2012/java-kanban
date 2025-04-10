

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

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());
        System.out.println();

        manager.updateTaskStatus(Status.DONE, 1);
        manager.updateTaskStatus(Status.IN_PROGRESS, 2);
        manager.updateSubtaskStatus(Status.DONE, 4);
        manager.updateSubtaskStatus(Status.IN_PROGRESS, 5);
        manager.updateSubtaskStatus(Status.DONE, 7);

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());
        System.out.println();

        manager.deleteTaskById(1);
        manager.deleteEpicById(3);

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());

    }
}
