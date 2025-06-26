package kanbanboard.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksIds;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
        this.subtasksIds = new ArrayList<>();
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
    }

    public Epic(Epic source) {
        super(source);
        this.subtasksIds = new ArrayList<>(source.subtasksIds);
        this.duration = source.duration;
        this.startTime = source.startTime;
        this.endTime = source.endTime;
    }

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void addSubtask(Subtask subtask) {
        if (subtask.getId() == null) {
            return;
        }
        this.subtasksIds.add(subtask.getId());
    }

    public void removeSubtask(int id) {
        subtasksIds.remove(Integer.valueOf(id));
    }

    public void removeSubtaskAll() {
        subtasksIds.clear();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtasksIds=" + subtasksIds +
                ", title='" + title + '\'' +
                ", description='" + description.length() + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}