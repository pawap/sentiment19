package sentiments.ml.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sentiments.domain.model.Language;
import sentiments.domain.repository.tweet.TrainingTweetRepository;
import sentiments.ml.classifier.Classifier;
import sentiments.ml.classifier.W2VTweetClassifier;

import java.util.HashMap;

@Service
public class ClassifierService {

    @Autowired
    TrainingTweetRepository tweetRepository;

    private HashMap<Language, Classifier> classifiers;

    public ClassifierService() {
        this.classifiers = new HashMap<>();
    }
//TODO reutrn null if bad lang
    public Classifier getClassifier(Language language) {
        return new W2VTweetClassifier(language);
//        if (!this.classifiers.containsKey(language)){
//            Classifier classifier = new W2VTweetClassifier(language);
//            if (!classifier.isTrained()) {
//                System.out.println("Classifier for language " + language.getName() + " has no persitent model. Training.");
//                classifier.train(tweetRepository);
//            }
//            this.classifiers.put(language, classifier);
//        }
//
//        return this.classifiers.get(language);
    }

    public void trainClassifier(Language language) {
        this.getClassifier(language).train(tweetRepository);
    }

}


