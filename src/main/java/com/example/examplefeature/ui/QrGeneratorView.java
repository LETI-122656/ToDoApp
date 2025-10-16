package com.example.examplefeature.ui;
import java.net.InetAddress;
import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.example.util.QRCodeGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route("generate-qrs")
@PageTitle("Gerar QR")
@Menu(order = 4, icon = "vaadin:qrcode", title = "Gerar QR")
public class QrGeneratorView extends Main {

    private final TaskService taskService;
    private final Grid<Task> grid;
    private final Checkbox generateAll;
    private final TextField baseName;
    private final Button generateBtn;

    public QrGeneratorView(TaskService taskService) {
        this.taskService = taskService;

        generateAll = new Checkbox("Gerar QRs para todas");
        baseName = new TextField("Nome base do ficheiro");
        baseName.setPlaceholder("Ex: Teste");
        generateBtn = new Button("Gerar QR", event -> generateQRs());
        generateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new ViewToolbar("Gerar QR", ViewToolbar.group(generateAll, baseName, generateBtn)));

        // ✅ Grid that supports multi-selection
        grid = new Grid<>(Task.class, false);
        grid.addColumn(Task::getDescription).setHeader("Descrição");
        grid.addColumn(Task::getDueDate).setHeader("Due Date");
        grid.addColumn(Task::getCreationDate).setHeader("Creation Date");
        grid.setItems(taskService.findAll());
        grid.setSelectionMode(Grid.SelectionMode.MULTI); // ✅ multi-selection

        // Apply styles
        addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Padding.MEDIUM
        );
        add(grid);
    }

    private void generateQRs() {
        List<Task> allTasks = taskService.findAll();
        if (allTasks.isEmpty()) {
            Notification.show("Nenhuma tarefa encontrada", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        String baseUrl = "http://192.168.0.239:8080/tasks";

        if (generateAll.getValue()) {
            // ✅ Generate QR for all tasks
            String ids = allTasks.stream()
                    .map(task -> String.valueOf(task.getId()))
                    .collect(Collectors.joining(","));
            String url = baseUrl + "?ids=" + ids;
            QRCodeGenerator.showQRCode(url);
        } else {
            // ✅ Generate QR for selected tasks
            Set<Task> selected = grid.getSelectedItems();
            if (selected.isEmpty()) {
                Notification.show("Selecione pelo menos uma tarefa ou marque 'Gerar QRs para todas'",
                                3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                return;
            }

            String ids = selected.stream()
                    .map(task -> String.valueOf(task.getId()))
                    .collect(Collectors.joining(","));
            String url = baseUrl + "?ids=" + ids;

            QRCodeGenerator.showQRCode(url);
        }
    }
}