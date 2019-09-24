package sentiments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * entry point for the app
 *
 * @author Paw, 6runge
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application {

    /**
     * runs the application
     *
     * @param args execution arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
