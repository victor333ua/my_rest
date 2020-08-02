package com.jasn.my_rest.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FilesOnlyVisitor extends SimpleFileVisitor<Path> {
    private boolean isDelete;
    private List<String> filesList = new ArrayList<>();

    public FilesOnlyVisitor(boolean isDelete) {
        this.isDelete = isDelete;
    }

    public FileVisitResult visitFile(Path path, BasicFileAttributes fileAttributes) {

        if(isDelete) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            filesList.add(path.toString());

        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult postVisitDirectory(Path path, IOException exc)  {

        if(isDelete) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return FileVisitResult.CONTINUE;
    }

    public List<String> getFilesList() {
        return filesList;
    }
}
