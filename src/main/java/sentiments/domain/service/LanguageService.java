package sentiments.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sentiments.domain.model.Language;
import sentiments.domain.repository.LanguageRepository;

import java.util.HashMap;

@Service
public class LanguageService {


    LanguageRepository languageRepository;

    private HashMap<String, Language> langs;

    @Autowired
    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
        reload();
    }

    public void reload() {
        langs = new HashMap<>();
        for(Language language: languageRepository.findAllByActive(true)) {
            langs.put(language.getIso(),language);
        }
    }

    public Language getLanguage(String iso) {
        return langs.get(iso);
    }

    public void setupLanguages(String[] isoCodes) {
        for (String s : isoCodes) {
            Language l = langs.get(s);
            if (l == null) {
                l = languageRepository.findOneByIso(s);
                if (l == null) {
                    l = new Language();
                    l.setIso(s);
                    l.setWordVectorsFilename("resources/word2vec_" + s + ".bin");
                    l.setClassifierFilename("resources/classifier_" + s + ".nn");
                    l.setName(s);
                    langs.put(s,l);
                    languageRepository.save(l);
                }
            }
        }
    }
}
