package com.example.base.ui.component;

import com.example.util.QRCodeGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class QrCodeButton extends HorizontalLayout {

    public QrCodeButton(String textToEncode) {
        // Create the button
        Button qrButton = new Button("Show QR Code", event -> QRCodeGenerator.showQRCode(textToEncode));
        qrButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(qrButton);
    }
}
