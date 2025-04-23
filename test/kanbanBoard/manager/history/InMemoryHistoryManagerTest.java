package kanbanBoard.manager.history;

import kanbanBoard.manager.task.InMemoryTaskManager;
import kanbanBoard.model.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
class InMemoryHistoryManagerTest {
    @Test
    void testTaskHistoryManagerPreservesPreviousVersionAndData(){
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task10 = null;

        for(int i=0; i<10; i++){
            manager.createTask(new Task("task " + (i + 1),"discription "+(i + 1)));
            if(i != 9){
                manager.getTask(i+1);
            } else {
                task10 = manager.getTask(i+1);
            }
        }
        manager.createTask(new Task("task ","discription "));
        manager.getTask(11);
        ArrayList<Task> historyManager = manager.getHistory();
        //Проверка, что предыдущая версия сохранена и остались все данные
        assertEquals(task10.getTitle(),historyManager.get(8).getTitle(),"Предыдущая версия не сохранена");
    }

}