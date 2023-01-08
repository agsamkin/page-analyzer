package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public class UrlController {
    private static final int URLS_PER_PAGE = 10;

    private static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int offset = (page - 1) * URLS_PER_PAGE;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(URLS_PER_PAGE)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);

        ctx.render("urls/urls.html");
    };

    private static Handler createUrl = ctx -> {
        String checkUrl = ctx.formParam("url");

        URL url;
        try {
            url = new URL(checkUrl);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.redirect("/urls");
            return;
        }

        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();

        String nameUrl = protocol + "://" + host;
        if (port != -1) {
            nameUrl += ":" + port;
        }

        Url result = new QUrl()
                .name.equalTo(nameUrl)
                .findOne();

        if (Objects.nonNull(result)) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.redirect("/urls");
            return;
        }

        Url newUrl = new Url(nameUrl);
        newUrl.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.redirect("/urls");
    };

    public static Handler getListUrls() {
        return listUrls;
    }

    public static Handler createUrl() {
        return createUrl;
    }
}
