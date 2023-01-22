package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;

import io.ebean.PagedList;

import io.javalin.http.Handler;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlController {
    private static final int URLS_PER_PAGE = 10;

    public static final Handler LIST_URLS = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int offset = (page - 1) * URLS_PER_PAGE;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(URLS_PER_PAGE)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int currentPage = pagedUrls.getPageIndex() + 1;
        int lastPage = pagedUrls.getTotalPageCount() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("currentPage", currentPage);
        ctx.attribute("pages", pages);

        ctx.render("urls/index.html");
    };

    public static final Handler CREATE_URL = ctx -> {
        String urlFromParam = ctx.formParam("url");

        URL url;
        try {
            url = new URL(urlFromParam);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();

        String normalizedUrl = protocol + "://" + host;
        if (port != -1) {
            normalizedUrl += ":" + port;
        }

        Url presentUrl = new QUrl()
                .name.equalTo(normalizedUrl)
                .findOne();

        if (Objects.nonNull(presentUrl)) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        new Url(normalizedUrl).save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static final Handler SHOW_URL = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static final Handler CHECK_URL = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();

            String body = response.getBody();
            Document doc = Jsoup.parse(body);

            int statusCode = response.getStatus();
            String title = doc.title();

            String h1 = doc.selectFirst("h1") != null
                    ? doc.selectFirst("h1").text()
                    : null;

            String description = doc.selectFirst("meta[name=description]") != null
                    ? doc.selectFirst("meta[name=description]").attr("content")
                    : null;

            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
            urlCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Страница не существует");
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + id);
    };
}
