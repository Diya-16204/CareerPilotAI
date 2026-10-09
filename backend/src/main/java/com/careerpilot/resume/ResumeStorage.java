package com.careerpilot.resume;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class ResumeStorage {
    private final Path root;
    public ResumeStorage(@Value("${app.upload.directory}") String directory){root=Path.of(directory).toAbsolutePath().normalize();}
    public void store(String key,byte[] bytes) throws IOException {
        Files.createDirectories(root);Path target=root.resolve(key).normalize();if(!target.getParent().equals(root))throw new IOException("Invalid storage key.");
        Files.copy(new java.io.ByteArrayInputStream(bytes),target);
    }
    public Path resolve(String key) throws IOException {Path target=root.resolve(key).normalize();if(!target.getParent().equals(root)||!Files.isRegularFile(target))throw new IOException("Stored document is unavailable.");return target;}
    public void delete(String key){try{Path target=root.resolve(key).normalize();if(target.getParent().equals(root))Files.deleteIfExists(target);}catch(IOException ignored){} }
}
