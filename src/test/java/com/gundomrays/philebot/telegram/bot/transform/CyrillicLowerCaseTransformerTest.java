package com.gundomrays.philebot.telegram.bot.transform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CyrillicLowerCaseTransformerTest {

    private final CyrillicLowerCaseTransformer cyrillicLowerCaseTransformer = new CyrillicLowerCaseTransformer();

    /**
     * Test to check if the transform function converts letters correctly.
     */
    @Test
    void transformTest() {
        // Input string with values to be converted
        String input = "cypoк";
        // Expected output with values converted
        String expectedOutput = "сурок";
        
        // Call transform on input and compare the result to expected output
        assertEquals(expectedOutput, cyrillicLowerCaseTransformer.transform(input));
    }

    @Test
    void transformIgnoreCaseTest() {
        // Input string with values to be converted
        String input = "CyPoк";
        // Expected output with values converted
        String expectedOutput = "сурок";

        // Call transform on input and compare the result to expected output
        assertEquals(expectedOutput, cyrillicLowerCaseTransformer.transform(input));
    }

    /**
     * Test to check if unknown letters are left unchanged.
     */
    @Test
    void transformTestUnknownLetters() {
        // Input string with values that should not be converted
        String input = "bdjklm";
        // The input is passed directly into transform function and returned
        assertEquals(input, cyrillicLowerCaseTransformer.transform(input));
    }

    /**
     * Test to check if the transform function leaves a string made of unknown characters unchanged.
     */
    @Test
    void transformEmptyStringTest() {
        // An empty string
        String input = "";
        // Transform function on empty string should return empty string
        assertEquals(input, cyrillicLowerCaseTransformer.transform(input));
    }
}