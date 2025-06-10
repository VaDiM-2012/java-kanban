package kanbanboard.manager.task;

import kanbanboard.manager.history.HistoryManager;
import kanbanboard.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int countId = 0;
    protected final HistoryManager viewHistory;
    protected final HashMap<Integer, Task> tasks = new HashMap<>(); // Исправлено с HashHashMap
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null && t2.getStartTime() == null) return t1.getId() - t2.getId();
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;
        return t1.getStartTime().compareTo(t2.getStartTime());
    });

    public InMemoryTaskManager(HistoryManager viewHistory) {
        this.viewHistory = viewHistory;
    }

    // Остальной код без изменений
    protected int getCountId() {
        return ++countId;
    }

    @Override
    public Task createTask(Task task) {
        if (task.getStartTime() != null && isTaskOverlapping(task)) {
            throw new IllegalArgumentException("Задача пересекается с другой задачей по времени");
        }
        task.setId(getCountId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
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
        if (subtask.getStartTime() != null && isTaskOverlapping(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается с другой задачей по времени");
        }
        subtask.setId(getCountId());
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        updateEpicTimeFields(subtask.getEpicId());
        updateEpicStatus(subtask.getEpicId());
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
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
        Task task = tasks.get(id);
        if (task == null) {
            return null;
        }
        viewHistory.add(task);
        return task;
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
        if (!tasks.containsKey(task.getId())) {
            return null;
        }
        if (task.getStartTime() != null && isTaskOverlapping(task)) {
            throw new IllegalArgumentException("Задача пересекается с другой задачей по времени");
        }
        prioritizedTasks.remove(tasks.get(task.getId()));
        tasks.replace(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return null;
        }
        epics.replace(epic.getId(), epic);
        updateEpicTimeFields(epic.getId());
        updateEpicStatus(epic.getId());
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return null;
        }
        if (subtask.getStartTime() != null && isTaskOverlapping(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается с другой задачей по времени");
        }
        prioritizedTasks.remove(subtasks.get(subtask.getId()));
        subtasks.replace(subtask.getId(), subtask);
        updateEpicTimeFields(subtask.getEpicId());
        updateEpicStatus(subtask.getEpicId());
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        return subtask;
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            tasks.remove(id);
            viewHistory.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (int taskId : epic.getSubtasksIds()) {
                prioritizedTasks.remove(subtasks.get(taskId));
                subtasks.remove(taskId);
                viewHistory.remove(taskId);
            }
            epics.remove(id);
            viewHistory.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            prioritizedTasks.remove(subtask);
            subtasks.remove(id);
            epics.get(epicId).removeSubtask(id);
            updateEpicTimeFields(epicId);
            updateEpicStatus(epicId);
            viewHistory.remove(id);
        }
    }

    @Override
    public void deleteTask() {
        tasks.values().forEach(prioritizedTasks::remove);
        for (Integer id : tasks.keySet()) {
            viewHistory.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void deleteEpic() {
        for (Epic epic : epics.values()) {
            for (Integer taskId : epic.getSubtasksIds()) {
                prioritizedTasks.remove(subtasks.get(taskId));
                subtasks.remove(taskId);
                viewHistory.remove(taskId);
            }
            viewHistory.remove(epic.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtask() {
        subtasks.values().forEach(prioritizedTasks::remove);
        for (Integer id : subtasks.keySet()) {
            viewHistory.remove(id);
        }
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.removeSubtaskAll();
            updateEpicTimeFields(epic.getId());
            updateEpicStatus(epic.getId());
        });
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksOfEpic(int id) {
        return epics.get(id).getSubtasksIds().stream()
                .map(subtasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean isTaskOverlapping(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }
        return prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .anyMatch(t -> isOverlapping(task, t));
    }

    private boolean isOverlapping(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();
        return !end1.isBefore(start2) && !end2.isBefore(start1);
    }

    private Status calculateEpicStatus(ArrayList<Subtask> epicSubtasks) {
        if (epicSubtasks.isEmpty()) {
            return Status.NEW;
        }
        boolean allNew = epicSubtasks.stream().allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = epicSubtasks.stream().allMatch(s -> s.getStatus() == Status.DONE);
        return allNew ? Status.NEW : allDone ? Status.DONE : Status.IN_PROGRESS;
    }

    protected void updateEpicStatus(int epicId) {
        ArrayList<Subtask> epicSubtasks = getAllSubtasksOfEpic(epicId);
        Status epicStatus = calculateEpicStatus(epicSubtasks);
        epics.get(epicId).setStatus(epicStatus);
    }

    protected void updateEpicTimeFields(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.updateTimeFields(getAllSubtasksOfEpic(epicId));
        }
    }

    @Override
    public List<Task> getHistory() {
        return viewHistory.getHistory();
    }
}