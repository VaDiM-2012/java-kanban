package kanbanboard.manager.task;

import kanbanboard.manager.Managers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault_returnNotNull() {
        TaskManager manager = Managers.getDefault(Managers.getDefaultHistoryManager());
        assertNotNull(manager);
    }

}