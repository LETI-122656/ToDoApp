package com.example.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRCodeGenerator {

    public static void generateQRCode(String text, String filePath) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int width = 250;
        int height = 250;

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            System.out.println("QR Code gerado com sucesso: " + filePath);
        } catch (WriterException | IOException e) {
            System.err.println("Erro ao gerar QR Code: " + e.getMessage());
        }
    }
}