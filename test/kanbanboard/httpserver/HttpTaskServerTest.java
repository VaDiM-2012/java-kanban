package kanbanboard.httpserver;

import kanbanboard.manager.Managers;
import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.manager.task.InMemoryTaskManager;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса HttpTaskServer.
 * Проверяют корректный запуск и остановку сервера, а также доступность базовых эндпоинтов.
 */
public class HttpTaskServerTest {
    private HttpTaskServer server;
    private static final int TEST_PORT = 8080;

    /**
     * Тест: Сервер успешно запускается и отвечает на запросы.
     * Проверяет, что сервер запускается на заданном порту и эндпоинт /tasks доступен.
     */
    @Test
    void serverStartsAndResponds() throws IOException, InterruptedException {
        // Инициализация сервера
        server = new HttpTaskServer(new InMemoryTaskManager(Managers.getDefaultHistoryManager()));
        server.start();

        try {
            // Создаем HTTP-клиент
            HttpClient client = HttpClient.newHttpClient();
            // Отправляем простой GET запрос к одному из эндпоинтов
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:" + TEST_PORT + "/tasks"))
                    .GET()
                    .build();

            // Получаем ответ
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Проверяем, что сервер вернул успешный статус
            assertEquals(200, response.statusCode(), "Сервер должен отвечать со статусом 200 OK");
        } finally {
            // Обязательно останавливаем сервер после теста
            server.stop();
        }
    }

    /**
     * Тест: Сервер корректно останавливается.
     * Проверяет, что после остановки сервера порт освобождается.
     */
    @Test
    void serverStopsCorrectly() throws IOException, InterruptedException {
        server = new HttpTaskServer(new InMemoryTaskManager(Managers.getDefaultHistoryManager()));
        server.start();
        server.stop();

        // Попытка запустить новый сервер на том же порту,
        // если предыдущий не остановился, это вызовет BindException.
        HttpTaskServer newServer = null;
        try {
            newServer = new HttpTaskServer(new InMemoryTaskManager(Managers.getDefaultHistoryManager()));
            newServer.start(); // Если здесь не будет исключения, значит, порт свободен
            // Если дошли сюда, значит, тест пройден успешно
        } catch (IOException e) {
            fail("Сервер не смог запуститься на том же порту после остановки предыдущего: " + e.getMessage());
        } finally {
            if (newServer != null) {
                newServer.stop();
            }
        }
    }

    /**
     * Тест: Проверка создания всех контекстов.
     * Подтверждает, что все ожидаемые эндпоинты были зарегистрированы.
     * (Это косвенная проверка, поскольку прямые методы получения контекстов в HttpServer отсутствуют).
     * Тест будет "зеленым", если сервер запускается и базовые пути отвечают.
     */
    @Test
    void allContextsAreCreated() throws IOException, InterruptedException {
        server = new HttpTaskServer(new InMemoryTaskManager(Managers.getDefaultHistoryManager()));
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String[] expectedPaths = {"/tasks", "/subtasks", "/epics", "/history", "/prioritized"};

            for (String path : expectedPaths) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:" + TEST_PORT + path))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                // Ожидаем 200 OK для GET /tasks, /subtasks, /epics (списки), /history, /prioritized
                // Даже если списки пусты, должен быть 200 OK и пустой JSON-массив.
                assertEquals(200, response.statusCode(), "Эндпоинт " + path + " должен быть доступен (200 OK)");
            }
        } finally {
            server.stop();
        }
    }
}
