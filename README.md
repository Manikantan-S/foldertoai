# FoldertoAI

A lightweight, DSA and backend-focused utility designed to aggregate the contents of multiple files into a single `.txt` document. Ideal for preprocessing and consolidating textual data, particularly for input into machine learning, natural language processing pipelines, or AI models.

---

## Table of Contents
- [Overview](#overview)
- [Getting Started](#getting-started)
- [Installation](#installation)
- [Usage](#usage)
- [Supported Flags](#supported-flags)
- [Algorithm (Step-by-Step)](#algorithm-step-by-step)
- [Technical Details & DSA](#technical-details--dsa)
- [Role of Spring Boot](#role-of-spring-boot)

---

## Overview
FoldertoAI scans a directory, filters and aggregates the contents of source code, json files etc and text files, and outputs a single, well-structured `.txt` file. It is optimized for speed and memory efficiency, making it suitable for large codebases and data preprocessing for AI/ML tasks.

---

## Getting Started
This tool helps you quickly gather and merge source code or text files from a directory into a single output file, making it easy to prepare data for AI and NLP tasks.

---

## Installation
1. Ensure you have Java 17 or higher installed.
2. Clone this repository or download the source code.
3. Change directory and build the project using Maven:
   ```sh
   cd foldertoai
   mvn clean package
   ```

---

## Usage
Run the application from the command line:

```sh
java -jar target/dirscribe-1.0-SNAPSHOT.jar -i "/Users/manismagic/Desktop/codeforces" -o "/path/to/output.txt" [flags]
```
- `-i <input_directory>`: The root directory to scan for files (required).
- `-o <output_file>`: The path to the output `.txt` file (required).
- `[flags]`: Optional flags for customization (see below).

---

## Supported Flags
- `-i, --input <input_directory>`: Specify the input directory to scan (required).
- `-o, --output <output_file>`: Specify the output file path (required).
- `--include <pattern>`: Only include files matching the pattern (e.g., `--include ".java"`).
- `--exclude <pattern>`: Exclude files or directories matching the pattern (e.g., `--exclude "node_modules"`).
- `--help`: Show usage information and exit.

> **Note:** The tool automatically ignores common binary and build files, and supports a wide range of source code and text file extensions.

---

## Algorithm (Step-by-Step)

**Process Flow Overview:**

- **CLI/REST API Entry:**
  - The user can interact with FoldertoAI either via the Command Line Interface (CLI) or through a REST API endpoint exposed by Spring Boot.
  - For CLI, arguments are parsed and passed to the service layer. For API, HTTP requests are routed by Spring Boot controllers to the service layer.
- **Spring Boot Service Layer:**
  - Spring Boot manages the lifecycle and dependencies of service classes (like `DirScribeService`).
  - The service layer receives the input parameters (input directory, output file, flags) and orchestrates the aggregation process.
- **Directory Traversal (DFS):**
  - The service uses Java NIO's `Files.walkFileTree` to perform a depth-first search (DFS) traversal of the directory tree, applying include/exclude patterns and filtering out unwanted files.
- **Tree Construction:**
  - As files are discovered, a custom `TreeNode` structure is built to represent the directory hierarchy.
- **Output Formatting:**
  - The tree is traversed (again using DFS) to generate a visual structure and aggregate file contents.
- **Aggregation & Writing:**
  - All output is concatenated using a `StringBuilder` and written to the specified output file.
- **Response/Completion:**
  - For CLI, the process exits after writing the file. For API, a response is returned to the client (e.g., file path, status, or file content).

**Textual Process Flow Diagram:**

```
User (CLI/API)
   |
   v
Spring Boot Controller (API) / CLI Parser
   |
   v
DirScribeService (Spring-managed Service)
   |
   v
[DFS Directory Traversal & Tree Construction]
   |
   v
[Output Formatting & Aggregation]
   |
   v
Output File 
```

---

## Technical Details & DSA
- **Tree Structure:** Uses a custom `TreeNode` class to model the directory hierarchy.
- **Depth-First Search (DFS):** Both for file system traversal and for output formatting.
- **Hash Sets/Maps:** For fast filtering and lookups (extensions, ignore patterns, children nodes).
- **StringBuilder:** Efficient aggregation of large text output.
- **Filtering Logic:** Ensures only relevant files are included, skipping binaries and common build artifacts.

These choices ensure the tool is both fast and memory-efficient, even for large codebases.

---

## Role of Spring Boot
- **Dependency Injection:** Spring Boot manages service classes (like `DirScribeService`) and their dependencies, making the code modular and testable.
- **Configuration:** Spring Boot can be extended to support configuration via properties or environment variables.
- **REST API:** The project includes a controller (`DirScribeController`) that can expose the aggregation functionality as a REST API, allowing integration with other backend systems or UIs.
- **CLI & Service Layer:** The CLI and service layers are cleanly separated, following best practices for backend architecture.

---

## Example Command
```sh
java -jar target/dirscribe-1.0-SNAPSHOT.jar -i "/Users/manismagic/Desktop/codeforces" -o "/Users/manismagic/Desktop/output.txt" --exclude "node_modules" --include ".java"
```

