package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ShareItGatewayApp {
    public static void main(String[] args) {
        SpringApplication.run(ShareItGatewayApp.class, args);
    }
}
