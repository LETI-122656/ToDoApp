package com.example.examplefeature;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public void createTask(String description, java.time.LocalDate dueDate) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Descrição não pode estar vazia");
        }

        Task task = new Task(description, java.time.Instant.now());
        task.setDueDate(dueDate);
        repository.save(task);
    }

    public org.springframework.data.domain.Page<Task> list(org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable);
    }
}
