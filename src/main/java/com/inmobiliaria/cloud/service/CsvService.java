package com.inmobiliaria.cloud.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvService {

    public List<Map<String, String>> read(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Seleccione un archivo CSV.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("El archivo debe tener extension .csv.");
        }
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader().setSkipHeaderRecord(true).setTrim(true).setIgnoreEmptyLines(true).build();
        List<Map<String, String>> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = format.parse(reader)) {
            if (parser.getHeaderNames().isEmpty()) {
                throw new IllegalArgumentException("El CSV debe incluir una fila de encabezados.");
            }
            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String header : parser.getHeaderNames()) {
                    String cleanHeader = header.trim();
                    if (cleanHeader.isBlank()) {
                        throw new IllegalArgumentException("El CSV contiene un encabezado vacio.");
                    }
                    row.put(cleanHeader, record.get(header).trim());
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
