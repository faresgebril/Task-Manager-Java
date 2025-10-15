package main.java;

import java.io.Serializable;
import java.time.LocalDate;

public class Task implements Serializable {
    private String description;
    private String priority;
    private LocalDate dueDate;
    private boolean isCompleted;

    public Task(String description, String priority, LocalDate dueDate) {
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.isCompleted = false;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void markComplete() {
        this.isCompleted = true;
    }

    public void markIncomplete() {
        this.isCompleted = false;
    }

    @Override
    public String toString() {
        return isCompleted
                ? String.format("[✔] [%s] %s (Due: %s)", priority, description, dueDate)
                : String.format("[%s] %s (Due: %s)", priority, description, dueDate);
    }
}
