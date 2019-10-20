package sentiments.ml.service;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import sentiments.domain.model.Language;

import java.io.File;
import java.util.HashMap;

/**
 * Allows synchronized saving and loading of word vectors.
 * @author Paw
 */
public class WordVectorsService {
	
    private static HashMap<Language,WordVectors> INSTANCE;

    /**
     * Grants access to the desired {@link WordVectors}.
     * @param language the the desired {@link Language} of the desired {@link WordVectors}
     * @return the desired {@link WordVectors}
     */
    synchronized public static WordVectors getWordVectors(Language language) {
    	if (INSTANCE == null) {
    		INSTANCE = new HashMap<>();
    	}
    	if (!INSTANCE.containsKey(language)) {
            WordVectors vec = WordVectorSerializer.readWord2VecModel(new File(language.getWordVectorsFilename()));
            INSTANCE.put(language, vec);
    	}

        return INSTANCE.get(language);
    }

    /**
     * Persists a given the {@link Word2Vec} model
     * @param vec the {@link WordVectors}
     * @param language the the desired {@link Language} of the {@link WordVectors}
     */
    synchronized public static void saveWordVectors(Word2Vec vec, Language language) {
        WordVectorSerializer.writeWord2VecModel(vec, new File(language.getWordVectorsFilename()));
        if (INSTANCE == null) {
            INSTANCE = new HashMap<>();
        }
        INSTANCE.put(language, vec);
    }
}
