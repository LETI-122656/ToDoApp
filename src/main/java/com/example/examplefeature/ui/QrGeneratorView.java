package com.example.examplefeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.QrCodeService;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.example.util.QRCodeGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Optional;
import java.util.Set;

@Route("generate-qrs")
@PageTitle("Gerar QR")
@Menu(order = 4, icon = "vaadin:qrcode", title = "Gerar QR")
public class QrGeneratorView extends Main {

    private final TaskService taskService;
    private final QrCodeService qrCodeService;
    private final Grid<Task> grid;
    private final Checkbox generateAll;
    private final TextField baseName;
    private final Button generateBtn;

    public QrGeneratorView(TaskService taskService, QrCodeService qrCodeService) {
        this.taskService = taskService;
        this.qrCodeService = qrCodeService;

        generateAll = new Checkbox("Gerar QRs para todas");
        baseName = new TextField("Nome base do ficheiro");
        baseName.setPlaceholder("Ex: Teste");
        generateBtn = new Button("Gerar QR", event -> generateQRs());
        generateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new ViewToolbar("Gerar QR", ViewToolbar.group(generateAll, baseName, generateBtn)));

        // Grid with multi-selection
        grid = new Grid<>(Task.class, false);
        grid.addColumn(Task::getDescription).setHeader("Descrição");
        grid.addColumn(Task::getDueDate).setHeader("Due Date");
        grid.addColumn(Task::getCreationDate).setHeader("Creation Date");
        grid.setItems(taskService.findAll());
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        // Apply layout styles
        addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Padding.MEDIUM
        );
        add(grid);
    }

    private void generateQRs() {
        Optional<String> urlOptional;

        if (generateAll.getValue()) {
            urlOptional = qrCodeService.generateAllTasksUrlOptional();
        } else {
            Set<Task> selected = grid.getSelectedItems();
            urlOptional = qrCodeService.generateSelectedTasksUrlOptional(selected);
        }

        if (urlOptional.isPresent()) {
            QRCodeGenerator.showQRCode(urlOptional.get());
        } else {
            Notification.show("Nenhuma tarefa encontrada ou selecionada",
                            3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
