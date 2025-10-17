package com.example.examplefeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Email;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

/**
 * View que permite enviar por email os detalhes de uma tarefa.
 */
@Route("email")
@PageTitle("Enviar E-mail")
@Menu(order = 5, icon = "vaadin:envelope", title = "Enviar E-mail")
public class EmailView extends Main {

    private final Email emailService;
    private final TaskService taskService;

    private final Grid<Task> grid = new Grid<>(Task.class, false);
    private final EmailField destinatario = new EmailField("Destinatário");
    private final Button enviarBtn = new Button("Enviar Email");

    @Autowired
    public EmailView(Email emailService, TaskService taskService) {
        this.emailService = emailService;
        this.taskService = taskService;

        addClassNames(LumoUtility.Padding.MEDIUM,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.MEDIUM);
        setSizeFull();

        // Campo destinatário
        destinatario.setPlaceholder("ex: colega@iscte-iul.pt");
        destinatario.setClearButtonVisible(true);
        destinatario.setWidth("20em");

        // Botão enviar
        enviarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        enviarBtn.setEnabled(false);
        enviarBtn.addClickListener(e -> enviarEmail());

        // Toolbar no topo da página
        var toolbar = new ViewToolbar("Seleciona a tarefa a enviar",
                ViewToolbar.group(destinatario, enviarBtn));
        add(toolbar);

        // Grid de tarefas
        grid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        grid.addColumn(Task::getDescription).setHeader("Descrição").setAutoWidth(true);
        grid.addColumn(t -> t.getDueDate() == null ? "Sem prazo" : t.getDueDate().toString())
                .setHeader("Prazo").setAutoWidth(true);
        grid.addColumn(t -> t.getCreationDate().toString())
                .setHeader("Criada em").setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();

        // Ativa botão apenas quando há tarefa + email
        grid.asSingleSelect().addValueChangeListener(e ->
                enviarBtn.setEnabled(validarCampos()));
        destinatario.addValueChangeListener(e ->
                enviarBtn.setEnabled(validarCampos()));

        add(grid);
    }

    private boolean validarCampos() {
        return grid.asSingleSelect().getValue() != null
                && destinatario.getValue() != null
                && !destinatario.getValue().isBlank();
    }

    private void enviarEmail() {
        Task t = grid.asSingleSelect().getValue();
        if (t == null) {
            Notification.show("Seleciona uma tarefa.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        String to = destinatario.getValue().trim();
        String subject = "Tarefa: " + (t.getDescription() == null ? "(Sem descrição)" : t.getDescription());
        String due = t.getDueDate() == null ? "Sem data limite" :
                t.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()));

        String body = """
                Olá,

                Aqui estão os detalhes da tarefa:

                📌 Descrição: %s
                🗓️ Prazo: %s

                Enviado automaticamente pela aplicação To-Do App.
                """.formatted(t.getDescription(), due);

        try {
            emailService.sendSimpleMail(to, subject, body);
            Notification.show("Email enviado com sucesso para " + to, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            Notification.show("Erro ao enviar email: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            ex.printStackTrace();
        }
    }
}
