package com.jasn.my_rest.service;

import com.jasn.my_rest.dto.HistoryDto;
import com.jasn.my_rest.dto.QueryFilesDto;
import com.jasn.my_rest.exception.GifNotFoundException;
import com.jasn.my_rest.exception.MyIoException;
import com.jasn.my_rest.utils.FilesOnlyVisitor;
import com.jasn.my_rest.utils.History;
import com.jasn.my_rest.utils.MyFileVisitor;
import com.jasn.my_rest.utils.Vocabulary;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class UserService {
    private Vocabulary vocabulary;
    private History history;
    private FileSystemService fileSystemService;
    private GiphyService giphyService;

    public UserService(Vocabulary vocabulary, History history, FileSystemService fileSystemService, GiphyService giphyService) {
        this.vocabulary = vocabulary;
        this.history = history;
        this.fileSystemService = fileSystemService;
        this.giphyService = giphyService;
    }

    public String getAndSaveGifForUser(String idUser, String theme, boolean force) throws MyIoException {
        Path fileName, userFileName;
        int iVisitCount = 1;

        try {
            for(;;) {
                if (force)
// get Gif from giphy.com and save to cache
                    fileName = giphyService.getAndSaveGif(theme);
                else {
// get gif from cache if exist
                    Optional<Path> optFileName =
                            fileSystemService.getRandomFile(fileSystemService.getCacheDir(theme), iVisitCount++);
                    fileName = optFileName.orElseGet(() -> giphyService.getAndSaveGif(theme));
                }

                Optional<Path> optPathToUserFile =
                        fileSystemService.createFile(
                                fileSystemService.getUserDir(idUser, theme),
                                fileName.getFileName().toString());

                if (optPathToUserFile.isEmpty()) continue;

// copy file from cache to user directory if it's new
                userFileName = Files.copy(fileName, optPathToUserFile.get(), REPLACE_EXISTING);

// add record to history.csv file
                history.addRecord(userFileName);
                break;
            }

        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return userFileName.toString();
    }


    public String getGifFromUser(String idUser, String theme, boolean force) throws MyIoException, GifNotFoundException {

        Path dir;
        if(!force) {
// search in memory
            Optional<String> gifForUser = vocabulary.getGif(idUser, theme);
            if(gifForUser.isPresent()) return gifForUser.get();
        }
// search in the disk in the user directory
        dir = fileSystemService.getUserDir(idUser, theme);
        Optional<Path> optFile = fileSystemService.getRandomFile(dir, 0);
        if(optFile.isEmpty()) throw  new GifNotFoundException("no gifs for this user and theme");

// add to memory (vocabulary)
        String fileName = optFile.get().toString();
        vocabulary.putGif(idUser, theme, fileName);

        return fileName;
    }

    public List<QueryFilesDto> getGifCollectionFromUser(String idUser) throws MyIoException, GifNotFoundException {

        Path pathSource = fileSystemService.getUserDir(idUser, "");
        if(!Files.exists(pathSource)) throw new GifNotFoundException("no such user");

        return fileSystemService.getGifsCollection(pathSource);
    }

    public void deleteVocabulary(String idUser, String theme) {
        vocabulary.deleteVocabulary(idUser, theme);
    }

    public void deleteAllForUser(String idUser) throws MyIoException {
        vocabulary.deleteVocabulary(idUser, "");

        Path pathSource = fileSystemService.getUserDir(idUser, "");
        if(!Files.exists(pathSource)) throw new GifNotFoundException("no such user");
        fileSystemService.deleteAll(pathSource);
    }

    public List<HistoryDto> getHistoryFromUser(String idUser) throws GifNotFoundException {
        Path pathToFile = Paths.get(fileSystemService.getUserDir(idUser, "").toString(), "history.csv");
        if(!Files.exists(pathToFile)) throw new GifNotFoundException("no such user or history file");
        return history.getHistory(pathToFile);
    }

    public void deleteHistoryForUser(String idUser)  throws MyIoException, GifNotFoundException {
        Path pathToFile = Paths.get(fileSystemService.getUserDir(idUser, "").toString(), "history.csv");
        if(!Files.exists(pathToFile)) throw new GifNotFoundException("no such user or history file");
        try {
            Files.delete(pathToFile);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
    }

}
