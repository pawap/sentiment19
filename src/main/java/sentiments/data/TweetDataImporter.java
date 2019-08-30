package sentiments.data;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.InputStream;
import java.util.Vector;

/**
 * Imports tweet data from the host specified in application.properties
 * @author 6runge
 */
public class TweetDataImporter extends BasicDataImporter{

    @Autowired
    Environment env;

    public void importTweets() {


        JSch jsch = new JSch();

        String remoteHost = env.getProperty("TweetHost");
        String remoteUsername = env.getProperty("TweetHostUsername");
        String remotePassword = env.getProperty("TweetHostPW");
        String tweetDir = env.getProperty("TweetDir");


        Session session;

        try {
            session = jsch.getSession(remoteUsername, remoteHost);


            // OR non-interactive version. Relies in host key being in known-hosts file
            session.setPassword(remotePassword);

            Channel channel = session.openChannel("sftp");
            channel.connect();

            ChannelSftp sftpChannel = (ChannelSftp) channel;

            //crawling
            //sftpChannel.cd(tweetDir);
            Vector filelist = sftpChannel.ls(tweetDir);
            for(int i=0; i<filelist.size();i++){
                InputStream in = sftpChannel.get(filelist.get(i).toString());
                this.importFromStream(in);
            }

            //sftpChannel.get("remote-file", "local-file");
            // OR

            // process inputstream as needed


            sftpChannel.exit();
            session.disconnect();

        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        }

    }

}
