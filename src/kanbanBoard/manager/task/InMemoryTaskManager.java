package kanbanBoard.manager.task;

import java.util.ArrayList;
import java.util.HashMap;

import kanbanBoard.manager.history.HistoryManager;
import kanbanBoard.model.*;

public class InMemoryTaskManager implements TaskManager {
    private int countId = 0;

    private int getCountId() {
        return ++countId;
    }

    private final HistoryManager viewHistory = Managers.getDefaultHistoryManager();

    //Структуры для хранения задач
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();




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
        if (!tasks.containsKey(id)) {
            return null;
        }
        viewHistory.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        if (!epics.containsKey(id)) {
            return null;
        }
        viewHistory.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            return null;
        }
        viewHistory.add(subtasks.get(id));
        return subtasks.get(id);
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
    }

    @Override
    public void deleteEpic(int id) {
        for (int taskId : epics.get(id).getSubtasksIds()) {
            subtasks.remove(taskId);
        }
        epics.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        int epicId = subtasks.get(id).getEpicId();
        subtasks.remove(id);
        epics.get(epicId).removeSubtask(id);
        updateEpicStatus(epicId);
    }

    @Override
    public void deleteTask() {
        tasks.clear();
    }

    @Override
    public void deleteEpic() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtask() {
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

    public ArrayList<Task> getHistory() {
        return viewHistory.getHistory();
    }
}


