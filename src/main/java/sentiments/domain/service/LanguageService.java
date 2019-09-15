package sentiments.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sentiments.domain.model.Language;
import sentiments.domain.repository.LanguageRepository;

@Service
public class LanguageService {

    @Autowired
    LanguageRepository languageRepository;

    public Language getLanguage(String iso) {
        return languageRepository.findOneByIso(iso);
    }

}
