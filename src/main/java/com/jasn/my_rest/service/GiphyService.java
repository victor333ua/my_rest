package com.jasn.my_rest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasn.my_rest.dto.GifDto;
import com.jasn.my_rest.dto.GiphyData;
import com.jasn.my_rest.dto.HistoryDto;
import com.jasn.my_rest.dto.QueryFilesDto;
import com.jasn.my_rest.exception.GifNotFoundException;
import com.jasn.my_rest.exception.MyIoException;
import com.jasn.my_rest.utils.FilesOnlyVisitor;
import com.jasn.my_rest.utils.History;
import com.jasn.my_rest.utils.MyFileVisitor;
import com.jasn.my_rest.utils.Vocabulary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@Service
public class GiphyService {
    @Value("${api.giphy.url}")
    private String apiGiphyUrl;

    @Value("${apiKey}")
    private String apiKey;

    @Value("${absPath}")
    private String absPath;

    @Value("${cache}")
    private String cache;

    private static final int LIMIT = 25;
    private Vocabulary vocabulary = new Vocabulary();
    private History history = new History();

    public Path getAndSaveGif(String theme) throws MyIoException, GifNotFoundException {

// create directory if necessary
        String directory = absPath + cache + theme ;
        Path pathFile = Paths.get(directory);
        GifDto gifDto;

        try {
            if (!Files.exists(pathFile)) Files.createDirectories(pathFile);

// get reference to gif file from Giphy.com
            var giphyData = getGiphyData(theme);

            for (;;) {
                gifDto = giphyData.getData().get((int) (Math.random() * LIMIT));
                pathFile = Paths.get(directory + "\\" + gifDto.getId() + ".gif");
                if (!Files.exists(pathFile)) break;
            }

            pathFile = Files.createFile(pathFile);

        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }

// load file
        String url = gifDto.getImages().getOriginal_still().getUrl();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "image/gif")
                    .build();

            var result = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if(result.statusCode() == 200) {
                InputStream inputStream = result.body();
                OutputStream outputStream = Files.newOutputStream(pathFile);
                inputStream.transferTo(outputStream);
            } else {
                throw new GifNotFoundException("bad response, status code = " + result.statusCode());
            }
        }
        catch(Exception err){
            throw new GifNotFoundException(err.getMessage());
        }
        return pathFile;
    }

    private GiphyData getGiphyData(String theme) throws GifNotFoundException {

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(URI.create(apiGiphyUrl +
                            "?api_key=" + apiKey +
                            "&q=" + theme +
                            "&limit=" + Integer.toString(LIMIT)))
                    .header("Content-Type", "application/json")
                    .build();

            var result = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(result.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                GiphyData giphyData = objectMapper.readValue(result.body(), GiphyData.class);
                return giphyData;
            } else {
                throw new GifNotFoundException("bad response, status code = " + result.statusCode());
            }
        }
        catch(Exception err){
            throw new GifNotFoundException(err.getMessage());
        }
    }

    public List<QueryFilesDto> getGifNamesFromPath(String theme) throws MyIoException {

        Path pathSource = Paths.get(absPath, cache, theme);
        var myFileVisitor = new MyFileVisitor(cache);

        try {
            Files.walkFileTree(pathSource, myFileVisitor);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return myFileVisitor.getCacheList();
    }

    public List<String> getAll() throws MyIoException {
        Path pathSource = Paths.get(absPath, cache);
        var filesOnlyVisitor = new FilesOnlyVisitor(false);
        try {
            Files.walkFileTree(pathSource, filesOnlyVisitor);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return filesOnlyVisitor.getFilesList();
    }

    public void deleteAll() throws MyIoException {

        Path pathSource = Paths.get(absPath, cache);
        var filesOnlyVisitor = new FilesOnlyVisitor(true);
        try {
            Files.walkFileTree(pathSource, filesOnlyVisitor);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
    }

    public String getAndSaveGifForUser(String idUser, String theme, boolean force) throws MyIoException {
        Path fileName, userFileName;
        int iVisitCount = 1;

        try {
            for(;;) {
                if (force)
// get Gif from giphy.com and save to cache
                    fileName = getAndSaveGif(theme);
                else {
// get gif from cache if exist
                    Optional<Path> optFileName = getFileFromCache(theme, iVisitCount++);
                    fileName = optFileName.orElseGet(() -> getAndSaveGif(theme));
                }

                String name = fileName.toString();
                name = name.substring(name.lastIndexOf("\\") + 1);

//                name = fileName.getFileName().toString();
                userFileName = Paths.get(absPath, "users\\", idUser, "\\", theme, "\\", name);
                if (Files.exists(userFileName)) continue;

// copy file from cache to user directory if it's new
                Files.createDirectories(userFileName);
                userFileName = Files.copy(fileName, userFileName, REPLACE_EXISTING);
// add record to history.csv file
                Path pathToUser = Paths.get(absPath, "users\\", idUser);
                history.addRecord(pathToUser, theme, userFileName.toString());
                break;
            }

        } catch (IOException e) {
                throw new MyIoException(e.getMessage());
        }
        return userFileName.toString();
    }

    private Optional<Path> getFileFromCache(String theme, int iVisitCount) throws MyIoException {
        Path file;
        try {
            Stream<Path> files = Files.list(Paths.get(absPath, cache, theme));
            List<Path> list = files.collect(Collectors.toList());
            int count = list.size();
            if(count==0 || iVisitCount > count) return Optional.empty();

            int iRandomChoice = (int)(Math.random()*count);
            file = list.get(iRandomChoice);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return Optional.of(file);
    }

    public String getGifFromUser(String idUser, String theme, boolean force) throws MyIoException {
        Path file;
        try {
            if(!force) {
// search in memory
                Optional<String> gifForUser = vocabulary.getGif(idUser, theme);
                if(gifForUser.isPresent()) return gifForUser.get();
            }
// search in the disk in the user directory
            file = Paths.get(absPath, "users\\", idUser, "\\", theme);
            if(!Files.exists(file)) throw new GifNotFoundException("no gif for this user and theme");

            List<Path> list = Files.list(file).collect(Collectors.toList());
            int count = list.size();
            if(count==0) throw  new GifNotFoundException("no gif for this user and theme");

            int index = (int)(Math.random()*count);
            file = list.get(index);

// add to memory (vocabulary)
            vocabulary.putGif(idUser, theme, file.toString());
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return file.toString();
    }

    public List<QueryFilesDto> getGifCollectionFromUser(String idUser) throws MyIoException, GifNotFoundException {

        Path pathSource = Paths.get(absPath, "users\\", idUser);
        if(!Files.exists(pathSource)) throw new GifNotFoundException("no such user");

        var myFileVisitor = new MyFileVisitor(idUser);

        try {
            Files.walkFileTree(pathSource, myFileVisitor);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return myFileVisitor.getCacheList();
    }

    public void deleteVocabulary(String idUser, String theme) {
        vocabulary.deleteVocabulary(idUser, theme);
    }

    public void deleteAllForUser(String idUser) throws MyIoException {
        vocabulary.deleteVocabulary(idUser, "");

        Path pathSource = Paths.get(absPath, "users\\", idUser);
        var filesOnlyVisitor = new FilesOnlyVisitor(true);
        try {
            Files.walkFileTree(pathSource, filesOnlyVisitor);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
    }

    public List<HistoryDto> getHistoryFromUser(String idUser) throws GifNotFoundException {
        Path pathToFile = Paths.get(absPath, "users\\", idUser, "\\history.csv");
        if(!Files.exists(pathToFile)) throw new GifNotFoundException("no such user or history file");
        return history.getHistory(pathToFile);
    }

    public void deleteHistoryForUser(String idUser)  throws MyIoException, GifNotFoundException {
        Path pathToFile = Paths.get(absPath, "users\\", idUser, "\\history.csv");
        if(!Files.exists(pathToFile)) throw new GifNotFoundException("no such user or history file");
        try {
            Files.delete(pathToFile);
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
    }
}