package com.example.utils;

public class QRCodeTest {
    public static void main(String[] args) {
        String outputPath = "qrcode_tarefa.png";
        System.out.println("A gerar QR Code em: " + System.getProperty("user.dir"));
        QRCodeGenerator.generateQRCode("Tarefa teste", outputPath);
    }
}
