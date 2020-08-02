package com.jasn.my_rest.dto;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDto {
    @CsvBindByPosition(position = 0)
    private String date;

    @CsvBindByPosition(position = 1)
    private String query;

    @CsvBindByPosition(position = 2)
    private String gif;
}
