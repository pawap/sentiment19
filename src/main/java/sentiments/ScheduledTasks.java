package sentiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sentiments.data.TweetDataImporter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class ScheduledTasks {

    @Autowired
    private TweetDataImporter tweetDataImporter;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static int maxThreadCount = 4;

    private static int threadCount = 0;


    @Async
    @Scheduled(cron = "*/30 * * * * *")
    public void crawlDataServer() throws InterruptedException {
        if (threadCount >= maxThreadCount) {
            int mycount = ++threadCount;
            log.info("Starting crawl (" + mycount + ") at {}", dateFormat.format(new Date()));
            CompletableFuture<Boolean> cf = tweetDataImporter.importTweets();
            try {
                cf.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            log.info("Ending crawl (" + mycount + ")  at {}", dateFormat.format(new Date()));
        }
    }
}