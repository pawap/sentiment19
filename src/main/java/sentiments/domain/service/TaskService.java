package sentiments.domain.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TaskService {


    private ConcurrentMap<String,Boolean> tasks;

    public TaskService() {
        this.tasks = new ConcurrentHashMap<>();
    }

    public boolean checkTaskExecution(String task, boolean activeByDefault) {
        if (this.tasks.get(task) == null) {
            this.tasks.put(task,activeByDefault);
        }
        return this.tasks.get(task);
    }
    public boolean checkTaskExecution(String task) {
        return this.checkTaskExecution(task,false);
    }

    public void setTaskStatus(String task, boolean active) {
        this.tasks.put(task,active);
    }
}
