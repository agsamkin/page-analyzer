package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;

import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    private static final String MOCK_SITE = "src/test/resources/mockSite.html";

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    // Тесты не зависят друг от друга
    // Но хорошей практикой будет возвращать базу данных между тестами в исходное состояние
    @BeforeEach
    void beforeEach() {
//        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Nested
    class RootTest {
        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);

            assertThat(response.getBody()).contains("Бесплатно проверяйте сайты на SEO пригодность");
        }
    }

    @Nested
    class UrlTest {
        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
            assertThat(response.getBody()).contains("https://www.youtube.com");
        }

        @Test
        void testCreate() {
            String inputUrl = "https://github.com";
            HttpResponse responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
            assertThat(body).contains(inputUrl);
            assertThat(body).contains("Страница успешно добавлена");

            Url actualUrl = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(inputUrl);
        }

        @Test
        void testShow() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();
            assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
            assertThat(response.getBody()).contains("https://www.youtube.com");
            assertThat(response.getBody()).contains("Проверки");
        }

        @Test
        void testCheckUrl() throws Exception {

            MockResponse response = new MockResponse();
            String body = FilesUtil.readFile(MOCK_SITE);
            response.setBody(body);

            MockWebServer server = new MockWebServer();
            server.enqueue(response);
            server.start();

            // Получить mockUrl
            String mockUrl = server.url("").toString();
            if (mockUrl.endsWith("/")) {
                mockUrl = mockUrl.substring(0, mockUrl.length() - 1);
            }

            // Добавить mockUrl в таблицу бд urls
            HttpResponse<String> addUrlResponse = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", mockUrl)
                    .asEmpty();

            assertThat(addUrlResponse.getStatus()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);
            assertThat(addUrlResponse.getHeaders().getFirst("Location")).isEqualTo("/urls");

            // Получить id добаленного в таблицу бд urls mockUrl
            Optional<Url> mockUrlIdResponse = new QUrl().name.equalTo(mockUrl).findOneOrEmpty();
            assertThat(mockUrlIdResponse).isNotEmpty();
            long mockUrlId = mockUrlIdResponse.get().getId();

            // Выполнить проверку mockUrl
            HttpResponse<String> checkUrlResponse = Unirest
                    .post(baseUrl + "/urls/" + mockUrlId + "/checks")
                    .asEmpty();

            assertThat(checkUrlResponse.getStatus()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);
            assertThat(checkUrlResponse.getHeaders().getFirst("Location")).isEqualTo("/urls/" + mockUrlId);

            // Получим страницу с проверками mockUrlId
            HttpResponse<String> urlsResponse = Unirest
                    .get(baseUrl + "/urls/" + mockUrlId)
                    .asString();

            assertThat(urlsResponse.getBody()).contains(mockUrl);
            assertThat(urlsResponse.getBody()).contains("description");
            assertThat(urlsResponse.getBody()).contains("title");
            assertThat(urlsResponse.getBody()).contains("h1");
        }

    }
}


