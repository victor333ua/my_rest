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

    private static final int LIMIT = 25;

    private FileSystemService fileSystemService;

    public GiphyService(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    public Path getAndSaveGif(String theme) throws MyIoException, GifNotFoundException {

// get reference to gif file from Giphy.com
        var giphyData = getGiphyData(theme);
        GifDto gifDto;
        Optional<Path> optPath;

// create unique gif file to load to
        for (;;) {
            gifDto = giphyData.getData().get((int) (Math.random() * LIMIT));
            optPath = fileSystemService.createFile(
                            fileSystemService.getCacheDir(theme), gifDto.getId() + ".gif");
            if (optPath.isPresent()) break;
        }
        Path pathFile = optPath.get();

// load file
        String url = gifDto.getImages().get("original_still").get("url");
        loadFile(pathFile, url);

        return pathFile;
    }

    private void loadFile(Path pathToLoad, String url) throws GifNotFoundException {
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "image/gif")
                    .build();

            var result = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if(result.statusCode() == 200) {
                try(InputStream inputStream = result.body();
                        OutputStream outputStream = Files.newOutputStream(pathToLoad);)
                {
                    inputStream.transferTo(outputStream);
                }
            } else {
                throw new GifNotFoundException("gif image hasn't been loaded");
            }
        }
        catch(Exception err){
            throw new GifNotFoundException(err.getMessage());
        }
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
                return objectMapper.readValue(result.body(), GiphyData.class);
            } else {
                throw new GifNotFoundException("data from Giphy.com not found");
            }
        }
        catch(Exception err){
            throw new GifNotFoundException(err.getMessage());
        }
    }

    public QueryFilesDto generateGif(String theme) {
        getAndSaveGif(theme);
        return fileSystemService.getGifsCollection(fileSystemService.getCacheDir(theme)).get(0);
    }

    public List<QueryFilesDto> getGifsFromCache(String theme) {
        return fileSystemService.getGifsCollection(fileSystemService.getCacheDir(theme));
    }

    public List<String> getAllFilesFromCache() {
        return fileSystemService.getAllFromCache();
    }

    public  void deleteAllFromCache() {
        fileSystemService.deleteAll(fileSystemService.getCacheDir(""));
    }

}