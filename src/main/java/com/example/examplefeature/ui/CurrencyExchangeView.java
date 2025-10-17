package com.example.examplefeature.ui;

import com.example.examplefeature.CurrencyExchange;
import com.example.examplefeature.ExchangeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Route("exchange")
@PageTitle("Currency Exchange")
@Menu(order = 3, icon = "vaadin:dollar", title = "Exchange")
public class CurrencyExchangeView extends Main {

    private static final List<String> CODES = List.of(
            "USD","EUR","GBP","JPY","AUD","CAD","CHF","CNY","INR",
            "BRL","SEK","NOK","NZD","ZAR","SGD","HKD","MXN","TRY","KRW","PLN","DKK"
    );

    private final CurrencyExchange service;
    private final ComboBox<String> from = new ComboBox<>("From");
    private final ComboBox<String> to   = new ComboBox<>("To");
    private final NumberField amount    = new NumberField("Amount");
    private final Button convertBtn     = new Button("Convert");
    private final Span resultLabel      = new Span();
    private final Grid<Row> table       = new Grid<>(Row.class, false);

    @Autowired
    public CurrencyExchangeView(CurrencyExchange service) {
        this.service = service;
        setupLayout();
        setupGrid();
        convertBtn.addClickListener(e -> convert());
        convert(); // primeira conversão automática
    }

    private void setupLayout() {
        from.setItems(CODES);
        from.setValue("USD");
        from.setWidth("9em");

        to.setItems(CODES);
        to.setValue("EUR");
        to.setWidth("9em");

        amount.setValue(100d);
        amount.setStep(1.0);
        amount.setWidth("8em");

        convertBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        convertBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        convertBtn.getStyle()
                .set("align-self", "end")                 // força alinhamento na base
                .set("height", "var(--lumo-size-m)")      // mesma altura dos campos
                .set("margin-top", "auto")
                .set("margin-bottom", "auto");


        // Layout horizontal para os controlos
        HorizontalLayout controls = new HorizontalLayout(from, to, amount, convertBtn);
        controls.setAlignItems(HorizontalLayout.Alignment.END);  // alinhamento vertical
        controls.setJustifyContentMode(HorizontalLayout.JustifyContentMode.START);
        controls.setSpacing(true);
        controls.setPadding(true);
        controls.setDefaultVerticalComponentAlignment(HorizontalLayout.Alignment.END);
        controls.setWidthFull();

        resultLabel.getStyle()
                .set("font-weight", "600")
                .set("margin-top", "0.5rem")
                .set("margin-bottom", "0.5rem");

        add(controls, resultLabel, table);
    }


    private void setupGrid() {
        table.addColumn(Row::code).setHeader("Currency").setAutoWidth(true);
        table.addColumn(r -> fmt(r.rate())).setHeader("Rate").setAutoWidth(true);
        table.addColumn(r -> fmt(r.value())).setHeader("Converted Value").setAutoWidth(true);
        table.setWidthFull();
        table.setHeight("500px");
    }

    private void convert() {
        try {
            String sourceCurrency = from.getValue();
            String targetCurrency = to.getValue();
            BigDecimal amt = BigDecimal.valueOf(amount.getValue()).setScale(4, RoundingMode.HALF_UP);

            BigDecimal converted = service.convert(amt, sourceCurrency, targetCurrency);
            resultLabel.setText(amt + " " + sourceCurrency + " ≈ " + converted + " " + targetCurrency);

            List<Row> rows = CODES.stream()
                    .map(code -> new Row(
                            code,
                            service.rate(sourceCurrency, code),
                            service.convert(amt, code, targetCurrency)
                    ))
                    .collect(Collectors.toList());
            table.setItems(rows);

        } catch (Exception ex) {
            Notification n = Notification.show("Conversion error: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private static String fmt(BigDecimal n) {
        return n.stripTrailingZeros().toPlainString();
    }

    public record Row(String code, BigDecimal rate, BigDecimal value) {}
}
