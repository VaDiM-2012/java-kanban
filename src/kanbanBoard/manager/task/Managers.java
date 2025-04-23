package kanbanBoard.manager.task;

import kanbanBoard.manager.history.HistoryManager;
import kanbanBoard.manager.history.InMemoryHistoryManager;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }


    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
