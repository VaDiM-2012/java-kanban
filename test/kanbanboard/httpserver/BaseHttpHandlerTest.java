package kanbanboard.httpserver;

import com.google.gson.Gson;
import kanbanboard.manager.Managers;
import kanbanboard.manager.history.InMemoryHistoryManager;
import kanbanboard.manager.task.InMemoryTaskManager;
import kanbanboard.manager.task.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Абстрактный базовый класс для тестирования HTTP-обработчиков.
 * Предоставляет общую логику для запуска и остановки HTTP-сервера,
 * а также для инициализации HttpClient и Gson.
 */
public abstract class BaseHttpHandlerTest {
    protected TaskManager taskManager;
    protected HttpTaskServer httpTaskServer;
    protected Gson gson;
    protected HttpClient client;

    // Порт для тестового сервера
    protected static final int PORT = 8080;

    /**
     * Настраивает тестовую среду перед каждым тестом.
     * Инициализирует TaskManager, HttpTaskServer, Gson и HttpClient, а затем запускает сервер.
     *
     * @throws IOException если произошла ошибка при запуске сервера
     */
    @BeforeEach
    public void setUp() throws IOException {
        // Инициализируем TaskManager с InMemoryHistoryManager для тестирования
        taskManager = new InMemoryTaskManager(Managers.getDefaultHistoryManager());
        // Инициализируем HttpTaskServer с тестовым TaskManager
        httpTaskServer = new HttpTaskServer(taskManager);
        // Получаем экземпляр Gson, используемый сервером, для согласованной сериализации/десериализации
        gson = HttpTaskServer.getGson();
        // Запускаем HTTP-сервер
        httpTaskServer.start();
        // Инициализируем HttpClient для отправки запросов
        client = HttpClient.newHttpClient();
    }

    /**
     * Очищает тестовую среду после каждого теста.
     * Останавливает HTTP-сервер.
     */
    @AfterEach
    public void tearDown() {
        // Останавливаем HTTP-сервер после каждого теста
        httpTaskServer.stop();
    }

    /**
     * Вспомогательный метод для отправки GET-запроса.
     *
     * @param path Путь URI
     * @return HttpResponse<String> ответ от сервера
     * @throws IOException          если произошла ошибка ввода-вывода
     * @throws InterruptedException если поток был прерван во время ожидания ответа
     */
    protected HttpResponse<String> sendGetRequest(String path) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:" + PORT + path);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Вспомогательный метод для отправки POST-запроса.
     *
     * @param path Путь URI
     * @param body Тело запроса в виде строки (JSON)
     * @return HttpResponse<String> ответ от сервера
     * @throws IOException          если произошла ошибка ввода-вывода
     * @throws InterruptedException если поток был прерван во время ожидания ответа
     */
    protected HttpResponse<String> sendPostRequest(String path, String body) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:" + PORT + path);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Вспомогательный метод для отправки DELETE-запроса.
     *
     * @param path Путь URI
     * @return HttpResponse<String> ответ от сервера
     * @throws IOException          если произошла ошибка ввода-вывода
     * @throws InterruptedException если поток был прерван во время ожидания ответа
     */
    protected HttpResponse<String> sendDeleteRequest(String path) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:" + PORT + path);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
