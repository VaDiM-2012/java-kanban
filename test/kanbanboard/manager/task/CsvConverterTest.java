package kanbanboard.manager.task;

import kanbanboard.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvConverterTest {
    private final CsvConverter converter = new CsvConverter();

    @Test
    void getCsvHeader_ShouldReturnCorrectHeader() {
        assertEquals("id,type,name,status,description,epic", converter.getCsvHeader());
    }

    @Test
    void toCsvString_Task_ConvertsCorrectly() {
        Task task = new Task("Task 1", "Description", Status.NEW, 1);
        String csv = converter.toCsvString(task);
        assertEquals("1,TASK,Task 1,NEW,Description,", csv);
    }

    @Test
    void toCsvString_Epic_ConvertsCorrectly() {
        Epic epic = new Epic("Epic 1", "Epic Description");
        epic.setId(1);
        epic.setStatus(Status.NEW);
        String csv = converter.toCsvString(epic);
        assertEquals("1,EPIC,Epic 1,NEW,Epic Description,", csv);
    }

    @Test
    void toCsvString_Subtask_ConvertsCorrectly() {
        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", 2, Status.DONE, 1);
        String csv = converter.toCsvString(subtask);
        assertEquals("1,SUBTASK,Subtask 1,DONE,Subtask Description,2", csv);
    }

    @Test
    void fromCsvString_Task_CreatesTask() {
        String csv = "1,TASK,Task 1,NEW,Description,";
        Task task = converter.fromCsvString(csv);
        assertNotNull(task);
        assertEquals(1, task.getId());
        assertEquals("Task 1", task.getTitle());
        assertEquals(Status.NEW, task.getStatus());
        assertEquals("Description", task.getDescription());
    }

    @Test
    void fromCsvString_Epic_CreatesEpic() {
        String csv = "1,EPIC,Epic 1,NEW,Epic Description,";
        Task epic = converter.fromCsvString(csv);
        assertInstanceOf(Epic.class, epic);
        assertEquals(1, epic.getId());
        assertEquals("Epic 1", epic.getTitle());
        assertEquals("Epic Description", epic.getDescription());
        // Статус не устанавливается, проверяем null или NEW
        assertEquals(Status.NEW, epic.getStatus()); // Предполагаем, что Epic инициализирует статус как NEW
    }

    @Test
    void fromCsvString_Subtask_CreatesSubtask() {
        String csv = "1,SUBTASK,Subtask 1,DONE,Subtask Description,2";
        Task subtask = converter.fromCsvString(csv);
        assertInstanceOf(Subtask.class, subtask);
        assertEquals(1, subtask.getId());
        assertEquals("Subtask 1", subtask.getTitle());
        assertEquals(Status.DONE, subtask.getStatus());
        assertEquals("Subtask Description", subtask.getDescription());
        assertEquals(2, ((Subtask) subtask).getEpicId());
    }

    @Test
    void fromCsvStringToTask_ValidLine_CreatesTask() {
        String csv = "1,TASK,Task 1,NEW,Description,";
        Task task = converter.fromCsvStringToTask(csv);
        assertEquals(1, task.getId());
        assertEquals("Task 1", task.getTitle());
        assertEquals(Status.NEW, task.getStatus());
        assertEquals("Description", task.getDescription());
    }

    @Test
    void fromCsvStringToEpic_ValidLine_CreatesEpic() {
        String csv = "1,EPIC,Epic 1,NEW,Epic Description,";
        Epic epic = converter.fromCsvStringToEpic(csv);
        assertEquals(1, epic.getId());
        assertEquals("Epic 1", epic.getTitle());
        assertEquals("Epic Description", epic.getDescription());
    }

    @Test
    void fromCsvStringToSubtask_ValidLine_CreatesSubtask() {
        String csv = "1,SUBTASK,Subtask 1,DONE,Subtask Description,2";
        Subtask subtask = converter.fromCsvStringToSubtask(csv);
        assertEquals(1, subtask.getId());
        assertEquals("Subtask 1", subtask.getTitle());
        assertEquals(Status.DONE, subtask.getStatus());
        assertEquals("Subtask Description", subtask.getDescription());
        assertEquals(2, subtask.getEpicId());
    }

    @Test
    void fromCsvString_InvalidLine_ThrowsIllegalArgumentException() {
        String invalidLine = "1,TASK,Title,NEW"; // Недостаточно полей
        assertThrows(IllegalArgumentException.class, () -> converter.fromCsvString(invalidLine),
                "Должно быть выброшено исключение для некорректной строки CSV");
    }

    @Test
    void fromCsvString_InvalidTaskType_ThrowsIllegalArgumentException() {
        String invalidLine = "1,INVALID,Title,NEW,Description,";
        assertThrows(IllegalArgumentException.class, () -> converter.fromCsvString(invalidLine),
                "Должно быть выброшено исключение для неизвестного типа задачи");
    }
}