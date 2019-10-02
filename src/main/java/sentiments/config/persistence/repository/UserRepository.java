package sentiments.config.persistence.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sentiments.config.persistence.model.User;

/**
 * @author Paw
 */
public interface UserRepository extends MongoRepository<User, Long> {

    User findByUsername(String username);
}