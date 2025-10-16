package com.example.examplefeature.ui;

import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.example.examplefeature.PDF;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Base64;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("pdf-generator")
@PageTitle("Exportar Tarefas em PDF")
@Menu(order = 4, icon = "vaadin:file-text", title = "Gerar PDF")
public class PDFView extends Main {

    private final TaskService taskService;
    private final PDF pdfService;

    private final Grid<Task> grid = new Grid<>();
    private final Checkbox allCheckbox = new Checkbox("Incluir todas as tarefas");
    private final TextField fileName = new TextField("Nome do ficheiro");
    private final Button generateButton = new Button("Gerar PDF");

    public PDFView(TaskService taskService, PDF pdfService) {
        this.taskService = taskService;
        this.pdfService = pdfService;

        addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        setSizeFull();

        fileName.setPlaceholder("Ex: tarefas_equipa");
        fileName.setClearButtonVisible(true);
        fileName.setWidth("16em");

        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generateButton.setEnabled(false);
        generateButton.addClickListener(e -> handleGenerate());

        allCheckbox.addValueChangeListener(e ->
                generateButton.setEnabled(e.getValue() || grid.asSingleSelect().getValue() != null)
        );

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addColumn(Task::getDescription).setHeader("Descrição").setAutoWidth(true);
        grid.addColumn(t -> t.getDueDate() == null ? "—" : t.getDueDate().toString())
                .setHeader("Data Limite").setAutoWidth(true);
        grid.addColumn(t -> t.getCreationDate().toString()).setHeader("Criada em").setAutoWidth(true);

        grid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        grid.asSingleSelect().addValueChangeListener(e ->
                generateButton.setEnabled(allCheckbox.getValue() || e.getValue() != null)
        );

        HorizontalLayout controls = new HorizontalLayout(fileName, allCheckbox, generateButton);
        controls.setAlignItems(FlexComponent.Alignment.END);
        add(controls, grid);
    }

    private void handleGenerate() {
        try {
            boolean all = allCheckbox.getValue();
            List<Task> tasks;
            byte[] pdf;

            if (all) {
                tasks = taskService.list(PageRequest.of(0, 1000)).getContent();
                if (tasks.isEmpty()) {
                    showWarn("Não existem tarefas para exportar.");
                    return;
                }
                pdf = pdfService.generateAllTasksPdf(tasks, Locale.getDefault());

            } else {
                Task selected = grid.asSingleSelect().getValue();
                if (selected == null) {
                    showWarn("Seleciona uma tarefa primeiro.");
                    return;
                }
                pdf = pdfService.generateTaskPdf(selected, Locale.getDefault());

            }

            String safeName = sanitize(fileName.getValue().isBlank()
                    ? (all ? "tarefas" : "tarefa") : fileName.getValue());
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String finalName = safeName + "_" + timestamp + ".pdf";

            // Cria download temporário invisível e inicia automaticamente
            String base64 = Base64.getEncoder().encodeToString(pdf);

            UI.getCurrent().getPage().executeJs(
                    "const binary = atob($0);" +
                            "const len = binary.length;" +
                            "const bytes = new Uint8Array(len);" +
                            "for (let i = 0; i < len; i++) bytes[i] = binary.charCodeAt(i);" +
                            "const blob = new Blob([bytes], {type: 'application/pdf'});" +
                            "const link = document.createElement('a');" +
                            "link.href = URL.createObjectURL(blob);" +
                            "link.download = $1;" +
                            "document.body.appendChild(link);" +
                            "link.click();" +
                            "document.body.removeChild(link);",
                    base64, finalName
            );


            showSuccess("PDF gerado e descarregado com sucesso!");
            fileName.clear();

        } catch (Exception ex) {
            showError("Erro ao gerar o PDF: " + ex.getMessage());
        }
    }

    private String sanitize(String s) {
        return s.strip().replaceAll("[^\\p{L}\\p{N}_-]+", "_");
    }

    private void showWarn(String msg) {
        Notification.show(msg, 4000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    private void showError(String msg) {
        Notification.show(msg, 4000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String msg) {
        Notification.show(msg, 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
