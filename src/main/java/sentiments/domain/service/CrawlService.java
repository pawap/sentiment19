package sentiments.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sentiments.domain.model.Crawl;
import sentiments.domain.repository.CrawlRepository;

import java.time.LocalDateTime;
import java.time.Month;

/**
 * Used for documenting file crawls and accessing the documented information.
 * @author Paw
 */
@Service
public class CrawlService {

    @Autowired
    private CrawlRepository crawlRepository;

    /**
     * When starting a new crawl, this method offers a starting point for the search.
     * @return the {@link LocalDateTime} of the last crawled tweet file
     */
    public LocalDateTime getLastStart() {
        Crawl crawl = crawlRepository.findTopByOrderByDateDesc();

        if (crawl == null) {
            return LocalDateTime.of(2018, Month.JANUARY,1, 0, 0);
        }
        System.out.println("looking for files dated after " + crawl.getDate());
        return crawl.getDate();
    }

    /**
     * Register the start of a new crawl.
     * @param date the {@link LocalDateTime} to be associated with the newly registered crawl
     */
    public void newCrawl(LocalDateTime date) {
        Crawl crawl = new Crawl();
        crawl.setDate(date);
        crawl.setStatus(Crawl.IN_PROGRESS);
        crawlRepository.save(crawl);
    }

    /**
     * Register the completion of a crawl.
     * @param date the {@link LocalDateTime} to be associated with the finished crawl
     */
    public void finishCrawl(LocalDateTime date) {
        Crawl crawl = crawlRepository.findByDate(date);
        crawl.setStatus(Crawl.FINISHED);
        crawlRepository.save(crawl);
    }

}
