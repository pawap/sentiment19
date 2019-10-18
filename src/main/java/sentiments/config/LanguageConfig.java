package sentiments.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import sentiments.domain.service.LanguageService;

/**
 * @author Paw
 */
@Configuration
public class LanguageConfig {

    @Autowired
    public void configureGlobal(LanguageService languageService) {
        languageService.setupLanguages(new String[]{"en","de"});
    }

}
