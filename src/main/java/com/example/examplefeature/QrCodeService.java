package com.example.examplefeature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QrCodeService {

    private final TaskService taskService;

    // Base URL for your server, no /tasks path hardcoded
    @Value("${app.base-url}")
    private String baseUrl;

    public QrCodeService(TaskService taskService) {
        this.taskService = taskService;
    }

    /** Generate QR code URL for all tasks, returns Optional */
    public Optional<String> generateAllTasksUrlOptional() {
        List<Task> tasks = taskService.findAll();
        if (tasks.isEmpty()) {
            return Optional.empty();
        }

        String ids = tasks.stream()
                .map(task -> String.valueOf(task.getId()))
                .collect(Collectors.joining(","));

        return Optional.of(baseUrl + "/tasks?ids=" + ids);
    }

    /** Generate QR code URL for selected tasks, returns Optional */
    public Optional<String> generateSelectedTasksUrlOptional(Set<Task> selectedTasks) {
        if (selectedTasks.isEmpty()) {
            return Optional.empty();
        }

        String ids = selectedTasks.stream()
                .map(task -> String.valueOf(task.getId()))
                .collect(Collectors.joining(","));

        return Optional.of(baseUrl + "/tasks?ids=" + ids);
    }
}
