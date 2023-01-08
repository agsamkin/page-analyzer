package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Url existingArticle;
    private static Database database;

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
        database.script().run("/truncate.sql");
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
            HttpResponse<String> responsePost = Unirest
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
    }
}


