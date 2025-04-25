package kanbanBoard.manager.task;

import kanbanBoard.manager.Managers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void null_returnNotNull_managerIsNotNull() {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistoryManager());
        assertNotNull(manager);
    }

}