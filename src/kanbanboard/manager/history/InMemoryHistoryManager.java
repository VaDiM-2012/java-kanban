package kanbanboard.manager.history;

import kanbanboard.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    // Узел двусвязного списка
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
            this.prev = null;
            this.next = null;
        }
    }

    private Node head; // Начало списка
    private Node tail; // Конец списка
    private final HashMap<Integer, Node> taskNodes; // Хранит id задачи -> узел

    public InMemoryHistoryManager() {
        this.taskNodes = new HashMap<>();
        this.head = null;
        this.tail = null;
    }

    @Override
    public void add(Task task) {
        if (task == null || task.getId() == null) {
            return;
        }

        // Удаляем задачу из истории, если она уже была просмотрена
        remove(task.getId());

        // Создаем новый узел
        Node newNode = new Node(new Task(task));

        // Добавляем в конец списка
        linkLast(newNode);

        // Сохраняем узел в HashMap
        taskNodes.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node node = taskNodes.get(id);
        if (node != null) {
            removeNode(node);
            taskNodes.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        ArrayList<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    // Добавляет узел в конец списка
    private void linkLast(Node node) {
        if (head == null) {
            // Список пуст
            head = node;
            tail = node;
        } else {
            // Добавляем в конец
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
    }

    // Удаляет узел из списка
    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        // Если узел — единственный
        if (node == head && node == tail) {
            head = null;
            tail = null;
        }
        // Если узел — первый
        else if (node == head) {
            head = node.next;
            if (head != null) {
                head.prev = null;
            } else {
                tail = null;
            }
        }
        // Если узел — последний
        else if (node == tail) {
            tail = node.prev;
            tail.next = null;
        }
        // Узел в середине
        else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        // Очищаем связи узла
        node.prev = null;
        node.next = null;
    }
}