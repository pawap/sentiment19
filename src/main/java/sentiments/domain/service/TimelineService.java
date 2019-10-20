package sentiments.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sentiments.domain.model.DayStats;
import sentiments.domain.model.Language;
import sentiments.domain.model.query.Timeline;
import sentiments.domain.model.query.TweetFilter;
import sentiments.domain.repository.DayStatsRepository;
import sentiments.domain.repository.tweet.TweetRepository;
import sentiments.domain.service.LanguageService;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * @author paw, 6runge
 */
@Service
public class TimelineService {

    @Autowired
    LanguageService languageService;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    DayStatsRepository dayStatsRepository;

    private int version;

    public TimelineService() {
        this.version = 0;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Timeline getTimeline(TweetFilter tf) {
        Timeline timeline = new Timeline(); // tweetRepository.countByOffensiveAndDayInInterval(tf);
        switch (version) {
            case (0): {

                timeline.timeline = new LinkedList<>();
                timeline.start = tf.getStart() != null ? tf.getStart().toLocalDateTime().toLocalDate() : tweetRepository.getFirstDate();
                timeline.end = tf.getEnd() != null ? tf.getEnd().toLocalDateTime().toLocalDate() : tweetRepository.getLastDate();
                LocalDate current = LocalDate.from(timeline.start);
                List<String> list = new LinkedList<>();
                for (Language l : languageService.getAvailableLanguages()) {
                    list.add(l.getIso());
                }
                Iterable<DayStats> dayStats = dayStatsRepository.findByDateBetweenAndLanguageInOrderBy(timeline.start, timeline.end, tf.getLanguages().isEmpty() ? list : tf.getLanguages());
                for (DayStats ds : dayStats) {
                    LocalDate currentDate = ds.getDate();
                    while (current.compareTo(currentDate) < 0) {
                        timeline.timeline.add(0);
                        current = current.plusDays(1);
                    }
                    current = current.plusDays(1);
                    timeline.timeline.add(tf.isOffensive() ? ds.getOffensive() : ds.getNonoffensive());

                }
                while (current.compareTo(timeline.end) < 0) {
                    timeline.timeline.add(0);
                    current = current.plusDays(1);
                }
                break;
            }
            case(1):
            default:{
                timeline = tweetRepository.countByOffensiveAndDayInInterval(tf);
            }
        }
        return timeline;
    }
}
