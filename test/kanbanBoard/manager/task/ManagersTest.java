package kanbanBoard.manager.task;

import kanbanBoard.manager.Managers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault_returnNotNull() {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistoryManager());
        assertNotNull(manager);
    }

}