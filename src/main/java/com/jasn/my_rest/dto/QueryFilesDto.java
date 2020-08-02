package com.jasn.my_rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class QueryFilesDto {
    private String query;
    private List<String> gifs = new LinkedList<>();
}
