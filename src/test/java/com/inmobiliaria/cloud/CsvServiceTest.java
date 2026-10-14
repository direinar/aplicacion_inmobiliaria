package com.inmobiliaria.cloud;

import com.inmobiliaria.cloud.service.CsvService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvServiceTest {

    private final CsvService service = new CsvService();

    @Test
    void readsHeadersAndRows() throws Exception {
        MockMultipartFile csv = new MockMultipartFile("file", "personas.csv", "text/csv",
                "nombre,correo\nAna,ana@example.com\nLuis,luis@example.com\n".getBytes(StandardCharsets.UTF_8));
        List<Map<String, String>> rows = service.read(csv);
        assertEquals(2, rows.size());
        assertEquals("Ana", rows.get(0).get("nombre"));
        assertEquals("luis@example.com", rows.get(1).get("correo"));
    }

    @Test
    void rejectsNonCsvFiles() {
        MockMultipartFile file = new MockMultipartFile("file", "datos.txt", "text/plain", "x".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.read(file));
    }
}
