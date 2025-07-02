package com.foldertoai.cli;

import picocli.CommandLine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.foldertoai.service.DirScribeService;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@CommandLine.Command(name = "dirscribe", mixinStandardHelpOptions = true, version = "1.0",
        description = "A tool to consolidate multiple files into a single .txt file.")
public class DirScribeCommand implements Runnable, CommandLineRunner {
    @CommandLine.Option(names = {"-i", "--input"}, description = "Input directory path", required = true)
    private String input;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Output file path", defaultValue = "output.txt")
    private String output;

    @CommandLine.Option(names = {"--fast"}, description = "Faster result but may be unordered", defaultValue = "false")
    private boolean fast;

    @Autowired
    private DirScribeService dirScribeService;

    @Override
    public void run() {
        try {
            int count = dirScribeService.collectSource(input, output);
            System.out.printf("Processed %d files. Output written to %s\n", count, output);
        } catch (Exception e) {
            System.err.printf("Error: %s\n", e.getMessage());
        }
    }

    @Override
    public void run(String... args) {
        new CommandLine(this).execute(args);
    }
} 