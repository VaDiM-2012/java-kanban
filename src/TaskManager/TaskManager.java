package TaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import TasksAndEpics.*;

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
        subtask.setId(getCountId());
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    //Передача нового объекта вместе с id старого объекта
    public Task updateTask(Task task, int id) {
        tasks.replace(id, task);
        //Присваиваю id старого объекта новому объекту
        task.setId(id);
        return task;
    }

    public Epic updateEpic(Epic epic, int id) {
        epics.replace(id, epic);
        epic.setId(id);
        return epic;
    }

    public Subtask updateSubtask(Subtask subtask, int id) {
        subtasks.replace(id, subtask);
        subtask.setId(id);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }


    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        epics.get(id).removeSubtaskAll();
        epics.remove(id);
    }

    public void deleteSubtaskById(int id) {
        int epicId = subtasks.get(id).getEpicId();
        subtasks.remove(id);
        epics.get(epicId).removeSubtask(id);
        updateEpicStatus(epicId);
    }

    public void deleteTaskall() {
        tasks.clear();
    }

    public void deleteEpicall() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteSubtaskall() {
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


