package com.foldertoai.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import com.foldertoai.service.JobManager;
import com.foldertoai.service.DirScribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.CompletableFuture;
import com.foldertoai.service.GitHubDownloader;
import java.nio.file.Files;
import java.nio.file.Path;


@Configuration
@EnableAsync
class AsyncConfig {}

@RestController
@RequestMapping("/api")
public class DirScribeController {
    @Autowired
    private JobManager jobManager;
    @Autowired
    private DirScribeService dirScribeService;

    @PostMapping("/clone")
    public ResponseEntity<Map<String, Object>> cloneRepo(@RequestBody Map<String, String> body) {
        String input = body.get("input");
        String output = body.getOrDefault("output", "output.txt");
        String jobId = jobManager.createJob();
        processJobAsync(jobId, input, output);
        return ResponseEntity.ok(Map.of("jobId", jobId, "status", "started"));
    }

    @Async
    public CompletableFuture<Void> processJobAsync(String jobId, String input, String output) {
        jobManager.updateJob(jobId, JobManager.Status.RUNNING, "Processing...", null, 0, null);
        Path tempDir = null;
        try {
            String processDir = input;
            if (input != null && input.startsWith("https://github.com/")) {
                tempDir = Files.createTempDirectory("repo-");
                Path extracted = GitHubDownloader.downloadAndExtract(input, tempDir);
                processDir = extracted.toString();
            }
            int count = dirScribeService.collectSource(processDir, output);
            jobManager.updateJob(jobId, JobManager.Status.COMPLETED, "Completed", output, count, null);
        } catch (Exception e) {
            jobManager.updateJob(jobId, JobManager.Status.FAILED, "Failed", null, 0, e.getMessage());
        } finally {
            if (tempDir != null) {
                try { Files.walk(tempDir).sorted(java.util.Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete); } catch (Exception ignore) {}
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String id) {
        JobManager.JobInfo info = jobManager.getJob(id);
        if (info == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Job not found"));
        }
        return ResponseEntity.ok(Map.of(
                "status", info.status,
                "message", info.message,
                "outputPath", info.outputPath,
                "filesProcessed", info.filesProcessed,
                "error", info.error
        ));
    }
} 