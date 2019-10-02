package sentiments.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sentiments.domain.model.Language;
/**
 * @author Paw
 */
public interface LanguageRepository extends MongoRepository<Language, Integer> {

    Language findOneByIso(String iso);

    Iterable<Language> findAllByActive(boolean active);
}
