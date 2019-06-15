package sentiments;

import java.io.File;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

/**
 * @author Paw
 *
 */
public class WordVectorsService {
	
    private static WordVectors INSTANCE;
    
    private WordVectors wv;
    
    private WordVectorsService() {}
    
    synchronized public static WordVectors getWordVectors() {
    	if (INSTANCE == null) {
    		INSTANCE = WordVectorSerializer.loadStaticModel(new File("resources/GoogleNews-vectors-negative300.bin.gz"));
    	}
        return INSTANCE;
    }

}
