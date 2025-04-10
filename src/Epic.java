import java.util.ArrayList;

public class Epic extends Task{
    private final ArrayList<Integer> subtasksIds;

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }


    public Epic(String title, String description) {
        super(title, description);
        this.subtasksIds = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask){
        this.subtasksIds.add(subtask.getId());
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
