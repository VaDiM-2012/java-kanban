package kanbanboard.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, int epicId, Status status, int id, Duration duration, LocalDateTime startTime) {
        super(title, description, status, id, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(Subtask source) {
        super(source);
        this.epicId = source.epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public void setId(Integer id) { // Добавляем @Override и используем Integer
        if (id == epicId) {
            this.id = null;
        } else {
            this.id = id;
        }
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", title='" + title + '\'' +
                ", description='" + description.length() + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}