package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Url extends Model {
    @Id
    private long id;

    @WhenCreated
    private Instant createdAt;

    private String name;
}
