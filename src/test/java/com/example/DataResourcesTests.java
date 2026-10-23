package com.example;

import com.example.Backend.DataResources;
import com.example.Backend.DataToGraph;
import com.example.Backend.DataToHashMap;
import com.example.Backend.Algorithms.AccesibilityFormula;
import com.example.Controller.HashController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DataResourcesTests {
    @TempDir
    Path directory;

    @Test
    void readsUtf8FilesystemInputsForCustomDatasets() throws Exception {
        Path file = directory.resolve("coordinates.txt");
        Files.writeString(file, "Maastricht – centrum\n", StandardCharsets.UTF_8);
        try (var reader = DataResources.reader(file.toString())) {
            assertEquals("Maastricht – centrum", reader.readLine());
        }
    }

    @Test
    void loadsBundledPostcodesAndGraphWithoutSourceTreePaths() {
        DataToHashMap<String> postcodes = new DataToHashMap<>("classpath:data/MassZipLatLon.txt");
        postcodes.hashMapConverter();
        assertEquals(50.8577644439073, postcodes.getLat("6211AB"), 0.0000001);
        DataToGraph<String> graph = new DataToGraph<>();
        graph.graphTheData("classpath:data/FilteredDistances.txt");
        assertTrue(graph.getGraph().getVertecesList().contains("6211AB"));
        assertFalse(graph.getGraph().neighbors("6211AB").isEmpty());
    }

    @Test
    void missingBundledDataFailsClearlyInsteadOfCreatingAnEmptyMap() {
        assertThrows(FileNotFoundException.class,
                () -> DataResources.open("classpath:data/missing.txt"));
        DataToHashMap<String> postcodes = new DataToHashMap<>("classpath:data/missing.txt");
        assertThrows(UncheckedIOException.class, postcodes::hashMapConverter);
    }

    @Test
    void cachedAccessibilityDataPreservesRepresentativeScores() throws Exception {
        HashController.map.hashMapConverter();
        AccesibilityFormula formula = new AccesibilityFormula();
        double first = formula.accessibility("6211AB");
        assertEquals(517, (int) first);
        assertEquals(662, (int) formula.accessibility("6211AS"));
        assertEquals(first, new AccesibilityFormula().accessibility("6211AB"));
    }
}
