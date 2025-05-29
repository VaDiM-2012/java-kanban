package kanbanboard.manager.task;

import kanbanboard.manager.history.HistoryManager;
import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    // Переопределение методов, модифицирующих состояние
    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        if (createdSubtask != null) {
            save();
        }
        return createdSubtask;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        if (updatedEpic != null) {
            save();
        }
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = super.updateSubtask(subtask);
        if (updatedSubtask != null) {
            save();
        }
        return updatedSubtask;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTask() {
        super.deleteTask();
        save();
    }

    @Override
    public void deleteEpic() {
        super.deleteEpic();
        save();
    }

    @Override
    public void deleteSubtask() {
        super.deleteSubtask();
        save();
    }

    // Метод сохранения состояния в файл
    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // Записываем заголовок
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            // Записываем задачи
            for (Task task : getTask()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getEpic()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getSubtask()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + file.getPath(), e);
        }
    }

    // Преобразование задачи в строку формата CSV
    private String toString(Task task) {
        String type;
        String epicId = "";
        if (task instanceof Epic) {
            type = TaskType.EPIC.name();
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK.name();
            epicId = String.valueOf(((Subtask) task).getEpicId());
        } else {
            type = TaskType.TASK.name();
        }
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), type, task.getTitle(), task.getStatus(), task.getDescription(), epicId);
    }

    // Создание задачи из строки CSV
    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case TASK:
                return new Task(title, description, status, id);
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                return new Subtask(title, description, epicId, status, id);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    // Статический метод для загрузки из файла
    public static FileBackedTaskManager loadFromFile(HistoryManager historyManager, File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager, file);
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            // Пропускаем заголовок
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.trim().isEmpty()) {
                    Task task = fromString(line);
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        Epic epic = manager.epics.get(((Subtask) task).getEpicId());
                        if (epic != null) {
                            epic.addSubtask((Subtask) task);
                        }
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }
            // Обновляем статусы эпиков
            for (Epic epic : manager.getEpic()) {
                manager.updateEpicStatus(epic.getId());
            }
            // Устанавливаем countId
            int maxId = 0;
            for (Task task : manager.getTask()) {
                maxId = Math.max(maxId, task.getId());
            }
            for (Epic epic : manager.getEpic()) {
                maxId = Math.max(maxId, epic.getId());
            }
            for (Subtask subtask : manager.getSubtask()) {
                maxId = Math.max(maxId, subtask.getId());
            }
            manager.countId = maxId;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла: " + file.getPath(), e);
        }
        return manager;
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\uvaro\\Desktop", "test.csv");
        FileBackedTaskManager manager = loadFromFile(new InMemoryHistoryManager(), file);
        for (Task task : manager.getTask()) {
            System.out.println(task);
        }
        for (Epic epic : manager.getEpic()) {
            System.out.println(epic);
        }
        manager.createTask(new Task("New task", "New task description"));

    }
}
