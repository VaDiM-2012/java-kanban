package kanbanboard.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void equals_returnTrue_idIsSame() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Different Description");
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void getEndTime_calculatesCorrectly_withStartTimeAndDuration() {
        Task task = new Task("Task 1", "Description");
        task.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        task.setDuration(Duration.ofMinutes(60));

        assertEquals(LocalDateTime.of(2025, 6, 10, 11, 0), task.getEndTime(), "Время окончания рассчитано неверно");
    }

    @Test
    void getEndTime_returnsNull_noStartTime() {
        Task task = new Task("Task 1", "Description");
        task.setDuration(Duration.ofMinutes(60));

        assertNull(task.getEndTime(), "Время окончания должно быть null без времени начала");
    }
}