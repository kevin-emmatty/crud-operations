package org.quarkus.assignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductCsvUtilMalformedTest {

    private static final Path CSV = Path.of("target/unit-csv/malformed.csv");

    @BeforeEach
    void setup() throws Exception {
        Files.createDirectories(CSV.getParent());
        Files.deleteIfExists(CSV);
        // write malformed data: header + bad numeric fields
        List<String> lines = List.of(
                "id,name,description,price,quantity",
                "x,Name,Desc,notANumber,",
                ",,,," // empty row
        );
        Files.write(CSV, String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void readAll_malformed_rows_are_tolerated_with_nulls() {
        var all = ProductCsvUtil.readAllProducts(CSV.toString());
        assertEquals(2, all.size());
        // first row: id and price should be null due to parse failure
        assertNull(all.get(0).getId());
        assertNull(all.get(0).getPrice());
    }
}


