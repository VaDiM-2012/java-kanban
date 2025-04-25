package kanbanBoard.model;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksIds;

    public Epic(String title, String description) {
        super(title, description);
        this.subtasksIds = new ArrayList<>();
    }

    public Epic(Epic source) {
        super(source);
        this.subtasksIds = new ArrayList<>(source.subtasksIds);
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
        subtasksIds.remove(id);
    }

    public void removeSubtaskAll() {
        subtasksIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtasksIds=" + subtasksIds +
                ", title='" + title + '\'' +
                ", description='" + description.length() + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
