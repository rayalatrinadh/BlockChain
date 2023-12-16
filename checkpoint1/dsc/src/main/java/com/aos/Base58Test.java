package com.aos;
import java.util.Arrays;

public class Base58Test {
    public static void main(String[] args) {
        // Test byte array
        byte[] byteArray = "Hello world".getBytes();

        // Test encode
        String encodedString = Base58.encode(byteArray);
        System.out.println("Encoded: " + encodedString);

        // Test decode
        byte[] decodedBytes = Base58.decode(encodedString);
        System.out.println("Decoded: " + new String(decodedBytes));

        // Compare
        System.out.println("Original and Decoded are same: " + Arrays.equals(byteArray, decodedBytes));
    }
}
