package kanbanBoard.manager.task;

import java.util.ArrayList;
import java.util.HashMap;
import kanbanBoard.model.*;

public class TaskManager {
    private int countId = 0;

    private int getCountId() {
        return ++countId;
    }

    //Структуры для хранения задач
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();


    public Task createTask(Task task) {
        task.setId(getCountId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(getCountId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
        return epic;
    }


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

    public ArrayList<Task> getTask() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpic() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtask() {
        return new ArrayList<>(subtasks.values());
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }


    public Task updateTask(Task task) {
        tasks.replace(task.getId(), task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        epics.replace(epic.getId(), epic);
        return epic;
    }

    public Subtask updateSubtask(Subtask subtask) {
        subtasks.replace(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }


    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        for (int taskId : epics.get(id).getSubtasksIds()) {
            subtasks.remove(taskId);
        }
        epics.remove(id);
    }

    public void deleteSubtask(int id) {
        int epicId = subtasks.get(id).getEpicId();
        subtasks.remove(id);
        epics.get(epicId).removeSubtask(id);
        updateEpicStatus(epicId);
    }

    public void deleteTask() {
        tasks.clear();
    }

    public void deleteEpic() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteSubtask() {
        subtasks.clear();
        for (int i : epics.keySet()) {
            epics.get(i).removeSubtaskAll();
            updateEpicStatus(i);
        }
    }

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
}


