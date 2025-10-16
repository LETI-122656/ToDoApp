package com.example.examplefeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.server.VaadinSession;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Route("tasks") // ✅ route updated so QR codes open this page
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
public class TaskListView extends Main implements BeforeEnterObserver {

    private final TaskService taskService;
    private final TextField description;
    private final DatePicker dueDate;
    private final Button createBtn;
    private final Grid<Task> taskGrid;

    public TaskListView(TaskService taskService) {
        this.taskService = taskService;

        // Input fields for new tasks
        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Date formatting
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(getLocale());

        // Task grid
        taskGrid = new Grid<>();
        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task ->
                Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Never")
        ).setHeader("Due Date");
        taskGrid.addColumn(task ->
                dateTimeFormatter.format(task.getCreationDate())
        ).setHeader("Creation Date");
        taskGrid.setSizeFull();

        // Default to all tasks
        taskGrid.setItems(taskService.findAll());

        // Layout styling
        setSizeFull();
        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.SMALL
        );

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn)));
        add(taskGrid);
    }

    private void createTask() {
        if (description.isEmpty()) {
            Notification.show("Please enter a task description", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.setItems(taskService.findAll());
        description.clear();
        dueDate.clear();

        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * ✅ This method makes the page react to QR code links like /tasks?ids=1,2,3
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters query = event.getLocation().getQueryParameters();

        if (query.getParameters().containsKey("ids")) {
            try {
                List<Long> ids = Arrays.stream(
                                query.getParameters().get("ids").get(0).split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                List<Task> allTasks = taskService.findAll();
                List<Task> filtered = allTasks.stream()
                        .filter(t -> ids.contains(t.getId()))
                        .collect(Collectors.toList());

                taskGrid.setItems(filtered);
            } catch (Exception e) {
                Notification.show("Invalid QR link format.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                taskGrid.setItems(taskService.findAll());
            }
        } else {
            // Normal view (no query parameters)
            taskGrid.setItems(taskService.findAll());
        }
    }
}