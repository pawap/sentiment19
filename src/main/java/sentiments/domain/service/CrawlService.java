package sentiments.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sentiments.domain.model.Crawl;
import sentiments.domain.repository.CrawlRepository;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class CrawlService {

    @Autowired
    private CrawlRepository crawlRepository;

    public LocalDateTime getLastStart() {
        Crawl crawl = crawlRepository.findTopByOrderByDateDesc();
        return crawl.getDate();
    }

    public void newCrawl(LocalDateTime date) {
        Crawl crawl = new Crawl();
        crawl.setDate(date);
        crawl.setStatus(Crawl.IN_PROGRESS);
        crawlRepository.save(crawl);
    }

    public void finishCrawl(LocalDateTime date) {
        Crawl crawl = crawlRepository.findByDate(LocalDateTime );
    }

}
