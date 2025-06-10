package kanbanboard.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected String title;
    protected String description;
    protected Integer id;
    protected Status status;
    protected Duration duration; // Новое поле: продолжительность в минутах
    protected LocalDateTime startTime; // Новое поле: время начала

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = Status.NEW;
        this.duration = Duration.ZERO; // По умолчанию 0 минут
        this.startTime = null; // По умолчанию время не задано
    }

    public Task(String title, String description, Status status, int id, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.id = id;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(Task source) {
        this.title = source.title;
        this.description = source.description;
        this.id = source.id;
        this.status = source.status;
        this.duration = source.duration;
        this.startTime = source.startTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description.length() + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}