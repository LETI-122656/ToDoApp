package com.example;

import com.example.utils.QRCodeGenerator;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("default")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        QRCodeGenerator.generateQRCode("ToDoApp", "qrcode_app.png");
        System.out.println("QR Code criado");
    }
}
