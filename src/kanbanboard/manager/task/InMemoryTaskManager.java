package kanbanboard.manager.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kanbanboard.manager.history.HistoryManager;
import kanbanboard.model.*;

public class InMemoryTaskManager implements TaskManager {
    private int countId = 0;

    private int getCountId() {
        return ++countId;
    }

    private final HistoryManager viewHistory;

    //Структуры для хранения задач
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public InMemoryTaskManager(HistoryManager viewHistory) {
        this.viewHistory = viewHistory;
    }


    @Override
    public Task createTask(Task task) {
        task.setId(getCountId());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getCountId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
        return epic;
    }


    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            return null;
        }
        subtask.setId(getCountId());
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public ArrayList<Task> getTask() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpic() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtask() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id); // Считываем значение один раз
        if (task == null) {        // Проверяем, существует ли задача
            return null;
        }
        viewHistory.add(task);     // Используем сохраненное значение
        return task;               // Возвращаем сохраненное значение
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        viewHistory.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return null;
        }
        viewHistory.add(subtask);
        return subtask;
    }


    @Override
    public Task updateTask(Task task) {
        tasks.replace(task.getId(), task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return null;
        }
        epics.replace(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return null;
        }
        subtasks.replace(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }


    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        viewHistory.remove(id); // Удаляем из истории
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (int taskId : epic.getSubtasksIds()) {
                subtasks.remove(taskId);
                viewHistory.remove(taskId); // Удаляем подзадачи из истории
            }
            epics.remove(id);
            viewHistory.remove(id); // Удаляем эпик из истории
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            subtasks.remove(id);
            epics.get(epicId).removeSubtask(id);
            updateEpicStatus(epicId);
            viewHistory.remove(id); // Удаляем подзадачу из истории
        }
    }

    @Override
    public void deleteTask() {
        for (Integer id : tasks.keySet()) {
            viewHistory.remove(id); // Удаляем все задачи из истории
        }
        tasks.clear();
    }

    @Override
    public void deleteEpic() {
        for (Epic epic : epics.values()) {
            for (Integer taskId : epic.getSubtasksIds()) {
                subtasks.remove(taskId);
                viewHistory.remove(taskId); // Удаляем подзадачи из истории
            }
            viewHistory.remove(epic.getId()); // Удаляем эпик из истории
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtask() {
        for (Integer id : subtasks.keySet()) {
            viewHistory.remove(id); // Удаляем подзадачи из истории
        }
        subtasks.clear();
        for (int i : epics.keySet()) {
            epics.get(i).removeSubtaskAll();
            updateEpicStatus(i);
        }
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksOfEpic(int id) {
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();
        for (int taskId : epics.get(id).getSubtasksIds()) {
            subtasksOfEpic.add(subtasks.get(taskId));
        }
        return subtasksOfEpic;
    }

    private Status calculateEpicStatus(ArrayList<Subtask> epicSubtasks) {
        if (epicSubtasks.isEmpty()) {
            return Status.NEW;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            return Status.NEW;
        } else if (allDone) {
            return Status.DONE;
        } else {
            return Status.IN_PROGRESS;
        }
    }

    private void updateEpicStatus(int epicId) {
        ArrayList<Subtask> epicSubtasks = getAllSubtasksOfEpic(epicId);
        Status epicStatus = calculateEpicStatus(epicSubtasks);
        epics.get(epicId).setStatus(epicStatus);
    }

    public List<Task> getHistory() {
        return viewHistory.getHistory();
    }
}


