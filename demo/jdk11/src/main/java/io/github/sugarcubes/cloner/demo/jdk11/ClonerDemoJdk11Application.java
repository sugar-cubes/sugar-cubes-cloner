package io.github.sugarcubes.cloner.demo.jdk11;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.sugarcubes.cloner.Cloner;
import io.github.sugarcubes.cloner.Cloners;

@SpringBootApplication
public class ClonerDemoJdk11Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ClonerDemoJdk11Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Cloner cloner = Cloners.builder()
            .build();
    }

}
