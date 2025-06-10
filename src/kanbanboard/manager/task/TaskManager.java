package kanbanboard.manager.task;

import kanbanboard.model.Epic;
import kanbanboard.model.Subtask;
import kanbanboard.model.Task;

import java.util.ArrayList;
import java.util.List;

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

    List<Task> getHistory();

    List<Task> getPrioritizedTasks(); // Новый метод для получения отсортированного списка задач

    boolean isTaskOverlapping(Task task); // Проверка пересечения задачи с другими
}