import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int countId = 0;

    private int getCountId() {
        return ++countId;
    }

    //Структуры для хранения задач
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();


    public void createTask(Task task) {
        task.setId(getCountId());
        tasks.put(task.getId(), task);
    }

    public void createEpic(Epic epic) {
        epic.setId(getCountId());
        epics.put(epic.getId(), epic);
    }


    public void createSubtask(Subtask subtask) {
        subtask.setId(getCountId());
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
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

    public void updateTask(Task task, int id) {
        tasks.replace(id, task);
        task.setId(id);
    }

    public void updateEpic(Epic epic, int id) {
        epics.replace(id, epic);
        epic.setId(id);
    }

    public void updateSubtask(Subtask subtask, int id) {
        subtasks.replace(id, subtask);
        subtask.setId(id);
    }


    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        for (int taskId : epics.get(id).getSubtasksIds()) {
            deleteSubtaskById(taskId);
        }
        epics.remove(id);
    }

    public void deleteSubtaskById(int id) {
        subtasks.remove(id);
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
    }

    public ArrayList<Subtask> getAllSubtasksOfEpic(int id) {
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();
        for (int taskId : epics.get(id).getSubtasksIds()) {
            subtasksOfEpic.add(subtasks.get(taskId));
        }
        return subtasksOfEpic;
    }

    private void updateEpicStatus(int epicId) {
        ArrayList<Subtask> epicSubtasks = getAllSubtasksOfEpic(epicId);
        if (epicSubtasks.isEmpty()) {
            epics.get(epicId).setStatus(Status.NEW);
            return;
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
            epics.get(epicId).setStatus(Status.NEW);
        } else if (allDone) {
            epics.get(epicId).setStatus(Status.DONE);
        } else {
            epics.get(epicId).setStatus(Status.IN_PROGRESS);
        }
    }

    public void updateTaskStatus(Status status, int id) {
        Task task = tasks.get(id);
        if (task != null) {
            task.setStatus(status);
        }
    }

    public void updateSubtaskStatus(Status status, int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            subtask.setStatus(status);
            updateEpicStatus(subtask.getEpicId());
        }
    }


}


