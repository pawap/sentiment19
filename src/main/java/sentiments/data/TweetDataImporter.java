package sentiments.data;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * Imports tweet data from the host specified in application.properties
 * @author 6runge
 */
@Transactional
@Service
public class TweetDataImporter extends BasicDataImporter{

  //  @Autowired
  //  Environment env;

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

            // OR non-interactive version. Relies in host key being in known-hosts file
            session.setPassword(remotePassword);

            session.connect(5000);
            System.out.println("Is Connected: " + session.isConnected());

            Channel channel = session.openChannel("sftp");
            channel.connect(5000);

            ChannelSftp sftpChannel = (ChannelSftp) channel;

            //crawling
            //sftpChannel.cd(tweetDir);
            Vector<ChannelSftp.LsEntry> filelist = sftpChannel.ls(tweetDir);
            for(int i=0; i<filelist.size();i++){
                System.out.println(filelist.get(i).getFilename());
                InputStream in = sftpChannel.get(tweetDir + "/" + filelist.get(i).getFilename());
                GZIPInputStream gin = new GZIPInputStream(in);
                this.importFromStream(gin);
            }

            //sftpChannel.get("remote-file", "local-file");
            // OR

            // process inputstream as needed


            sftpChannel.exit();
            session.disconnect();

        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
        }

    }

}
