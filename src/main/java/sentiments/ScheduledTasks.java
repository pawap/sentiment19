package sentiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sentiments.data.ImportManager;
import sentiments.domain.model.Language;
import sentiments.domain.model.tweet.Tweet;
import sentiments.domain.repository.tweet.TweetRepository;
import sentiments.ml.service.ClassifierService;
import sentiments.service.ExceptionService;
import sentiments.domain.service.LanguageService;
import sentiments.service.TaskService;
import sentiments.ml.classifier.Classifier;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Paw , 6runge
 */
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

    @Autowired
    private TaskService taskService;

    @Autowired
    private ExceptionService exceptionService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static int maxThreadCount = 4;

    private static int threadCount = 0;

    private static boolean classifying = false;

   // private static int batchSize = 1048;

    @Scheduled(cron = "*/5 * * * * *")
    public void classifyNextBatch() {
        boolean execute = taskService.checkTaskExecution("classify");
        if (!execute || classifying) {
            return;
        }
        System.out.println("new call");
            classifying = true;
            Iterable<Language> langs = languageService.getAvailableLanguages();

        long time = System.currentTimeMillis();
        AtomicInteger tweetCount = new AtomicInteger();

        for (Language lang : langs) {
                log.info("try classifying " + lang.getIso() + " tweets");

                Classifier classifier = classifierService.getClassifier(lang);
                if (classifier == null) {
                continue;
                }
                Date runDate = new Date();
                Stream<Tweet> tweets = tweetRepository.findAllByClassifiedAndLanguage(null, lang.getIso());
                AtomicInteger index = new AtomicInteger(0);

                int batchSize = 512;
                int multiBatch = 1;
                Stream<List<Tweet>> stream = tweets.collect(Collectors.groupingBy(x -> index.getAndIncrement() / batchSize ))
                        .entrySet().stream()
                        .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue);
                // remove .parallel() for serial

                stream.parallel().forEach(tweetList -> {
                    tweetCount.addAndGet(tweetList.size());
                    classifier.classifyTweets(tweetList,runDate);
                    tweetRepository.saveAll(tweetList);

                    // --- bulkOps  multiBatch just multiplies batchSize to get effective batchSize
//                    BulkOperations ops = tweetRepository.getBulkOps();
//                    long time = System.currentTimeMillis();
//                    for (Tweet tweet : tweetList) {
//
//                        Classification classification = classifier.classifyTweet(tweet.getText());
//
//                        Update update = new Update();
//                        update.addToSet("offenisve", true);//classification.isOffensive());
//                        update.addToSet("classified", runDate);
//                        ops.updateOne(query(where("_id").is(tweet.get_id())), update);
//                    }
//                    System.out.println(System.currentTimeMillis() - time);
//                    ops.execute();

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
                });
            }

        long timeOverall = (System.currentTimeMillis() - time);
        String report;
        report = "##CLASSIFYING## Overall Time: " + timeOverall + "ms" + System.lineSeparator();
        report += "##CLASSIFYING## ~ " + tweetCount.get() * 1000 / timeOverall + " tweets per sec" + System.lineSeparator();
        report += "##CLASSIFYING## Tried to classify " + tweetCount.get() + " tweets. Done.";
        log.info(report);
        taskService.log(report);
        classifying = false;

    }


    @Async
    @Scheduled(cron = "*/5 * * * * *")
    public void crawlDataServer() throws InterruptedException {
        boolean execute = taskService.checkTaskExecution("import");
        if (execute && threadCount < maxThreadCount) {
            int mycount = ++threadCount;
            log.info("Starting crawl (" + mycount + ") at {}", dateFormat.format(new Date()));
            taskService.log("Starting crawl (" + mycount + ") at " + dateFormat.format(new Date()));
            CompletableFuture completableFuture = importManager.importTweets();
            try {
                System.out.println(completableFuture.get());
            } catch (ExecutionException e) {

                e.printStackTrace();

                String exceptionAsString = exceptionService.exceptionToString(e);

                log.warn("Crawl Exception: " + exceptionAsString);
                taskService.log("Crawl Exception: " + exceptionAsString);

            }
            log.info("Ending crawl (" + mycount + ")  at {}", dateFormat.format(new Date()));
            taskService.log("Starting crawl (" + mycount + ") at " + dateFormat.format(new Date()));

            threadCount--;
        }
    }
}