package TasksAndEpics;

public class Subtask extends Task {
    private final int epicId;


    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, int epicId, Status status) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "TasksAndEpics.Subtask{" +
                "status=" + status +
                ", id=" + id +
                ", description='" + description.length() + '\'' +
                ", title='" + title + '\'' +
                ", epicId=" + epicId +
                '}';
    }

}
