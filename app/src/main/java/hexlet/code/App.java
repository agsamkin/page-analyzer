package hexlet.code;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import hexlet.code.controllers.RootController;

import static hexlet.code.controllers.UrlController.CHECK_URL;
import static hexlet.code.controllers.UrlController.CREATE_URL;
import static hexlet.code.controllers.UrlController.LIST_URLS;
import static hexlet.code.controllers.UrlController.SHOW_URL;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class App {

    private static final String DEFAULT_PORT = "5050";
    private static final String DEFAULT_ENV = "development";
    private static final String DEFAULT_MODE = "production";

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        return Integer.valueOf(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", DEFAULT_ENV);
    }

    private static boolean isProduction() {
        return getMode().equals(DEFAULT_MODE);
    }

    private static TemplateEngine getTemplateEngine() {

        TemplateEngine templateEngine = new TemplateEngine();

        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");
        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.getWelcome());

        app.routes(() -> {
            path("urls", () -> {
                get(LIST_URLS);
                post(CREATE_URL);
                get("{id}", SHOW_URL);
                post("{id}/checks", CHECK_URL);
            });
        });
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });

        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}

