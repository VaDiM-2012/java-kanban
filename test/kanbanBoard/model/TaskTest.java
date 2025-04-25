package kanbanBoard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void equals_returnTrue_idIsSame() {
        //Создаем Task c id = 1
        Task task1 = new Task("Задача №1", "Описание задачи №1");
        task1.setId(1);

        //Создаем еще один экземпляр Task с тем же самым id
        Task task2 = new Task("Задача №2", "Описание задачи №2");
        task2.setId(1);

        //Проверяем, что два экземпляра равны
        assertEquals(task1, task2, "Экземпляры класса Task не равны");
    }

}