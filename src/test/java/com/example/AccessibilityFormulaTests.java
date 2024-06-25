package com.example;

import static org.junit.Assert.*;

import org.junit.jupiter.api.*;

import com.example.Backend.Algorithms.AccesibilityFormula;

public class AccessibilityFormulaTests {

    AccesibilityFormula formula;

    @BeforeEach                                         
    void setUp() {
        formula = new AccesibilityFormula();
    }

    @Test                                     
    void test1() {
        double value = AccesibilityFormula.fallofFunction(20.0);
        assertTrue(Math.abs(value - 0.9705) <= 0.0001);
    }

    @Test                                     
    void test2() {
        double value = AccesibilityFormula.fallofFunction(10000);
        assertTrue(Math.abs(value) <= 0.0001);
    }

    @Test                                     
    void test3() {
        double value = AccesibilityFormula.fallofFunction(2024);
        assertTrue(Math.abs(value - 0.0482) <= 0.0001);
    }

    @Test                                     
    void test4() {
        double value = AccesibilityFormula.fallofFunction(5.42901123);
        assertTrue(Math.abs(value - 0.9919) <= 0.0001);
    }

    @Test                                     
    void test5() {
        double value = AccesibilityFormula.fallofFunction(Math.PI);
        assertTrue(Math.abs(value - 0.9953) <= 0.0001);
    }

    @Test
    void test6() {
        double value = formula.amenityBenefit("playground");
        assertTrue(value == 0);
    }

    @Test
    void test7() {
        double value = formula.amenityBenefit("null");
        assertTrue(value == 0);
    }

    @Test
    void test8() {
        double value = formula.amenityBenefit("JKOADSASJODASJFASJFASJF");
        assertTrue(value == 0);
    }

    @Test
    void test9() {
        double value = formula.amenityBenefit("supermarket");
        assertTrue(value == 5);
    }

    @Test
    void test10() {
        double value = formula.amenityBenefit("gift");
        assertTrue(value == 2);
    }
}
