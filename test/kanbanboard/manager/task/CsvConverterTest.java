package kanbanboard.manager.task;

import kanbanboard.model.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CsvConverterTest {
    private final CsvConverter converter = new CsvConverter();

    @Test
    void getCsvHeader_returnsCorrectHeader() {
        assertEquals("id,type,name,status,description,duration,startTime,epic", converter.getCsvHeader());
    }

    @Test
    void toCsvString_Task_convertsCorrectly() {
        Task task = new Task("Task 1", "Description", Status.NEW, 1, Duration.ofMinutes(60),
                LocalDateTime.of(2025, 6, 10, 10, 0));
        String csv = converter.toCsvString(task);
        assertEquals("1,TASK,Task 1,NEW,Description,60,2025-06-10T10:00:00,", csv);
    }

    @Test
    void toCsvString_Epic_convertsCorrectly() {
        Epic epic = new Epic("Epic 1", "Description");
        epic.setId(1);
        epic.setStatus(Status.NEW);
        String csv = converter.toCsvString(epic);
        assertEquals("1,EPIC,Epic 1,NEW,Description,0,,", csv);
    }

    @Test
    void toCsvString_Subtask_convertsCorrectly() {
        Subtask subtask = new Subtask("Subtask 1", "Description", 2, Status.DONE, 1,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 10, 12, 0));
        String csv = converter.toCsvString(subtask);
        assertEquals("1,SUBTASK,Subtask 1,DONE,Description,30,2025-06-10T12:00:00,2", csv);
    }

    @Test
    void fromCsvString_Task_createsTask() {
        String csv = "1,TASK,Task 1,NEW,Description,60,2025-06-10T10:00:00,";
        Task task = converter.fromCsvString(csv);
        assertNotNull(task);
        assertEquals(1, task.getId());
        assertEquals("Task 1", task.getTitle());
        assertEquals(Status.NEW, task.getStatus());
        assertEquals("Description", task.getDescription());
        assertEquals(Duration.ofMinutes(60), task.getDuration());
        assertEquals(LocalDateTime.of(2025, 6, 10, 10, 0), task.getStartTime());
    }

    @Test
    void fromCsvString_Epic_createsEpic() {
        String csv = "1,EPIC,Epic 1,NEW,Description,0,,";
        Task epic = converter.fromCsvString(csv);
        assertInstanceOf(Epic.class, epic);
        assertEquals(1, epic.getId());
        assertEquals("Epic 1", epic.getTitle());
        assertEquals("Description", epic.getDescription());
    }

    @Test
    void fromCsvString_Subtask_createsSubtask() {
        String csv = "1,SUBTASK,Subtask 1,DONE,Description,30,2025-06-10T12:00:00,2";
        Task subtask = converter.fromCsvString(csv);
        assertInstanceOf(Subtask.class, subtask);
        assertEquals(1, subtask.getId());
        assertEquals("Subtask 1", subtask.getTitle());
        assertEquals(Status.DONE, subtask.getStatus());
        assertEquals("Description", subtask.getDescription());
        assertEquals(2, ((Subtask) subtask).getEpicId());
        assertEquals(Duration.ofMinutes(30), subtask.getDuration());
        assertEquals(LocalDateTime.of(2025, 6, 10, 12, 0), subtask.getStartTime());
    }

    @Test
    void fromCsvString_invalidLine_throwsIllegalArgumentException() {
        String invalidLine = "1,TASK,Title,NEW";
        assertThrows(IllegalArgumentException.class, () -> converter.fromCsvString(invalidLine),
                "Должно быть выброшено исключение для некорректной строки CSV");
    }

    @Test
    void fromCsvString_invalidTaskType_throwsIllegalArgumentException() {
        String invalidLine = "1,INVALID,Title,NEW,Description,0,,";
        assertThrows(IllegalArgumentException.class, () -> converter.fromCsvString(invalidLine),
                "Должно быть выброшено исключение для неизвестного типа задачи");
    }
}