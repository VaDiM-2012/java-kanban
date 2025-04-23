package kanbanBoard.manager.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultTest() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
    }

}