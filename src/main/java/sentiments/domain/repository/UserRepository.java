package sentiments.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sentiments.domain.model.User;

public interface UserRepository extends MongoRepository<User, Long> {

    User findByUsername(String username);
}