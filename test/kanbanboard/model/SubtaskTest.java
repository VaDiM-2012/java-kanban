package kanbanboard.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void equals_returnTrue_idIsSame() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description", 4);
        subtask1.setId(1);
        Subtask subtask2 = new Subtask("Subtask 2", "Different Description", 5);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
    }

    @Test
    void setId_returnNull_idEqualsEpicId() {
        Subtask subtask = new Subtask("Subtask 1", "Description", 1);
        subtask.setId(1);

        assertNull(subtask.getId(), "ID подзадачи не должен совпадать с ID эпика");
    }

    @Test
    void getEndTime_calculatesCorrectly_withStartTimeAndDuration() {
        Subtask subtask = new Subtask("Subtask 1", "Description", 1);
        subtask.setStartTime(LocalDateTime.of(2025, 6, 10, 12, 0));
        subtask.setDuration(Duration.ofMinutes(30));

        assertEquals(LocalDateTime.of(2025, 6, 10, 12, 30), subtask.getEndTime(), "Время окончания рассчитано неверно");
    }
}