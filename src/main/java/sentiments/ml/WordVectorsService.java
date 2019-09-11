package sentiments.ml;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import java.io.File;

/**
 * @author Paw
 *
 */
public class WordVectorsService {
	
    private static WordVectors INSTANCE;

    private static String vectorPath = "resources/word2vec.bin";

    private WordVectors wv;
    
    private WordVectorsService() {}

    public static String getWordVectorPath() {
        return vectorPath;
    }

    synchronized public static WordVectors getWordVectors() {
    	if (INSTANCE == null) {
    		INSTANCE = WordVectorSerializer.loadStaticModel(new File(vectorPath));
    	}
        return INSTANCE;
    }

}
