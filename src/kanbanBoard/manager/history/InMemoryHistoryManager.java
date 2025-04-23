package kanbanBoard.manager.history;

import kanbanBoard.model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    //Список просмотренных задач по ID
    ArrayList<Task> viewHistory = new ArrayList<>();
    private final int MAX_SIZE_VIEW_HISTORY = 10;

    @Override
    public void add(Task task) {
        viewHistory.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        if (viewHistory.size() > MAX_SIZE_VIEW_HISTORY) {
            viewHistory.removeFirst();
        }
        return new ArrayList<>(viewHistory);
    }
}
