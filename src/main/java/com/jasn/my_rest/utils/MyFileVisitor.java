package com.jasn.my_rest.utils;

import com.jasn.my_rest.dto.QueryFilesDto;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public  class MyFileVisitor implements FileVisitor<Path> {
    private QueryFilesDto listOfFiles;
    private List<QueryFilesDto> cacheList = new LinkedList<>();
    private String rootDir; // root directory

    public MyFileVisitor(String rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes fileAttributes) {
        String fileName = path.toString();

        if(!fileName.endsWith("csv")) listOfFiles.getGifs().add(fileName);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){

// skip root directory
        String theme = dir.getFileName().toString();
        if(!theme.equals(rootDir)) {
            listOfFiles = new QueryFilesDto();
            listOfFiles.setQuery(theme);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

        if(!dir.getFileName().toString().equals(rootDir)) cacheList.add(listOfFiles);

        return FileVisitResult.CONTINUE;
    }


    public List<QueryFilesDto> getCacheList() {
        return this.cacheList;
    }
}

