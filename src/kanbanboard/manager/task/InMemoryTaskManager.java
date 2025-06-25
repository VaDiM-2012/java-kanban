package kanbanboard.manager.task;

import kanbanboard.manager.history.HistoryManager;
import kanbanboard.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int countId = 0;
    protected final HistoryManager viewHistory;
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
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

    protected int getCountId() {
        return ++countId;
    }

    protected void checkTaskOverlapping(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return;
        }
        boolean isOverlapping = prioritizedTasks.stream()
                .anyMatch(t -> isOverlapping(task, t));
        if (isOverlapping) {
            throw new IllegalArgumentException("Задача пересекается с другой задачей по времени");
        }
    }

    private boolean isOverlapping(Task task1, Task task2) {
        if (task1.getId() != null && task2.getId() != null && task1.getId().equals(task2.getId())) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();
        return start1 != null && end1 != null && start2 != null && end2 != null &&
                !end1.isBefore(start2) && !end2.isBefore(start1);
    }

    @Override
    public Task createTask(Task task) {
        checkTaskOverlapping(task);
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
            throw new NotFoundException("Эпик с ID " + subtask.getEpicId() + " не найден");
        }
        checkTaskOverlapping(subtask);
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
    public Task updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new NotFoundException("Задача с ID " + task.getId() + " не найдена");
        }
        checkTaskOverlapping(task);
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
            throw new NotFoundException("Эпик с ID " + epic.getId() + " не найден");
        }
        epics.replace(epic.getId(), epic);
        updateEpicTimeFields(epic.getId());
        updateEpicStatus(epic.getId());
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new NotFoundException("Подзадача с ID " + subtask.getId() + " не найдена");
        }
        checkTaskOverlapping(subtask);
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
        if (task == null) {
            throw new NotFoundException("Задача с ID " + id + " не найдена");
        }
        prioritizedTasks.remove(task);
        tasks.remove(id);
        viewHistory.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с ID " + id + " не найден");
        }
        for (int taskId : epic.getSubtasksIds()) {
            prioritizedTasks.remove(subtasks.get(taskId));
            subtasks.remove(taskId);
            viewHistory.remove(taskId);
        }
        epics.remove(id);
        viewHistory.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с ID " + id + " не найдена");
        }
        int epicId = subtask.getEpicId();
        prioritizedTasks.remove(subtask);
        subtasks.remove(id);
        epics.get(epicId).removeSubtask(id);
        updateEpicTimeFields(epicId);
        updateEpicStatus(epicId);
        viewHistory.remove(id);
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
    public ArrayList<Subtask> getAllSubtasksOfEpic(int id) throws NotFoundException {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с ID " + id + " не найден");
        }
        return epic.getSubtasksIds().stream()
                .map(subtasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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
        ArrayList<Subtask> epicSubtasks;
        try {
            epicSubtasks = getAllSubtasksOfEpic(epicId);
        } catch (NotFoundException e) {
            return; // Эпик не найден, пропускаем обновление
        }
        Status epicStatus = calculateEpicStatus(epicSubtasks);
        epics.get(epicId).setStatus(epicStatus);
    }

    protected void updateEpicTimeFields(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        ArrayList<Subtask> subtasks;
        try {
            subtasks = getAllSubtasksOfEpic(epicId);
        } catch (NotFoundException e) {
            return; // Эпик не найден, пропускаем обновление
        }
        if (subtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        long totalMinutes = 0;

        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() != null) {
                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }
                LocalDateTime subtaskEnd = subtask.getEndTime();
                if (subtaskEnd != null && (latestEnd == null || subtaskEnd.isAfter(latestEnd))) {
                    latestEnd = subtaskEnd;
                }
                totalMinutes += subtask.getDuration().toMinutes();
            }
        }

        epic.setStartTime(earliestStart);
        epic.setDuration(Duration.ofMinutes(totalMinutes));
        epic.setEndTime(latestEnd);
    }

    @Override
    public List<Task> getHistory() {
        return viewHistory.getHistory();
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
    public Task getTask(int id) throws NotFoundException {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задача с ID " + id + " не найдена");
        }
        viewHistory.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) throws NotFoundException {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с ID " + id + " не найден");
        }
        viewHistory.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) throws NotFoundException {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с ID " + id + " не найдена");
        }
        viewHistory.add(subtask);
        return subtask;
    }
}