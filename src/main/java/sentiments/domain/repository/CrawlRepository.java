package sentiments.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sentiments.domain.model.Crawl;


public interface CrawlRepository extends MongoRepository<Crawl, Integer> {

    public Crawl findTopByOrderByDateDesc();

    public Crawl findByDate();
}
