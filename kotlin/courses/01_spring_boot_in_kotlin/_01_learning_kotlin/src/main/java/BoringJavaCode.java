package main.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;

public class BoringJavaCode {
    public static void main(String[] args) {
        // Hello World
        System.out.println("Hello world");

        // Mutable
        String name = "Matt";
        name = "John";

        // Immutable
        final String surname = "Croft";
        //surname = "Something";

        // Method from java.lang package
        System.out.println(name + " " + surname);

        // ---
        // Casting in Java
        // ---

        Object result;

        Integer randomNumber = new Random().nextInt(3);

        if (randomNumber == 1) {
            result = new BigDecimal(30);
        } else {
            result = "hello";
        }
        System.out.println("Result is currently " + result);

        if (result instanceof BigDecimal) {
            // add 47
            result = ((BigDecimal) result).add(new BigDecimal(47));
        } else {
            // put it into uppercase
            String tempResult = (String) result;
            result = tempResult.toUpperCase();
        }

        System.out.println("Result is currently " + result);

        // we need to surround unhandled exceptions on java with try catch or adding the exception
        // to the method signature
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            System.out.println("caught");
        }

        // if a function throws an exceptions, we need to handle it
        try {
            System.out.println(divide(6, 3));
        } catch(InterruptedException e) {
            System.out.println("caught");
        }
    }

    // in Java, we have to handle the exceptions
    public static Double divide(int a, int b) throws InterruptedException{
        Thread.sleep(1000);
        return (double) a / b;
    }

    // closing resources in Java
    private void printFile() throws IOException {
        // after reading it, will close it, so we don't need to add a finally
        try (FileInputStream input = new FileInputStream("file.txt")) {
            int data = input.read();
            while (data != -1) {
                System.out.println((char) data);
                data = input.read();
            }
        }
    }
}
