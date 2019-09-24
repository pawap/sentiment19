package sentiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sentiments.data.ImportManager;
import sentiments.domain.model.Classification;
import sentiments.domain.model.Language;
import sentiments.domain.model.Tweet;
import sentiments.domain.repository.TweetRepository;
import sentiments.domain.service.ClassifierService;
import sentiments.domain.service.LanguageService;
import sentiments.ml.Classifier;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Component
public class ScheduledTasks {

    @Autowired
    private ImportManager importManager;

    @Autowired
    private ClassifierService classifierService;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private LanguageService languageService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static int maxThreadCount = 4;

    private static int threadCount = 0;

    private static boolean classifying = false;

    private static int batchSize = 512;

    @Scheduled(cron = "*/5 * * * * *")
    public void classifyNextBatch() {
        if (classifying) {
            System.out.println("already classifying");
            return;
        }
        System.out.println("new call");
            classifying = true;
            Iterable<Language> langs = languageService.getActiveLanguages();
            for (Language lang : langs) {
                log.info("begin classifying " + lang.getIso() + " tweets");
                Classifier classifier = classifierService.getClassifier(lang);
                Stream<Tweet> tweets = tweetRepository.findAllByClassifiedAndLanguage(null, lang.getIso());
                List<Tweet> currentBatch = new LinkedList<Tweet>();
                tweets.forEach(tweet -> {
                    if (classifier != null) {
                        Classification classification = classifier.classifyTweet(tweet.getText());
                        tweet.setOffensive(classification.isOffensive());
                        tweet.setClassified(new Date());
                        currentBatch.add(tweet);
                        System.out.println(currentBatch.size() + " in batch");
//                        if (currentBatch.size() == batchSize) {
 //                           tweetRepository.saveAll(currentBatch);
   //                         log.info("batch of " + currentBatch.size() + " classified");
     //                       currentBatch.clear();
       //                 }
                    }
                });
                if (!currentBatch.isEmpty()) {
                    tweetRepository.saveAll(currentBatch);
                    log.info("batch of " + currentBatch.size() + " classified");
                    currentBatch.clear();
                }

            }
        log.info("done classifying language");
            classifying = false;
    }


    @Async
    //   @Scheduled(cron = "*/5 * * * * *")
    public void crawlDataServer() throws InterruptedException {
        if (threadCount < maxThreadCount) {
            int mycount = ++threadCount;
            log.info("Starting crawl (" + mycount + ") at {}", dateFormat.format(new Date()));
            CompletableFuture completableFuture = importManager.importTweets();
            try {
                System.out.println(completableFuture.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            log.info("Ending crawl (" + mycount + ")  at {}", dateFormat.format(new Date()));
            threadCount--;
        }
    }
}