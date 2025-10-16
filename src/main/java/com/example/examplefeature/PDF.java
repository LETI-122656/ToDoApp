package com.example.examplefeature;

import com.example.examplefeature.Task;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

@Service
public class PDF {

    /**
     * Gera um PDF em bytes contendo os detalhes de uma única tarefa.
     */
    public byte[] generateTaskPdf(Task task, Locale locale) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String html = createSingleTaskHtml(task, locale);
            new PdfRendererBuilder()
                    .useFastMode()
                    .withHtmlContent(html, null)
                    .toStream(out)
                    .run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criar PDF da tarefa", e);
        }
    }

    /**
     * Gera um PDF em bytes contendo uma tabela com todas as tarefas.
     */
    public byte[] generateAllTasksPdf(List<Task> tasks, Locale locale) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String html = createAllTasksHtml(tasks, locale);
            new PdfRendererBuilder()
                    .useFastMode()
                    .withHtmlContent(html, null)
                    .toStream(out)
                    .run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criar PDF das tarefas", e);
        }
    }

    // ----------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------

    private String createSingleTaskHtml(Task task, Locale locale) {
        DateTimeFormatter dateFmt = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault());

        DateTimeFormatter dateTimeFmt = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault());

        String desc = safe(task.getDescription());
        String due = task.getDueDate() == null ? "Sem data" : safe(dateFmt.format(task.getDueDate()));
        String created = safe(dateTimeFmt.format(task.getCreationDate()));

        return """
            <html>
              <head>
                <meta charset="UTF-8"/>
                <style>
                  @page { size: A4; margin: 20mm; }
                  body { font-family: Arial, sans-serif; font-size: 12px; color: #222; }
                  h1 { font-size: 20px; margin-bottom: 10px; }
                  .label { font-weight: bold; }
                  .row { margin: 5px 0; }
                  hr { border: none; border-top: 1px solid #ccc; margin: 12px 0; }
                </style>
              </head>
              <body>
                <h1>Detalhes da Tarefa</h1>
                <div class="row"><span class="label">Descrição:</span> %s</div>
                <div class="row"><span class="label">Data limite:</span> %s</div>
                <div class="row"><span class="label">Criada em:</span> %s</div>
                <hr/>
                <div>Gerado em: %s</div>
              </body>
            </html>
            """.formatted(desc, due, created,
                safe(java.time.LocalDateTime.now().format(dateTimeFmt)));
    }

    private String createAllTasksHtml(List<Task> tasks, Locale locale) {
        DateTimeFormatter dateFmt = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault());

        DateTimeFormatter dateTimeFmt = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault());

        StringBuilder rows = new StringBuilder();
        for (Task t : tasks) {
            String desc = safe(t.getDescription());
            String due = t.getDueDate() == null ? "Sem data" : safe(dateFmt.format(t.getDueDate()));
            String created = safe(dateTimeFmt.format(t.getCreationDate()));
            rows.append("<tr>")
                    .append("<td>").append(desc).append("</td>")
                    .append("<td>").append(due).append("</td>")
                    .append("<td>").append(created).append("</td>")
                    .append("</tr>");
        }

        return """
            <html>
              <head>
                <meta charset="UTF-8"/>
                <style>
                  @page { size: A4; margin: 20mm; }
                  body { font-family: Arial, sans-serif; font-size: 12px; color: #222; }
                  h1 { font-size: 18px; margin-bottom: 10px; }
                  table { width: 100%%; border-collapse: collapse; }
                  th, td { padding: 6px 8px; border-bottom: 1px solid #ccc; text-align: left; }
                </style>
              </head>
              <body>
                <h1>Lista de Tarefas</h1>
                <table>
                  <thead>
                    <tr><th>Descrição</th><th>Data limite</th><th>Data de criação</th></tr>
                  </thead>
                  <tbody>
                    %s
                  </tbody>
                </table>
              </body>
            </html>
            """.formatted(rows.toString());
    }

    private String safe(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
