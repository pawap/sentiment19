package sentiments;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * @author Paw
 *
 */
@Transactional
@Service
public class BasicDataImporter {

	private static final int BATCH_SIZE = 1024;

	@Autowired
	Environment env;

	@PersistenceContext
	EntityManager entityManager;

	DateTimeFormatter dateTimeFormatter;

	public BasicDataImporter() {
		super();
		this.dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss Z yyyy", Locale.UK);
	}

	public void importFromJson(String jsonPath, TweetProvider tweetProvider) {
		try {
			InputStream stream = new FileInputStream(jsonPath);
			JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
			Gson gson = new GsonBuilder().create();
			reader.setLenient(true);

			int i = 0;
			while (reader.hasNext()) {
				// Read data into object model
				try {
					if (reader.peek() == JsonToken.END_DOCUMENT) {
						break;
					}
					JsonElement element = gson.fromJson(reader, JsonElement.class);
					JsonObject object = element.getAsJsonObject();
					AbstractTweet tweet = tweetProvider.createTweet();
					this.mapJsonToTweet(object, tweet);
					if (tweet != null && tweet.getText() != null) {
						i++;
						entityManager.persist(tweet);
					}
					System.out.println(i);
				} catch (IllegalStateException | JsonSyntaxException e) {
					reader.skipValue();
				}
				// persist tweets in batch
				if (i % BATCH_SIZE == 0) {
					entityManager.flush();
					entityManager.clear();
				}
			}
			entityManager.flush();
			entityManager.clear();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void importFromTsv(String tsvPath, TweetProvider tweetProvider) {
		Reader in;
        int i = 0;
		try {
			FileInputStream fstream = new FileInputStream(tsvPath);
			in = new BufferedReader(new InputStreamReader(fstream));
			Iterable<CSVRecord> records = CSVFormat.TDF.withHeader().parse(in);
			for (CSVRecord record : records) {
				AbstractTweet tweet = tweetProvider.createTweet();
				this.mapTsvToTweet(record, tweet);
        		if (tweet != null && tweet.getText() != null) {
        			i++;
					entityManager.persist(tweet);
        		}
				System.out.println(i);
				if (i % BATCH_SIZE == 0) {
            		entityManager.flush();
					entityManager.clear();
            	}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	// persist tweets in batch (256 per insert)
		entityManager.flush();
		entityManager.clear();
	}

	private void mapTsvToTweet(CSVRecord record, AbstractTweet tweet) {
		tweet.setText(record.get("tweet"));
		switch (record.get("subtask_a")) {
		case "OFF":
			tweet.setOffensive(true);
			break;
		case "NOT":
			tweet.setOffensive(false);
			break;
		default:
		}
	}

	private void mapJsonToTweet(JsonObject object, AbstractTweet tweet) {
		if (object.has("text")) {
			tweet.setText(object.get("text").getAsString());
		}
		if (object.has("created_at")) {
			LocalDateTime dateTime;
			try {
				dateTime = LocalDateTime.parse(object.get("created_at").getAsString(), this.dateTimeFormatter);
				tweet.setCrdate(Timestamp.valueOf(dateTime));
			} catch (DateTimeParseException | NullPointerException e) {
				Parser parser = new Parser();
				List<DateGroup> groups = parser.parse(object.get("created_at").getAsString());

				// Get Natty's interpreted Date
				Instant targetDate = groups.get(0).getDates().get(0).toInstant();
				tweet.setCrdate(Timestamp.from(targetDate));
			}
		}
		tweet.setTmstamp(Timestamp.valueOf(LocalDateTime.now()));
	}

	public void importExampleJson() {
		String jsonPath = this.env.getProperty("localTweetJson");
		importFromJson(jsonPath, () -> new Tweet());
	}

	public void importTsvTestAndTrain() {
		importFromTsv(this.env.getProperty("localTweetTsv.train"), () -> new TrainingTweet());
		importFromTsv(this.env.getProperty("localTweetTsv.test"), () -> {
			TrainingTweet tweet = new TrainingTweet();
			tweet.setTest(true);
			return tweet;
		});
	}

}
