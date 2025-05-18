package kanbanboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void equals_returnTrue_idIsSame() {
        //Создаем Epic c id = 1
        Epic epic1 = new Epic("Задача №1", "Описание задачи №1");
        epic1.setId(1);

        //Создаем еще один экземпляр Epic с тем же самым id
        Epic epic2 = new Epic("Задача №2", "Описание задачи №2");
        epic2.setId(1);

        //Проверяем, что два экземпляра равны
        assertEquals(epic1, epic2, "Экземпляры класса Epic не равны");
    }


    // Задание: проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи
    // У меня Epic при добавлении подзадачи принимает объект Subtask, поэтому при добавлении Epic в качестве
    // подзадачи получаю ошибку компиляции.
    // В этом тесте я проверяю, что в список ArrayList<Integer> subtasksIds Эпика не будет добавлен ID Эпика
    @Test
    void addSubtask_subtaskNutAdded_subtaskIdEqualsEpicId() {
        // Создаём Эпик
        Epic epic = new Epic("Создать тестовый Эпик", "Описание тестового Эпика");
        epic.setId(1);

        //Создаём подзадачу
        Subtask subtask = new Subtask("Создать тестовую подзадачу", "Описание тестовой подзадачи", 1);
        //Устанавливаем ID подзадачи равный ID Эпика
        subtask.setId(1);

        //Добавляем подзадачу в Эпик
        epic.addSubtask(subtask);

        // Проверяем, что Subtask не добавился в список подзадач Эпика
        assertTrue(epic.getSubtasksIds().isEmpty(), "В список добавлен ID Эпика");
    }

}