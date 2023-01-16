package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public final class Url extends Model {
    @Id
    private long id;

    @WhenCreated
    private Instant createdAt;

    private String name;

    @OneToMany(mappedBy = "url")
    private List<UrlCheck> urlChecks;

    public Url(String newName) {
        this.name = newName;
    }
}

