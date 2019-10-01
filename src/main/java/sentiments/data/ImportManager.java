package sentiments.data;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sentiments.ScheduledTasks;
import sentiments.domain.service.CrawlService;
import sentiments.service.ExceptionService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

/**
 * Manages importing tweet data into the DB
 *
 * @author 6runge, paw
 */
@Transactional
@Service
public class ImportManager {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    BasicDataImporter basicDataImporter;

    @Autowired
    Environment env;

    @Autowired
    CrawlService crawlService;

    @Autowired
    ExceptionService exceptionService;

    /**
     * Transfers tweets from the location specified in application.properties to the Database specified in application.properties
     */
    @Async
    public CompletableFuture importTweets() {

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        String remoteHost = env.getProperty("TweetHost");
        String remoteUsername = env.getProperty("TweetHostUsername");
        String remotePassword = env.getProperty("TweetHostPW");
        String tweetDir = env.getProperty("TweetParentDir");

        JSch jsch = new JSch();
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "password");
        jsch.setConfig(config);

        Session session;

        try {
            session = jsch.getSession(remoteUsername, remoteHost);
            session.setConfig("PreferredAuthentications", "password");
            session.setPassword(remotePassword);

            session.connect(20000);

            Channel channel = session.openChannel("sftp");
            channel.connect(20000);

            ChannelSftp sftpChannel = (ChannelSftp) channel;

            LocalDateTime last = crawlService.getLastStart();
            String month = last.format(DateTimeFormatter.ofPattern("MM"));


            String filename = null;
            String path = null;

            while (filename == null && Integer.valueOf(month) < 12) {

                path = tweetDir + "/2018/" + month;
                Vector<ChannelSftp.LsEntry> filelist = sftpChannel.ls(path);
                filelist.sort(Comparator.comparing(ChannelSftp.LsEntry::getFilename));
                filename = crawlFolder(filelist, last);
                int monthAsInt = Integer.valueOf(month) + 1;
                month = String.format("%02d", monthAsInt);
            }

            if (filename == null){
                completableFuture.complete(false);
                return completableFuture;
            }

            crawlService.newCrawl(filenameToDateTime(filename));

            InputStream in = sftpChannel.get(path + "/" + filename);
            basicDataImporter.importFromStream(new GZIPInputStream(in));

            sftpChannel.exit();
            session.disconnect();

            crawlService.finishCrawl(filenameToDateTime(filename));

        } catch (JSchException | SftpException | IOException e) {
            String eString = exceptionService.exceptionToString(e);
            log.warn(eString);
            e.printStackTrace();
        }
        completableFuture.complete(true);
        return completableFuture;

    }

    private String crawlFolder(Vector<ChannelSftp.LsEntry> fileList, LocalDateTime last) throws SftpException, IOException {

        System.out.println("crawling...");
        for (ChannelSftp.LsEntry entry : fileList) {
            String filename = entry.getFilename();
            if (!filename.startsWith("tweet")){
                System.out.println("skipped entry " + "'" + filename + "'");
                continue;
            }
            LocalDateTime current = filenameToDateTime(filename);
            System.out.println("checking " + current + " against " + last);
            if (!(current.isBefore(last) || current.isEqual(last))) {
                System.out.println("chose filename " + filename);
                return filename;
            }
        }
        return null;
    }

    /*
     * Extracts a LocalDateTime from a filename that follows the pattern tweetyyyyMMDDmm:HH*
     * @param name a filename in the correct format
     * @return the corresponding LocalDateTime
     */
    private LocalDateTime filenameToDateTime(String name) {

        int year = Integer.valueOf(name.substring(5, 9));
        Month month = Month.of(Integer.valueOf(name.substring(9, 11)));
        int day = Integer.valueOf(name.substring(11, 13));
        int hour = Integer.valueOf(name.substring(13, 15));
        int minute = Integer.valueOf(name.substring(16, 18));

        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);

        return dateTime;
    }


}
