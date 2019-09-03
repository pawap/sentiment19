package sentiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sentiments.data.ImportManager;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {

    @Autowired
    private ImportManager importManager;

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
            importManager.importTweets();
            log.info("Ending crawl (" + mycount + ")  at {}", dateFormat.format(new Date()));
        }
    }
}