package com.foldertoai.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Service
public class DirScribeService {
    private static final Set<String> VALID_EXTENSIONS = Set.of(
            ".js", ".jsx", ".ts", ".tsx", ".vue", ".svelte",
            ".py", ".rb", ".php", ".java", ".c", ".cpp", ".h", ".hpp",
            ".cs", ".go", ".rs", ".swift", ".kt", ".scala",
            ".html", ".htm", ".css", ".scss", ".sass", ".less",
            ".json", ".xml", ".yaml", ".yml", ".toml",
            ".sql", ".sh", ".bash", ".zsh", ".fish",
            ".r", ".m", ".pl", ".lua", ".dart", ".elm",
            ".clj", ".cljs", ".hs", ".ml", ".fs", ".ex", ".exs",
            ".jl", ".nim", ".cr", ".zig", ".odin", ".v",
            ".dockerfile", ".makefile", ".cmake", ".gradle",
            ".config", ".conf", ".ini", ".env"
    );
    private static final Set<String> IGNORE_PATTERNS = Set.of(
            "node_modules", "bower_components", "dist", "build", ".next", ".nuxt",
            "coverage", "vendor", "tmp", "temp", "logs", "log", ".git", ".svn",
            ".DS_Store", "Thumbs.db", "package-lock.json", "yarn.lock",
            ".vscode", ".idea", "__pycache__", ".pytest_cache", ".mypy_cache",
            "target", "bin", "obj", ".gradle", ".maven"
    );
    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            ".exe", ".dll", ".so", ".dylib", ".a", ".lib", ".o", ".obj",
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".ico",
            ".mp3", ".mp4", ".avi", ".mov", ".wav", ".flac",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".zip", ".tar", ".gz", ".rar", ".7z", ".dmg", ".iso"
    );
    private static final List<String> IMPORTANT_FILES = List.of(
            "dockerfile", "makefile", "rakefile", "gemfile", "procfile",
            "readme", "license", "changelog", "contributing", "authors"
    );

    public int collectSource(String inputDir, String outputFile) throws IOException {
        List<Path> files = new ArrayList<>();
        Path inputPath = Paths.get(inputDir);
        if (!Files.exists(inputPath) || !Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Input path does not exist or is not a directory: " + inputDir);
        }
        // Recursively scan for valid files
        Files.walkFileTree(inputPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (shouldInclude(file, inputPath)) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (shouldIgnore(dir, inputPath)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        // Write output
        StringBuilder content = new StringBuilder();
        content.append("Source code files structure\n\n");
        content.append(formatTree(inputPath, files));
        content.append("\n\n");
        for (Path file : files) {
            String relPath = inputPath.relativize(file).toString();
            String fileName = file.getFileName().toString();
            content.append("Name: ").append(fileName).append("\n");
            content.append("Path: ").append(relPath).append("\n");
            content.append("```").append("\n");
            try {
                content.append(Files.readString(file, StandardCharsets.UTF_8));
            } catch (IOException e) {
                content.append("[Error reading file: ").append(e.getMessage()).append("]\n");
            }
            content.append("\n```").append("\n\n");
        }
        Files.writeString(Paths.get(outputFile), content.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return files.size();
    }

    private boolean shouldIgnore(Path path, Path base) {
        Path rel = base.relativize(path);
        for (Path part : rel) {
            String partStr = part.toString();
            if (IGNORE_PATTERNS.stream().anyMatch(partStr::contains)) return true;
            if (partStr.startsWith(".") && !partStr.equals(".") && !partStr.equals("..")) return true;
        }
        return false;
    }

    private boolean shouldInclude(Path file, Path base) {
        if (shouldIgnore(file, base)) return false;
        String ext = getExtension(file.getFileName().toString()).toLowerCase();
        if (BINARY_EXTENSIONS.contains(ext)) return false;
        if (VALID_EXTENSIONS.contains(ext)) return true;
        String baseName = file.getFileName().toString().toLowerCase();
        return IMPORTANT_FILES.stream().anyMatch(baseName::startsWith);
    }

    private String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx == -1 ? "" : fileName.substring(idx);
    }

    private String formatTree(Path base, List<Path> files) {
        // Build a tree structure from the list of files
        TreeNode root = new TreeNode(base.getFileName() == null ? base.toString() : base.getFileName().toString());
        for (Path file : files) {
            Path rel = base.relativize(file);
            TreeNode curr = root;
            for (int i = 0; i < rel.getNameCount(); i++) {
                String part = rel.getName(i).toString();
                curr = curr.children.computeIfAbsent(part, TreeNode::new);
            }
        }
        StringBuilder sb = new StringBuilder();
        printTree(root, sb, "", true, false);
        return sb.toString();
    }

    // Helper class for tree nodes
    private static class TreeNode {
        String name;
        Map<String, TreeNode> children = new TreeMap<>();
        TreeNode(String name) { this.name = name; }
    }

    // Print the tree using ├──, │, └──, etc.
    private void printTree(TreeNode node, StringBuilder sb, String prefix, boolean isRoot, boolean isLast) {
        if (!isRoot) {
            sb.append(prefix);
            sb.append(isLast ? "└──" : "├──");
            sb.append(node.name).append("\n");
        } else {
            sb.append(node.name).append("\n");
        }
        List<TreeNode> children = new ArrayList<>(node.children.values());
        for (int i = 0; i < children.size(); i++) {
            boolean last = (i == children.size() - 1);
            String childPrefix = prefix + (isRoot ? "" : (isLast ? "    " : "│  "));
            printTree(children.get(i), sb, childPrefix, false, last);
        }
    }
} 