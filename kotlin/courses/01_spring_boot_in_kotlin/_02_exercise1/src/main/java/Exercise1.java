package main.java;

import java.util.GregorianCalendar;

public class Exercise1 {

    public static void main(String[] Args) {
        JavaPerson john = new JavaPerson(1L, "Mr", "John", "Blue", new GregorianCalendar(1977,9,3));
        JavaPerson jane = new JavaPerson(2L, "Mrs", "Jane", "Green", null);
        System.out.println (john + "'s age is " + john.getAge());
        System.out.println (jane + "'s age is " + jane.getAge());
        System.out.println ("The age of someone born on 3rd May 1988 is " + JavaPerson.getAge(new GregorianCalendar(1988,5,3)));
    }
}
