package kanbanBoard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void epicWithTheSameIdAreEquals() {
        //Создаем Epic c id = 1
        Epic epic1 = new Epic("Задача №1", "Описание задачи №1");
        epic1.setId(1);

        //Создаем еще один экземпляр Epic с тем же самым id
        Epic epic2 = new Epic("Задача №2", "Описание задачи №2");
        epic2.setId(1);

        //Проверяем, что два экземпляра равны
        assertEquals(epic1, epic2, "Экземпляры класса Epic не равны");
    }

}