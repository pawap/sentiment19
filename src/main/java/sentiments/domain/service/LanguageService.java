package sentiments.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sentiments.domain.model.Language;
import sentiments.domain.repository.LanguageRepository;

import java.util.HashMap;

/**
 * Offers access to all available {@link Language}s.
 * @author Paw
 */
@Service
public class LanguageService {

    LanguageRepository languageRepository;

    private HashMap<String, Language> languages;

    @Autowired
    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
        reload();
    }

    /**
     * Update the currently active languages from the DB.
     */
    public void reload() {
        languages = new HashMap<>();
        for(Language language: languageRepository.findAllByActive(true)) {
            languages.put(language.getIso(),language);
        }
    }

    /**
     * @param iso the iso code of the desired {@link Language}
     * @return the desired {@link Language}
     */
    public Language getLanguage(String iso) {
        return languages.get(iso.toLowerCase());
    }

    /**
     * Registers {@link Language}s provided as a String array of iso codes
     * @param isoCodes an array of iso codes
     */
    public void setupLanguages(String[] isoCodes) {
        for (String isoCode : isoCodes) {
            Language language = languages.get(isoCode);
            if (language == null) {
                language = languageRepository.findOneByIso(isoCode);
                if (language == null) {
                    language = new Language();
                    language.setIso(isoCode);
                    language.setWordVectorsFilename("resources/word2vec_" + isoCode + ".bin");
                    language.setClassifierFilename("resources/classifier_" + isoCode + ".nn");
                    language.setName(isoCode);
                    language.setActive(true);
                }
                languageRepository.save(language);
                languages.put(isoCode,language);
            }
        }
    }

    /**
     * {@link #reload() reload} might have to be called before this to guaranty correct results
     * @return all active languages
     */
    public Iterable<Language> getAvailableLanguages() {
            return languages.values();

    }
}
