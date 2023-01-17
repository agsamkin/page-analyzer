package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
public final class UrlCheck extends Model {
    @Id
    private long id;

    @WhenCreated
    private Instant createdAt;

    private int statusCode;

    private String title;

    private String h1;

//    @Lob
    private String description;

    @ManyToOne(optional = false)
    private Url url;

    public UrlCheck(int newStatusCode, String newTitle, String newH1, String newDescription, Url newUrl) {
        this.statusCode = newStatusCode;
        this.title = newTitle;
        this.h1 = newH1;
        this.description = newDescription;
        this.url = newUrl;
    }
}
