package kanbanBoard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void equals_returnTrue_idIsSame() {
        //Создаем Subtask c id = 1
        Subtask subtask1 = new Subtask("Задача №1", "Описание задачи №1",4);
        subtask1.setId(1);

        //Создаем еще один экземпляр Subtask с тем же самым id
        Subtask subtask2 = new Subtask("Задача №2", "Описание задачи №2",5);
        subtask2.setId(1);

        //Проверяем, что два экземпляра равны
        assertEquals(subtask1, subtask2, "Экземпляры класса Subtask не равны");
    }

    @Test
    void setId_returnNullInId_idSubtaskSameAsEpicId() {
        //Создаём подзадачу
        Subtask subtask = new Subtask("Создать тестовую подзадачу", "Описание тестовой подзадачи", 1);
        subtask.setId(1);

        //Проверяем, что подзадачу нельзя сделать своим Эпиком
        assertNull(subtask.getId());
    }
}