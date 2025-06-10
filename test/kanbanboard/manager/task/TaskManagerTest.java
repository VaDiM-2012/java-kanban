package kanbanboard.manager.task;

import kanbanboard.manager.history.HistoryManager;
import kanbanboard.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    protected HistoryManager historyManager;

    @BeforeEach
    public abstract void setUp();

    @Test
    void createTask_createsAndStoresTask() {
        Task task = new Task("Task 1", "Description");
        task.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        task.setDuration(Duration.ofMinutes(60));
        manager.createTask(task);

        Task retrieved = manager.getTask(task.getId());
        assertNotNull(retrieved, "Задача не создана");
        assertEquals("Task 1", retrieved.getTitle(), "Неверное название задачи");
        assertEquals(Duration.ofMinutes(60), retrieved.getDuration(), "Неверная длительность");
        assertEquals(LocalDateTime.of(2025, 6, 10, 10, 0), retrieved.getStartTime(), "Неверное время начала");
    }

    @Test
    void createEpic_createsAndStoresEpic() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);

        Epic retrieved = manager.getEpic(epic.getId());
        assertNotNull(retrieved, "Эпик не создан");
        assertEquals("Epic 1", retrieved.getTitle(), "Неверное название эпика");
        assertEquals(Status.NEW, retrieved.getStatus(), "Неверный статус эпика");
    }

    @Test
    void createSubtask_createsAndStoresSubtask_withValidEpic() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description", epic.getId());
        subtask.setStartTime(LocalDateTime.of(2025, 6, 10, 12, 0));
        subtask.setDuration(Duration.ofMinutes(30));
        manager.createSubtask(subtask);

        Subtask retrieved = manager.getSubtask(subtask.getId());
        assertNotNull(retrieved, "Подзадача не создана");
        assertEquals("Subtask 1", retrieved.getTitle(), "Неверное название подзадачи");
        assertEquals(epic.getId(), retrieved.getEpicId(), "Неверный ID эпика");
        assertEquals(Duration.ofMinutes(30), retrieved.getDuration(), "Неверная длительность");
        assertEquals(LocalDateTime.of(2025, 6, 10, 12, 0), retrieved.getStartTime(), "Неверное время начала");
    }

    @Test
    void createSubtask_returnsNull_invalidEpicId() {
        Subtask subtask = new Subtask("Subtask 1", "Description", 999);
        assertNull(manager.createSubtask(subtask), "Подзадача создана с несуществующим эпиком");
    }

    @Test
    void getTask_returnsTask_taskExists() {
        Task task = new Task("Task 1", "Description");
        manager.createTask(task);
        assertNotNull(manager.getTask(task.getId()), "Задача не найдена");
    }

    @Test
    void getTask_returnsNull_taskDoesNotExist() {
        assertNull(manager.getTask(999), "Найдена несуществующая задача");
    }

    @Test
    void getEpic_returnsEpic_epicExists() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        assertNotNull(manager.getEpic(epic.getId()), "Эпик не найден");
    }

    @Test
    void getSubtask_returnsSubtask_subtaskExists() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description", epic.getId());
        manager.createSubtask(subtask);
        assertNotNull(manager.getSubtask(subtask.getId()), "Подзадача не найдена");
    }

    @Test
    void updateTask_updatesFields_taskExists() {
        Task task = new Task("Task 1", "Description");
        manager.createTask(task);
        Task updatedTask = new Task("Updated Task", "New Description");
        updatedTask.setId(task.getId());
        updatedTask.setStartTime(LocalDateTime.of(2025, 6, 10, 11, 0));
        updatedTask.setDuration(Duration.ofMinutes(90));
        manager.updateTask(updatedTask);

        Task retrieved = manager.getTask(task.getId());
        assertEquals("Updated Task", retrieved.getTitle(), "Название задачи не обновлено");
        assertEquals(Duration.ofMinutes(90), retrieved.getDuration(), "Длительность не обновлена");
        assertEquals(LocalDateTime.of(2025, 6, 10, 11, 0), retrieved.getStartTime(), "Время начала не обновлено");
    }

    @Test
    void updateEpic_updatesFields_epicExists() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Epic updatedEpic = new Epic("Updated Epic", "New Description");
        updatedEpic.setId(epic.getId());
        manager.updateEpic(updatedEpic);

        Epic retrieved = manager.getEpic(epic.getId());
        assertEquals("Updated Epic", retrieved.getTitle(), "Название эпика не обновлено");
    }

    @Test
    void updateSubtask_updatesFields_subtaskExists() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description", epic.getId());
        manager.createSubtask(subtask);
        Subtask updatedSubtask = new Subtask("Updated Subtask", "New Description", epic.getId());
        updatedSubtask.setId(subtask.getId());
        updatedSubtask.setStartTime(LocalDateTime.of(2025, 6, 10, 13, 0));
        updatedSubtask.setDuration(Duration.ofMinutes(45));
        manager.updateSubtask(updatedSubtask);

        Subtask retrieved = manager.getSubtask(subtask.getId());
        assertEquals("Updated Subtask", retrieved.getTitle(), "Название подзадачи не обновлено");
        assertEquals(Duration.ofMinutes(45), retrieved.getDuration(), "Длительность не обновлена");
    }

    @Test
    void deleteTask_removesTask_taskExists() {
        Task task = new Task("Task 1", "Description");
        manager.createTask(task);
        manager.deleteTask(task.getId());
        assertNull(manager.getTask(task.getId()), "Задача не удалена");
    }

    @Test
    void deleteEpic_removesEpicAndSubtasks_epicExists() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description", epic.getId());
        manager.createSubtask(subtask);
        manager.deleteEpic(epic.getId());

        assertNull(manager.getEpic(epic.getId()), "Эпик не удален");
        assertNull(manager.getSubtask(subtask.getId()), "Подзадача не удалена");
    }

    @Test
    void deleteSubtask_removesSubtask_subtaskExists() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description", epic.getId());
        manager.createSubtask(subtask);
        manager.deleteSubtask(subtask.getId());

        assertNull(manager.getSubtask(subtask.getId()), "Подзадача не удалена");
        assertFalse(manager.getEpic(epic.getId()).getSubtasksIds().contains(subtask.getId()), "Подзадача осталась в эпике");
    }

    @Test
    void deleteTask_clearsAllTasks() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        manager.createTask(task1);
        manager.createTask(task2);
        manager.deleteTask();
        assertTrue(manager.getTask().isEmpty(), "Задачи не удалены");
    }

    @Test
    void deleteEpic_clearsAllEpicsAndSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description", epic.getId());
        manager.createSubtask(subtask);
        manager.deleteEpic();

        assertTrue(manager.getEpic().isEmpty(), "Эпики не удалены");
        assertTrue(manager.getSubtask().isEmpty(), "Подзадачи не удалены");
    }

    @Test
    void deleteSubtask_clearsAllSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description", epic.getId());
        manager.createSubtask(subtask);
        manager.deleteSubtask();

        assertTrue(manager.getSubtask().isEmpty(), "Подзадачи не удалены");
        assertTrue(manager.getEpic(epic.getId()).getSubtasksIds().isEmpty(), "Подзадачи остались в эпике");
    }

    @Test
    void getAllSubtasksOfEpic_returnsCorrectSubtasks_epicExists() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        ArrayList<Subtask> subtasks = manager.getAllSubtasksOfEpic(epic.getId());
        assertEquals(2, subtasks.size(), "Неверное количество подзадач");
        assertTrue(subtasks.contains(subtask1), "Подзадача 1 отсутствует");
        assertTrue(subtasks.contains(subtask2), "Подзадача 2 отсутствует");
    }

    @Test
    void getPrioritizedTasks_returnsTasksSortedByStartTime() {
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(LocalDateTime.of(2025, 6, 10, 9, 0));
        task2.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size(), "Неверное количество задач в приоритетном списке");
        assertEquals(task2, prioritized.get(0), "Первая задача должна быть с более ранним временем");
        assertEquals(task1, prioritized.get(1), "Вторая задача должна быть с более поздним временем");
    }

    @Test
    void isTaskOverlapping_throwsException_onOverlap() {
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 30));
        task2.setDuration(Duration.ofMinutes(30));
        assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2),
                "Пересекающаяся задача должна вызвать исключение");
    }

    @Test
    void epicStatus_allSubtasksNew_statusNew() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.NEW);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.NEW);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(Status.NEW, manager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    void epicStatus_allSubtasksDone_statusDone() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.DONE);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.DONE);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    void epicStatus_mixedNewAndDone_statusInProgress() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.NEW);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.DONE);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void epicStatus_subtasksInProgress_statusInProgress() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.IN_PROGRESS);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void epicTimeFields_calculatedCorrectly() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2025, 6, 10, 12, 0));
        subtask2.setDuration(Duration.ofMinutes(30));
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic retrieved = manager.getEpic(epic.getId());
        assertEquals(Duration.ofMinutes(90), retrieved.getDuration(), "Неверная длительность эпика");
        assertEquals(LocalDateTime.of(2025, 6, 10, 10, 0), retrieved.getStartTime(), "Неверное время начала эпика");
        assertEquals(LocalDateTime.of(2025, 6, 10, 12, 30), retrieved.getEndTime(), "Неверное время окончания эпика");
    }
}