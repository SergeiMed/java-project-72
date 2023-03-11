package hexlet.code;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Url extends Model {

    @Id
    long id;
    String domain;
    @WhenCreated
    Instant createdAt;

    public Url(String domain) {
        this.domain = domain;
    }

    public long getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
