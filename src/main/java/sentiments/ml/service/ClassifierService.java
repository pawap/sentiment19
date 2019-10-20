package sentiments.ml.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sentiments.domain.model.Language;
import sentiments.domain.repository.tweet.TrainingTweetRepository;
import sentiments.ml.classifier.Classifier;
import sentiments.ml.classifier.W2VTweetClassifier;

import java.util.HashMap;

/**
 * @author Paw
 */
@Service
public class ClassifierService {

    @Autowired
    TrainingTweetRepository tweetRepository;

    private HashMap<Language, Classifier> classifiers;

    /**
     * basic constructor
     */
    public ClassifierService() {
        this.classifiers = new HashMap<>();
    }

    /**
     * Creates a language specific {@link Classifier}.
     * @param language the language of the desired {@link Classifier}
     * @return a {@link Classifier} for the given {@link Language}
     */
    public Classifier getClassifier(Language language) {
        return new W2VTweetClassifier(language);
    }

    /**
     * Trains a classifier for a given language.
     * @param language the language a classifier should be trained for
     */
    public void trainClassifier(Language language) {
        this.getClassifier(language).train(tweetRepository);
    }

}


