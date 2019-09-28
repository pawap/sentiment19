package sentiments.domain.service;

import org.springframework.stereotype.Service;

@Service
public class TaskService {

    public boolean isClassificationEnabled() {
        return classificationEnabled;
    }

    public void setClassificationEnabled(boolean classificationEnabled) {
        this.classificationEnabled = classificationEnabled;
    }

    private boolean classificationEnabled = false;
}
