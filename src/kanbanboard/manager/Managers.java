package kanbanboard.manager;

import kanbanboard.manager.history.HistoryManager;
import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.manager.task.InMemoryTaskManager;
import kanbanboard.manager.task.TaskManager;

public class Managers {

    public static TaskManager getDefault(HistoryManager manager) {
        return new InMemoryTaskManager(manager);
    }

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
