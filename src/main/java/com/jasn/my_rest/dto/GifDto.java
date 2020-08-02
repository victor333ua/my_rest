package com.jasn.my_rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GifDto {
    private String id;
    private String url;
    private String embed_url;
    private String title;
    private Images images;
}
