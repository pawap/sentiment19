package sentiments.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sentiments.domain.model.Crawl;

import java.time.LocalDateTime;

/**
 * @author Paw
 */
public interface CrawlRepository extends MongoRepository<Crawl, Integer> {

    public Crawl findTopByOrderByDateDesc();

    public Crawl findByDate(LocalDateTime date);
}
