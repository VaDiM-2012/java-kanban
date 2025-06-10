package kanbanboard.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void equals_returnTrue_idIsSame() {
        Epic epic1 = new Epic("Epic 1", "Description");
        epic1.setId(1);
        Epic epic2 = new Epic("Epic 2", "Different Description");
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
    }

    @Test
    void addSubtask_doesNotAdd_subtaskIdEqualsEpicId() {
        Epic epic = new Epic("Epic 1", "Description");
        epic.setId(1);
        Subtask subtask = new Subtask("Subtask 1", "Description", 1);
        subtask.setId(1);

        epic.addSubtask(subtask);
        assertTrue(epic.getSubtasksIds().isEmpty(), "Подзадача с ID эпика не должна быть добавлена");
    }

    @Test
    void updateTimeFields_calculatesCorrectly_withSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        List<Subtask> subtasks = new ArrayList<>();
        Subtask subtask1 = new Subtask("Subtask 1", "Description", 1);
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask("Subtask 2", "Description", 1);
        subtask2.setStartTime(LocalDateTime.of(2025, 6, 10, 12, 0));
        subtask2.setDuration(Duration.ofMinutes(30));
        subtasks.add(subtask1);
        subtasks.add(subtask2);

        epic.updateTimeFields(subtasks);
        assertEquals(Duration.ofMinutes(90), epic.getDuration(), "Неверная длительность эпика");
        assertEquals(LocalDateTime.of(2025, 6, 10, 10, 0), epic.getStartTime(), "Неверное время начала эпика");
        assertEquals(LocalDateTime.of(2025, 6, 10, 12, 30), epic.getEndTime(), "Неверное время окончания эпика");
    }

    @Test
    void updateTimeFields_emptySubtasks_setsNull() {
        Epic epic = new Epic("Epic 1", "Description");
        epic.updateTimeFields(new ArrayList<>());

        assertEquals(Duration.ZERO, epic.getDuration(), "Длительность должна быть нулевой");
        assertNull(epic.getStartTime(), "Время начала должно быть null");
        assertNull(epic.getEndTime(), "Время окончания должно быть null");
    }
}