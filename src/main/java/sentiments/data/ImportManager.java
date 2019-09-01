package sentiments.data;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * Manages importing tweet data into the DB
 * @author 6runge
 */
@Transactional
@Service
public class ImportManager extends BasicDataImporter{

    /**
     * Trasnfers tweets from the location specified in application.properties to the Database specified in application.properties
     */
    public void importTweets() {


        JSch jsch = new JSch();

        String remoteHost = env.getProperty("TweetHost");
        String remoteUsername = env.getProperty("TweetHostUsername");
        String remotePassword = env.getProperty("TweetHostPW");
        String tweetDir = env.getProperty("TweetParentDir");

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "password");
        jsch.setConfig(config);


        Session session;

        try {
            session = jsch.getSession(remoteUsername, remoteHost);

            System.out.println(remoteHost);
            System.out.println(remoteUsername);
            System.out.println(remotePassword);
            System.out.println(tweetDir);


            session.setPassword(remotePassword);

            session.connect(5000);
            System.out.println("Is Connected: " + session.isConnected());

            Channel channel = session.openChannel("sftp");
            channel.connect(5000);

            ChannelSftp sftpChannel = (ChannelSftp) channel;

            Vector<ChannelSftp.LsEntry> filelist = sftpChannel.ls(tweetDir);
            for(int i=0; i<filelist.size();i++){
                System.out.println(filelist.get(i).getFilename());
                InputStream in = sftpChannel.get(tweetDir + "/" + filelist.get(i).getFilename());
                GZIPInputStream gin = new GZIPInputStream(in);
                this.importFromStream(gin);
            }

            sftpChannel.exit();
            session.disconnect();

        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Extracts a LocalDateTime from a filename that follows the pattern tweetyyyyMMDDmm:HH.json
     * @param name a filename in the correct format
     * @return the corresponding LocalDateTime
     */
    public LocalDateTime FilenameToDateTime(String name){

        int year = Integer.valueOf(name.substring(5,9));
        int month = Integer.valueOf(name.substring(9,11));
        int day = Integer.valueOf(name.substring(11,13));
        int hour = Integer.valueOf(name.substring(13,15));
        int minute = Integer.valueOf(name.substring(16,18));


        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);

        return dateTime;
    }

}
