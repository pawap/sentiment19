package sentiments.domain.model;

import com.mongodb.lang.NonNull;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.Entity;

@Entity
public class User {

    @Indexed(unique = true)
    @NonNull
    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
