package com.dances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TournamentDancesApp {
    public static void main(String[] args) {

        SpringApplication.run(TournamentDancesApp.class, args);

    }
}
