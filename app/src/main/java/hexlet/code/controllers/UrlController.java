package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public final class UrlController {
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

        ctx.render("urls/index.html");
    };

    private static Handler createUrl = ctx -> {
        String checkingUrl = ctx.formParam("url");

        URL url;
        try {
            url = new URL(checkingUrl);
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

        new Url(nameUrl).save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.redirect("/urls");
    };

    private static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        List<UrlCheck> checks = new QUrlCheck()
                .url.equalTo(url)
                .findList();

        ctx.attribute("url", url);
        ctx.attribute("checks", checks);
        ctx.render("urls/show.html");
    };

    private static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        // check url

        HttpResponse<String> response = Unirest.get(url.getName()).asString();
        int getStatus = response.getStatus();
        String body = response.getBody();

        Document doc = Jsoup.parse(body);

        int statusCode = response.getStatus();
        String title = doc.title();
        String h1 = doc.selectFirst("h1") != null
                ? Objects.requireNonNull(doc.selectFirst("h1")).text()
                : null;
        String description = doc.selectFirst("meta[name=description]") != null
                ? Objects.requireNonNull(doc.selectFirst("meta[name=description]")).attr("content")
                : null;

        UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
        urlCheck.save();

        List<UrlCheck> checks = new QUrlCheck()
                .url.equalTo(url)
                .findList();

        ctx.attribute("url", url);
        ctx.attribute("checks", checks);
        ctx.redirect("/urls/" + id);
//        ctx.render("urls/show.html");
    };

    public static Handler getListUrls() {
        return listUrls;
    }

    public static Handler createUrl() {
        return createUrl;
    }

    public static Handler showUrl() {
        return showUrl;
    }

    public static Handler checkUrl() {
        return checkUrl;
    }
}
