package sentiments.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import sentiments.domain.service.LanguageService;

@Configuration
public class LanguageConfig {

    @Autowired
    LanguageService languageService;

    @Autowired
    public void configureGlobal() {
        languageService.setupLanguages(new String[]{"en","de"});
    }

}
