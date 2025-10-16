package com.example.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QRCodeGenerator {

    /**
     * Show a dialog with a PNG QR code for the given text.
     *
     * @param text the content encoded in the QR code
     */
    public static void showQRCode(String text) {
        showQRCode(text, 250);
    }

    /**
     * Show a dialog with a PNG QR code for the given text and size (px).
     *
     * @param text the content encoded in the QR code
     * @param size width and height in pixels of the produced QR image
     */
    public static void showQRCode(String text, int size) {
        byte[] pngData;
        // generate PNG bytes
        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            pngData = pngOutputStream.toByteArray();
        } catch (WriterException e) {
            Notification.show("Failed to generate QR code (encoding error): " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            return;
        } catch (IOException e) {
            Notification.show("Failed to generate QR code (IO error): " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            return;
        }

        // Create Vaadin StreamResource from PNG bytes
        StreamResource resource = new StreamResource("qrcode.png", () -> new ByteArrayInputStream(pngData));
        Image qrImage = new Image(resource, "QR Code");
        qrImage.setAlt("QR code image");

        // Build dialog and open it (create dialog before referencing it)
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        Button close = new Button("Close", e -> dialog.close());
        VerticalLayout layout = new VerticalLayout(qrImage, close);
        layout.setPadding(true);
        layout.setSpacing(true);

        dialog.add(layout);
        dialog.open();
    }
}