package org.drools.agentic.example.agents;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File storage agent that provides file operations functionality.
 * Can create, read, write, delete files and manage directories.
 */
public class FileStorageAgent {

    private final ChatModel chatModel;
    private final Path storageRoot;

    public FileStorageAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.storageRoot = Paths.get(System.getProperty("user.home"), ".drools-agent-storage");
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + storageRoot, e);
        }
    }

    @Tool("Write content to a file")
    public String writeFile(
            @P("The filename (relative to storage root)") String filename,
            @P("The content to write to the file") String content) {
        try {
            Path filePath = storageRoot.resolve(filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content.getBytes());
            return "Successfully wrote to file: " + filename;
        } catch (IOException e) {
            return "Error writing file " + filename + ": " + e.getMessage();
        }
    }

    @Tool("Read content from a file")
    public String readFile(@P("The filename (relative to storage root)") String filename) {
        try {
            Path filePath = storageRoot.resolve(filename);
            if (!Files.exists(filePath)) {
                return "File not found: " + filename;
            }
            return Files.readString(filePath);
        } catch (IOException e) {
            return "Error reading file " + filename + ": " + e.getMessage();
        }
    }

    @Tool("List files in a directory")
    public String listFiles(@P("The directory path (relative to storage root, use '.' for root)") String directory) {
        try {
            Path dirPath = ".".equals(directory) ? storageRoot : storageRoot.resolve(directory);
            if (!Files.exists(dirPath)) {
                return "Directory not found: " + directory;
            }
            if (!Files.isDirectory(dirPath)) {
                return "Path is not a directory: " + directory;
            }
            
            List<String> files = Files.list(dirPath)
                    .map(path -> {
                        String name = path.getFileName().toString();
                        return Files.isDirectory(path) ? name + "/" : name;
                    })
                    .sorted()
                    .collect(Collectors.toList());
            
            if (files.isEmpty()) {
                return "Directory is empty: " + directory;
            }
            
            return "Files in " + directory + ":\n" + String.join("\n", files);
        } catch (IOException e) {
            return "Error listing directory " + directory + ": " + e.getMessage();
        }
    }

    @Tool("Delete a file")
    public String deleteFile(@P("The filename (relative to storage root)") String filename) {
        try {
            Path filePath = storageRoot.resolve(filename);
            if (!Files.exists(filePath)) {
                return "File not found: " + filename;
            }
            Files.delete(filePath);
            return "Successfully deleted file: " + filename;
        } catch (IOException e) {
            return "Error deleting file " + filename + ": " + e.getMessage();
        }
    }

    @Tool("Create a directory")
    public String createDirectory(@P("The directory path (relative to storage root)") String directory) {
        try {
            Path dirPath = storageRoot.resolve(directory);
            Files.createDirectories(dirPath);
            return "Successfully created directory: " + directory;
        } catch (IOException e) {
            return "Error creating directory " + directory + ": " + e.getMessage();
        }
    }

    @Tool("Append content to a file")
    public String appendToFile(
            @P("The filename (relative to storage root)") String filename,
            @P("The content to append to the file") String content) {
        try {
            Path filePath = storageRoot.resolve(filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return "Successfully appended to file: " + filename;
        } catch (IOException e) {
            return "Error appending to file " + filename + ": " + e.getMessage();
        }
    }

    @Tool("Check if file exists")
    public String fileExists(@P("The filename or directory path (relative to storage root)") String path) {
        Path filePath = storageRoot.resolve(path);
        boolean exists = Files.exists(filePath);
        String type = exists ? (Files.isDirectory(filePath) ? "directory" : "file") : "does not exist";
        return "Path '" + path + "' " + (exists ? "exists as a " + type : type);
    }

    @Tool("Get file information")
    public String getFileInfo(@P("The filename (relative to storage root)") String filename) {
        try {
            Path filePath = storageRoot.resolve(filename);
            if (!Files.exists(filePath)) {
                return "File not found: " + filename;
            }
            
            long size = Files.size(filePath);
            String creationTime = Files.getFileAttributeView(filePath, java.nio.file.attribute.BasicFileAttributeView.class)
                    .readAttributes().creationTime().toString();
            String modificationTime = Files.getLastModifiedTime(filePath).toString();
            boolean isDirectory = Files.isDirectory(filePath);
            
            return String.format("File: %s%nType: %s%nSize: %d bytes%nCreated: %s%nModified: %s",
                    filename, 
                    isDirectory ? "Directory" : "File",
                    size, 
                    creationTime, 
                    modificationTime);
        } catch (IOException e) {
            return "Error getting file info for " + filename + ": " + e.getMessage();
        }
    }

    /**
     * Factory method to create a FileStorageAgent with the provided chat model.
     */
    public static FileStorageAgent create(ChatModel chatModel) {
        return new FileStorageAgent(chatModel);
    }
}
