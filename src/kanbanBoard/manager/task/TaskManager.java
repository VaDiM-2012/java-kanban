package kanbanBoard.manager.task;

import kanbanBoard.model.Epic;
import kanbanBoard.model.Subtask;
import kanbanBoard.model.Task;

import java.util.ArrayList;

public interface TaskManager {
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    ArrayList<Task> getTask();

    ArrayList<Epic> getEpic();

    ArrayList<Subtask> getSubtask();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    void deleteTask();

    void deleteEpic();

    void deleteSubtask();

    ArrayList<Subtask> getAllSubtasksOfEpic(int id);

    ArrayList<Task> getHistory();
}
