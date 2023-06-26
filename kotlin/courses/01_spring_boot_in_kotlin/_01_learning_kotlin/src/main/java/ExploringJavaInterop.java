package main.java;

import main.kotlin.KotlinCustomer;
import main.kotlin.KotlinCustomerDatabase;

import java.util.List;

public class ExploringJavaInterop {
    public static void main(String[] args) {
        // this won't compile, however this is not a genuine null check
        //Customer phil = new Customer(null, null);

        // this will compile however it won't work
        // this happened because Long is compiled to primitive null
        // and because in Java there is no null check as design time, it won't complain
        //Customer phil = new Customer(0, null);

        // this works
        KotlinCustomer phil = new KotlinCustomer(0, "Phil");

        // data classes can be used from java
        phil.copy(1, "David");

        // however getters are as they are in java
        phil.getId();

        // because values are vals, we cannot set them, so this won't compile
        //phil.setId();

        KotlinCustomerDatabase db = new KotlinCustomerDatabase();

        //List<KotlinCustomer> customers = db.getKotlinCustomers();
        // because in Java there is no way of hiding the methods, at design time there is no way of telling
        // the developer that that list from kotlin is immutable
        // in run time it will fail with an UnsupportedOperationException, so documentation is important here
        // or maybe calling customers immutableCustomers, or such
        //customers.add(new KotlinCustomer(5, "Visitor"));

        // it will compile, but we will get an exception
        //db.addCustomer(phil);

        // now lets try to catch it and this won't compile unless we add the Throw annotation in Kotlin
        //try {
        //    db.addCustomer(phil);
        //} catch (IllegalAccessException e) {
        //    System.out.println("Caught exception");

        try {
            db.addCustomerForJava(phil);
        } catch (IllegalAccessException e) {
            System.out.println("Caught exception");
        }

        // we can loop through the list just as in Kotlin, and we will see the toString in the kotlin fancy way
        for (KotlinCustomer c : db.getKotlinCustomers()) {
            System.out.println(c);
        }

        // so we can see that for accessing static functions from kotlin, we should use the Companion object
        // this is a static method that is hidden inside this Companion
        KotlinCustomerDatabase.Companion.helloWorld();

        // for normal Java usage, we need to add the annotation JvmStatic at kotlin
        KotlinCustomerDatabase.greetings();

        // you cannot access top-level functions from classes in Java, there's no way of doing that
        // so, we cannot call for example the someTopClassFunction() method
    }
}
