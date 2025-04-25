package kanbanBoard.manager.history;

import kanbanBoard.model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    //Список просмотренных задач по ID
    private ArrayList<Task> viewHistory = new ArrayList<>();
    private final int MAX_SIZE_VIEW_HISTORY = 10;

    @Override
    public void add(Task task) {
        viewHistory.add(new Task(task));

        if (viewHistory.size() > MAX_SIZE_VIEW_HISTORY) {
            viewHistory.removeFirst();
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(viewHistory);
    }
}
