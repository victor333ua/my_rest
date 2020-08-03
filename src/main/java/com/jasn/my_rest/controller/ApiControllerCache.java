package com.jasn.my_rest.controller;

import com.jasn.my_rest.dto.GifThemeDto;
import com.jasn.my_rest.dto.HistoryDto;
import com.jasn.my_rest.dto.QueryFilesDto;
import com.jasn.my_rest.service.FileSystemService;
import com.jasn.my_rest.service.GiphyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@Slf4j
public class ApiControllerCache {
    private final GiphyService giphyService;

    @Autowired
    public ApiControllerCache(GiphyService giphyService) {

        this.giphyService = giphyService;
    }

    @PostMapping("/cache/generate")
    public QueryFilesDto generateGif(@RequestBody GifThemeDto gifThemeDto) {
        String theme = gifThemeDto.getQuery();
        return giphyService.generateGif(theme);
    }

    @GetMapping("/cache")
    public List<QueryFilesDto> queryGifCollection(@RequestParam Map<String, String> query) {
        String theme = query.get("query");
        return giphyService.getGifsFromCache(theme);
    }

    @GetMapping("/cache/gifs")
    public List<String> getAll() {
        return giphyService.getAllFilesFromCache();
    }

    @DeleteMapping()
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteAll() {
        giphyService.deleteAllFromCache();
    }
}
