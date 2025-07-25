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
    private final CsvConverter csvConverter;

    private FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
        this.csvConverter = new CsvConverter();
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
            writer.write(csvConverter.getCsvHeader());
            writer.newLine();

            // Записываем задачи
            for (Task task : tasks.values()) {
                writer.write(csvConverter.toCsvString(task));
                writer.newLine();
            }
            for (Epic epic : epics.values()) {
                writer.write(csvConverter.toCsvString(epic));
                writer.newLine();
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(csvConverter.toCsvString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + file.getPath(), e);
        }
    }

    // Статический метод для загрузки из файла
    public static FileBackedTaskManager loadFromFile(HistoryManager historyManager, File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager, file);
        try {
            List<String> lines = manager.readLinesFromFile();
            manager.processCsvLines(lines);
            manager.updateAllEpicStatuses();
            manager.updateCountId();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла: " + file.getPath(), e);
        }
        return manager;
    }

    // Чтение строк из файла
    private List<String> readLinesFromFile() throws IOException {
        return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    }

    // Обработка строк CSV
    private void processCsvLines(List<String> lines) {
        // Пропускаем заголовок
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                processCsvLine(line);
            }
        }
    }

    // Обработка одной строки CSV
    private void processCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Недостаточно полей в строке CSV: " + line);
        }
        TaskType type = TaskType.valueOf(parts[1]);
        switch (type) {
            case TASK:
                Task task = csvConverter.fromCsvStringToTask(line);
                tasks.put(task.getId(), task);
                break;
            case EPIC:
                Epic epic = csvConverter.fromCsvStringToEpic(line);
                epics.put(epic.getId(), epic);
                break;
            case SUBTASK:
                Subtask subtask = csvConverter.fromCsvStringToSubtask(line);
                subtasks.put(subtask.getId(), subtask);
                Epic subtaskEpic = epics.get(subtask.getEpicId());
                if (subtaskEpic != null) {
                    subtaskEpic.addSubtask(subtask);
                }
                break;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    // Обновление статусов всех эпиков
    private void updateAllEpicStatuses() {
        for (Epic epic : getEpic()) {
            updateEpicStatus(epic.getId());
        }
    }

    // Обновление countId
    private void updateCountId() {
        int maxId = 0;
        for (Task task : getTask()) {
            maxId = Math.max(maxId, task.getId());
        }
        for (Epic epic : getEpic()) {
            maxId = Math.max(maxId, epic.getId());
        }
        for (Subtask subtask : getSubtask()) {
            maxId = Math.max(maxId, subtask.getId());
        }
        countId = maxId;
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