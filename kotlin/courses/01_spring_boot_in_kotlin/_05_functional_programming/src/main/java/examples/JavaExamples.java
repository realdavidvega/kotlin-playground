package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JavaExamples {
    // FP example of function applying some function to a string input param
    public static String applySomeFunctionToAString(String inputString, Function<String, String> myFunction) {
        return myFunction.apply(inputString);
    }

    // we can also to this in Java
    public static Function<String, String> toSentenceCase =
            x -> x.substring(0, 1).toUpperCase() + x.substring(1);


    public static void main(String[] args) {
        // same as lambda: x -> x.toUpperCase()
        String result = applySomeFunctionToAString("hello", String::toUpperCase);
        System.out.println(result);

        // equivalent to what we defined in Kotlin
        System.out.println(applySomeFunctionToAString("hello", toSentenceCase));

        // and obviously, because of substitution model of a pure function,
        // we can also can write it this way, which is equivalent to the upper one
        System.out.println(toSentenceCase.apply("hello"));

        List<String> colors = new ArrayList<>();
        colors.add("red");
        colors.add("green");
        colors.add("blue");
        colors.add("black");

        // Java OOP-way of creating a list and returning a copy applying some function
        List<String> upperCaseColors = new ArrayList<>();
        for (String color : colors) {
            upperCaseColors.add(color.toUpperCase());
        }

        for (String color : upperCaseColors) {
            System.out.println(color);
        }
    }
}
