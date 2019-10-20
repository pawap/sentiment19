package sentiments.data;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sentiments.domain.model.tweet.AbstractTweet;
import sentiments.domain.model.Language;
import sentiments.domain.model.tweet.TrainingTweet;
import sentiments.domain.model.tweet.Tweet;
import sentiments.domain.preprocessor.ImportTweetPreProcessor;
import sentiments.domain.preprocessor.TweetPreProcessor;
import sentiments.domain.repository.tweet.TrainingTweetRepository;
import sentiments.domain.repository.tweet.TweetRepository;
import sentiments.service.ExceptionService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;



/**
 * Offers an API for importing tweets into MongoDBs.
 *
 * @author Paw, 6runge
 */
@Transactional
@Service
public class BasicDataImporter {

	private static final Logger log = LoggerFactory.getLogger(BasicDataImporter.class);

	private static final int BATCH_SIZE = 512;

	@Autowired
	Environment env;

	@Autowired
	TweetRepository tweetRepository;

	@Autowired
	TrainingTweetRepository trainingTweetRepository;

	@Autowired
	ExceptionService exceptionService;

	DateTimeFormatter dateTimeFormatter;

	/**
	 * basic constructor
	 */
	public BasicDataImporter() {
		super();
		this.dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss Z yyyy", Locale.UK);
	}

	/**
	 * Imports tweets from a from a JSON. The JSON should contain an attribute "text"
	 * "full_text" representing the texts of the tweets and an attribute "id_str" holding the tweet id. If an attribute
	 * "created_at" exists it will be imported as well.
	 * @param jsonPath the path of the JSON to import
	 * @param tweetProvider a {@link TweetProvider}
	 * @param processor a {@link TweetPreProcessor} to be used on each tweet during import
	 * @param repo the {@link MongoRepository} the tweets should be imported to
	 */
	public void importFromJson(String jsonPath, TweetProvider<Tweet> tweetProvider, TweetPreProcessor processor, MongoRepository repo)
	{
		try {
			InputStream stream = new FileInputStream(jsonPath);
			importFromStream(stream, tweetProvider, processor, repo);
		} catch (FileNotFoundException e) {
			String eString = exceptionService.exceptionToString(e);
			log.warn("Exception during Import: " + eString);
		}
	}

	/**
	 * Imports tweets from a given {@link InputStream} created from a JSON. The JSON should contain an attribute "text"
	 * "full_text" representing the texts of the tweets and an attribute "id_str" holding the tweet id. If an attribute
	 * "created_at" exists it will be imported as well.
	 * @param stream an {@link InputStream} of the JSON to import
	 * @param tweetProvider a {@link TweetProvider}
	 * @param processor a {@link TweetPreProcessor} to be used on each tweet during import
	 * @param repo the {@link MongoRepository} the tweets should be imported to
	 */
	public void importFromStream(InputStream stream, TweetProvider<Tweet> tweetProvider, TweetPreProcessor processor, MongoRepository repo) {
		try {
			JsonReader reader = new JsonReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			Gson gson = new GsonBuilder().create();
			reader.setLenient(true);
			List<Tweet> tweets = tweetProvider.getNewTweetList();
			int i = 0;
			JsonElement element;
			JsonObject object;
			Tweet tweet;
			while (reader.hasNext()) {
				// Read data into object model

				try {
					if (reader.peek() == JsonToken.END_DOCUMENT) {
						break;
					}
					element = gson.fromJson(reader, JsonElement.class);
					object = element.getAsJsonObject();
					tweet = tweetProvider.createTweet();
					this.mapJsonToTweet(object, tweet);
					if (tweet != null && tweet.getText() != null) {
						i++;
						processor.preProcess(tweet);
						tweets.add(tweet);
					}

				} catch (IllegalStateException | JsonSyntaxException e) {
					reader.skipValue();
				}
				// persist tweets in batch
				if (i % BATCH_SIZE == 0) {
					System.out.println("Persisting another batch. " + i + " tweets persisted in DB.");
					tweetRepository.saveAll(tweets);
					tweets.clear();
				}
			}
			tweetRepository.saveAll(tweets);
			tweets.clear();
			reader.close();
			processor.destroy();
			System.gc();
		} catch (IOException e) {
			String eString = exceptionService.exceptionToString(e);
			log.warn("Exception during Import: " + eString);
		}
	}

	/**
	 * Imports tweets from a given {@link InputStream} created from a JSON. The JSON should contain an attribute "text"
	 * "full_text" representing the texts of the tweets and an attribute "id_str" holding the tweet id. If an attribute
	 * "created_at" exists it will be imported as well.
	 * Imports to the standard {@link TweetRepository}.
	 * @param stream an {@link InputStream} of the JSON to import
	 */
	public void importFromStream(InputStream stream) {
		importFromStream(stream, new TweetProvider<Tweet>() {
					@Override
					public Tweet createTweet() {
						return new Tweet();
					}
				}, new ImportTweetPreProcessor()
				, tweetRepository);
	}

	/**
	 * Imports Tweets from a tsv file into a {@link MongoRepository}. The TSV should contain the text of a tweet in a
	 * column called "tweet" and an additional column called "subtask_a" containing a String starting with "OFF" if the
	 * tweet is offensive.
	 * @param tsvPath the path of the source file
	 * @param tweetProvider a {@link TweetProvider}
	 * @param repo the target {@link MongoRepository}
	 * @param processor a {@link TweetPreProcessor} to be used on each tweet during import
	 * @param lang the language of the tweets
	 */
	public void importFromTsv(String tsvPath, TweetProvider<TrainingTweet> tweetProvider, MongoRepository repo, TweetPreProcessor processor, String lang) {
		Reader in;
		int i = 0;
		try {
			System.out.println(tsvPath);
			FileInputStream fstream = new FileInputStream(tsvPath);
			in = new BufferedReader(new InputStreamReader(fstream));
			Iterable<CSVRecord> records = CSVFormat.TDF.withHeader().withQuote(null).parse(in);
			List<TrainingTweet> tweets = tweetProvider.getNewTweetList();
			for (CSVRecord record : records) {
				// Read data into object model
				TrainingTweet tweet = tweetProvider.createTweet();
				this.mapTsvToTweet(record, tweet, lang);
				if (tweet != null && tweet.getText() != null) {
					i++;
					processor.preProcess(tweet);
					tweets.add(tweet);
				}
				System.out.println(i);
				if (i % BATCH_SIZE == 0) {
					//persist if batch is full
					repo.saveAll(tweets);
					tweets.clear();
				}
			}
			if (!tweets.isEmpty()) {
				repo.saveAll(tweets);
			}
		} catch (FileNotFoundException e) {
			String eString = exceptionService.exceptionToString(e);
			log.warn("Exception during Import: " + eString);
		} catch (IOException e) {
			String eString = exceptionService.exceptionToString(e);
			log.warn("Exception during Import: " + eString);
		}
	}

	/**
	 * Imports training tweets from a tsv while keeping a ratio of 1/3 nonoffensive and 2/3 offensive tweets.
	 * The tweets shoul be located in a tsv file with the text of a tweet in a column called "tweet" and an
	 * additional column called "subtask_a" containing a String starting with "OFF" if the tweet is offensive.
	 * @param lang the language of the tweets
	 */
	public void importFromTsvTwoThirdsOff(String lang) {
		int i = 0;
		int nonoff = 0;
		int off = 0;
		String tsvPath = this.env.getProperty("localTweetTsv.train." + lang);
		TweetPreProcessor processor = new ImportTweetPreProcessor();
		TweetProvider<TrainingTweet> tweetProvider = new TweetProvider<>() {
			@Override
			public TrainingTweet createTweet() {
				return new TrainingTweet();
			}};

		try {
			log.info("start importing tweets from " + tsvPath + ", keeping a ratio of 1/3 nonoffensive and 2/3 offensive tweets");
			FileInputStream fileInputStream = new FileInputStream(tsvPath);
			Reader reader = new BufferedReader(new InputStreamReader(fileInputStream));
			Iterable<CSVRecord> records = CSVFormat.TDF.withHeader().withQuote(null).parse(reader);
			List<TrainingTweet> tweets = tweetProvider.getNewTweetList();
			for (CSVRecord record : records) {
				// Read data into object model
				if (record.get("subtask_a").toLowerCase().startsWith("off")){
					nonoff++;
				} else {
					off++;
				}
			}
			int maxNonoff = Math.min(2 * nonoff, off);
			int maxOff = Math.min(nonoff, off / 2);
			log.info("About to import " + maxOff + " offensive and " + maxNonoff + "nonoffensive tweets.");
			off = 0;
			nonoff = 0;
			fileInputStream = new FileInputStream(tsvPath);
			reader = reader = new BufferedReader(new InputStreamReader(fileInputStream));
			records = CSVFormat.TDF.withHeader().withQuote(null).parse(reader);
			for (CSVRecord record : records) {
				if (record.get("subtask_a").toLowerCase().startsWith("off")){
					if (++off > maxOff) {
						continue;
					}
				} else if (++nonoff > maxNonoff){
					continue;
				}
				System.out.println("passed the guards");
				TrainingTweet tweet = tweetProvider.createTweet();
				this.mapTsvToTweet(record, tweet, lang);
				if (tweet != null && tweet.getText() != null) {
					i++;
					processor.preProcess(tweet);
					tweets.add(tweet);
					System.out.println("persisting tweet " + i + ": " + tweet.getText());
				}
				System.out.println(i);
				if (i % BATCH_SIZE == 0) {
					trainingTweetRepository.saveAll(tweets);
					tweets.clear();
				}
			}
			if (!tweets.isEmpty()) {
				trainingTweetRepository.saveAll(tweets);
			}
		} catch (FileNotFoundException e) {
			String eString = exceptionService.exceptionToString(e);
			log.warn("Exception during Import: " + eString);
		} catch (IOException e) {
			String eString = exceptionService.exceptionToString(e);
			log.warn("Exception during Import: " + eString);
		}
	}

	/**
	 * Imports test and training data for a given language from given file names.
	 * files should be tsv files with the text of a tweet in a column called "tweet"
	 * and an additional column called "subtask_a" containing a String starting with
	 * "OFF" if the tweet is offensive.
	 * @param lang the language of the data
	 * @param filenameTrain the name of the tsv containing the training data
	 * @param filenameTest the name of the tsv containing the test data
	 */
	public void importTsvTestAndTrain(Language lang, String filenameTrain, String filenameTest) {
		TweetPreProcessor preproc = new ImportTweetPreProcessor();
		String iso = lang.getIso();
		if (filenameTrain != null) {
			importFromTsv(filenameTrain, new TweetProvider<>() {
				@Override
				public TrainingTweet createTweet() {
					return new TrainingTweet();
				}
			}, trainingTweetRepository, preproc, iso);
		}
		if (filenameTest != null) {
			importFromTsv(filenameTest,new TweetProvider<TrainingTweet>()
			{
				@Override
				public TrainingTweet createTweet() {
					TrainingTweet tweet = new TrainingTweet();
					tweet.setTest(true);
					return tweet;
				}

			}, trainingTweetRepository, preproc, iso);
		}
	}

	/**
	 * Imports test and training data for a given language. File paths should be defined in the application properties as the language's iso code
	 * prefixed with "localTweetTsv.train." and "localTweetTsv.test.", files should be tsv files with the text of a tweet in a column called "tweet"
	 * and an additionla column called "subtask_a" containing a String starting with "OFF" if the tweet is offensive.
	 * @param lang the language of the data
	 */
	public void importTsvTestAndTrain(Language lang) {
		importTsvTestAndTrain(lang,
				this.env.getProperty("localTweetTsv.train." + lang.getIso()),
				this.env.getProperty("localTweetTsv.test." + lang.getIso())
						);

	}


	@Deprecated
	public void importExampleJson() {
		String jsonPath = this.env.getProperty("localTweetJson");
		importFromJson(jsonPath, new TweetProvider<Tweet>() {
					@Override
					public Tweet createTweet() {
						return new Tweet();
					}
				}, new ImportTweetPreProcessor()
				, tweetRepository);
	}

	private void mapTsvToTweet(CSVRecord record, AbstractTweet tweet, String lang) {
		tweet.setText(record.get("tweet"));
		tweet.setLanguage(lang);
		switch (record.get("subtask_a").substring(0,3).toUpperCase()) {
			case "OFF":
				tweet.setOffensive(true);
				break;
			default:
				tweet.setOffensive(false);
		}
	}

	private void mapJsonToTweet(JsonObject object, Tweet tweet) {
		String text = null;
		if (object.has("full_text")) {
			text = object.get("full_text").getAsString();
		} else if (object.has("text")){
			text = object.get("text").getAsString();
		}
		tweet.setText(text);
		if (object.has("id_str")) {
			tweet.setTwitterId(object.get("id_str").getAsString());
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
		tweet.setClassified(null);
	}

}

