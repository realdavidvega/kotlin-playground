import arrow.optics.Every
import arrow.optics.dsl.every
import models.*

// Every
// Used to focus over 0 to n elements on a list.
val everyEmployeeStreetName: Every<Employees, String> =
    Employees.employees
        .every(Every.list())
        .company
        .address
        .street
        .name

// The every DSL method requires an instance of Every, and here we are passing a default one for List.
// We could pass other instances like the one provided by FilterIndex if we wanted to only filter some elements
// by their index. We will not go that far for this exercise.

// We use Every fixAllStrings function, so it fixes all GuitarStrings by replacing those by GuitarString.newStrings()
// for all the Guitars found on the provided Instruments instance.
fun fixAllStrings(instruments: Instruments): Instruments =
    Instruments.instruments
        .every(Every.list())
        .guitar
        .strings
        .modify(instruments) { GuitarString.newStrings() }

// Take a look to the official Every docs: https://arrow-kt.io/docs/next/optics/every/
