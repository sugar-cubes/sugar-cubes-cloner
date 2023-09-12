package io.github.sugarcubes.cloner.demo.jdk17;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.sugarcubes.cloner.Cloner;
import io.github.sugarcubes.cloner.Cloners;

@SpringBootApplication
public class ClonerDemoJdk17Application implements CommandLineRunner {

    static {
        String cmdLine;
        try {
            cmdLine = FileUtils.readFileToString(new File("/proc/self/cmdline"), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("cmdLine = " + cmdLine);
        System.out.println("this module = " + ClonerDemoJdk17Application.class.getModule());
        System.out.println("this classloader = " + ClonerDemoJdk17Application.class.getClassLoader());
        System.out.println("cloner module = " + Cloner.class.getModule());
        System.out.println("cloner classloader = " + Cloner.class.getClassLoader());
    }

    public static void main(String[] args) {
        SpringApplication.run(ClonerDemoJdk17Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Cloner cloner = Cloners.builder()
            .build();
        Date date = cloner.clone(new Date());
        System.out.println("date = " + date);
    }

}
