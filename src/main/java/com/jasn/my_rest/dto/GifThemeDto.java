package com.jasn.my_rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;



@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@NoArgsConstructor
@Slf4j
public final class GifThemeDto {
    private String query;
    private boolean force;

}
