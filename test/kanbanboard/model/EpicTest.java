package kanbanboard.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

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
    void setTimeFields_updatesCorrectly() {
        Epic epic = new Epic("Epic 1", "Description");
        epic.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        epic.setDuration(Duration.ofMinutes(90));
        epic.setEndTime(LocalDateTime.of(2025, 6, 10, 12, 30));

        assertEquals(Duration.ofMinutes(90), epic.getDuration(), "Неверная длительность эпика");
        assertEquals(LocalDateTime.of(2025, 6, 10, 10, 0), epic.getStartTime(), "Неверное время начала эпика");
        assertEquals(LocalDateTime.of(2025, 6, 10, 12, 30), epic.getEndTime(), "Неверное время окончания эпика");
    }

    @Test
    void setTimeFields_emptyValues_setsNull() {
        Epic epic = new Epic("Epic 1", "Description");
        epic.setStartTime(null);
        epic.setDuration(Duration.ZERO);
        epic.setEndTime(null);

        assertEquals(Duration.ZERO, epic.getDuration(), "Длительность должна быть нулевой");
        assertNull(epic.getStartTime(), "Время начала должно быть null");
        assertNull(epic.getEndTime(), "Время окончания должно быть null");
    }
}