package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;

import java.util.List;

public class RootController {
    private static Handler welcome = ctx -> {

        List<Url> customers =
                new QUrl().id.eq(1).findList();

        ctx.render("index.html");
    };

    public static Handler getWelcome() {
        return welcome;
    }
}
