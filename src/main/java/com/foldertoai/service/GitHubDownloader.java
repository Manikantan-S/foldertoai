package com.foldertoai.service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GitHubDownloader {
    /**
     * Downloads and extracts a GitHub repository (main or master branch) to a temporary directory.
     * Returns the path to the extracted root folder.
     */
    public static Path downloadAndExtract(String repoUrl, Path tempDir) throws IOException, InterruptedException {
        String[] parts = parseGitHubUrl(repoUrl);
        if (parts == null) {
            throw new IllegalArgumentException("Invalid GitHub URL: " + repoUrl);
        }
        String owner = parts[0];
        String repo = parts[1];
        String[] branches = {"main", "master"};
        Exception lastError = null;
        for (String branch : branches) {
            String zipUrl = String.format("https://github.com/%s/%s/archive/refs/heads/%s.zip", owner, repo, branch);
            try {
                Path zipPath = tempDir.resolve(repo + "-" + branch + ".zip");
                downloadFile(zipUrl, zipPath);
                unzip(zipPath, tempDir);
                Path extractedDir = tempDir.resolve(repo + "-" + branch);
                if (Files.exists(extractedDir)) {
                    Files.deleteIfExists(zipPath);
                    return extractedDir;
                }
            } catch (Exception e) {
                lastError = e;
            }
        }
        throw new IOException("Failed to download or extract repo: " + repoUrl, lastError);
    }

    /**
     * Parses a GitHub repository URL and returns [owner, repo] or null if invalid.
     */
    private static String[] parseGitHubUrl(String url) {
        String regex = "https://github.com/([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)(?:.git)?/?";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(url);
        if (m.matches()) return new String[]{m.group(1), m.group(2)};
        return null;
    }

    /**
     * Downloads a file from the given URL to the destination path.
     */
    private static void downloadFile(String url, Path dest) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(dest));
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download: " + url);
        }
    }

    /**
     * Unzips the given zip file into the destination directory, protecting against zip slip.
     */
    private static void unzip(Path zipFile, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = zipSlipProtect(entry, destDir);
                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    try (OutputStream os = Files.newOutputStream(newPath)) {
                        zis.transferTo(os);
                    }
                }
            }
        }
    }

    /**
     * Protects against zip slip vulnerability.
     */
    private static Path zipSlipProtect(ZipEntry entry, Path destDir) throws IOException {
        Path target = destDir.resolve(entry.getName());
        Path norm = target.normalize();
        if (!norm.startsWith(destDir)) {
            throw new IOException("Bad zip entry: " + entry.getName());
        }
        return norm;
    }
} 