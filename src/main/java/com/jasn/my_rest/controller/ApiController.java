package com.jasn.my_rest.controller;

import com.jasn.my_rest.dto.GifThemeDto;
import com.jasn.my_rest.dto.HistoryDto;
import com.jasn.my_rest.dto.QueryFilesDto;
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
public class ApiController {
    private final GiphyService giphyService;

    @Autowired
    public ApiController(GiphyService giphyService) {
        this.giphyService = giphyService;
    }

    @PostMapping("/cache/generate")
    public QueryFilesDto generateGif(@RequestBody GifThemeDto gifThemeDto) {
        String theme = gifThemeDto.getQuery();
        giphyService.getAndSaveGif(theme);
        return giphyService.getGifNamesFromPath(theme).get(0);
    }

    @GetMapping("/cache")
    public List<QueryFilesDto> queryGifCollection(@RequestParam Map<String, String> query) {
        String theme = query.get("query");
        return giphyService.getGifNamesFromPath(theme);
    }

    @GetMapping("/cache/gifs")
    public List<String> getAll() {
        return giphyService.getAll();
    }

    @DeleteMapping()
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteAll() {
       giphyService.deleteAll();
    }

    @PostMapping("/user/{idUser}/generate")
    public String generateGifForUser(@PathVariable String idUser,
                                     @RequestBody GifThemeDto gifThemeDto) {

        String theme = gifThemeDto.getQuery();
        boolean force = gifThemeDto.isForce();
        return JSONObject.quote(giphyService.getAndSaveGifForUser(idUser, theme, force));
    }

    @GetMapping("/user/{idUser}/search")
    public String getGifFromUser(@PathVariable String idUser, @RequestParam Map<String, String> query) {
        String theme = query.get("query");
        boolean force = Boolean.parseBoolean(query.get("force"));
        return JSONObject.quote(giphyService.getGifFromUser(idUser, theme, force));
    }

    @GetMapping("/user/{idUser}/all")
    public List<QueryFilesDto> getGifCollectionFromUser(@PathVariable String idUser) {
        return giphyService.getGifCollectionFromUser(idUser);
    }

    @DeleteMapping("/user/{idUser}/reset")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteVocabulary(@PathVariable String idUser, @RequestParam Map<String, String> query) {
        String theme = query.get("query");
        giphyService.deleteVocabulary(idUser, theme);
    }

    @DeleteMapping("/user/{idUser}/clean")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteAllForUser(@PathVariable String idUser) {
        giphyService.deleteAllForUser(idUser);
    }

    @GetMapping("/user/{idUser}/history")
    public List<HistoryDto> getHistoryFromUser(@PathVariable String idUser) {
        return giphyService.getHistoryFromUser(idUser);
    }

    @DeleteMapping("/user/{idUser}/history/clean")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteHistoryForUser(@PathVariable String idUser) {
        giphyService.deleteHistoryForUser(idUser);
    }
}
