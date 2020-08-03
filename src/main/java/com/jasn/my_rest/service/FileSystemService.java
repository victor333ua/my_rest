package com.jasn.my_rest.service;

import com.jasn.my_rest.dto.GifDto;
import com.jasn.my_rest.dto.QueryFilesDto;
import com.jasn.my_rest.exception.GifNotFoundException;
import com.jasn.my_rest.exception.MyIoException;
import com.jasn.my_rest.utils.FilesOnlyVisitor;
import com.jasn.my_rest.utils.MyFileVisitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSystemService {
        @Value("${absPath}")
        private String absPath;
        @Value("${cache}")
        private String cache;

    public Path  getCacheDir(String theme) {
        return Paths.get(absPath,cache, theme);
    }

    public Path getUserDir(String idUser, String theme) {
        return Paths.get(absPath,"users", idUser, theme);
    }

    public Optional<Path> createFile(Path pathFile, String fileName) throws MyIoException {
// return empty if exist
         try {
            if (!Files.exists(pathFile)) Files.createDirectories(pathFile);
            pathFile = Paths.get(pathFile.toString(), fileName);
            if (Files.exists(pathFile)) return Optional.empty();
            return Optional.of(Files.createFile(pathFile));
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
    }

    public Optional<Path> getRandomFile(Path pathToDir, int iVisitCount) {
        if(!Files.exists(pathToDir)) return Optional.empty();

        List<Path> list = null;
        try {
            list = Files.list(pathToDir).collect(Collectors.toList());
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        int count = list.size();
        if(count==0 || iVisitCount > count) return Optional.empty();

        return Optional.of(list.get((int)(Math.random()*count)));
    }

    public List<QueryFilesDto> getGifsCollection(Path root) throws MyIoException {


        var myFileVisitor = new MyFileVisitor(root.getFileName().toString());

        try {
            Files.walkFileTree(root, myFileVisitor);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return myFileVisitor.getCacheList();
    }

    public List<String> getAllFromCache() throws MyIoException {
        Path pathSource = getCacheDir("");
        var filesOnlyVisitor = new FilesOnlyVisitor(false);
        try {
            Files.walkFileTree(pathSource, filesOnlyVisitor);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return filesOnlyVisitor.getFilesList();
    }

    public void deleteAll(Path root) throws MyIoException {

        var filesOnlyVisitor = new FilesOnlyVisitor(true);
        try {
            Files.walkFileTree(root, filesOnlyVisitor);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
    }
}
