package kanbanboard.manager.task;

import kanbanboard.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvConverter {
    private static final String CSV_HEADER = "id,type,name,status,description,duration,startTime,epic";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public String getCsvHeader() {
        return CSV_HEADER;
    }

    public String toCsvString(Task task) {
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().format(FORMATTER) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%s,",
                task.getId(), TaskType.TASK, task.getTitle(), task.getStatus(), task.getDescription(),
                duration, startTime);
    }

    public String toCsvString(Epic epic) {
        String duration = epic.getDuration() != null ? String.valueOf(epic.getDuration().toMinutes()) : "";
        String startTime = epic.getStartTime() != null ? epic.getStartTime().format(FORMATTER) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%s,",
                epic.getId(), TaskType.EPIC, epic.getTitle(), epic.getStatus(), epic.getDescription(),
                duration, startTime);
    }

    public String toCsvString(Subtask subtask) {
        String duration = subtask.getDuration() != null ? String.valueOf(subtask.getDuration().toMinutes()) : "";
        String startTime = subtask.getStartTime() != null ? subtask.getStartTime().format(FORMATTER) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%s,%d",
                subtask.getId(), TaskType.SUBTASK, subtask.getTitle(), subtask.getStatus(),
                subtask.getDescription(), duration, startTime, subtask.getEpicId());
    }

    public Task fromCsvString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Недостаточно полей в строке CSV: " + value);
        }
        TaskType type = TaskType.valueOf(parts[1]);
        switch (type) {
            case TASK:
                return fromCsvStringToTask(value);
            case EPIC:
                return fromCsvStringToEpic(value);
            case SUBTASK:
                return fromCsvStringToSubtask(value);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    public Task fromCsvStringToTask(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = parts.length > 5 && !parts[5].isEmpty() ? Duration.ofMinutes(Long.parseLong(parts[5])) : Duration.ZERO;
        LocalDateTime startTime = parts.length > 6 && !parts[6].isEmpty() ? LocalDateTime.parse(parts[6], FORMATTER) : null;
        return new Task(title, description, status, id, duration, startTime);
    }

    public Epic fromCsvStringToEpic(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2];
        String description = parts[4];
        Epic epic = new Epic(title, description);
        epic.setId(id);
        // Duration и startTime будут пересчитаны
        return epic;
    }

    public Subtask fromCsvStringToSubtask(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = parts.length > 5 && !parts[5].isEmpty() ? Duration.ofMinutes(Long.parseLong(parts[5])) : Duration.ZERO;
        LocalDateTime startTime = parts.length > 6 && !parts[6].isEmpty() ? LocalDateTime.parse(parts[6], FORMATTER) : null;
        int epicId = Integer.parseInt(parts[7]);
        return new Subtask(title, description, epicId, status, id, duration, startTime);
    }
}