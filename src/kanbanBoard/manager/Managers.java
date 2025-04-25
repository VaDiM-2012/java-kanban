package kanbanBoard.manager;

import kanbanBoard.manager.history.HistoryManager;
import kanbanBoard.manager.history.InMemoryHistoryManager;
import kanbanBoard.manager.task.InMemoryTaskManager;
import kanbanBoard.manager.task.TaskManager;

public class Managers {

    public static TaskManager getDefault(HistoryManager manager) {
        return new InMemoryTaskManager(manager);
    }


    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
