package com.example.examplefeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.QrCodeService;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.example.util.QRCodeGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
    private final Button generateBtn;

    public QrGeneratorView(TaskService taskService, QrCodeService qrCodeService) {
        this.taskService = taskService;
        this.qrCodeService = qrCodeService;

        generateBtn = new Button("Gerar QR", event -> generateQRs());
        generateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new ViewToolbar("Gerar QR", ViewToolbar.group(generateBtn)));

        grid = new Grid<>(Task.class, false);
        grid.addColumn(Task::getDescription).setHeader("Descrição");
        grid.addColumn(Task::getDueDate).setHeader("Due Date");
        grid.addColumn(Task::getCreationDate).setHeader("Creation Date");
        grid.setItems(taskService.findAll());
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Padding.MEDIUM
        );
        add(grid);
    }

    private void generateQRs() {
        Set<Task> selected = grid.getSelectedItems();
        Optional<String> urlOptional = qrCodeService.generateSelectedTasksUrlOptional(selected);

        if (urlOptional.isPresent()) {
            QRCodeGenerator.showQRCode(urlOptional.get());
        } else {
            Notification.show("Selecione pelo menos uma tarefa para gerar o QR code",
                            3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
