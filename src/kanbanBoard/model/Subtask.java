package kanbanBoard.model;

public class Subtask extends Task {
    private final int epicId;


    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, int epicId, Status status, int id) {
        super(title, description, status, id);
        this.epicId = epicId;
    }

    public Subtask(Subtask source) {
        super(source);
        this.epicId = source.epicId;
    }


    public int getEpicId() {
        return epicId;
    }

    public void setId(int id) {
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
                '}';
    }
}
