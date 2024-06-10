package com.example;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.example.Backend.Algorithms.Algorithms;

public class AlgorithmsTests {
    Algorithms<String> Algorithms;

    @BeforeEach                                         
    void setUp() {
        Algorithms = new Algorithms<String>();
        // hasmap reads from src/main/java/com/example/Data/MassZipLatLon.txt
    }

    @Test                                               
    void dummyTest() {
        assertTrue(4 == 4);  
    }
}
