package com.example.springboot;

import java.util.concurrent.CompletableFuture;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.JsonNode;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Entry;
import com.linecorp.centraldogma.common.Query;
import com.linecorp.centraldogma.common.Revision;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // CentralDogma is injected automatically by CentralDogmaConfiguration.
    @Bean
    public CommandLineRunner commandLineRunner(CentralDogma dogma) {
        return captureValueChange(dogma);
    }

    private static CommandLineRunner getProjects(CentralDogma dogma) {
        return args -> {
            System.err.println(dogma.listProjects().join());
        };
    }

    private static CommandLineRunner getFile(CentralDogma dogma) {
        return args -> {
            CompletableFuture<Entry<JsonNode>> future =
                    dogma.getFile("project-1", "repository-1", Revision.HEAD,
                                  Query.ofJson("/file.json"));

            var result = future.join();
            System.err.println(result.contentAsPrettyText());
        };
    }

    private static CommandLineRunner getValue(CentralDogma dogma) {
        return args -> {
            CompletableFuture<Entry<JsonNode>> future =
                    dogma.getFile("project-1", "repository-1", Revision.HEAD,
                                  Query.ofJsonPath("/file.json", "$.foo"));

            var result = future.join();
            System.err.println(result.contentAsPrettyText());
        };
    }

    private static CommandLineRunner captureValueChange(CentralDogma dogma) {
        return args -> {
            Watcher<JsonNode> watcher =
                    dogma.fileWatcher("project-1", "repository-1",
                                      Query.ofJsonPath("/file.json", "$.foo"));
            // Registering a callback to capture changes
            watcher.watch((revision, value) -> {
                System.err.printf("Updated to %s at %s%n", value, revision);
            });
        };
    }
}
