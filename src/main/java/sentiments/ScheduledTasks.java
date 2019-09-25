package sentiments;

import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Update;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

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

   // private static int batchSize = 1048;

    @Scheduled(cron = "*/5 * * * * *")
    public void classifyNextBatch() {
        if (classifying) {
            System.out.println("already classifying");
            return;
        }
        System.out.println("new call");
            classifying = true;
            Iterable<Language> langs = languageService.getAvailableLanguages();
            for (Language lang : langs) {
                log.info("begin classifying " + lang.getIso() + " tweets");
                Classifier classifier = classifierService.getClassifier(lang);
                if (classifier == null) {
                continue;
                }
                Date runDate = new Date();
                Stream<Tweet> tweets = tweetRepository.findAllByClassifiedAndLanguage(null, lang.getIso());
                AtomicInteger index = new AtomicInteger(0);

                int batchSize = 1048;
                int multiBatch = 1;
                Stream<List<Tweet>> stream = tweets.collect(Collectors.groupingBy(x -> index.getAndIncrement() / batchSize ))
                        .entrySet().stream()
                        .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue);

                // remove .parallel() for serial
                stream.parallel().forEach(tweetList -> {

                    // --- bulkOps  multiBatch just multiplies batchSize to get effective batchSize
                    BulkOperations ops = tweetRepository.getBulkOps();
                    for (Tweet tweet : tweetList) {
                        Classification classification = classifier.classifyTweet(tweet.getText());
                        Update update = new Update();
                        update.addToSet("offenisve", classification.isOffensive());
                        update.addToSet("classified", runDate);
                        ops.updateOne(query(where("_id").is(tweet.get_id())), update);
                    }
                    ops.execute();

                    // single batch multiBatch needs to be set to one
//                    for(Tweet tweet: tweetList) {
//                        Classification classification = classifier.classifyTweet(tweet.getText());
//                        tweet.setOffensive(classification.isOffensive());
//                        tweet.setClassified(runDate);
//                        batch.add(tweet);
//                        if (batch.size() % batchSize == 0) {
//                            tweetRepository.saveAll(batch);
//                            batch.clear();
//                        }
//                    }
//                    tweetRepository.saveAll(tweetList);

                    // multi batch need to set multiBatch > 1
//                    List<Tweet> batch = new LinkedList<>();
//                    for(Tweet tweet: tweetList) {
//                        Classification classification = classifier.classifyTweet(tweet.getText());
//                        tweet.setOffensive(classification.isOffensive());
//                        tweet.setClassified(runDate);
//                        batch.add(tweet);
//                        if (batch.size() % batchSize == 0) {
//                            tweetRepository.saveAll(batch);
//                            batch.clear();
//                        }
//                    }
                    System.out.println("finished a list of size " + tweetList.size());
//                    tweetRepository.saveAll(tweetList);
                });
//          old stuff
//                    List<Tweet> currentBatch = new LinkedList<>();
//                    for (int i = 0; i < batchSize && split.tryAdvance(tweet -> {
//
//                        Classification classification = classifier.classifyTweet(tweet.getText());
//                        tweet.setOffensive(classification.isOffensive());
//                        tweet.setClassified(new Date());
//                        currentBatch.add(tweet);
//                        System.out.println(currentBatch.size() + " in batch");
//
//                    }); i++) {
//                    }
//                    if (currentBatch.isEmpty()) {
//                        System.out.println("break");
//                        break;
//
//                    }
//                    tweetRepository.saveAll(currentBatch);
//                    log.info("batch of " + currentBatch.size() + " classified");
//                    currentBatch.clear();
                //}
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