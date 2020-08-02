package com.jasn.my_rest.utils;

import com.jasn.my_rest.dto.HistoryDto;
import com.jasn.my_rest.exception.MyIoException;
import com.opencsv.*;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public class History {

    public List<HistoryDto> getHistory(Path pathToHistoryFile) throws MyIoException {
        List<HistoryDto> list = new LinkedList<>();
        Class clazz = HistoryDto.class;

        ColumnPositionMappingStrategy ms = new ColumnPositionMappingStrategy();
        ms.setType(clazz);

        Reader reader = null;
        try {
            reader = Files.newBufferedReader(pathToHistoryFile);

// it's only for reading '\' character from gif path
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(new RFC4180Parser())
                    .build();

            CsvToBean cb = new CsvToBeanBuilder(reader)
                    .withType(clazz)
                    .withMappingStrategy(ms)
                    .build();
            cb.setCsvReader(csvReader);

            list = cb.parse();
            reader.close();

        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
        return list;
    }

    public void addRecord(Path pathToUser, String theme, String fileName) throws MyIoException {

        Path path = Paths.get(pathToUser.toString(), "\\history.csv");
        if(!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new MyIoException(e.getMessage());
            }
        }

        Writer writer  = null;
        try {
            writer = new FileWriter(path.toString());

            StatefulBeanToCsv sbc = new StatefulBeanToCsvBuilder(writer)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();

            List<HistoryDto> list = new ArrayList<>();

            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = myDateObj.format(myFormatObj);

            list.add(new HistoryDto(formattedDate, theme, fileName));

            try {
                sbc.write(list);
            } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
                throw new MyIoException(e.getMessage());
            }
            writer.close();
        } catch (IOException e) {
            throw new MyIoException(e.getMessage());
        }
    }
}

