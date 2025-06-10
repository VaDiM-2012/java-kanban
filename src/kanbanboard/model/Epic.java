package kanbanboard.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksIds;
    private LocalDateTime endTime; // Новое поле для времени завершения

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

    public void updateTimeFields(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            this.startTime = null;
            this.duration = Duration.ZERO;
            this.endTime = null;
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        long totalMinutes = 0;

        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() != null) {
                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }
                LocalDateTime subtaskEnd = subtask.getEndTime();
                if (subtaskEnd != null && (latestEnd == null || subtaskEnd.isAfter(latestEnd))) {
                    latestEnd = subtaskEnd;
                }
                totalMinutes += subtask.getDuration().toMinutes();
            }
        }

        this.startTime = earliestStart;
        this.duration = Duration.ofMinutes(totalMinutes);
        this.endTime = latestEnd;
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