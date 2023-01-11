package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
public final class UrlCheck extends Model {
    @Id
    private long id;

    @WhenCreated
    private Instant createdAt;

    private int statusCode;

    @Lob
    private String title;

    @Lob
    private String h1;

    @Lob
    private String description;

    @ManyToOne(optional = false)
    private Url url;

    public UrlCheck() {
    }

    public UrlCheck(int newStatusCode, String newTitle, String newH1, String newDescription, Url newUrl) {
        this.statusCode = newStatusCode;
        this.title = newTitle;
        this.h1 = newH1;
        this.description = newDescription;
        this.url = newUrl;
    }

    public long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public String getDescription() {
        return description;
    }

    public Url getUrl() {
        return url;
    }
}
