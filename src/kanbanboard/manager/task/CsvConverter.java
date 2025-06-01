package kanbanboard.manager.task;

import kanbanboard.model.*;

public class CsvConverter {
    private static final String CSV_HEADER = "id,type,name,status,description,epic";

    // Возвращает заголовок CSV
    public String getCsvHeader() {
        return CSV_HEADER;
    }

    // Перегруженные методы для конвертации задач в CSV
    public String toCsvString(Task task) {
        return String.format("%d,%s,%s,%s,%s,",
                task.getId(), TaskType.TASK, task.getTitle(), task.getStatus(), task.getDescription());
    }

    public String toCsvString(Epic epic) {
        return String.format("%d,%s,%s,%s,%s,",
                epic.getId(), TaskType.EPIC, epic.getTitle(), epic.getStatus(), epic.getDescription());
    }

    public String toCsvString(Subtask subtask) {
        return String.format("%d,%s,%s,%s,%s,%d",
                subtask.getId(), TaskType.SUBTASK, subtask.getTitle(), subtask.getStatus(),
                subtask.getDescription(), subtask.getEpicId());
    }

    // Конвертация строки CSV в задачу (диспетчер)
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

    // Конвертация строки CSV в Task
    public Task fromCsvStringToTask(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        return new Task(title, description, status, id);
    }

    // Конвертация строки CSV в Epic
    public Epic fromCsvStringToEpic(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2];
        String description = parts[4];
        Epic epic = new Epic(title, description);
        epic.setId(id);
        // Статус не устанавливается, будет пересчитан
        return epic;
    }

    // Конвертация строки CSV в Subtask
    public Subtask fromCsvStringToSubtask(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        int epicId = Integer.parseInt(parts[5]);
        return new Subtask(title, description, epicId, status, id);
    }
}