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
    private String cache; // root directory

    public MyFileVisitor(String cache) {
        this.cache = cache;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes fileAttributes) {
        listOfFiles.getGifs().add(path.toString());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){

// skip root directory
        String theme = GetCurrentDirectory(dir);
        if(!theme.equals(cache)) {
            listOfFiles = new QueryFilesDto();
            listOfFiles.setQuery(theme);
            log.info(theme);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException exc) {

        if(!GetCurrentDirectory(path).equals(cache)) cacheList.add(listOfFiles);

        return FileVisitResult.CONTINUE;
    }

    private String GetCurrentDirectory(Path dir) {
        String directory = dir.toString();
        int index = directory.lastIndexOf("\\");
        return directory.substring(index+1);
    }

    public List<QueryFilesDto> getCacheList() {
        return this.cacheList;
    }
}

