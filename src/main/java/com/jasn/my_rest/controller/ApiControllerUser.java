package com.jasn.my_rest.controller;

import com.jasn.my_rest.dto.GifThemeDto;
import com.jasn.my_rest.dto.HistoryDto;
import com.jasn.my_rest.dto.QueryFilesDto;
import com.jasn.my_rest.service.FileSystemService;
import com.jasn.my_rest.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class ApiControllerUser {
    private final UserService userService;
    private final FileSystemService fileSystemService;


    public ApiControllerUser(UserService userService, FileSystemService fileSystemService) {
        this.userService = userService;
        this.fileSystemService = fileSystemService;
    }

    @PostMapping("/{idUser}/generate")
    public String generateGifForUser(@PathVariable String idUser,
                                     @RequestBody GifThemeDto gifThemeDto) {

        String theme = gifThemeDto.getQuery();
        boolean force = gifThemeDto.isForce();
        return JSONObject.quote(userService.getAndSaveGifForUser(idUser, theme, force));
    }

    @GetMapping("/{idUser}/search")
    public String getGifFromUser(@PathVariable String idUser, @RequestParam Map<String, String> query) {
        String theme = query.get("query");
        boolean force = Boolean.parseBoolean(query.get("force"));
        return JSONObject.quote(userService.getGifFromUser(idUser, theme, force));
    }

    @GetMapping("/{idUser}/all")
    public List<QueryFilesDto> getGifCollectionFromUser(@PathVariable String idUser) {
        return userService.getGifCollectionFromUser(idUser);
    }

    @DeleteMapping("/{idUser}/reset")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteVocabulary(@PathVariable String idUser, @RequestParam Map<String, String> query) {
        String theme = query.get("query");
        userService.deleteVocabulary(idUser, theme);
    }

    @DeleteMapping("/{idUser}/clean")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteAllForUser(@PathVariable String idUser) {
        userService.deleteAllForUser(idUser);
    }

    @GetMapping("/{idUser}/history")
    public List<HistoryDto> getHistoryFromUser(@PathVariable String idUser) {
        return userService.getHistoryFromUser(idUser);
    }

    @DeleteMapping("/{idUser}/history/clean")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteHistoryForUser(@PathVariable String idUser) {
        userService.deleteHistoryForUser(idUser);
    }
}

