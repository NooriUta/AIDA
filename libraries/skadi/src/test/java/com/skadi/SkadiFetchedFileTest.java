package com.skadi;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SkadiFetchedFileTest {

    @Test
    void suggestedFilename_format() {
        var f = new SkadiFetchedFile("GET_SALARY", "HR",
                SkadiFetchConfig.ObjectType.PROCEDURE, "CREATE ...", null, Map.of());
        assertEquals("hr__get_salary.procedure.sql", f.suggestedFilename());
    }

    @Test
    void suggestedFilename_lowerCase() {
        var f = new SkadiFetchedFile("MY_FUNC", "PUBLIC",
                SkadiFetchConfig.ObjectType.FUNCTION, "...", null, Map.of());
        assertThat(f.suggestedFilename()).matches("[a-z0-9_]+__[a-z0-9_]+\\.[a-z]+\\.sql");
    }

    @Test
    void suggestedFilename_sanitizesSpecialChars() {
        var f = new SkadiFetchedFile("MY-PROC.123", "HR",
                SkadiFetchConfig.ObjectType.PROCEDURE, "...", null, Map.of());
        assertThat(f.suggestedFilename()).matches("[a-z0-9_.]+\\.sql");
    }

    @Test
    void suggestedFilename_nullSchema_usesDefault() {
        var f = new SkadiFetchedFile("fn", null,
                SkadiFetchConfig.ObjectType.FUNCTION, "...", null, Map.of());
        assertThat(f.suggestedFilename()).startsWith("default__");
    }

    @Test
    void suggestedFilename_packageBodyType() {
        var f = new SkadiFetchedFile("PKG_UTIL", "APP",
                SkadiFetchConfig.ObjectType.PACKAGE_BODY, "...", null, Map.of());
        assertThat(f.suggestedFilename()).endsWith(".package_body.sql");
    }
}
