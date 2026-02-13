package org.example.telegramhabit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramHabitApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramHabitApplication.class, args);
    }

}
