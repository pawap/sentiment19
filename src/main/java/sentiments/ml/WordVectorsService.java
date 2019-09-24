package sentiments.ml;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import sentiments.domain.model.Language;

import java.io.File;
import java.util.HashMap;

/**
 * @author Paw
 *
 */
public class WordVectorsService {
	
    private static HashMap<Language,WordVectors> INSTANCE;

    private WordVectors wv;

    synchronized public static WordVectors getWordVectors(Language language) {
    	if (INSTANCE == null) {
    		INSTANCE = new HashMap<Language,WordVectors>();
    	}
    	if (!INSTANCE.containsKey(language)) {
            WordVectors vec = WordVectorSerializer.readWord2VecModel(new File(language.getWordVectorsFilename()));
            INSTANCE.put(language, vec);
    	}

        return INSTANCE.get(language);
    }


    synchronized public static void saveWordVectors(Word2Vec vec, Language language) {
        WordVectorSerializer.writeWord2VecModel(vec, new File(language.getWordVectorsFilename()));
        if (INSTANCE == null) {
            INSTANCE = new HashMap<Language,WordVectors>();
        }
        INSTANCE.put(language, vec);
    }
}
